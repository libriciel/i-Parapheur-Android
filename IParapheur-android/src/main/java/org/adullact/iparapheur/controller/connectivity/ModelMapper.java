package org.adullact.iparapheur.controller.connectivity;

import android.util.Log;

import org.adullact.iparapheur.controller.utils.TransformUtils;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.EtapeCircuit;
import org.adullact.iparapheur.model.RequestResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jmaire on 04/11/2013.
 */
public class ModelMapper
{

    public static Dossier getDossier(RequestResponse requestResponse) {
        Dossier dossier = null;
        if (requestResponse.getResponse() != null)
        {
            dossier = getDossier(requestResponse.getResponse());
        }
        return dossier;
    }

    private static Dossier getDossier(JSONObject jsonObject) {
        Dossier dossier = null;
        try
        {
            dossier = new Dossier(jsonObject.getString("dossierRef").substring("workspace://SpacesStore/".length()),
                    jsonObject.getString("titre"),
                    Dossier.Action.valueOf(jsonObject.getString("actionDemandee")),
                    jsonObject.getString("type"),
                    jsonObject.getString("sousType"),
                    TransformUtils.parseISO8601Date(jsonObject.getString("dateCreation")),
                    TransformUtils.parseISO8601Date(jsonObject.optString("dateLimite")));

            if (jsonObject.has("documents"))
            {
                JSONArray documents = jsonObject.getJSONArray("documents");
                for (int index = 0; index < documents.length(); index++)
                {
                    JSONObject doc = documents.getJSONObject(index);
                    dossier.addDocument(new Document(
                            doc.getString("nodeRef").substring("workspace://SpacesStore/".length()),
                            doc.getString("name"),
                            doc.getString("downloadUrl")));
                }
            }
        } catch (JSONException e) {
            Log.e("ModelMapper", "Erreur lors de la transformation en dossier.", e);
        }
        return dossier;
    }

    public static ArrayList<Dossier> getDossiers(RequestResponse requestResponse) {
        ArrayList<Dossier> dossiers = new ArrayList<Dossier>();
        if (requestResponse.getResponse() != null) {
            JSONArray array = requestResponse.getResponse().optJSONArray("dossiers");
            if (array != null) {
                for (int i = 0; i < array.length(); i++)
                {
                    try {
                        Dossier dossier = getDossier(array.getJSONObject(i));
                        if (dossier != null) {
                            dossiers.add(dossier);
                        }
                    } catch (JSONException e) {
                        Log.e("ModelMapper", "Erreur lors de la transformation en liste de dossiers.", e);
                    }
                }
            }
        }
        return dossiers;
    }

    public static ArrayList<EtapeCircuit> getCircuit(RequestResponse response) {
        ArrayList<EtapeCircuit> circuit = new ArrayList<EtapeCircuit>();
        if (response.getResponse() != null)
        {
            JSONArray circuitArray = response.getResponse().optJSONArray("circuit");
            if (circuit != null) {
                for (int i = 0; i < circuitArray.length(); i++)
                {
                    try {
                        JSONObject etapeObject = circuitArray.getJSONObject(i);
                        circuit.add(new EtapeCircuit(
                                TransformUtils.parseISO8601Date(etapeObject.getString("dateValidation")),
                                etapeObject.optBoolean("approved", false),
                                etapeObject.optString("parapheurName", ""),
                                etapeObject.optString("signataire", ""),
                                Dossier.Action.valueOf(etapeObject.optString("actionDemandee", "")),
                                etapeObject.optString("annotPub", "")));

                    } catch (JSONException e) {
                        Log.e("ModelMapper", "Erreur lors de la transformation en circuit", e);
                    }
                }
            }
        }
        return circuit;
    }
}
