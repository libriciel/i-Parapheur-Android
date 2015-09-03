package org.adullact.iparapheur.model;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.RESTUtils;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;

public class RequestResponse {

	private int code;
	private String error;
	private JSONObject response;
	private JSONArray responseArray;

	public RequestResponse(HttpURLConnection httpURLConnection) throws IParapheurException {
		this(httpURLConnection, false);
	}

	public RequestResponse(HttpURLConnection httpURLConnection, boolean ignoreResponseData) throws IParapheurException {
		String data;
		try {
			this.code = httpURLConnection.getResponseCode();

			if (this.code < HttpURLConnection.HTTP_BAD_REQUEST) { // if code < 400, response is in inputStream
				if (!ignoreResponseData) {
					data = StringUtils.inputStreamToString(httpURLConnection.getInputStream());
					//Log.d("debug", "data : " + data);
					Object json = new JSONTokener(data).nextValue();
					//Log.d("debug", "json : " + json);

					if (json instanceof JSONObject)
						this.response = (JSONObject) json;
					else if (json instanceof JSONArray)
						this.responseArray = (JSONArray) json;
				}
			}
			else { // if code >= 400, response is in errorStream
				data = StringUtils.inputStreamToString(httpURLConnection.getErrorStream());
				//Log.d("debug", "data : " + data);
				Object json = new JSONTokener(data).nextValue();
				if (json instanceof JSONObject) {
					this.error = ((JSONObject) json).optString("message", "");
				}
				throw RESTUtils.getExceptionForError(this.code, error);
			}
		}
		catch (JSONException e) {
			throw new IParapheurException(R.string.error_parse, null);
		}
		catch (UnknownHostException e) {
			throw new IParapheurException(R.string.http_error_malformed_url, httpURLConnection.getURL().getHost());
		}
		catch (IOException e) {
			throw new IParapheurException(R.string.error_parse, null);
		}
	}

	public int getCode() {
		return code;
	}

	public String getError() {
		return error;
	}

	public JSONObject getResponse() {
		return response;
	}

	public JSONArray getResponseArray() {
		return responseArray;
	}
}
