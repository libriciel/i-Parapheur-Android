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

import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

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
import org.adullact.iparapheur.utils.DossierUtils;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.SerializableSparseArray;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by jmaire on 09/06/2014.
 * API i-Parapheur version 1
 * <p/>
 * de la v4.0.00 / v3.4.00 (comprise)
 * a la v4.1.00 / v3.5.00 (exclue)
 * <p/>
 * La v4.0.00 est la première version i-Parapheur
 * compatible Android
 */
public class RestClientApi1 extends RestClientApi {

	protected static final String RESOURCE_DOSSIERS = "/parapheur/dossiers";

	protected static final String ACTION_GET_BUREAUX = "/parapheur/api/getBureaux";
	private static final String ACTION_GET_DOSSIER = "/parapheur/api/getDossier";
	private static final String ACTION_GET_DOSSIERS = "/parapheur/api/getDossiersHeaders";
	private static final String ACTION_GET_CIRCUIT = "/parapheur/api/getCircuit";
	private static final String ACTION_GET_TYPOLOGIE = "/parapheur/api/getTypologie";

	private static final String ACTION_GET_ANNOTATIONS = "/parapheur/api/getAnnotations";
	private static final String ACTION_CREATE_ANNOTATION = "/parapheur/api/addAnnotation";
	private static final String ACTION_UPDATE_ANNOTATION = "/parapheur/api/updateAnnotation";
	private static final String ACTION_DELETE_ANNOTATION = "/parapheur/api/removeAnnotation";

	private static final String ACTION_VISA = "/parapheur/api/visa";
	private static final String ACTION_SIGNATURE = "/parapheur/api/signature";
	private static final String ACTION_TDT = "/parapheur/api/tdt";
	private static final String ACTION_MAILSEC = "/parapheur/api/mailsec";
	private static final String ACTION_ARCHIVAGE = "/parapheur/api/archivage";
	private static final String ACTION_REJET = "/parapheur/api/rejet";

	protected ModelMapper modelMapper = new ModelMapper();

	@Override public List<Bureau> getBureaux(@NonNull Account account) throws IParapheurException {
		return null;
	}

	@Override public Dossier getDossier(String bureauId, String dossierId) throws IParapheurException {

		String url = buildUrl(RESOURCE_DOSSIERS + "/" + dossierId, "bureauCourant=" + bureauId);
		String response = RESTUtils.get(url).getResponse().toString();
		Dossier dossierParsed = CollectionUtils.buildGsonWithDateParser().fromJson(new JsonParser().parse(response).getAsJsonObject(), Dossier.class);

		DossierUtils.fixActions(dossierParsed);
		return dossierParsed;
	}

	@Override public List<Dossier> getDossiers(@NonNull Account account, @NonNull String bureauId, @Nullable Filter filter) throws IParapheurException {

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

		String url = buildUrl(account, RESOURCE_DOSSIERS, params, true);
		String response = RESTUtils.get(url).getResponse().toString();

		// Parse

		Type listDossierType = new TypeToken<ArrayList<Dossier>>() {}.getType();
		ArrayList<Dossier> dossiersParsed = CollectionUtils.buildGsonWithDateParser().fromJson(new JsonParser().parse(response).getAsJsonObject(),
																							   listDossierType
		);

		for (Dossier dossier : dossiersParsed)
			DossierUtils.fixActions(dossier);

		return dossiersParsed;
	}

	@Override public List<ParapheurType> getTypologie() throws IParapheurException {
		return null;
	}

	@Override public Circuit getCircuit(String dossierId) throws IParapheurException {
		String url = buildUrl(ACTION_GET_CIRCUIT);
		String body = "{\"dossier\": \"workspace://SpacesStore/" + dossierId + "\"}";
		return null;
	}

	@Override public SerializableSparseArray<PageAnnotations> getAnnotations(@NonNull String dossierId, @NonNull String documentId) throws IParapheurException {
		String url = buildUrl(ACTION_GET_ANNOTATIONS);
		String body = "{\"dossier\": \"workspace://SpacesStore/" + dossierId + "\"}";
		return modelMapper.getAnnotations(RESTUtils.post(url, body));
	}

	@Override public String createAnnotation(@NonNull Account account, @NonNull String dossierId, @NonNull String documentId, //
											 @NonNull Annotation annotation, int page) throws IParapheurException {

		String url = buildUrl(account, ACTION_CREATE_ANNOTATION);
		JSONObject annot = new JSONObject();
		float annotHeight = annotation.getRect().height();
		float annotwidth = annotation.getRect().width();
		float centerX = annotation.getRect().centerX();
		float centerY = annotation.getRect().centerY();

		try {
			JSONObject rect = new JSONObject().putOpt("bottomRight",
													  new JSONObject().put("x", centerX + annotwidth / 2).put("y", centerY - annotHeight / 2)
			).putOpt("topLeft", new JSONObject().put("x", centerX - annotwidth / 2).put("y", centerY + annotHeight / 2));

			annot.put("dossier", "workspace://SpacesStore/" + dossierId).put("annotations",
																			 new JSONArray().put(new JSONObject().put("author", annotation.getAuthor()).put(
																					 "page",
																					 page
																			 ).put("rect", rect).put("text", annotation.getText()).put("type", "rect"))
			);
		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors de la création de l'annotation", e);
		}

		RequestResponse response = RESTUtils.post(url, annot.toString());
		if (response != null && response.getCode() == HttpURLConnection.HTTP_OK) {
			JSONObject idsAnnots = response.getResponse();
			if (idsAnnots != null) {
				JSONArray ids = idsAnnots.optJSONArray("uuids");
				if (ids != null) {
					return ids.optString(0);
				}
			}
		}
		return null;
	}

	@Override public void updateAnnotation(@NonNull String dossierId, @NonNull String documentId, @NonNull Annotation annotation,
										   int page) throws IParapheurException {
		String url = buildUrl(ACTION_CREATE_ANNOTATION);
		JSONObject annot = new JSONObject();
		float annotHeight = annotation.getRect().height();
		float annotwidth = annotation.getRect().width();
		float centerX = annotation.getRect().centerX();
		float centerY = annotation.getRect().centerY();

		try {
			JSONObject rect = new JSONObject().putOpt("bottomRight",
													  new JSONObject().put("x", centerX + annotwidth / 2).put("y", centerY - annotHeight / 2)
			).putOpt("topLeft", new JSONObject().put("x", centerX - annotwidth / 2).put("y", centerY + annotHeight / 2));

			annot.put("dossier", "workspace://SpacesStore/" + dossierId).put("annotation",
																			 new JSONObject().put("uuid", annotation.getUuid()).put("rect", rect).put("text",
																																					  annotation.getText()
																			 )
			);
		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors de l'enregistrement de l'annotation", e);
		}

		RequestResponse response = RESTUtils.post(url, annot.toString());
		if (response == null || response.getCode() != HttpURLConnection.HTTP_OK) {
			throw new IParapheurException(R.string.Error_on_annotation_update, "");
		}
	}

	@Override public void deleteAnnotation(@NonNull String dossierId, @NonNull String documentId, @NonNull String annotationId,
										   int page) throws IParapheurException {
		String url = buildUrl(ACTION_DELETE_ANNOTATION);
		String body = "{\"dossier\": \"workspace://SpacesStore/" + dossierId + "\"," +
				"\"page\": " + page + "," +
				"\"uuid\": \"" + annotationId + "\"}";
		RequestResponse response = RESTUtils.post(url, body);
		if (response == null || response.getCode() != HttpURLConnection.HTTP_OK) {
			throw new IParapheurException(R.string.Error_on_annotation_delete, "");
		}
	}

	@Override public boolean updateAccountInformations(@NonNull Account account) throws IParapheurException {
		return false;
	}

	@Override public boolean viser(Dossier dossier, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
		try {
			JSONObject json = new JSONObject();
			JSONArray dossiersId = new JSONArray();
			dossiersId.put("workspace://SpacesStore/" + dossier.getId());
			json.put("dossiers", dossiersId);
			json.put("bureauCourant", "workspace://SpacesStore/" + bureauId);
			json.put("annotPub", annotPub);
			json.put("annotPriv", annotPriv);

			RESTUtils.post(buildUrl(ACTION_VISA), json.toString());
		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors du visa", e);
		}
		return true;
	}

	@Override public boolean signer(String dossierId, String signValue, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
		return false;
	}

	@Override public boolean signPapier(String dossierId, String bureauId) throws IParapheurException {
		return false;
	}

	@Override public boolean archiver(String dossierId, String archiveTitle, boolean withAnnexes, String bureauId) throws IParapheurException {
		return false;
	}

	@Override public boolean envoiTdtHelios(String dossierId, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
		return false;
	}

	@Override public boolean envoiTdtActes(String dossierId, String nature, String classification, String numero, long dateActes, String objet, String annotPub,
										   String annotPriv, String bureauId) throws IParapheurException {
		return false;
	}

	@Override public boolean envoiMailSec(String dossierId, List<String> destinataires, List<String> destinatairesCC, List<String> destinatairesCCI,
										  String sujet, String message, String password, boolean showPassword, boolean annexesIncluded,
										  String bureauId) throws IParapheurException {
		return false;
	}

	@Override public boolean rejeter(String dossierId, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
		return false;
	}

	@Override public SignInfo getSignInfo(String dossierId, String bureauId) throws IParapheurException {
		return null;
	}
}
