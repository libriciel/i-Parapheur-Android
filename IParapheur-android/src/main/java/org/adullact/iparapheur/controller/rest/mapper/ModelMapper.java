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
import org.adullact.iparapheur.model.Circuit;
import org.adullact.iparapheur.model.EtapeCircuit;
import org.adullact.iparapheur.model.PageAnnotations;
import org.adullact.iparapheur.model.RequestResponse;
import org.adullact.iparapheur.model.SignInfo;
import org.adullact.iparapheur.utils.JsonExplorer;

import java.util.ArrayList;
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
	protected static String DOSSIER_ACTIONS = "actions";

	protected static String CIRCUIT_ETAPES_DATE_VALIDATION = "dateValidation";
	protected static String CIRCUIT_ETAPES_APPROVED = "approved";
	protected static String CIRCUIT_ETAPES_REJECTED = "rejected";
	protected static String CIRCUIT_ETAPES_PARAPHEUR_NAME = "parapheurName";
	protected static String CIRCUIT_ETAPES_SIGNATAIRE = "signataire";
	protected static String CIRCUIT_ETAPES_ACTION_DEMANDEE = "actionDemandee";
	protected static String CIRCUIT_ETAPES_PUBLIC_ANNOTATIONS = "annotPub";

	protected static String SIGN_INFO_SIGNATURE_INFORMATIONS = "signatureInformations";
	protected static String SIGN_INFO_HASH = "hash";

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

	public @NonNull SignInfo getSignInfo(RequestResponse response) {
		SignInfo result = new SignInfo();

		JsonExplorer jsonExplorer = new JsonExplorer(response.getResponse());
		String hash = jsonExplorer.findObject(SIGN_INFO_SIGNATURE_INFORMATIONS).optString(SIGN_INFO_HASH, null);

		result.setHash(hash);

		return result;
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
