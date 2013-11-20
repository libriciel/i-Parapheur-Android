package org.adullact.iparapheur.controller.connectivity;

import android.os.Environment;
import android.util.Log;

import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.EtapeCircuit;
import org.adullact.iparapheur.model.RequestResponse;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by jmaire on 23/10/13.
 */
public enum  RESTClient {

    INSTANCE;

    public static final String BASE_PATH = "https://m.";

    private static final String ACTION_LOGIN = "/parapheur/api/login";
    private static final String ACTION_GET_DOSSIER = "/parapheur/api/getDossier";
    private static final String ACTION_GET_DOSSIERS = "/parapheur/api/getDossiersHeaders";
    private static final String ACTION_GET_CIRCUIT = "/parapheur/api/getCircuit";
    private static final String ACTION_GET_BUREAUX = "/parapheur/api/getBureaux";

    private static String getTicket(Account account)
    {
        String ticket = account.getTicket();
        Log.d("debug", "getTicket : " + ticket);
        if (ticket == null) {
            try {
                String request = "{'username': '" + account.getLogin() + "', 'password': '" + account.getPassword() + "'}";
                RequestResponse response = RESTUtils.post(BASE_PATH + account.getUrl() + ACTION_LOGIN, request);

                ticket = response.getResponse().getJSONObject("data").getString("ticket");
                account.setTicket(ticket);
            } catch (JSONException ex) {
                Log.e("RESTClient", "Error while trying to log in.", ex);
            }
        }
        return ticket;
    }

    public static Dossier getDossier(String bureauId, String dossierId) {
        String url = buildUrl(ACTION_GET_DOSSIER);
        String body = "{\"dossier\": \"workspace://SpacesStore/" + dossierId + "\"," +
                       "\"bureauCourant\": \"workspace://SpacesStore/" + bureauId + "\"}";
        return ModelMapper.getDossier(RESTUtils.post(url, body));
    }

    public static ArrayList<EtapeCircuit> getCircuit(String dossierId) {
        String url = buildUrl(ACTION_GET_CIRCUIT);
        String body = "{\"dossier\": \"workspace://SpacesStore/" + dossierId + "\"}";
        return ModelMapper.getCircuit(RESTUtils.post(url, body));
    }

    public static ArrayList<Dossier> getDossiers(String bureauId) {
        String url = buildUrl(ACTION_GET_DOSSIERS);
        String body = "{\"bureauCourant\": \"workspace://SpacesStore/" + bureauId + "\"," +
                "\"filters\": \"\"," +
                "\"page\": 0," +
                "\"pageSize\": 10}";
        //Log.d( IParapheurHttpClient.class, "REQUEST on " + FOLDERS_PATH + ": " + requestBody );
        return ModelMapper.getDossiers(RESTUtils.post(url, body));
    }

    public ArrayList<Bureau> getBureaux() {
        String url = buildUrl(ACTION_GET_BUREAUX);
        String body = "{\"username\": \"" + MyAccounts.INSTANCE.getSelectedAccount().getLogin() + "\"}";
        //Log.d( IParapheurHttpClient.class, "REQUEST on " + FOLDERS_PATH + ": " + requestBody );
        return ModelMapper.getBureaux(RESTUtils.post(url, body));
    }

    public static String buildUrl(String action) {
        String ticket = getTicket(MyAccounts.INSTANCE.getSelectedAccount());
        return BASE_PATH + MyAccounts.INSTANCE.getSelectedAccount().getUrl() + action + ((ticket == null)? "" : "?alf_ticket=" + ticket);
    }

    public static String downloadFile(String url, String path) {

        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            throw new RuntimeException("The external storage is not accessible");
        }
        File file = new File(path);
        String fullUrl = buildUrl(url);
        InputStream response = RESTUtils.downloadFile(fullUrl);

        FileOutputStream fileOutput = null;
        try {
            fileOutput = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bufferLength;
            while ((bufferLength = response.read(buffer)) > 0 ) {
                fileOutput.write(buffer, 0, bufferLength);
            }
            //close the output stream when done
            fileOutput.close();

        } catch (Exception e) {
            Log.e("RESTClient", "Erreur lors du téléchargement du pdf : " + e);
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
