package org.adullact.iparapheur.model;

import com.crashlytics.android.Crashlytics;

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
import java.util.Arrays;


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
					Object json = new JSONTokener(data).nextValue();

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
				Crashlytics.logException(new Exception(error));
				throw RESTUtils.getExceptionForError(this.code, error);
			}
		}
		catch (JSONException e) {
			Crashlytics.logException(e);
			throw new IParapheurException(R.string.error_parse, Arrays.toString(e.getStackTrace()));
		}
		catch (UnknownHostException e) {
			Crashlytics.logException(e);
			e.printStackTrace();
			throw new IParapheurException(R.string.http_error_404, httpURLConnection.getURL().getHost());
		}
		catch (IOException e) {
			Crashlytics.logException(e);
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
