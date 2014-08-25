package org.adullact.iparapheur.controller.rest.api;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.controller.rest.RESTUtils;
import org.adullact.iparapheur.controller.rest.mapper.ModelMapper;
import org.adullact.iparapheur.controller.dossier.filter.MyFilters;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.EtapeCircuit;
import org.adullact.iparapheur.model.Filter;
import org.adullact.iparapheur.model.RequestResponse;
import org.apache.http.HttpStatus;
import org.adullact.iparapheur.controller.utils.IParapheurException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jmaire on 09/06/2014.
 * API i-Parapheur version 1
 *
 * de la v4.0.00 / v3.4.00 (comprise)
 * a la v4.1.00 / v3.5.00 (exclue)
 *
 * La v4.0.00 est la première version i-Parapheur
 * compatible Android
 */
public class RESTClientAPI1 extends RESTClientAPI {

    private static final String ACTION_GET_DOSSIER = "/parapheur/api/getDossier";
    private static final String ACTION_GET_DOSSIERS = "/parapheur/api/getDossiersHeaders";
    private static final String ACTION_GET_CIRCUIT = "/parapheur/api/getCircuit";
    protected static final String ACTION_GET_BUREAUX = "/parapheur/api/getBureaux";
    private static final String ACTION_GET_TYPOLOGIE = "/parapheur/api/getTypologie";

    private static final String ACTION_VISA = "/parapheur/api/visa";
    private static final String ACTION_SIGNATURE = "/parapheur/api/signature";
    private static final String ACTION_TDT = "/parapheur/api/tdt";
    private static final String ACTION_MAILSEC = "/parapheur/api/mailsec";
    private static final String ACTION_ARCHIVAGE = "/parapheur/api/archivage";
    private static final String ACTION_REJET = "/parapheur/api/rejet";

    protected ModelMapper modelMapper = new ModelMapper();

    @Override
    public List<Bureau> getBureaux() throws IParapheurException {
        String url = buildUrl(ACTION_GET_BUREAUX);
        String body = "{\"username\": \"" + MyAccounts.INSTANCE.getSelectedAccount().getLogin() + "\"}";
        return modelMapper.getBureaux(RESTUtils.post(url, body));
    }

    @Override
    public Dossier getDossier(String bureauId, String dossierId) throws IParapheurException {
        String url = buildUrl(ACTION_GET_DOSSIER);
        String body = "{\"dossier\": \"workspace://SpacesStore/" + dossierId + "\"," +
                "\"bureauCourant\": \"workspace://SpacesStore/" + bureauId + "\"}";
        //Log.d("debug", "body : " + body);
        return modelMapper.getDossier(RESTUtils.post(url, body));
    }

    @Override
    public List<Dossier> getDossiers(String bureauId) throws IParapheurException {
        String url = buildUrl(ACTION_GET_DOSSIERS);
        Filter filter = MyFilters.INSTANCE.getSelectedFilter();
        if (filter == null) {
            filter = new Filter();
        }
        String body = "{\"bureauCourant\": \"workspace://SpacesStore/" + bureauId + "\"," +
                "\"filters\": " + filter.getJSONFilter() + "," +
                "\"page\": 0," +
                "\"pageSize\": 15," +
                "parent: \"" + filter.getEtat() + "\"," +
                "asc: \"false\","+
                "propSort: \"cm:created\"}";
        //Log.d( IParapheurHttpClient.class, "REQUEST on " + FOLDERS_PATH + ": " + requestBody );
        return modelMapper.getDossiers(RESTUtils.post(url, body));
    }

    @Override
    public Map<String, ArrayList<String>> getTypologie() throws IParapheurException {
        String url = buildUrl(ACTION_GET_TYPOLOGIE);
        String body = "{\"getAll\": \"true\"}";
        return modelMapper.getTypologie(RESTUtils.post(url, body));
    }

    @Override
    public List<EtapeCircuit> getCircuit(String dossierId) throws IParapheurException {
        String url = buildUrl(ACTION_GET_CIRCUIT);
        String body = "{\"dossier\": \"workspace://SpacesStore/" + dossierId + "\"}";
        return modelMapper.getCircuit(RESTUtils.post(url, body));
    }

    @Override
    public boolean viser(Dossier dossier, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
        try {
            JSONObject json = new JSONObject();
            JSONArray dossiersId = new JSONArray();
            dossiersId.put("workspace://SpacesStore/" + dossier.getId());
            json.put("dossiers", dossiersId);
            json.put("bureauCourant", "workspace://SpacesStore/" + bureauId);
            json.put("annotPub", annotPub);
            json.put("annotPriv", annotPriv);

            RESTUtils.post(buildUrl(ACTION_VISA), json.toString());
        } catch (JSONException e) {
            throw new RuntimeException("Une erreur est survenue lors du visa", e);
        }
        return true;
    }

    @Override
    public boolean signer(String dossierId, String signValue, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
        return false;
    }

    @Override
    public boolean archiver(String dossierId, String archiveTitle, boolean withAnnexes, String bureauId) throws IParapheurException {
        return false;
    }

    @Override
    public boolean envoiTdtHelios(String dossierId, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
        return false;
    }

    @Override
    public boolean envoiTdtActes(String dossierId, String nature, String classification, String numero, long dateActes, String objet, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
        return false;
    }

    @Override
    public boolean envoiMailSec(String dossierId, List<String> destinataires, List<String> destinatairesCC, List<String> destinatairesCCI, String sujet, String message, String password, boolean showPassword, boolean annexesIncluded, String bureauId) throws IParapheurException {
        return false;
    }

    @Override
    public boolean rejeter(String dossierId, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
        return false;
    }
}
