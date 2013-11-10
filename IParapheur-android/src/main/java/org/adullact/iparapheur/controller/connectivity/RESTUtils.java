package org.adullact.iparapheur.controller.connectivity;

import android.util.Log;

import org.adullact.iparapheur.model.RequestResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by jmaire on 04/11/2013.
 */
public class RESTUtils {

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

    public static RequestResponse post(String url, String body) {
        Log.d("debug", "POST request on : " + url);
        RequestResponse res = null;
        OutputStream output = null;
        try {
            HttpURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            ((HttpsURLConnection)connection).setSSLSocketFactory(getSSLSocketFactory());
            connection.setDoOutput(true); // Triggers POST.
            connection.setChunkedStreamingMode(0);
            //connection.setReadTimeout(10000);
            connection.setConnectTimeout(10000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            output = connection.getOutputStream();
            output.write(body.getBytes());
            res = new RequestResponse(connection);
        } catch (Exception e) {
            Log.e("RESTUtils", "Error while sending post request.", e);
            res = new RequestResponse();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException logOrIgnore) {
                }
            }
        }
        return res;
    }


    public static RequestResponse get(String url, String params) {
        Log.d("debug", "GET request on : " + url);
        RequestResponse res = null;
        try {
            HttpURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            ((HttpsURLConnection)connection).setSSLSocketFactory(getSSLSocketFactory());

            connection.setRequestMethod("GET");
            connection.setDoOutput(false); // Don't trigger POST.
            connection.setChunkedStreamingMode(0);
            //connection.setReadTimeout(10000);
            connection.setConnectTimeout(10000);
            //connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept-Charset", "UTF-8");

            res = new RequestResponse(connection);

        } catch (Exception e) {
            res = new RequestResponse();
        }
        return res;
    }

    public static InputStream downloadFile(String url)
    {
        Log.d("debug", "GET (download file) request on : " + url);
        InputStream fileStream = null;
        try {
            HttpURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            ((HttpsURLConnection)connection).setSSLSocketFactory(getSSLSocketFactory());

            connection.setRequestMethod("GET");
            connection.setDoOutput(false);
            connection.setChunkedStreamingMode(0);

            fileStream = connection.getInputStream();

        } catch (Exception e) {
            //Log.e("StaticHttpClient", "Erreur lors du téléchargement du pdf : " + e);
            e.printStackTrace();
        }
        return fileStream;
    }

    private static SSLSocketFactory getSSLSocketFactory() throws GeneralSecurityException, IOException {
        InputStream in = new ByteArrayInputStream(certAcAdullact.getBytes());
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        Certificate certif = factory.generateCertificate(in);
        final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        trustStore.setCertificateEntry("trust", certif);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(trustStore);

        SSLContext context = SSLContext.getInstance("SSL");
        context.init(null, tmf.getTrustManagers(), null);
        return context.getSocketFactory();
    }
}
