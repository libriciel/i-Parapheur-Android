package org.adullact.iparapheur.controller.rest.mapper;

import android.graphics.RectF;
import android.util.SparseArray;

import org.adullact.iparapheur.controller.utils.TransformUtils;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Annotation;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.EtapeCircuit;
import org.adullact.iparapheur.model.PageAnnotations;
import org.adullact.iparapheur.model.RequestResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * Created by jmaire on 04/11/2013.
 * Utilitaire de conversion des réponses json du serveur i-Parapheur pour les versions d'API 1 et 2.
 */
public class ModelMapper {

    public Dossier getDossier(RequestResponse requestResponse) throws RuntimeException {
        Dossier dossier = null;

        if (requestResponse.getResponse() != null)
            dossier = getDossier(requestResponse.getResponse());

        return dossier;
    }

    protected Dossier getDossier(JSONObject jsonObject)
    {

        String dossierRef = jsonObject.optString("dossierRef");
        if (dossierRef.contains("workspace://SpacesStore/")) {
            dossierRef = dossierRef.substring("workspace://SpacesStore/".length());
        }
        ArrayList<Action> actions = getActionsForDossier(jsonObject);
        Dossier dossier = new Dossier(dossierRef,
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
                        docRef = dossierRef.substring("workspace://SpacesStore/".length());
                    }
                    String downloadUrl = doc.optString("downloadUrl");
                    if (doc.has("visuelPdfUrl")) {
                        downloadUrl += ";ph:visuel-pdf";
                    }
                    dossier.addDocument(new Document(
                            docRef,
                            dossierRef,
                            doc.optString("name"),
                            doc.optInt("size", -1),
                            downloadUrl));
                }
            }
        }
        return dossier;
    }

    protected ArrayList<Action> getActionsForDossier(JSONObject dossier) {
        ArrayList<Action> actions = new ArrayList<Action>();
        boolean isActeurCourant = dossier.optBoolean("isActeurCourant", false);
        if (isActeurCourant) {
            actions.add(Action.EMAIL);
            actions.add(Action.JOURNAL);
            actions.add(Action.ENREGISTRER);
        }
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
                        actions.add(Action.SUPPRESSION);
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
                        else if (actionDemandee.equals("MAILSEC")) {
                            actions.add(Action.MAILSEC);
                        }
                    }
                }
            }
        }
        return actions;
    }


    public ArrayList<Dossier> getDossiers(RequestResponse requestResponse)
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

    public ArrayList<EtapeCircuit> getCircuit(RequestResponse response) {
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
                            etapeObject.optBoolean("rejected", false),
                            etapeObject.optString("parapheurName"),
                            etapeObject.optString("signataire"),
                            Action.valueOf(etapeObject.optString("actionDemandee", "VISA")),
                            etapeObject.optString("annotPub")));

                }
            }
        }
        return circuit;
    }

    public ArrayList<Bureau> getBureaux(RequestResponse response) {
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

    public LinkedHashMap<String, ArrayList<String>> getTypologie(RequestResponse response) {
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

    public SparseArray<PageAnnotations> getAnnotations(RequestResponse response) {
        SparseArray<PageAnnotations> annotations = new SparseArray<PageAnnotations>();
        if (response.getResponseArray() != null) {
            JSONArray etapes = response.getResponseArray();
            for (int step = 0; step < etapes.length(); step++) {
                JSONObject etapeAnnotations = etapes.optJSONObject(step);
                if (etapeAnnotations != null) {
                    Iterator pages = etapeAnnotations.keys();
                    while (pages.hasNext()) {
                        String pageStr = (String) pages.next();
                        JSONArray pagesAnnotations = etapeAnnotations.optJSONArray(pageStr);
                        if (pagesAnnotations != null) {
                            PageAnnotations pageAnnotations = new PageAnnotations();
                            for (int page = 0; page < pagesAnnotations.length(); page++) {
                                JSONObject jsonAnnotation = pagesAnnotations.optJSONObject(page);
                                if (jsonAnnotation != null) {

                                    try {
                                        Date date = null;
                                        try {
                                            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.US).parse(jsonAnnotation.getString("date"));
                                        } catch (ParseException e) {
                                            date = new Date();
                                        } catch (JSONException e) {
                                            date = new Date();
                                        }
                                        String outDate = new SimpleDateFormat("dd/MM/yyyy' - 'HH:mm").format(date);

                                        JSONObject jsonRect = jsonAnnotation.getJSONObject("rect");
                                        JSONObject topLeft = jsonRect.getJSONObject("topLeft");
                                        JSONObject bottomRight = jsonRect.getJSONObject("bottomRight");
                                        RectF rect = new RectF(topLeft.getLong("x"), topLeft.getLong("y"), bottomRight.getLong("x"), bottomRight.getLong("y"));

                                        pageAnnotations.add(
                                                new Annotation(
                                                        jsonAnnotation.optString("id"),
                                                        jsonAnnotation.optString("author"),
                                                        Integer.parseInt(pageStr),
                                                        jsonAnnotation.optBoolean("secretaire"),
                                                        outDate,
                                                        rect,
                                                        jsonAnnotation.optString("text"),
                                                        jsonAnnotation.optString("type", "rect"),
                                                        step));
                                    } catch (JSONException e) {
                                        // Tant pis, on passe l'annotation
                                    }
                                }
                            }
                            annotations.append(Integer.parseInt(pageStr), pageAnnotations);
                        }
                    }
                }
            }
        }
        return annotations;
    }
}
