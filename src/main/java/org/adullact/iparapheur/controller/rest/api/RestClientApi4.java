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
package org.adullact.iparapheur.controller.rest.api;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutCompat;

import org.adullact.iparapheur.controller.rest.RESTUtils;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.RequestResponse;
import org.adullact.iparapheur.utils.IParapheurException;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Locale;


public class RestClientApi4 extends RestClientApi3 {


	private static final String ACTION_SEAL = "/parapheur/dossiers/%s/seal";

	private static final String RESOURCE_ANNOTATIONS = "/parapheur/dossiers/%s/%s/annotations";
	private static final String RESOURCE_ANNOTATION = "/parapheur/dossiers/%s/%s/annotations/%s";

	@Override protected @NonNull String getAnnotationsUrlSuffix(@NonNull String dossierId, @NonNull String documentId) {
		return String.format(Locale.US, RESOURCE_ANNOTATIONS, dossierId, documentId);
	}

	@Override protected @NonNull String getAnnotationUrlSuffix(@NonNull String dossierId, @NonNull String documentId, @NonNull String annotationId) {
		return String.format(Locale.US, RESOURCE_ANNOTATION, dossierId, documentId, annotationId);
	}

	public boolean seal(@NonNull Account currentAccount, Dossier dossier, String annotPub, String annotPriv, String bureauId) throws IParapheurException {

		String actionUrl = String.format(Locale.US, ACTION_SEAL, dossier.getId());
		try {
			JSONObject json = new JSONObject();
			json.put("bureauCourant", bureauId);
			json.put("annotPub", annotPub);
			json.put("annotPriv", annotPriv);
			RequestResponse response = RESTUtils.post(buildUrl(currentAccount, actionUrl), json.toString());
			return (response != null && response.getCode() == HttpURLConnection.HTTP_OK);
		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors du cachet", e);
		}
	}


}
