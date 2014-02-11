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
import java.util.Iterator;
import java.util.LinkedHashMap;

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

        String ref = jsonObject.optString("dossierRef");
        if (ref.contains("workspace://SpacesStore/")) {
            ref = ref.substring("workspace://SpacesStore/".length());
        }
        ArrayList<Action> actions = getActionsForDossier(jsonObject);
        Dossier dossier = new Dossier(ref,
                jsonObject.optString("titre"),
                Action.valueOf(jsonObject.optString("actionDemandee", "VISA")),
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
                    String downloadUrl = doc.optString("downloadUrl");
                    if (doc.has("visuelPdfUrl")) {
                        downloadUrl += ";ph:visuel-pdf";
                    }
                    dossier.addDocument(new Document(
                            docRef,
                            doc.optString("name"),
                            downloadUrl));
                }
            }
        }
        return dossier;
    }

    private static ArrayList<Action> getActionsForDossier(JSONObject dossier) {
        ArrayList<Action> actions = new ArrayList<Action>();
        JSONObject returnedActions = dossier.optJSONObject("actions");
        String actionDemandee = dossier.optString("actionDemandee");
        if (returnedActions != null) {
            Iterator actionsIterator = returnedActions.keys();
            while (actionsIterator.hasNext()) {
                String action = (String) actionsIterator.next();
                boolean isActionEnabled = returnedActions.optBoolean(action, false);
                if (isActionEnabled) {
                    if (action.equals("archive")) {
                        if (actionDemandee.equals("ARCHIVAGE")) {
                            actions.add(Action.ARCHIVAGE);
                        }
                    }
                    else if (action.equals("delete")) {
                        actions.add(Action.SUPPRIMER);
                    }
                    else if (action.equals("reject")) {
                        actions.add(Action.REJET);
                    }
                    else if (action.equals("remorse")) {
                        actions.add(Action.REMORD);
                    }
                    else if (action.equals("secretary")) {
                        actions.add(Action.SECRETARIAT);
                    }
                    else if (action.equals("sign")) {
                        if (actionDemandee.equals("SIGNATURE")) {
                            actions.add(Action.SIGNATURE);
                        }
                        else if (actionDemandee.equals("VISA")) {
                            actions.add(Action.VISA);
                        }
                        else if (actionDemandee.equals("TDT")) {
                            actions.add(Action.TDT);
                        }
                        else if (actionDemandee.equals("MAILSEC")) {
                            actions.add(Action.MAILSEC);
                        }
                    }
                }
            }
        }
        return actions;
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

    public static LinkedHashMap<String, ArrayList<String>> getTypologie(RequestResponse response) {
        LinkedHashMap<String, ArrayList<String>> typologie = new LinkedHashMap<String, ArrayList<String>>();
        if (response.getResponse() != null) {
            JSONObject data = response.getResponse().optJSONObject("data");
            if (data != null) {
                JSONObject typology = data.optJSONObject("typology");
                if (typology != null) {
                    Iterator types = typology.keys();
                    while (types.hasNext()) {
                        String type = (String) types.next();
                        JSONArray jsonSousTypes =  typology.optJSONArray(type);
                        if (jsonSousTypes != null) {
                            ArrayList<String> sousTypes = new ArrayList<String>(jsonSousTypes.length());
                            for (int i = 0; i < jsonSousTypes.length(); i++) {
                                String sousType = jsonSousTypes.optString(i);
                                if (!sousType.isEmpty()) {
                                    sousTypes.add(sousType);
                                }
                            }
                            typologie.put(type, sousTypes);
                        }
                    }
                }
            }
        }
        return typologie;
    }
}
