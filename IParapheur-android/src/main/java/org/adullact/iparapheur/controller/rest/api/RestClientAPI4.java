package org.adullact.iparapheur.controller.rest.api;

import android.support.annotation.NonNull;

import java.util.Locale;

public class RESTClientAPI4 extends RESTClientAPI3 {

	private static final String RESOURCE_ANNOTATIONS = "/parapheur/dossiers/%s/%s/annotations";
	private static final String RESOURCE_ANNOTATION = "/parapheur/dossiers/%s/%s/annotations/%s";

	@Override
	protected @NonNull String getAnnotationsUrlSuffix(@NonNull String dossierId, @NonNull String documentId) {
		return String.format(Locale.US, RESOURCE_ANNOTATIONS, dossierId, documentId);
	}

	@Override
	protected @NonNull String getAnnotationUrlSuffix(@NonNull String dossierId, @NonNull String documentId, @NonNull String annotationId) {
		return String.format(Locale.US, RESOURCE_ANNOTATION, dossierId, documentId, annotationId);
	}

}
