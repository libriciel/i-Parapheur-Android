package org.adullact.iparapheur.controller.rest.api;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.dossier.filter.MyFilters;
import org.adullact.iparapheur.controller.rest.RESTUtils;
import org.adullact.iparapheur.controller.rest.mapper.ModelMapper;
import org.adullact.iparapheur.controller.rest.mapper.ModelMapper3;
import org.adullact.iparapheur.model.Annotation;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.EtapeCircuit;
import org.adullact.iparapheur.model.Filter;
import org.adullact.iparapheur.model.PageAnnotations;
import org.adullact.iparapheur.model.RequestResponse;
import org.adullact.iparapheur.utils.IParapheurException;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by jmaire on 09/06/2014.
 * API i-Parapheur version 3
 * <p/>
 * a partir de la v4.2.00 / v3.6.00 (comprise)
 * <p/>
 * Refonte totale de l'API.
 * On passe sur une API Restful CRUDL
 * <p/>
 * Il y a des sous ressources (ex. les bureaux de l'utilisateur),
 * et quelques action spécifiques qui ne suivent pas l'architecture CRUDL
 * (ex. mise à jour des classifications ACTES).
 */
// TODO : JSONStringer, maybe ?
public class RESTClientAPI3 extends RESTClientAPI {

	/* Ressources principales */
	private static final String RESOURCE_BUREAUX = "/parapheur/bureaux";
	private static final String RESOURCE_DOSSIERS = "/parapheur/dossiers";
	private static final String RESOURCE_DOSSIER_CIRCUIT = "/parapheur/dossiers/%s/circuit";
	private static final String RESOURCE_TYPES = "/parapheur/types";
	private static final String RESOURCE_ANNOTATIONS = "/parapheur/dossiers/%s/annotations";
	private static final String RESOURCE_ANNOTATION = "/parapheur/dossiers/%s/annotations/%s";
	private static final String RESOURCE_DELEGATIONS = "/parapheur/delegations";

	/* Ressources secondaires */
	private static final String RESOURCE_ANNEXES = "/parapheur/dossiers/%s/annexes";
	private static final String RESOURCE_CONSECUTIVE_STEPS = "/parapheur/dossiers/%s/consecutiveSteps";
	private static final String RESOURCE_JOURNAL_EVENEMENT = "/parapheur/dossiers/%s/evenements";

    /* Resources sur la lecture des documents */
	/**
	 * Le premier argument est l'id du dossier, le second l'id du document, le dernier le numéro de page
	 */
	private static final String RESOURCE_DOCUMENT_PAGE = "/parapheur/dossiers/%s/%s/%d";
	private static final String RESOURCE_XEMELIOS_VIEWER = "/parapheur/dossiers/%s/%s/xemelios";

	/* Actions de validation principales les dossiers */
	private static final String ACTION_VISA = "/parapheur/dossiers/%s/visa";
	private static final String ACTION_SIGNATURE = "/parapheur/dossiers/%s/signature";
	private static final String ACTION_TDT_ACTES = "/parapheur/dossiers/%s/tdtActes";
	private static final String ACTION_TDT_HELIOS = "/parapheur/dossiers/%s/tdtHelios";
	private static final String ACTION_MAILSEC = "/parapheur/dossiers/%s/mailsec";
	private static final String ACTION_ARCHIVAGE = "/parapheur/dossiers/%s/archive";
	private static final String ACTION_REJET = "/parapheur/dossiers/%s/rejet";

	/* Autres actions possibles sur les dossiers */
	private static final String ACTION_TRANSFERT_SIGNATURE = "/parapheur/dossiers/%s/transfertSignature";
	private static final String ACTION_AVIS_COMPLEMENTAIRE = "/parapheur/dossiers/%s/avis";

	protected ModelMapper modelMapper = new ModelMapper3();

	@Override
	public List<Bureau> getBureaux() throws IParapheurException {
		return modelMapper.getBureaux(RESTUtils.get(buildUrl(RESOURCE_BUREAUX)));
	}

	@Override
	public Dossier getDossier(String bureauId, String dossierId) throws IParapheurException {
		String url = buildUrl(RESOURCE_DOSSIERS + "/" + dossierId, "bureauCourant=" + bureauId);
		return modelMapper.getDossier(RESTUtils.get(url));
	}

	@Override
	public List<Dossier> getDossiers(String bureauId) throws IParapheurException {

		Filter filter = MyFilters.INSTANCE.getSelectedFilter();
		if (filter == null) {
			filter = new Filter();
		}
		String params = "asc=true" +
				"&bureau=" + bureauId +
				"&corbeilleName=" + filter.getEtat() +
				"&filter=" + filter.getJSONFilter() +
				"&metas={}" +
				"&page=0" +
				"&pageSize=25" +
				"&pendingFile=0" +
				"&skipped=0" +
				"&sort=cm:created";
		//Log.d( IParapheurHttpClient.class, "REQUEST on " + FOLDERS_PATH + ": " + requestBody );
		String url = buildUrl(RESOURCE_DOSSIERS, params);

		return modelMapper.getDossiers(RESTUtils.get(url));
	}

	@Override
	public Map<String, ArrayList<String>> getTypologie() throws IParapheurException {
		String url = buildUrl(RESOURCE_TYPES);
		return modelMapper.getTypologie(RESTUtils.get(url));
	}

	@Override
	public List<EtapeCircuit> getCircuit(String dossierId) throws IParapheurException {
		String url = buildUrl(String.format(Locale.US, RESOURCE_DOSSIER_CIRCUIT, dossierId));
		return modelMapper.getCircuit(RESTUtils.get(url));
	}

	@Override
	public SparseArray<PageAnnotations> getAnnotations(String dossierId) throws IParapheurException {
		String url = buildUrl(String.format(Locale.US, RESOURCE_ANNOTATIONS, dossierId));
		return modelMapper.getAnnotations(RESTUtils.get(url));
	}

	@Override
	public String createAnnotation(@NonNull String dossierId, @NonNull Annotation annotation, int page) throws IParapheurException {

		// Build json object

		JSONStringer annotJson = new JSONStringer();

		try {
			annotJson.object();
			annotJson.key("rect").object();
			{
				annotJson.key("topLeft");
				annotJson.object();
				annotJson.key("x").value(annotation.getRect().left);
				annotJson.key("y").value(annotation.getRect().top);
				annotJson.endObject();

				annotJson.key("bottomRight");
				annotJson.object();
				annotJson.key("x").value(annotation.getRect().right);
				annotJson.key("y").value(annotation.getRect().bottom);
				annotJson.endObject();
			}
			annotJson.endObject();

			annotJson.key("author").value(annotation.getAuthor());
			annotJson.key("date").value(annotation.getDate());
			annotJson.key("page").value(page);
			annotJson.key("text").value(annotation.getText());
			annotJson.key("type").value("rect");
			annotJson.endObject();
		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors de la création de l'annotation", e);
		}

		// Send request

		String url = buildUrl(String.format(Locale.US, RESOURCE_ANNOTATIONS, dossierId));
		RequestResponse response = RESTUtils.post(url, annotJson.toString());

		if (response != null && response.getCode() == HttpStatus.SC_OK) {
			JSONObject idObj = response.getResponse();

			if (idObj != null)
				return idObj.optString("id", null);
		}

		return null;
	}

	@Override
	public void updateAnnotation(@NonNull String dossierId, @NonNull Annotation annotation, int page) throws IParapheurException {

		// Build Json object

		JSONStringer annotJson = new JSONStringer();

		try {
			annotJson.object();
			annotJson.key("rect").object();
			{
				annotJson.key("topLeft");
				annotJson.object();
				annotJson.key("x").value(annotation.getRect().left);
				annotJson.key("y").value(annotation.getRect().top);
				annotJson.endObject();

				annotJson.key("bottomRight");
				annotJson.object();
				annotJson.key("x").value(annotation.getRect().right);
				annotJson.key("y").value(annotation.getRect().bottom);
				annotJson.endObject();
			}
			annotJson.endObject();

			annotJson.key("author").value(annotation.getAuthor());
			annotJson.key("date").value(annotation.getDate());
			annotJson.key("page").value(page);
			annotJson.key("text").value(annotation.getText());
			annotJson.key("type").value("rect");
			annotJson.key("id").value(annotation.getUuid());
			annotJson.key("uuid").value(annotation.getUuid());
			annotJson.endObject();
		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors de l'enregistrement de l'annotation", e);
		}

		// Send request

		String url = buildUrl(String.format(Locale.US, RESOURCE_ANNOTATION, dossierId, annotation.getUuid()));

		RequestResponse response = RESTUtils.put(url, annotJson.toString(), true);

		if (response == null || response.getCode() != HttpStatus.SC_OK)
			throw new IParapheurException(R.string.error_annotation_update, "");
	}

	@Override public void deleteAnnotation(@NonNull String dossierId, @NonNull String annotationId, int page) throws IParapheurException {
		String url = buildUrl(String.format(Locale.US, RESOURCE_ANNOTATION, dossierId, annotationId));
		RESTUtils.delete(url, true);
	}

	@Override public boolean viser(Dossier dossier, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
		String actionUrl = String.format(Locale.US, ACTION_VISA, dossier.getId());
		try {
			JSONObject json = new JSONObject();
			json.put("bureauCourant", bureauId);
			json.put("annotPub", annotPub);
			json.put("annotPriv", annotPriv);
			RequestResponse response = RESTUtils.post(buildUrl(actionUrl), json.toString());
			return (response != null && response.getCode() == HttpStatus.SC_OK);
		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors du visa", e);
		}
	}

	@Override public boolean signer(String dossierId, String signValue, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
		String actionUrl = String.format(Locale.US, ACTION_SIGNATURE, dossierId);
		try {
			JSONObject json = new JSONObject();
			json.put("bureauCourant", bureauId);
			json.put("annotPub", annotPub);
			json.put("annotPriv", annotPriv);
			json.put("signature", signValue);
			RequestResponse response = RESTUtils.post(buildUrl(actionUrl), json.toString());
			return (response != null && response.getCode() == HttpStatus.SC_OK);
		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors de la signature", e);
		}
	}

	@Override public boolean archiver(String dossierId, String archiveTitle, boolean withAnnexes, String bureauId) throws IParapheurException {
		/** FIXME : weird copy/paste. Maybe it has no utility too.
		 String actionUrl = String.format(Locale.US, ACTION_SIGNATURE, dossierId);
		 try {
		 JSONObject json = new JSONObject();
		 json.put("bureauCourant", bureauId);
		 json.put("name", archiveTitle);
		 json.put("annexesInclude", withAnnexes);
		 RequestResponse response = RESTUtils.post(buildUrl(actionUrl), json.toString());
		 return (response != null && response.getCode() == HttpStatus.SC_OK);

		 } catch (JSONException e) {
		 throw new RuntimeException("Une erreur est survenue lors de l'archivage", e);
		 }*/
		return false;
	}

	@Override public boolean envoiTdtHelios(String dossierId, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
		String actionUrl = String.format(Locale.US, ACTION_TDT_HELIOS, dossierId);
		try {
			JSONObject json = new JSONObject();
			json.put("bureauCourant", bureauId);
			json.put("annotPub", annotPub);
			json.put("annotPriv", annotPriv);
			RequestResponse response = RESTUtils.post(buildUrl(actionUrl), json.toString());
			return (response != null && response.getCode() == HttpStatus.SC_OK);

		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors de l'envoi au TdT (Helios)", e);
		}
	}

	@Override public boolean envoiTdtActes(String dossierId, String nature, String classification, String numero, long dateActes, String objet, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
		String actionUrl = String.format(Locale.US, ACTION_TDT_ACTES, dossierId);
		try {
			JSONObject json = new JSONObject();
			json.put("bureauCourant", bureauId);
			json.put("annotPub", annotPub);
			json.put("annotPriv", annotPriv);
			json.put("objet", objet);
			json.put("nature", nature);
			json.put("classification", classification);
			json.put("numero", numero);
			json.put("dateActes", dateActes);
			RequestResponse response = RESTUtils.post(buildUrl(actionUrl), json.toString());
			return (response != null && response.getCode() == HttpStatus.SC_OK);

		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors de l'envoi au TdT (ACTES)", e);
		}
	}

	@Override public boolean envoiMailSec(String dossierId, List<String> destinataires, List<String> destinatairesCC, List<String> destinatairesCCI, String sujet, String message, String password, boolean showPassword, boolean annexesIncluded, String bureauId) throws IParapheurException {

		String actionUrl = String.format(Locale.US, ACTION_MAILSEC, dossierId);
		try {
			JSONObject json = new JSONObject();
			json.put("bureauCourant", bureauId);
			json.put("destinataires", destinataires);
			json.put("destinatairesCC", destinatairesCC);
			json.put("destinatairesCCI", destinatairesCCI);
			json.put("objet", sujet);
			json.put("message", message);
			json.put("password", password);
			json.put("showpass", showPassword);
			json.put("annexesIncluded", annexesIncluded);
			RequestResponse response = RESTUtils.post(buildUrl(actionUrl), json.toString());
			return (response != null && response.getCode() == HttpStatus.SC_OK);

		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors de l'envoi par mail sécurisé", e);
		}
	}

	@Override public boolean rejeter(String dossierId, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
		String actionUrl = String.format(Locale.US, ACTION_REJET, dossierId);
		try {
			JSONObject json = new JSONObject();
			json.put("bureauCourant", bureauId);
			json.put("annotPub", annotPub);
			json.put("annotPriv", annotPriv);
			RequestResponse response = RESTUtils.post(buildUrl(actionUrl), json.toString());
			return (response != null && response.getCode() == HttpStatus.SC_OK);

		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors du rejet", e);
		}
	}
}