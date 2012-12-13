package org.adullact.iparapheur.tab.services;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.adullact.iparapheur.tab.model.Account;
import org.codeartisans.java.toolbox.Strings;
import org.codeartisans.java.toolbox.exceptions.NullArgumentException;
import org.json.JSONException;
import org.json.JSONObject;

import de.akquinet.android.androlog.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton allowing to keep one HttpClient instance only.
 *
 * Static fields are attached to the Class instance as a whole, which is in turn
 * attached to the ClassLoader which loaded the class. the_instance would be
 * unloaded when the entire ClassLoader is reclaimed. I am 90% sure this happens
 * when Android destroys the app (not when it goes into the background, or
 * pauses, but is completely shut down.)
 *
 * It'll be retained until your entire process is destroyed. When your process
 * is revived, your singleton will reappear!
 *
 * It should be noted that the singleton will be recreated, but the original
 * state of the singleton is not automaticaly restored. This would have to be
 * done manually.
 */
public class StaticHttpClient {

    private static final Map<String, String> accountSessionTickets = new HashMap<String, String>();
    public static final String BASE_PATH = "https://m.";
    
    private static final String cert = "-----BEGIN CERTIFICATE-----\n" +
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
                                        "-----END CERTIFICATE-----\n";
    
    public static synchronized void releaseInstance() {
        //instance.httpClient.getOutputStream().close();
        accountSessionTickets.clear();
    }

    public static JSONObject postToJson(Account account, String path, String request) {
        JSONObject ret = new JSONObject();
        OutputStream output = null;
        HttpsURLConnection connection = null;
        String url = StaticHttpClient.buildUrl(account, path);
        try {
            InputStream in = new ByteArrayInputStream(cert.getBytes()); 
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Certificate certif = factory.generateCertificate(in);
            final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            trustStore.setCertificateEntry("trust", certif);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(trustStore);
            
            Log.i("StaticHttpClient", "TrustStore initialized --- url : " + url);

            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, tmf.getTrustManagers(), null);
            
            connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setSSLSocketFactory(context.getSocketFactory());
            connection.setDoOutput(true); // Triggers POST.
            connection.setChunkedStreamingMode(0);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            output = connection.getOutputStream();
            output.write(request.getBytes());
            InputStream response = connection.getInputStream();

            String r = StaticHttpClient.inputStreamToString(response);
            Log.i("StaticHttpClient", "response size : " + r.length());
            ret = new JSONObject(r);
        } catch (Exception e) {
            Log.e("StaticHttpClient", "exception : " + e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException logOrIgnore) {
                }
            }
        }
        return ret;
    }

    // Fast Implementation
    private static String inputStreamToString(InputStream is) throws IOException {
        String line = "";
        StringBuilder total = new StringBuilder();

        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        // Read response until the end
        while ((line = rd.readLine()) != null) {
            total.append(line);
        }
        return total.toString();
    }

    
    /* package */ static synchronized void ensureLoggedIn(Account account)
            throws IParapheurHttpException {
        NullArgumentException.ensureNotNull("Account", account);
        if (!accountSessionTickets.containsKey(account.getIdentity())) {
            try {
                Log.d(IParapheurHttpClient.class, "REQUEST to " + BASE_PATH + IParapheurHttpClient.LOGIN_PATH);
                String request = "{'username': '" + account.getLogin() + "', 'password': '" + account.getPassword() + "'}";
                JSONObject json = StaticHttpClient.postToJson(account, IParapheurHttpClient.LOGIN_PATH, request);
                String ticket = json.getJSONObject("data").getString("ticket");
                accountSessionTickets.put(account.getIdentity(), ticket);
            } catch (JSONException ex) {
                throw new IParapheurHttpException(account.getTitle() + " : " + (Strings.isEmpty(ex.getMessage()) ? ex.getClass().getSimpleName() : ex.getMessage()), ex);
            }
        } else {
            Log.d("Already logged-in");
        }
    }
    
    public static String buildUrl(Account account, String path) {
        NullArgumentException.ensureNotNull("Account", account);
        NullArgumentException.ensureNotEmpty("Path", path);
        String ticket = (accountSessionTickets.get(account.getIdentity()) == null)? "" : "?alf_ticket=" + accountSessionTickets.get(account.getIdentity());
        return BASE_PATH + account.getUrl() + path + ticket;
    }
    
    public static String downloadFile(Context context, Account account, String url, String fileName) {
        
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            throw new RuntimeException("The external storage is not accessible");
        }
        File file = new File(context.getExternalCacheDir(), fileName);
        
        OutputStream fileOutput = null;
        HttpsURLConnection connection = null;
        String fileUrl = buildUrl(account, url);
        Log.i("StaticHttpClient", "Downloading : " + fileUrl);
        try {
            InputStream in = new ByteArrayInputStream(cert.getBytes());
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Certificate certif = factory.generateCertificate(in);
            final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            trustStore.setCertificateEntry("trust", certif);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, tmf.getTrustManagers(), null);
            
            connection = (HttpsURLConnection) new URL(fileUrl).openConnection();
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setRequestMethod("GET");
            connection.setDoOutput(false);
            connection.setChunkedStreamingMode(0);
            
            InputStream response = connection.getInputStream();
            
            fileOutput = new FileOutputStream(file);
            
            byte[] buffer = new byte[1024];
            int bufferLength = 0;
            /*int totalSize = connection.getContentLength();
            int downloadedSize = 0;
            int previousPercent = 0;
            int percent = 0;*/
            
            Log.i("StaticHttpClient", "writing file...");
            while ((bufferLength = response.read(buffer)) > 0 ) {
                    fileOutput.write(buffer, 0, bufferLength);
                    /*downloadedSize += bufferLength;
                    percent = (int) Math.floor(downloadedSize * 100 / totalSize);
                    if (percent > previousPercent) {
                        Log.i("StaticHttpClient", "progress : " + percent + "%");
                        previousPercent = percent;
                    }*/
                    //updateProgress(downloadedSize, totalSize);
            }
            Log.i("StaticHttpClient", "writing file...done");
            //close the output stream when done
            fileOutput.close();
            
            
        } catch (Exception e) {
            Log.e("StaticHttpClient", "Erreur lors du téléchargement du pdf : " + e);
            e.printStackTrace();
        } finally {
            if (fileOutput != null) {
                try {
                    fileOutput.close();
                } catch (IOException logOrIgnore) {
                }
            }
        }
        return (file != null)? file.getAbsolutePath() : null;
    }
}