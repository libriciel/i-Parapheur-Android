package org.adullact.iparapheur.controller.rest.mapper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.JsonObject;

import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Circuit;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.EtapeCircuit;
import org.adullact.iparapheur.model.RequestResponse;
import org.adullact.iparapheur.utils.JsonExplorer;
import org.adullact.iparapheur.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;


public class ModelMapper3 extends ModelMapper {

	@Override public Dossier getDossier(RequestResponse requestResponse) throws RuntimeException {

		Dossier dossier = null;

		JsonObject dossierJsonObject = new JsonExplorer(requestResponse.getResponse()).optCurrentJsonObject();
		if (dossierJsonObject != null)
			dossier = getDossier(dossierJsonObject);

		return dossier;
	}

	@Override protected Dossier getDossier(JSONObject jsonObject) {
		JsonObject dossierJsonObject = new JsonExplorer(jsonObject).optCurrentJsonObject();
		return getDossier(dossierJsonObject);
	}

	@Nullable protected Dossier getDossier(@Nullable JsonObject jsonObject) {

		// Default case

		if (jsonObject == null)
			return null;

		// Parsing

		JsonExplorer jsonExplorer = new JsonExplorer(jsonObject);
		String dossierId = jsonExplorer.optString(DOSSIER_ID);
		ArrayList<Action> actions = getActionsForDossier(jsonObject);

		Dossier dossier = new Dossier(
				jsonExplorer.optString(DOSSIER_ID, ""),
				jsonExplorer.optString(DOSSIER_TITLE, ""),
				Action.valueOf(jsonExplorer.optString(DOSSIER_ACTION_DEMANDEE, Action.VISA.toString())),
				actions,
				jsonExplorer.optString(DOSSIER_TYPE, ""),
				jsonExplorer.optString(DOSSIER_SUBTYPE, ""),
				StringUtils.parseISO8601Date(jsonExplorer.optString(DOSSIER_EMISSION_DATE)),
				StringUtils.parseISO8601Date(jsonExplorer.optString(DOSSIER_DATE_LIMITE)),
				jsonExplorer.optBoolean(DOSSIER_IS_SIGN_PAPIER, false)
		);

		for (int index = 0; index < jsonExplorer.findArray(DOSSIER_DOCUMENTS).getCurrentArraySize(); index++) {
			JsonObject doc = jsonExplorer.findArray(DOSSIER_DOCUMENTS).find(index).optCurrentJsonObject();
			dossier.addDocument(parseDocument(doc, dossierId, index));
		}

		return dossier;
	}

	private @Nullable Document parseDocument(@Nullable JsonObject documentJson, @NonNull String dossierId, int index) {

		if (documentJson == null)
			return null;

		JsonExplorer jsonExplorer = new JsonExplorer(documentJson);

		String downloadUrl = "/api/node/workspace/SpacesStore/" + jsonExplorer.optString(DOCUMENT_ID, "") + "/content";
		if (jsonExplorer.optBoolean(DOCUMENT_VISUEL_PDF, false))
			downloadUrl += ";ph:visuel-pdf";

		return new Document(
				jsonExplorer.optString(DOCUMENT_ID),
				dossierId,
				jsonExplorer.optString(DOCUMENT_NAME),
				jsonExplorer.optInt(DOCUMENT_SIZE, -1),
				downloadUrl,
				jsonExplorer.optBoolean(DOCUMENT_IS_LOCKED, false),
				jsonExplorer.optBoolean(DOCUMENT_IS_MAIN_DOCUMENT, (index == 0))
		);
	}

	@Override protected ArrayList<Action> getActionsForDossier(JSONObject dossier) {
		JsonObject actionsJsonObject = new JsonExplorer(dossier).optCurrentJsonObject();
		return getActionsForDossier(actionsJsonObject);
	}

	protected ArrayList<Action> getActionsForDossier(JsonObject dossier) {

		ArrayList<Action> actions = new ArrayList<>();
		JsonExplorer jsonExplorer = new JsonExplorer(dossier);

		for (int index = 0; index < jsonExplorer.findArray("actions").getCurrentArraySize(); index++) {
			String action = jsonExplorer.findArray("actions").find(index).optCurrentString();

			if (action != null)
				actions.add(Action.valueOf(action));
		}

		return actions;
	}

	@Override public ArrayList<Dossier> getDossiers(RequestResponse requestResponse) {

		ArrayList<Dossier> dossiers = new ArrayList<>();

		JsonExplorer jsonExplorer = new JsonExplorer(requestResponse.getResponseArray());
		for (int i = 0; i < jsonExplorer.getCurrentArraySize(); i++) {

			JsonObject dossierJsonObject = jsonExplorer.find(i).optCurrentJsonObject();
			Dossier dossier = getDossier(dossierJsonObject);

			if (dossier != null)
				dossiers.add(dossier);
		}

		return dossiers;
	}

	@Override public @NonNull Circuit getCircuit(@NonNull RequestResponse response) {

		JsonExplorer jsonExplorer = new JsonExplorer(response.getResponse());

		// Parsing root fields

		boolean isDigitSigeMandatory = jsonExplorer.findObject(DOSSIER_CIRCUIT).optBoolean(CIRCUIT_IS_DIGITAL_SIGNATURE_MANDATORY, true);
		boolean hasSelectionScript = jsonExplorer.findObject(DOSSIER_CIRCUIT).optBoolean(CIRCUIT_HAS_SELECTION_SCRIPT, false);
		String signatureFormat = jsonExplorer.findObject(DOSSIER_CIRCUIT).optString(CIRCUIT_SIG_FORMAT, "");

		// Parsing Etapes

		ArrayList<EtapeCircuit> etapes = new ArrayList<>();

		jsonExplorer.findObject(DOSSIER_CIRCUIT).findArray(CIRCUIT_ETAPES).rebase();
		for (int index = 0; index < jsonExplorer.getCurrentArraySize(); index++) {

			EtapeCircuit currentEtape = new EtapeCircuit(
					jsonExplorer.find(index).optLong(CIRCUIT_ETAPES_DATE_VALIDATION, -1),
					jsonExplorer.find(index).optBoolean(CIRCUIT_ETAPES_APPROVED, false),
					jsonExplorer.find(index).optBoolean(CIRCUIT_ETAPES_REJECTED, false),
					jsonExplorer.find(index).optString(CIRCUIT_ETAPES_PARAPHEUR_NAME),
					jsonExplorer.find(index).optString(CIRCUIT_ETAPES_SIGNATAIRE),
					jsonExplorer.find(index).optString(CIRCUIT_ETAPES_ACTION_DEMANDEE, Action.VISA.toString()),
					jsonExplorer.find(index).optString(CIRCUIT_ETAPES_PUBLIC_ANNOTATIONS)
			);

			etapes.add(currentEtape);
		}

		return new Circuit(etapes, signatureFormat, isDigitSigeMandatory, hasSelectionScript);
	}

	@Override public ArrayList<Bureau> getBureaux(RequestResponse response) {
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

	@Override public LinkedHashMap<String, ArrayList<String>> getTypologie(RequestResponse response) {

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
