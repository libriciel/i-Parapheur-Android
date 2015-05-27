package org.adullact.iparapheur.controller.rest.api;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.RESTUtils;
import org.adullact.iparapheur.model.Annotation;
import org.adullact.iparapheur.model.PageAnnotations;
import org.adullact.iparapheur.model.RequestResponse;
import org.adullact.iparapheur.utils.IParapheurException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.net.HttpURLConnection;
import java.util.Locale;

public class RESTClientAPI4 extends RESTClientAPI3 {

	private static final String RESOURCE_ANNOTATIONS = "/parapheur/dossiers/%s/%s/annotations";
	private static final String RESOURCE_ANNOTATION = "/parapheur/dossiers/%s/%s/annotations/%s";

	@Override
	public SparseArray<PageAnnotations> getAnnotations(@NonNull String dossierId, @NonNull String documentId) throws IParapheurException {
		String url = buildUrl(String.format(Locale.US, RESOURCE_ANNOTATIONS, dossierId, documentId));
		return modelMapper.getAnnotations(RESTUtils.get(url));
	}

	@Override
	public String createAnnotation(@NonNull String dossierId, @NonNull String documentId, @NonNull Annotation annotation, int page) throws IParapheurException {

		// Build json object

		JSONStringer annotJson = new JSONStringer();

		try {
			annotJson.object();
			{
				annotJson.key("rect");
				annotJson.object();
				{
					annotJson.key("topLeft");
					annotJson.object();
					annotJson.key("x").value(annotation.getRect().left);
					annotJson.key("y").value(annotation.getRect().top);
					annotJson.endObject();

					annotJson.key("bottomRight");
					annotJson.object();
					annotJson.key("x").value(annotation.getRect().right);
					annotJson.key("y").value(annotation.getRect().bottom);
					annotJson.endObject();
				}
				annotJson.endObject();

				annotJson.key("author").value(annotation.getAuthor());
				annotJson.key("date").value(annotation.getDate());
				annotJson.key("page").value(page);
				annotJson.key("text").value(annotation.getText());
				annotJson.key("type").value("rect");
			}
			annotJson.endObject();
		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors de la création de l'annotation", e);
		}

		// Send request

		String url = buildUrl(String.format(Locale.US, RESOURCE_ANNOTATIONS, dossierId, documentId));
		RequestResponse response = RESTUtils.post(url, annotJson.toString());

		if (response != null && response.getCode() == HttpURLConnection.HTTP_OK) {
			JSONObject idObj = response.getResponse();

			if (idObj != null)
				return idObj.optString("id", null);
		}

		return null;
	}

	@Override
	public void updateAnnotation(@NonNull String dossierId, @NonNull String documentId, @NonNull Annotation annotation, int page) throws IParapheurException {

		// Build Json object

		JSONStringer annotationJson = new JSONStringer();

		try {
			annotationJson.object();
			{
				annotationJson.key("rect");
				annotationJson.object();
				{
					annotationJson.key("topLeft");
					annotationJson.object();
					annotationJson.key("x").value(annotation.getRect().left);
					annotationJson.key("y").value(annotation.getRect().top);
					annotationJson.endObject();

					annotationJson.key("bottomRight");
					annotationJson.object();
					annotationJson.key("x").value(annotation.getRect().right);
					annotationJson.key("y").value(annotation.getRect().bottom);
					annotationJson.endObject();
				}
				annotationJson.endObject();

				annotationJson.key("author").value(annotation.getAuthor());
				annotationJson.key("date").value(annotation.getDate());
				annotationJson.key("page").value(page);
				annotationJson.key("text").value(annotation.getText());
				annotationJson.key("type").value("rect");
				annotationJson.key("id").value(annotation.getUuid());
				annotationJson.key("uuid").value(annotation.getUuid());
			}
			annotationJson.endObject();
		}
		catch (JSONException e) {
			throw new RuntimeException("Une erreur est survenue lors de l'enregistrement de l'annotation", e);
		}

		// Send request

		String url = buildUrl(String.format(Locale.US, RESOURCE_ANNOTATION, dossierId, documentId, annotation.getUuid()));

		RequestResponse response = RESTUtils.put(url, annotationJson.toString(), true);

		if (response == null || response.getCode() != HttpURLConnection.HTTP_OK)
			throw new IParapheurException(R.string.error_annotation_update, "");
	}

	@Override
	public void deleteAnnotation(@NonNull String dossierId, @NonNull String documentId, @NonNull String annotationId, int page) throws IParapheurException {
		String url = buildUrl(String.format(Locale.US, RESOURCE_ANNOTATION, dossierId, documentId, annotationId));
		RESTUtils.delete(url, true);
	}

}
