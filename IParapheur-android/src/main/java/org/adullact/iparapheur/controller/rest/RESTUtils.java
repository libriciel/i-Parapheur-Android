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
package org.adullact.iparapheur.controller.rest;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.api.RestClientApi;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.RequestResponse;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.NaiveTrustManager;
import org.json.JSONException;
import org.json.JSONStringer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;


public class RESTUtils {

    private static final String LOG_TAG = "RestUtils";
    private static final String certAcAdullact = "-----BEGIN CERTIFICATE-----\n" +
            "MIIHyTCCBbGgAwIBAgIUbyl4BzfA+DWwMPJHFgkdXxI7UGwwDQYJKoZIhvcNAQEF\n" +
            "BQAwgbUxCzAJBgNVBAYTAkZSMRAwDgYDVQQIDAdIZXJhdWx0MRQwEgYDVQQHDAtN\n" +
            "b250cGVsbGllcjEdMBsGA1UECgwUQXNzb2NpYXRpb24gQURVTExBQ1QxHDAaBgNV\n" +
            "BAsME0FDX0FEVUxMQUNUX1JPT1RfRzMxHDAaBgNVBAMME0FDX0FEVUxMQUNUX1JP\n" +
            "T1RfRzMxIzAhBgkqhkiG9w0BCQEWFHN5c3RlbWVAYWR1bGxhY3Qub3JnMB4XDTEy\n" +
            "MTEwODE2MzkwN1oXDTIyMTEwNTE2MzkwN1owgaUxCzAJBgNVBAYTAkZSMRAwDgYD\n" +
            "VQQIDAdIZXJhdWx0MR0wGwYDVQQKDBRBc3NvY2lhdGlvbiBBRFVMTEFDVDEfMB0G\n" +
            "A1UECwwWQUNfQURVTExBQ1RfTU9CSUxFU19HMzEfMB0GA1UEAwwWQUNfQURVTExB\n" +
            "Q1RfTU9CSUxFU19HMzEjMCEGCSqGSIb3DQEJARYUc3lzdGVtZUBhZHVsbGFjdC5v\n" +
            "cmcwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQCtEO41chkIGtvbmwg8\n" +
            "TtGSmAGW/zRLV9kWCFby1NSGdu9ehw19w4op3Fvxqj5m7K4MQbvKsaggvXsMaHeO\n" +
            "pgg+OJMbchKZ6qxSBvnZQC078LYObhzSbZH8gJzpFPQfuji8Vg8v7ELJuz7kxaIz\n" +
            "m9PTlN7qEOCJCbfZA8GDK7qaNmkFxgR6KjYrx1h0g6MgKfUDMcGNG1d8GHn5EeIF\n" +
            "aGjPOSsFOTLhwo058oMCo2GJ5XRlzS1D7YsOP/rC7F9tNC3pc5Bi2EPYLQ7uHQHv\n" +
            "cKLT3/ioPk3V4T+twOh5eY26lSIQOj9nrfJ1PNDHXGVReKP9VUkpMMIocS4vRHE1\n" +
            "Eu8ZoRRJrV+olfE3FUiIwfSbVdPnlveTSU+GMy7oHlYC1JDZtgqnyEmAV1eoF3/L\n" +
            "Fcktv/D9ai9TAKGWplEtmn05KrXD6Q2qS09Zn8YMJYL5W8hVu/XE6ZYbmEufGqPt\n" +
            "lUz0BIxkn0KKhmOkOC4WejyKTEsxkXpfxgaAR61pw87BM5rVTqj1BotPv8hp3NLq\n" +
            "wjkum4NGvIPhWKMw5bZS8HDNtQP1HyuWKI1Wf4/n+23Ssx7doI+lF1Bv+s7t4kn+\n" +
            "vMPGsfJuC8yetCogb4GN3FHiDnNxGqAjXS4yI7YqQD2z+8DQkMw1Byhe9zf5wCEe\n" +
            "hh91LYPmgErKTcySG1xZy1Eq4QIDAQABo4IB3TCCAdkwDwYDVR0TAQH/BAUwAwEB\n" +
            "/zALBgNVHQ8EBAMCAQYwHQYDVR0OBBYEFOaz8i0aLIUrCm/SrMng5ckisdA0MIHq\n" +
            "BgNVHSMEgeIwgd+AFFxN7NasH+0Q+ZTBy4qAB2+AXQB4oYG7pIG4MIG1MQswCQYD\n" +
            "VQQGEwJGUjEQMA4GA1UECAwHSGVyYXVsdDEUMBIGA1UEBwwLTW9udHBlbGxpZXIx\n" +
            "HTAbBgNVBAoMFEFzc29jaWF0aW9uIEFEVUxMQUNUMRwwGgYDVQQLDBNBQ19BRFVM\n" +
            "TEFDVF9ST09UX0czMRwwGgYDVQQDDBNBQ19BRFVMTEFDVF9ST09UX0czMSMwIQYJ\n" +
            "KoZIhvcNAQkBFhRzeXN0ZW1lQGFkdWxsYWN0Lm9yZ4IJAN9K115QgPAvMB8GA1Ud\n" +
            "EQQYMBaBFHN5c3RlbWVAYWR1bGxhY3Qub3JnMB8GA1UdEgQYMBaBFHN5c3RlbWVA\n" +
            "YWR1bGxhY3Qub3JnMDAGCWCGSAGG+EIBDQQjFiFBdXRvcml0ZSBkZSBjZXJ0aWZp\n" +
            "Y2F0aW9uIHJhY2luZS4wOQYJYIZIAYb4QgEEBCwWKmh0dHA6Ly9jcmwuYWR1bGxh\n" +
            "Y3Qub3JnL0FDX1JPT1RfQ1JMX2czLnBlbTANBgkqhkiG9w0BAQUFAAOCAgEAolXJ\n" +
            "QHhmi63F5LXcF2dBSkUew1hHJY1IXu/QE8LDvvBnakCZB/3S55r1dW0X14i8gzmg\n" +
            "f4GI0Ok+FmLNFPR+hruIj5sye2ChI6IB7Wfm0mNle9cDsYE43NPu7RW8EGDSD+Fe\n" +
            "yOxfSPR5yhZoR2xpjRnzEHkspvFxeYZ1ao8/yDskvTn1OuXGNCgtU0jLj5BS7N1n\n" +
            "qGCDsL2HSpCWx4om/Q7CGyb2A1uXI3jM99EFuArBimBqK638AKWb7PO/Bs7Uh++O\n" +
            "Xv8S/dca+yYGM67mdgt4tnpQuXpORruu041piMm4tVaYkf2zoqaKrT9wJdeWSxkx\n" +
            "prIlEwHZEEkTxhVETvPq04nPGuRWgdkDgORbjQD/VxOfgrtpzH8gBGhaKs/k/dDK\n" +
            "vaWB1tXUXoU6sxllIrT9bNP1WV1GXfk5YP1bSzVrXOqTuEo3wVWscGfGlUoqNXX7\n" +
            "rLL0x0jZsw+4I095dqENQyyEeh2pV5DmCCzzN2vBYYg87eG+kzYDtxGdcnsD14pp\n" +
            "r1Mmy8eIQuvtKK1VTX70mP/E44w4LtP0UVcq6iqUt+C8ckNO90IH+taPweTSqrx1\n" +
            "m7YJzeNKBg8frRMeeyYYov47Ta3FZimb9aWAnwI3E75vmMpndVPUQigkfaWiisIp\n" +
            "RhvLQliUUxVMDX2+VTGKgEGGB+knAIKObp4vr2o=\n" +
            "-----END CERTIFICATE-----";


    public static RequestResponse post(String url, String body) throws IParapheurException {
        Log.d(LOG_TAG, "POST request on : " + url);
        Log.d(LOG_TAG, "with body : " + body);
        RequestResponse res;
        OutputStream output;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            if (connection instanceof HttpsURLConnection)
                ((HttpsURLConnection) connection).setSSLSocketFactory(getSSLSocketFactory());

            connection.setDoOutput(true); // Triggers POST.
            connection.setChunkedStreamingMode(0);
            connection.setConnectTimeout(10000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            output = connection.getOutputStream();
            output.write(body.getBytes());
            res = new RequestResponse(connection);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new IParapheurException(R.string.http_error_malformed_url, url);
        } catch (ProtocolException e) {
            e.printStackTrace();
            throw new IParapheurException(R.string.http_error_405, null);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new IParapheurException(R.string.http_error_ssl_failed, null);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new IParapheurException(R.string.error_no_internet, null);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IParapheurException(R.string.http_error_400, null);
        }

        return res;
    }


    public static RequestResponse put(String url, String body) throws IParapheurException {
        return put(url, body, false);
    }


    public static RequestResponse put(String url, String body, boolean ignoreResponseData) throws IParapheurException {
        Log.d(LOG_TAG, "POST request on : " + url);
        Log.d(LOG_TAG, "with body : " + body);
        RequestResponse res;
        OutputStream output;

        try {
            HttpURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            ((HttpsURLConnection) connection).setSSLSocketFactory(getSSLSocketFactory());
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            connection.setChunkedStreamingMode(0);
            //connection.setReadTimeout(10000);
            connection.setConnectTimeout(10000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            output = connection.getOutputStream();
            output.write(body.getBytes());
            res = new RequestResponse(connection, ignoreResponseData);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new IParapheurException(R.string.http_error_malformed_url, url);
        } catch (ProtocolException e) {
            throw new IParapheurException(R.string.http_error_405, null);
        } catch (GeneralSecurityException e) {
            throw new IParapheurException(R.string.http_error_ssl_failed, null);
        } catch (IOException e) {
            throw new IParapheurException(R.string.http_error_400, null);
        }

        return res;
    }


    public static RequestResponse get(@NonNull String url) throws IParapheurException {
        Log.d(LOG_TAG, "GET request on : " + url);
        RequestResponse res;

        try {

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            if (connection instanceof HttpsURLConnection)
                ((HttpsURLConnection) connection).setSSLSocketFactory(getSSLSocketFactory());

            connection.setRequestMethod("GET");
            connection.setDoOutput(false);
            connection.setChunkedStreamingMode(0);
            connection.setConnectTimeout(10000);
            connection.setRequestProperty("Accept-Charset", "UTF-8");

            res = new RequestResponse(connection);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new IParapheurException(R.string.http_error_malformed_url, url);
        } catch (ProtocolException e) {
            e.printStackTrace();
            throw new IParapheurException(R.string.http_error_405, null);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new IParapheurException(R.string.http_error_ssl_failed, null);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IParapheurException(R.string.http_error_400, null);
        }

        return res;
    }


    public static RequestResponse delete(String url) throws IParapheurException {
        return delete(url, false);
    }


    public static RequestResponse delete(String url, boolean ignoreResponseData) throws IParapheurException {
        Log.d(LOG_TAG, "GET request on : " + url);
        RequestResponse res;

        try {
            HttpURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            ((HttpsURLConnection) connection).setSSLSocketFactory(getSSLSocketFactory());

            connection.setRequestMethod("DELETE");
            connection.setDoOutput(false);
            connection.setChunkedStreamingMode(0);
            //connection.setReadTimeout(10000);
            connection.setConnectTimeout(10000);
            //connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept-Charset", "UTF-8");

            res = new RequestResponse(connection, ignoreResponseData);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new IParapheurException(R.string.http_error_malformed_url, url);
        } catch (ProtocolException e) {
            throw new IParapheurException(R.string.http_error_405, null);
        } catch (GeneralSecurityException e) {
            throw new IParapheurException(R.string.http_error_ssl_failed, null);
        } catch (IOException e) {
            throw new IParapheurException(R.string.http_error_400, null);
        }

        return res;
    }


    public static InputStream downloadFile(String url) throws IParapheurException {
        Log.d(LOG_TAG, "GET (download file) request on : " + url);
        InputStream fileStream;
        HttpURLConnection connection;
        try {
            connection = (HttpsURLConnection) new URL(url).openConnection();
            ((HttpsURLConnection) connection).setSSLSocketFactory(getSSLSocketFactory());
            connection.setRequestMethod("GET");
            connection.setDoOutput(false);
            connection.setChunkedStreamingMode(0);
            fileStream = connection.getInputStream();
        } catch (GeneralSecurityException e) {
            throw new IParapheurException(R.string.http_error_ssl_failed, null);
        } catch (IOException e) {
            throw new IParapheurException(R.string.http_error_400, null);
        }
        return fileStream;
    }


    private static SSLSocketFactory getSSLSocketFactory() throws GeneralSecurityException {

        TrustManager[] tm = new TrustManager[]{new NaiveTrustManager()};
        SSLContext context = SSLContext.getInstance("TLSv1");
        context.init(new KeyManager[0], tm, new SecureRandom());

        return context.getSocketFactory();
    }


    public static @Nullable String getAuthenticationJsonData(@NonNull Account account) {

        String requestContent = null;

        try {
            JSONStringer requestStringer = new JSONStringer();
            requestStringer.object();
            requestStringer.key("username").value(account.getLogin());
            requestStringer.key("password").value(account.getPassword());
            requestStringer.endObject();

            requestContent = requestStringer.toString();
        } catch (JSONException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }

        return requestContent;
    }


    public static boolean hasValidTicket(@NonNull Account account) {

        String ticket = account.getTicket();
        Long time = new Date().getTime();

        return ((!TextUtils.isEmpty(ticket)) && (account.getLastRequest() != null) && ((time - account.getLastRequest().getTime()) < RestClientApi.SESSION_TIMEOUT));
    }


    public static IParapheurException getExceptionForError(int code, String message) {

        Log.d(LOG_TAG, "getExceptionForError : " + code + " " + message);
        if (message != null)
            return new IParapheurException(R.string.http_error_explicit, message);

        IParapheurException exception;
        if (code == 400)
            exception = new IParapheurException(R.string.http_error_400, null);
        else if (code == 401)
            exception = new IParapheurException(R.string.http_error_401, null);
        else if (code == 403)
            exception = new IParapheurException(R.string.http_error_403, null);
        else if (code == 404)
            exception = new IParapheurException(R.string.http_error_404, null);
        else if (code == 405)
            exception = new IParapheurException(R.string.http_error_405, null);
        else if (code == 503)
            exception = new IParapheurException(R.string.http_error_503, null);
        else
            exception = new IParapheurException(R.string.http_error_undefined, null);

        return exception;
    }

}
