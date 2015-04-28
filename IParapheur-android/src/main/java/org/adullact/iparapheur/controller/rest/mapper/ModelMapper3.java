package org.adullact.iparapheur.controller.rest.mapper;

import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.EtapeCircuit;
import org.adullact.iparapheur.model.RequestResponse;
import org.adullact.iparapheur.utils.JsonExplorer;
import org.adullact.iparapheur.utils.TransformUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Created by jmaire on 04/11/2013.
 * Utilitaire de conversion des réponses json du serveur i-Parapheur pour la version d'API 3.
 */
public class ModelMapper3 extends ModelMapper {

	@Override
	public Dossier getDossier(RequestResponse requestResponse) throws RuntimeException {
		Dossier dossier = null;

		if (requestResponse.getResponse() != null)
			dossier = getDossier(requestResponse.getResponse());

		return dossier;
	}

	@Override
	protected Dossier getDossier(JSONObject jsonObject) {

		String dossierId = jsonObject.optString("id");
		ArrayList<Action> actions = getActionsForDossier(jsonObject);
		Dossier dossier = new Dossier(dossierId, jsonObject.optString("title"), Action.valueOf(jsonObject.optString("actionDemandee", "VISA")), actions, jsonObject.optString("type"), jsonObject.optString("sousType"), TransformUtils.parseISO8601Date(jsonObject.optString("dateEmission")), TransformUtils.parseISO8601Date(jsonObject.optString("dateLimite")));

		JSONArray documents = jsonObject.optJSONArray("documents");
		if (documents != null) {
			for (int index = 0; index < documents.length(); index++) {
				JSONObject doc = documents.optJSONObject(index);
				if (doc != null) {
					String docId = doc.optString("id");
					String downloadUrl = "/api/node/workspace/SpacesStore/" + docId + "/content";

					if (doc.optBoolean("visuelPdf", false))
						downloadUrl += ";ph:visuel-pdf";

					dossier.addDocument(new Document(docId, dossierId, doc.optString("name"), doc.optInt("size", -1), downloadUrl));
				}
			}
		}
		return dossier;
	}

	@Override
	protected ArrayList<Action> getActionsForDossier(JSONObject dossier) {
		ArrayList<Action> actions = new ArrayList<>();
		JSONArray JSONActions = dossier.optJSONArray("actions");
		if (JSONActions != null) {
			for (int i = 0; i < JSONActions.length(); i++) {
				String action = JSONActions.optString(i, null);
				if (action != null) {
					actions.add(Action.valueOf(action));
				}
			}
		}
		return actions;
	}

	@Override
	public ArrayList<Dossier> getDossiers(RequestResponse requestResponse) {
		ArrayList<Dossier> dossiers = new ArrayList<>();
		JSONArray array = requestResponse.getResponseArray();
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
		return dossiers;
	}

	@Override
	public ArrayList<EtapeCircuit> getCircuit(RequestResponse response) {
		ArrayList<EtapeCircuit> circuit = new ArrayList<>();
		if (response.getResponse() != null) {
			JSONObject circuitObject = response.getResponse().optJSONObject("circuit");
			if (circuitObject != null) {
				JSONArray circuitArray = circuitObject.optJSONArray("etapes");
				if (circuitArray != null) {
					for (int i = 0; i < circuitArray.length(); i++) {
						JSONObject etapeObject = circuitArray.optJSONObject(i);
						circuit.add(new EtapeCircuit(new Date(etapeObject.optLong("dateValidation")), etapeObject.optBoolean("approved"), etapeObject.optBoolean("rejected"), etapeObject.optString("parapheurName"), etapeObject.optString("signataire"), Action.valueOf(etapeObject.optString("actionDemandee", "VISA")), etapeObject.optString("annotPub")));

					}
				}
			}
		}
		return circuit;
	}

	@Override
	public ArrayList<Bureau> getBureaux(RequestResponse response) {
		ArrayList<Bureau> bureaux = new ArrayList<>();

		if (response.getResponseArray() == null)
			return bureaux;

		JsonExplorer jsonExplorer = new JsonExplorer(response.getResponseArray());
		for (int i = 0; i < response.getResponseArray().length(); i++) {

			int lateCount = jsonExplorer.find(i).optInt("en-retard", 0);
			int todoCount = jsonExplorer.find(i).optInt("a-traiter", 0);
			String name = jsonExplorer.find(i).optString("name", "");
			String bureauRef = jsonExplorer.find(i).optString("nodeRef", null);

			if (bureauRef != null)
				bureaux.add(new Bureau(bureauRef, name, todoCount, lateCount));
		}

		return bureaux;
	}

	@Override
	public LinkedHashMap<String, ArrayList<String>> getTypologie(RequestResponse response) {

		LinkedHashMap<String, ArrayList<String>> typologie = new LinkedHashMap<>();
		JSONArray JSONTypes = response.getResponseArray();
		if (JSONTypes != null) {

			for (int i = 0; i < JSONTypes.length(); i++) {
				JSONObject JSONType = JSONTypes.optJSONObject(i);

				if (JSONType != null) {
					String type = JSONType.optString("id");
					JSONArray JSONSousTypes = JSONType.optJSONArray("sousTypes");

					if (JSONSousTypes != null) {
						ArrayList<String> sousTypes = new ArrayList<>(JSONSousTypes.length());

						for (int j = 0; j < JSONSousTypes.length(); j++) {
							String sousType = JSONSousTypes.optString(j);
							if (sousType != null) {
								sousTypes.add(sousType);
							}
						}
						typologie.put(type, sousTypes);
					}
				}
			}
		}
		return typologie;
	}
}
