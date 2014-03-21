package org.adullact.iparapheur.model;

import org.adullact.iparapheur.controller.utils.TransformUtils;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.HttpURLConnection;

/**
 * Created by jmaire on 04/11/2013.
 */
public class RequestResponse {

    private int code;
    private String error;

    private JSONObject response;
    private JSONArray responseArray;

    /**
     * Constructor used when an error occurs.
     */
    public RequestResponse() {
        this.code = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        this.error = "Impossible d'accéder au Parapheur";
    }

    public RequestResponse(HttpURLConnection httpURLConnection) {
        String data;
        try {
            this.code = httpURLConnection.getResponseCode();
            if (this.code < HttpStatus.SC_BAD_REQUEST) { // if code < 400, response is in inputStream
                data = TransformUtils.inputStreamToString(httpURLConnection.getInputStream());
                //Log.d("debug", "data : " + data);
                Object json = new JSONTokener(data).nextValue();
                //Log.d("debug", "json : " + json);
                if (json instanceof JSONObject) {
                    this.response = (JSONObject) json;
                }
                else if (json instanceof JSONArray) {
                    this.responseArray = (JSONArray) json;
                }
            }
            else { // if code >= 400, response is in errorStream
                data = TransformUtils.inputStreamToString(httpURLConnection.getErrorStream());
                //Log.d("debug", "data : " + data);
                Object json = new JSONTokener(data).nextValue();
                this.error = ((JSONObject) json).optString("message", "");
            }

        } catch (Exception e) {
            //Log.e("debug", "Error while converting request response to RequestResponse : " + e);
            code = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            // TODO : internationalisation
            error = "Impossible d'accéder au Parapheur";
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
