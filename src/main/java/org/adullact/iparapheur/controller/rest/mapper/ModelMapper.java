/*
 * iParapheur Android
 * Copyright (C) 2016-2019 Libriciel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.adullact.iparapheur.controller.rest.mapper;

import androidx.annotation.NonNull;
import android.util.SparseArray;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.adullact.iparapheur.model.Annotation;
import org.adullact.iparapheur.model.PageAnnotations;
import org.adullact.iparapheur.model.RequestResponse;
import org.adullact.iparapheur.model.SignInfo;
import org.adullact.iparapheur.utils.JsonExplorer;
import org.adullact.iparapheur.utils.SerializableSparseArray;

import java.util.Map;


public class ModelMapper {

	protected static String DOSSIER_CIRCUIT = "circuit";

	protected static String CIRCUIT_ETAPES_DATE_VALIDATION = "dateValidation";
	protected static String CIRCUIT_ETAPES_APPROVED = "approved";
	protected static String CIRCUIT_ETAPES_REJECTED = "rejected";
	protected static String CIRCUIT_ETAPES_PARAPHEUR_NAME = "parapheurName";
	protected static String CIRCUIT_ETAPES_SIGNATAIRE = "signataire";
	protected static String CIRCUIT_ETAPES_ACTION_DEMANDEE = "actionDemandee";
	protected static String CIRCUIT_ETAPES_PUBLIC_ANNOTATIONS = "annotPub";

	protected static String SIGN_INFO_SIGNATURE_INFORMATIONS = "signatureInformations";
	protected static String SIGN_INFO_HASH = "hash";

	public @NonNull SignInfo getSignInfo(RequestResponse response) {
		SignInfo result = new SignInfo();

		JsonExplorer jsonExplorer = new JsonExplorer(response.getResponse());
		String hash = jsonExplorer.findObject(SIGN_INFO_SIGNATURE_INFORMATIONS).optString(SIGN_INFO_HASH, null);

		result.setHash(hash);

		return result;
	}

	public SerializableSparseArray<PageAnnotations> getAnnotations(RequestResponse response) {
		SerializableSparseArray<PageAnnotations> annotations = new SerializableSparseArray<>();

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
