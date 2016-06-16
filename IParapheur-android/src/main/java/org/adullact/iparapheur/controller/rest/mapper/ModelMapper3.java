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
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonObject;

import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.RequestResponse;
import org.adullact.iparapheur.utils.JsonExplorer;
import org.adullact.iparapheur.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
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
		HashSet<Action> actions = getActionsForDossier(jsonObject);
		Action actionDemandée = Action.valueOf(jsonExplorer.optString(DOSSIER_ACTION_DEMANDEE, Action.VISA.toString()));

		// Patching weird Signature case :
		// "actionDemandee" can have any "actions" value...
		// ... Except when "actionDemandee=SIGNATURE", where "actions" only contains VISA, for some reason
		// A SIGNATURE action is acceptable in VISA too...

		if (actionDemandée == Action.SIGNATURE) {
			actions.remove(Action.VISA);
			actions.add(Action.SIGNATURE);
		}

		if (actionDemandée == Action.VISA)
			actions.add(Action.SIGNATURE);

		// Actions patch end

		Dossier dossier = new Dossier(
				jsonExplorer.optString(DOSSIER_ID, ""),
				jsonExplorer.optString(DOSSIER_TITLE, ""),
				actionDemandée,
				actions,
				jsonExplorer.optString(DOSSIER_TYPE, ""),
				jsonExplorer.optString(DOSSIER_SUBTYPE, ""),
				StringUtils.parseIso8601Date(jsonExplorer.optString(DOSSIER_EMISSION_DATE)),
				StringUtils.parseIso8601Date(jsonExplorer.optString(DOSSIER_DATE_LIMITE)),
				jsonExplorer.optBoolean(DOSSIER_IS_SIGN_PAPIER, false)
		);

		for (int index = 0; index < jsonExplorer.findArray(DOSSIER_DOCUMENTS).getCurrentArraySize(); index++) {
			JsonObject doc = jsonExplorer.findArray(DOSSIER_DOCUMENTS).find(index).optCurrentJsonObject();

			if (dossierId != null)
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

	@Override protected HashSet<Action> getActionsForDossier(JSONObject dossier) {
		JsonObject actionsJsonObject = new JsonExplorer(dossier).optCurrentJsonObject();
		return getActionsForDossier(actionsJsonObject);
	}

	protected @NonNull HashSet<Action> getActionsForDossier(JsonObject dossier) {

		HashSet<Action> actions = new HashSet<>();
		JsonExplorer jsonExplorer = new JsonExplorer(dossier);

		for (int index = 0; index < jsonExplorer.findArray("actions").getCurrentArraySize(); index++) {
			String action = jsonExplorer.findArray("actions").find(index).optCurrentString();

			if (action != null)
				actions.add(Action.valueOf(action));
		}

		return actions;
	}

	@Override public @NonNull ArrayList<Dossier> getDossiers(RequestResponse requestResponse) {

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

	@Override public @NonNull ArrayList<Bureau> getBureaux(RequestResponse response) {
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

}
