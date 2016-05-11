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

import java.util.Locale;


public class RESTClientAPI4 extends RESTClientAPI3 {

	private static final String RESOURCE_ANNOTATIONS = "/parapheur/dossiers/%s/%s/annotations";
	private static final String RESOURCE_ANNOTATION = "/parapheur/dossiers/%s/%s/annotations/%s";

	@Override protected @NonNull String getAnnotationsUrlSuffix(@NonNull String dossierId, @NonNull String documentId) {
		return String.format(Locale.US, RESOURCE_ANNOTATIONS, dossierId, documentId);
	}

	@Override protected @NonNull String getAnnotationUrlSuffix(@NonNull String dossierId, @NonNull String documentId, @NonNull String annotationId) {
		return String.format(Locale.US, RESOURCE_ANNOTATION, dossierId, documentId, annotationId);
	}

}
