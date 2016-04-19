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
package org.adullact.iparapheur.controller.rest.mapper;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Annotation;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Circuit;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.EtapeCircuit;
import org.adullact.iparapheur.model.PageAnnotations;
import org.adullact.iparapheur.model.RequestResponse;
import org.adullact.iparapheur.model.SignInfo;
import org.adullact.iparapheur.utils.JsonExplorer;
import org.adullact.iparapheur.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


public class ModelMapper {

	protected static String DOSSIER_ID = "id";
	protected static String DOSSIER_TITLE = "title";
	protected static String DOSSIER_TYPE = "type";
	protected static String DOSSIER_SUBTYPE = "sousType";
	protected static String DOSSIER_ACTION_DEMANDEE = "actionDemandee";
	protected static String DOSSIER_EMISSION_DATE = "dateEmission";
	protected static String DOSSIER_DATE_LIMITE = "dateLimite";
	protected static String DOSSIER_IS_SIGN_PAPIER = "isSignPapier";
	protected static String DOSSIER_DOCUMENTS = "documents";
	protected static String DOSSIER_CIRCUIT = "circuit";

	protected static String DOCUMENT_ID = "id";
	protected static String DOCUMENT_NAME = "name";
	protected static String DOCUMENT_VISUEL_PDF = "visuelPdf";
	protected static String DOCUMENT_SIZE = "size";
	protected static String DOCUMENT_IS_LOCKED = "isLocked";
	protected static String DOCUMENT_IS_MAIN_DOCUMENT = "isMainDocument";

	protected static String CIRCUIT_IS_DIGITAL_SIGNATURE_MANDATORY = "isDigitalSignatureMandatory";
	protected static String CIRCUIT_HAS_SELECTION_SCRIPT = "hasSelectionScript";
	protected static String CIRCUIT_SIG_FORMAT = "sigFormat";
	protected static String CIRCUIT_ETAPES = "etapes";

	protected static String CIRCUIT_ETAPES_DATE_VALIDATION = "dateValidation";
	protected static String CIRCUIT_ETAPES_APPROVED = "approved";
	protected static String CIRCUIT_ETAPES_REJECTED = "rejected";
	protected static String CIRCUIT_ETAPES_PARAPHEUR_NAME = "parapheurName";
	protected static String CIRCUIT_ETAPES_SIGNATAIRE = "signataire";
	protected static String CIRCUIT_ETAPES_ACTION_DEMANDEE = "actionDemandee";
	protected static String CIRCUIT_ETAPES_PUBLIC_ANNOTATIONS = "annotPub";

	protected static String SIGN_INFO_SIGNATURE_INFORMATIONS = "signatureInformations";
	protected static String SIGN_INFO_HASH = "hash";

	public Dossier getDossier(RequestResponse requestResponse) throws RuntimeException {
		Dossier dossier = null;

		if (requestResponse.getResponse() != null)
			dossier = getDossier(requestResponse.getResponse());

		return dossier;
	}

	protected Dossier getDossier(JSONObject jsonObject) {

		String dossierRef = jsonObject.optString("dossierRef");
		if (dossierRef.contains("workspace://SpacesStore/")) {
			dossierRef = dossierRef.substring("workspace://SpacesStore/".length());
		}
		ArrayList<Action> actions = getActionsForDossier(jsonObject);
		Dossier dossier = new Dossier(dossierRef,
									  jsonObject.optString("titre"),
									  Action.valueOf(jsonObject.optString("actionDemandee", "VISA")),
									  actions,
									  jsonObject.optString("type"),
									  jsonObject.optString("sousType"),
									  StringUtils.parseIso8601Date(jsonObject.optString("dateCreation")),
									  StringUtils.parseIso8601Date(jsonObject.optString("dateLimite")),
									  jsonObject.optBoolean("isSignPapier", false)
		);

		JSONArray documents = jsonObject.optJSONArray("documents");
		if (documents != null) {

			for (int index = 0; index < documents.length(); index++) {
				JSONObject doc = documents.optJSONObject(index);

				if (doc != null) {
					String docRef = jsonObject.optString("dossierRef");
					if (docRef.contains("workspace://SpacesStore/"))
						docRef = dossierRef.substring("workspace://SpacesStore/".length());

					String downloadUrl = doc.optString("downloadUrl");
					if (doc.has("visuelPdfUrl")) {
						downloadUrl += ";ph:visuel-pdf";
					}

					dossier.addDocument(new Document(docRef, dossierRef, doc.optString("name"), doc.optInt("size", -1), downloadUrl, false, (index == 0)));
				}
			}
		}
		return dossier;
	}

	protected ArrayList<Action> getActionsForDossier(JSONObject dossier) {
		ArrayList<Action> actions = new ArrayList<>();
		boolean isActeurCourant = dossier.optBoolean("isActeurCourant", false);
		if (isActeurCourant) {
			actions.add(Action.EMAIL);
			actions.add(Action.JOURNAL);
			actions.add(Action.ENREGISTRER);
		}
		JSONObject returnedActions = dossier.optJSONObject("actions");
		String actionDemandee = dossier.optString("actionDemandee");
		if (returnedActions != null) {
			Iterator actionsIterator = returnedActions.keys();
			while (actionsIterator.hasNext()) {
				String action = (String) actionsIterator.next();
				boolean isActionEnabled = returnedActions.optBoolean(action, false);
				if (isActionEnabled) {
					if (action.equals("archive")) {
						if (actionDemandee.equals("ARCHIVAGE")) {
							actions.add(Action.ARCHIVAGE);
						}
					}
					else if (action.equals("delete")) {
						actions.add(Action.SUPPRESSION);
					}
					else if (action.equals("reject")) {
						actions.add(Action.REJET);
					}
					else if (action.equals("remorse")) {
						actions.add(Action.REMORD);
					}
					else if (action.equals("secretary")) {
						actions.add(Action.SECRETARIAT);
					}
					else if (action.equals("sign")) {
						if (actionDemandee.equals("SIGNATURE")) {
							actions.add(Action.SIGNATURE);
						}
						else if (actionDemandee.equals("VISA")) {
							actions.add(Action.VISA);
						}
						else if (actionDemandee.equals("MAILSEC")) {
							actions.add(Action.MAILSEC);
						}
					}
				}
			}
		}
		return actions;
	}

	public ArrayList<Dossier> getDossiers(RequestResponse requestResponse) {
		ArrayList<Dossier> dossiers = new ArrayList<>();
		if (requestResponse.getResponse() != null) {
			JSONArray array = requestResponse.getResponse().optJSONArray("dossiers");
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					JSONObject dossierJSON = array.optJSONObject(i);
					if (dossierJSON != null) {
						Dossier dossier = getDossier(dossierJSON);
						if (dossier != null) {
							dossiers.add(dossier);
						}
					}
				}
			}
		}
		return dossiers;
	}

	public @NonNull Circuit getCircuit(@NonNull RequestResponse response) {

		ArrayList<EtapeCircuit> circuit = new ArrayList<>();

		JsonExplorer jsonExplorer = new JsonExplorer(response.getResponse());
		for (int index = 0; index < jsonExplorer.findArray(DOSSIER_CIRCUIT).getCurrentArraySize(); index++) {

			EtapeCircuit etapeCircuit = new EtapeCircuit(jsonExplorer.findArray(DOSSIER_CIRCUIT).find(index).optString(CIRCUIT_ETAPES_DATE_VALIDATION),
														 jsonExplorer.findArray(DOSSIER_CIRCUIT).find(index).optBoolean(CIRCUIT_ETAPES_APPROVED, false),
														 jsonExplorer.findArray(DOSSIER_CIRCUIT).find(index).optBoolean(CIRCUIT_ETAPES_REJECTED, false),
														 jsonExplorer.findArray(DOSSIER_CIRCUIT).find(index).optString(CIRCUIT_ETAPES_PARAPHEUR_NAME, ""),
														 jsonExplorer.findArray(DOSSIER_CIRCUIT).find(index).optString(CIRCUIT_ETAPES_SIGNATAIRE, ""),
														 jsonExplorer.findArray(DOSSIER_CIRCUIT).find(index).optString(CIRCUIT_ETAPES_ACTION_DEMANDEE,
																													   Action.VISA.toString()
														 ),
														 jsonExplorer.findArray(DOSSIER_CIRCUIT).find(index).optString(CIRCUIT_ETAPES_PUBLIC_ANNOTATIONS, "")
			);

			circuit.add(etapeCircuit);
		}

		return new Circuit(circuit, null, true, false);
	}

	public ArrayList<Bureau> getBureaux(RequestResponse response) {
		ArrayList<Bureau> bureaux = new ArrayList<Bureau>();
		if (response.getResponse() != null) {
			JSONArray array = response.getResponse().optJSONArray("bureaux");
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					JSONObject bureau = array.optJSONObject(i);
					if (bureau != null) {
						String bureauRef = bureau.optString("nodeRef");
						if (bureauRef.contains("workspace://SpacesStore/")) {
							bureauRef = bureauRef.substring("workspace://SpacesStore/".length());
						}
						bureaux.add(new Bureau(bureauRef, bureau.optString("name")));
					}
				}
			}
		}
		return bureaux;
	}

	public @NonNull SignInfo getSignInfo(RequestResponse response) {
		SignInfo result = new SignInfo();

		JsonExplorer jsonExplorer = new JsonExplorer(response.getResponse());
		String hash = jsonExplorer.findObject(SIGN_INFO_SIGNATURE_INFORMATIONS).optString(SIGN_INFO_HASH, null);

		result.setHash(hash);

		return result;
	}

	public LinkedHashMap<String, ArrayList<String>> getTypologie(RequestResponse response) {
		LinkedHashMap<String, ArrayList<String>> typologie = new LinkedHashMap<String, ArrayList<String>>();
		if (response.getResponse() != null) {
			JSONObject data = response.getResponse().optJSONObject("data");
			if (data != null) {
				JSONObject typology = data.optJSONObject("typology");
				if (typology != null) {
					Iterator types = typology.keys();
					while (types.hasNext()) {
						String type = (String) types.next();
						JSONArray jsonSousTypes = typology.optJSONArray(type);
						if (jsonSousTypes != null) {
							ArrayList<String> sousTypes = new ArrayList<String>(jsonSousTypes.length());
							for (int i = 0; i < jsonSousTypes.length(); i++) {
								String sousType = jsonSousTypes.optString(i);
								if (!sousType.isEmpty()) {
									sousTypes.add(sousType);
								}
							}
							typologie.put(type, sousTypes);
						}
					}
				}
			}
		}
		return typologie;
	}

	public SparseArray<PageAnnotations> getAnnotations(RequestResponse response) {
		SparseArray<PageAnnotations> annotations = new SparseArray<>();

		// Default case

		if (response.getResponseArray() == null)
			return annotations;

		// Parsing

		JsonExplorer jsonExplorer = new JsonExplorer(response.getResponseArray());

		for (int etapeNumber = 0; etapeNumber < jsonExplorer.getCurrentArraySize(); etapeNumber++) {
			JsonObject pagesDict = jsonExplorer.find(etapeNumber).optCurrentJsonObject(new JsonObject());

			for (Map.Entry<String, JsonElement> pageDict : pagesDict.entrySet()) {
				JsonExplorer jsonPageExplorer = new JsonExplorer(pageDict.getValue());

				PageAnnotations pageAnnotations = new PageAnnotations();

				for (int annotationNumber = 0; annotationNumber < jsonPageExplorer.getCurrentArraySize(); annotationNumber++) {
					pageAnnotations.add(new Annotation(jsonPageExplorer.find(annotationNumber).optCurrentJsonObject(new JsonObject()),
													   Integer.valueOf(pageDict.getKey()),
													   etapeNumber
					));
				}

				annotations.put(Integer.valueOf(pageDict.getKey()), pageAnnotations);
			}
		}

		return annotations;
	}
}
