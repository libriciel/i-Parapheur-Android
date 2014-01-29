package org.adullact.iparapheur.controller.connectivity;

import org.adullact.iparapheur.controller.utils.TransformUtils;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.EtapeCircuit;
import org.adullact.iparapheur.model.RequestResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jmaire on 04/11/2013.
 */
public class ModelMapper
{

    public static Dossier getDossier(RequestResponse requestResponse) throws RuntimeException {
        Dossier dossier = null;
        if (requestResponse.getResponse() != null)
        {
            dossier = getDossier(requestResponse.getResponse());
        }
        return dossier;
    }

    private static Dossier getDossier(JSONObject jsonObject)
    {
        ArrayList<Action> actions = new ArrayList<Action>(1);
        actions.add(Action.valueOf(jsonObject.optString("actionDemandee", "VISA")));
        String ref = jsonObject.optString("dossierRef");
        if (ref.contains("workspace://SpacesStore/")) {
            ref = ref.substring("workspace://SpacesStore/".length());
        }
        Dossier dossier = new Dossier(ref,
                jsonObject.optString("titre"),
                actions,
                jsonObject.optString("type"),
                jsonObject.optString("sousType"),
                TransformUtils.parseISO8601Date(jsonObject.optString("dateCreation")),
                TransformUtils.parseISO8601Date(jsonObject.optString("dateLimite")));

        JSONArray documents = jsonObject.optJSONArray("documents");
        if (documents != null)
        {

            for (int index = 0; index < documents.length(); index++)
            {
                JSONObject doc = documents.optJSONObject(index);
                if (doc != null) {
                    String docRef = jsonObject.optString("dossierRef");
                    if (docRef.contains("workspace://SpacesStore/")) {
                        docRef = ref.substring("workspace://SpacesStore/".length());
                    }
                    dossier.addDocument(new Document(
                            docRef,
                            doc.optString("name"),
                            doc.optString("downloadUrl")));
                }
            }
        }
        return dossier;
    }

    public static ArrayList<Dossier> getDossiers(RequestResponse requestResponse)
    {
        ArrayList<Dossier> dossiers = new ArrayList<Dossier>();
        if (requestResponse.getResponse() != null) {
            JSONArray array = requestResponse.getResponse().optJSONArray("dossiers");
            if (array != null) {
                for (int i = 0; i < array.length(); i++)
                {
                    JSONObject dossierJSON = array.optJSONObject(i);
                    if (dossierJSON != null) {
                        Dossier dossier = getDossier(dossierJSON);
                        if (dossier != null) {
                            dossiers.add(dossier);
                        }
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
                    JSONObject etapeObject = circuitArray.optJSONObject(i);
                    circuit.add(new EtapeCircuit(
                            TransformUtils.parseISO8601Date(etapeObject.optString("dateValidation")),
                            etapeObject.optBoolean("approved"),
                            etapeObject.optString("parapheurName"),
                            etapeObject.optString("signataire"),
                            Action.valueOf(etapeObject.optString("actionDemandee", "VISA")),
                            etapeObject.optString("annotPub")));

                }
            }
        }
        return circuit;
    }

    public static ArrayList<Bureau> getBureaux(RequestResponse response) {
        ArrayList<Bureau> bureaux = new ArrayList<Bureau>();
        if (response.getResponse() != null) {
            JSONArray array = response.getResponse().optJSONArray("bureaux");
            if (array != null) {
                for (int i = 0; i < array.length(); i++)
                {
                    JSONObject bureau = array.optJSONObject(i);
                    if (bureau != null) {
                        String bureauRef = bureau.optString("nodeRef");
                        if (bureauRef.contains("workspace://SpacesStore/")) {
                            bureauRef = bureauRef.substring("workspace://SpacesStore/".length());
                        }
                        bureaux.add(new Bureau(bureauRef, bureau.optString("name")));
                    }
                }
            }
        }
        return bureaux;
    }
}
