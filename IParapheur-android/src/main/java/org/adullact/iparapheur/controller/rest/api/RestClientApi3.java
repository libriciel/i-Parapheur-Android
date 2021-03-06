/*
 * <p>iParapheur Android<br/>
 * Copyright (C) 2016 Adullact-Projet.</p>
 *
 * <p>This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.</p>
 *
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.</p>
 *
 * <p>You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.</p>
 */
package org.adullact.iparapheur.controller.rest.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.RESTUtils;
import org.adullact.iparapheur.controller.rest.mapper.ModelMapper;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Annotation;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Circuit;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.Filter;
import org.adullact.iparapheur.model.PageAnnotations;
import org.adullact.iparapheur.model.ParapheurType;
import org.adullact.iparapheur.model.RequestResponse;
import org.adullact.iparapheur.model.SignInfo;
import org.adullact.iparapheur.model.State;
import org.adullact.iparapheur.utils.CollectionUtils;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.JsonExplorer;
import org.adullact.iparapheur.utils.SerializableSparseArray;
import org.adullact.iparapheur.utils.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Locale;


public class RestClientApi3 extends RestClientApi {

	private static final String RESOURCE_DOSSIERS = "/parapheur/dossiers";

	/* Ressources principales */
	private static final String RESOURCE_BUREAUX = "/parapheur/bureaux";
	private static final String RESOURCE_DOSSIER_CIRCUIT = "/parapheur/dossiers/%s/circuit";
	private static final String RESOURCE_SIGN_INFO = "/parapheur/dossiers/%s/getSignInfo";
	private static final String RESOURCE_TYPES = "/parapheur/types";
	private static final String RESOURCE_ANNOTATIONS = "/parapheur/dossiers/%s/annotations";
	private static final String RESOURCE_ANNOTATION = "/parapheur/dossiers/%s/annotations/%s";
	private static final String RESOURCE_USER_INFO = "/webframework/content/metadata2";
	// private static final String RESOURCE_DELEGATIONS = "/parapheur/delegations";

	private static final String USER_INFO_FIRST_NAME = "{http://www.alfresco.org/model/content/1.0}firstName";
	private static final String USER_INFO_LAST_NAME = "{http://www.alfresco.org/model/content/1.0}lastName";
	/* Ressources secondaires */
	// private static final String RESOURCE_ANNEXES = "/parapheur/dossiers/%s/annexes";
	// private static final String RESOURCE_CONSECUTIVE_STEPS = "/parapheur/dossiers/%s/consecutiveSteps";
	// private static final String RESOURCE_JOURNAL_EVENEMENT = "/parapheur/dossiers/%s/evenements";

	/* Resources sur la lecture des documents */
	/* Le premier argument est l'id du dossier, le second l'id du document, le dernier le numéro de page */
	// private static final String RESOURCE_DOCUMENT_PAGE = "/parapheur/dossiers/%s/%s/%d";
	// private static final String RESOURCE_XEMELIOS_VIEWER = "/parapheur/dossiers/%s/%s/xemelios";

	/* Actions de validation principales les dossiers */
	private static final String ACTION_VISA = "/parapheur/dossiers/%s/visa";
	private static final String ACTION_SIGNATURE = "/parapheur/dossiers/%s/signature";
	private static final String ACTION_SIGNATURE_PAPIER = "/parapheur/dossiers/%s/signPapier";
	private static final String ACTION_TDT_ACTES = "/parapheur/dossiers/%s/tdtActes";
	private static final String ACTION_TDT_HELIOS = "/parapheur/dossiers/%s/tdtHelios";
	private static final String ACTION_MAILSEC = "/parapheur/dossiers/%s/mailsec";
	// private static final String ACTION_ARCHIVAGE = "/parapheur/dossiers/%s/archive";
	private static final String ACTION_REJET = "/parapheur/dossiers/%s/rejet";

	/* Autres actions possibles sur les dossiers */
	// private static final String ACTION_TRANSFERT_SIGNATURE = "/parapheur/dossiers/%s/transfertSignature";
	// private static final String ACTION_AVIS_COMPLEMENTAIRE = "/parapheur/dossiers/%s/avis";

	private Gson mGson = CollectionUtils.buildGsonWithDateParser();
	private ModelMapper modelMapper = new ModelMapper();

	@Override public Dossier getDossier(@NonNull Account currentAccount, String bureauId, String dossierId) throws IParapheurException {

		String url = buildUrl(RESOURCE_DOSSIERS + "/" + dossierId, "bureauCourant=" + bureauId);
		String response = RESTUtils.get(url).getResponse().toString();

		return Dossier.fromJsonObject(response, mGson);
	}

	@Override public List<Dossier> getDossiers(@NonNull Account currentAccount, @NonNull String bureauId, @Nullable Filter filter) throws IParapheurException {

		String params = "asc=true" +
				"&bureau=" + bureauId +
				"&corbeilleName=" + ((filter != null) ? filter.getState().getServerValue() : State.A_TRAITER.getServerValue()) +
				((filter != null) ? "&filter=" + filter.getJSONFilter() : "") +
				"&metas={}" +
				"&page=0" +
				"&pageSize=25" +
				"&pendingFile=0" +
				"&skipped=0" +
				"&sort=cm:created";

		//Log.d( IParapheurHttpClient.class, "REQUEST on " + FOLDERS_PATH + ": " + requestBody );

		String url = buildUrl(currentAccount, RESOURCE_DOSSIERS, params, true);
		String response = RESTUtils.get(url).getResponseArray().toString();

		return Dossier.fromJsonArray(response, mGson);
	}

	@Override public List<Bureau> getBureaux(@NonNull Account currentAccount) throws IParapheurException {

		String url = buildUrl(currentAccount, RESOURCE_BUREAUX);
		RequestResponse result = RESTUtils.get(url);

		return Bureau.fromJsonArray(result.getResponseArray().toString(), mGson);
	}

	@Override public List<ParapheurType> getTypologie(@NonNull Account currentAccount) throws IParapheurException {

		String url = buildUrl(RESOURCE_TYPES);
		RequestResponse result = RESTUtils.get(url);

		return ParapheurType.fromJsonArray(result.getResponseArray().toString(), mGson);
	}

	@Override public Circuit getCircuit(@NonNull Account currentAccount, String dossierId) throws IParapheurException {

		String url = buildUrl(String.format(Locale.US, RESOURCE_DOSSIER_CIRCUIT, dossierId));
		String response = String.valueOf(RESTUtils.get(url).getResponse());
		String responseCircuit = new JsonParser().parse(response).getAsJsonObject().get("circuit").toString();

		return Circuit.fromJsonObject(responseCircuit, mGson);
	}

	@Override public SignInfo getSignInfo(@NonNull Account currentAccount, String dossierId, String bureauId) throws IParapheurException {

		String url = buildUrl(String.format(Locale.US, RESOURCE_SIGN_INFO, dossierId), "bureauCourant=" + bureauId);
		RequestResponse response = RESTUtils.get(url);
		return modelMapper.getSignInfo(response);
	}

	@Override public boolean updateAccountInformations(@NonNull Account currentAccount) throws IParapheurException {

		String params = "user=" + currentAccount.getLogin();
		String url = buildUrl(currentAccount, RESOURCE_USER_INFO, params, true);
		RequestResponse response = RESTUtils.get(url);

		if (response.getResponse() != null) {

			JsonExplorer json = new JsonExplorer(response.getResponse());
			String firstName = json.findObject("data").findObject("properties").optString(USER_INFO_FIRST_NAME);
			String lastName = json.findObject("data").findObject("properties").optString(USER_INFO_LAST_NAME);

			if (StringUtils.areNotEmpty(firstName, lastName))
				currentAccount.setUserFullName(firstName + " " + lastName);
		}

		return true;
	}

	// <editor-fold desc="Annotations">

	protected @NonNull String getAnnotationsUrlSuffix(@NonNull String dossierId, @NonNull String documentId) {
		return String.format(Locale.US, RESOURCE_ANNOTATIONS, dossierId);
	}

	protected @NonNull String getAnnotationUrlSuffix(@NonNull String dossierId, @NonNull String documentId, @NonNull String annotationId) {
		return String.format(Locale.US, RESOURCE_ANNOTATION, dossierId, annotationId);
	}

	@Override public SerializableSparseArray<PageAnnotations> getAnnotations(@NonNull Account currentAccount, @NonNull String dossierId,
																			 @NonNull String documentId) throws IParapheurException {
		String url = buildUrl(currentAccount, getAnnotationsUrlSuffix(dossierId, documentId));
		return modelMapper.getAnnotations(RESTUtils.get(url));
	}

	@Override public String createAnnotation(@NonNull Account currentAccount, @NonNull String dossierId, @NonNull String documentId, //
											 @NonNull Annotation annotation, int page) throws IParapheurException {

		// Build json object

		JSONStringer annotationJson = new JSONStringer();

		try {
			annotationJson.object();
			{
				annotationJson.key("rect").object();
				{
					annotationJson.key("topLeft");
					annotationJson.object();
					annotationJson.key("x").value(annotation.getRect().left);
					annotationJson.key("y").value(annotation.getRect().top);
					annotationJson.endObject();

					annotationJson.key("bottomRight");
					annotationJson.object();
					annotationJson.key("x").value(annotation.getRect().right);
					annotationJson.key("y").value(annotation.getRect().bottom);
					annotationJson.endObject();
				}
				annotationJson.endObject();

				annotationJson.key("author").value(annotation.getAuthor());
				annotationJson.key("date").value(annotation.getDate());
				annotationJson.key("page").value(page);
				annotationJson.key("text").value(annotation.getText() != null ? annotation.getText() : "");
				annotationJson.key("type").value("rect");

			}
			annotationJson.endObject();
		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors de la création de l'annotation", e);
		}

		// Send request

		String url = buildUrl(currentAccount, getAnnotationsUrlSuffix(dossierId, documentId));
		RequestResponse response = RESTUtils.post(url, annotationJson.toString());

		if (response != null && response.getCode() == HttpURLConnection.HTTP_OK) {
			JSONObject idObj = response.getResponse();

			if (idObj != null)
				return idObj.optString("id", null);
		}

		return null;
	}

	@Override public void updateAnnotation(@NonNull Account currentAccount, @NonNull String dossierId, @NonNull String documentId,
										   @NonNull Annotation annotation, int page) throws IParapheurException {

		// Build Json object

		JSONStringer annotationJson = new JSONStringer();

		try {
			annotationJson.object();
			{
				annotationJson.key("rect").object();
				{
					annotationJson.key("topLeft");
					annotationJson.object();
					annotationJson.key("x").value(annotation.getRect().left);
					annotationJson.key("y").value(annotation.getRect().top);
					annotationJson.endObject();

					annotationJson.key("bottomRight");
					annotationJson.object();
					annotationJson.key("x").value(annotation.getRect().right);
					annotationJson.key("y").value(annotation.getRect().bottom);
					annotationJson.endObject();
				}
				annotationJson.endObject();

				annotationJson.key("author").value(annotation.getAuthor());
				annotationJson.key("date").value(annotation.getDate());
				annotationJson.key("page").value(page);
				annotationJson.key("text").value(annotation.getText() != null ? annotation.getText() : "");
				annotationJson.key("type").value("rect");
				annotationJson.key("id").value(annotation.getUuid());
				annotationJson.key("uuid").value(annotation.getUuid());
			}
			annotationJson.endObject();
		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors de l'enregistrement de l'annotation", e);
		}

		// Send request

		String url = buildUrl(getAnnotationUrlSuffix(dossierId, documentId, annotation.getUuid()));
		RequestResponse response = RESTUtils.put(url, annotationJson.toString(), true);

		if (response == null || response.getCode() != HttpURLConnection.HTTP_OK)
			throw new IParapheurException(R.string.Error_on_annotation_update, "");
	}

	@Override public void deleteAnnotation(@NonNull Account currentAccount, @NonNull String dossierId, @NonNull String documentId, @NonNull String annotationId,
										   int page) throws IParapheurException {

		String url = buildUrl(getAnnotationUrlSuffix(dossierId, documentId, annotationId));
		RESTUtils.delete(url, true);
	}

	// </editor-fold desc="Annotations">

	// <editor-fold desc="Actions">

	@Override public boolean viser(@NonNull Account currentAccount, Dossier dossier, String annotPub, String annotPriv,
								   String bureauId) throws IParapheurException {
		String actionUrl = String.format(Locale.US, ACTION_VISA, dossier.getId());
		try {
			JSONObject json = new JSONObject();
			json.put("bureauCourant", bureauId);
			json.put("annotPub", annotPub);
			json.put("annotPriv", annotPriv);
			RequestResponse response = RESTUtils.post(buildUrl(currentAccount, actionUrl), json.toString());
			return (response != null && response.getCode() == HttpURLConnection.HTTP_OK);
		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors du visa", e);
		}
	}

	@Override public boolean seal(@NonNull Account currentAccount, Dossier dossier, String annotPub, String annotPriv,
								  String bureauId) throws IParapheurException {

		throw new IParapheurException(R.string.Error_forward_version_action, null);
	}

	@Override public boolean signer(@NonNull Account currentAccount, String dossierId, String signValue, String annotPub, String annotPriv,
									String bureauId) throws IParapheurException {
		String actionUrl = String.format(Locale.US, ACTION_SIGNATURE, dossierId);

		JSONStringer jsonStringer = new JSONStringer();
		try {
			jsonStringer.object();
			{
				jsonStringer.key("bureauCourant").value(bureauId);
				jsonStringer.key("annotPub").value(annotPub);
				jsonStringer.key("annotPriv").value(annotPriv);
				jsonStringer.key("signature").value(signValue);
			}
			jsonStringer.endObject();
		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors de la signature", e);
		}

		RequestResponse response = RESTUtils.post(buildUrl(actionUrl), jsonStringer.toString());
		return (response != null && response.getCode() == HttpURLConnection.HTTP_OK);
	}

	@Override public boolean signPapier(@NonNull Account currentAccount, String dossierId, String bureauId) throws IParapheurException {
		String actionUrl = String.format(Locale.US, ACTION_SIGNATURE_PAPIER, dossierId);

		JSONStringer jsonStringer = new JSONStringer();
		try {
			jsonStringer.object();
			jsonStringer.key("bureauCourant").value(bureauId);
			jsonStringer.endObject();
		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors de la conversion en signature papier", e);
		}

		RequestResponse response = RESTUtils.post(buildUrl(actionUrl), jsonStringer.toString());
		return (response != null && response.getCode() == HttpURLConnection.HTTP_OK);
	}

	@Override public boolean archiver(@NonNull Account currentAccount, String dossierId, String archiveTitle, boolean withAnnexes,
									  String bureauId) throws IParapheurException {
		/** FIXME : weird copy/paste. Maybe it has no utility too.
		 String actionUrl = String.format(Locale.US, ACTION_SIGNATURE, dossierId);
		 try {
		 JSONObject json = new JSONObject();
		 json.put("bureauCourant", bureauId);
		 json.put("name", archiveTitle);
		 json.put("annexesInclude", withAnnexes);
		 RequestResponse response = RESTUtils.post(buildUrl(actionUrl), json.toString());
		 return (response != null && response.getCode() == HttpURLConnection.HTTP_OK);

		 } catch (JSONException e) {
		 throw new RuntimeException("Une erreur est survenue lors de l'archivage", e);
		 }*/
		return false;
	}

	@Override public boolean envoiTdtHelios(@NonNull Account currentAccount, String dossierId, String annotPub, String annotPriv,
											String bureauId) throws IParapheurException {
		String actionUrl = String.format(Locale.US, ACTION_TDT_HELIOS, dossierId);
		try {
			JSONObject json = new JSONObject();
			json.put("bureauCourant", bureauId);
			json.put("annotPub", annotPub);
			json.put("annotPriv", annotPriv);
			RequestResponse response = RESTUtils.post(buildUrl(actionUrl), json.toString());
			return (response != null && response.getCode() == HttpURLConnection.HTTP_OK);

		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors de l'envoi au TdT (Helios)", e);
		}
	}

	@Override public boolean envoiTdtActes(@NonNull Account currentAccount, String dossierId, String nature, String classification, String numero,
										   long dateActes, String objet, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
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
			return (response != null && response.getCode() == HttpURLConnection.HTTP_OK);

		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors de l'envoi au TdT (ACTES)", e);
		}
	}

	@Override public boolean envoiMailSec(@NonNull Account currentAccount, String dossierId, List<String> destinataires, List<String> destinatairesCC,
										  List<String> destinatairesCCI, String sujet, String message, String password, boolean showPassword,
										  boolean annexesIncluded, String bureauId) throws IParapheurException {

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
			return (response != null && response.getCode() == HttpURLConnection.HTTP_OK);

		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors de l'envoi par mail sécurisé", e);
		}
	}

	@Override public boolean rejeter(@NonNull Account currentAccount, String dossierId, String annotPub, String annotPriv,
									 String bureauId) throws IParapheurException {
		String actionUrl = String.format(Locale.US, ACTION_REJET, dossierId);
		try {
			JSONObject json = new JSONObject();
			json.put("bureauCourant", bureauId);
			json.put("annotPub", annotPub);
			json.put("annotPriv", annotPriv);
			RequestResponse response = RESTUtils.post(buildUrl(actionUrl), json.toString());
			return (response != null && response.getCode() == HttpURLConnection.HTTP_OK);

		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors du rejet", e);
		}
	}

	// </editor-fold desc="Actions">
}
