package org.adullact.iparapheur.controller.rest.api;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.controller.rest.RESTUtils;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.EtapeCircuit;
import org.adullact.iparapheur.model.RequestResponse;
import org.apache.http.HttpStatus;
import org.adullact.iparapheur.controller.utils.IParapheurException;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jmaire on 23/10/13.
 */
public enum RESTClient implements IParapheurAPI {

    INSTANCE;
    private static final String RESOURCE_API_VERSION = "/parapheur/api/getApiLevel";
    private static final int API_VERSION_MAX = 3;

    private final RESTClientAPI1 restClientAPI1 = new RESTClientAPI1();
    private final RESTClientAPI2 restClientAPI2 = new RESTClientAPI2();
    private final RESTClientAPI3 restClientAPI3 = new RESTClientAPI3();

    /**
     * Renvoie la version d'API du serveur i-Parapheur associé à ce compte.
     * Cette méthode peut faire une requête au serveur, il faut donc l'appeler dans
     * un thread (ex. AsyncTask).
     * @param account le compte pour lequel on veur récupérer la version de l'API
     * @return in entier représentant la version de l'API.
     */
    private int getAPIVersion(Account account) throws IParapheurException {
        Integer apiVersion = account.getApiVersion();
        if (apiVersion == null) {

            String tenant = account.getTenant();

            String url = BASE_PATH +
                    ((tenant != null)? tenant + "." : "") +
                    account.getUrl() +
                    RESOURCE_API_VERSION +
                    (account.getTicket() != null ? "?alf_ticket=" + account.getTicket() : "");

            try {
                RequestResponse response = RESTUtils.get(url);
                apiVersion = response.getResponse().getInt("level");
                account.setApiVersion(apiVersion);
            }
            catch (JSONException e) {
                throw new IParapheurException(R.string.error_mismatch_versions, account.getTitle());
            }
            catch (IParapheurException e) {
                // Pour apiVersion < 3, authentification obligatoire...
                if (e.getResId() == R.string.http_error_401) {
                    restClientAPI1.getTicket(account);
                    url = BASE_PATH +
                            ((tenant != null)? tenant + "." : "") +
                            account.getUrl() +
                            RESOURCE_API_VERSION +
                            "?alf_ticket=" + account.getTicket();
                    RequestResponse response = RESTUtils.get(url);
                    try {
                        apiVersion = response.getResponse().getInt("level");
                    } catch (JSONException e1) {
                        throw new IParapheurException(R.string.error_mismatch_versions, account.getTitle());
                    }
                    account.setApiVersion(apiVersion);
                }
                else {
                    throw e;
                }
            }

        }
        return apiVersion;
    }



    @Override
    public int test(Account account) throws IParapheurException {
        return getRESTClient(account).test(account);
    }

    @Override
    public String getTicket(Account account) throws IParapheurException {
        return null;
    }

    @Override
    public List<Bureau> getBureaux() throws IParapheurException {
        return getRESTClient().getBureaux();
    }

    private IParapheurAPI getRESTClient() throws IParapheurException {
        return getRESTClient(MyAccounts.INSTANCE.getSelectedAccount());
    }

    private IParapheurAPI getRESTClient(Account account) throws IParapheurException {
        Integer apiVersion = getAPIVersion(account);
        IParapheurAPI apiClient;
        if (apiVersion > API_VERSION_MAX) {
            throw new RuntimeException("La version du i-Parapheur associé au compte " +
                    MyAccounts.INSTANCE.getSelectedAccount().getTitle() +
                    " est trop récente pour cette application. " +
                    "Veuillez mettre à jour votre application.");
        }
        switch (apiVersion) {
            case 1 :
                apiClient = restClientAPI1;
                break;
            case 2 :
                apiClient = restClientAPI2;
                break;
            case 3 :
                apiClient = restClientAPI3;
                break;
            default:
                apiClient = restClientAPI2;
                break;
        }
        return apiClient;
    }

    @Override
    public Dossier getDossier(String bureauId, String dossierId) throws IParapheurException {
        return getRESTClient().getDossier(bureauId, dossierId);
    }

    @Override
    public List<Dossier> getDossiers(String bureauId) throws IParapheurException {
        return getRESTClient().getDossiers(bureauId);
    }

    @Override
    public Map<String, ArrayList<String>> getTypologie() throws IParapheurException {
        return getRESTClient().getTypologie();
    }

    @Override
    public List<EtapeCircuit> getCircuit(String dossierId) throws IParapheurException {
        return getRESTClient().getCircuit(dossierId);
    }

    @Override
    public boolean downloadFile(String url, String path) throws IParapheurException {

        return getRESTClient().downloadFile(url, path);
    }

    @Override
    public boolean viser(Dossier dossier, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
        return getRESTClient().viser(dossier, annotPub, annotPriv, bureauId);
    }

    @Override
    public boolean signer(String dossierId, String signValue, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
        return getRESTClient().signer(dossierId, signValue, annotPub, annotPriv, bureauId);
    }

    @Override
    public boolean archiver(String dossierId, String archiveTitle, boolean withAnnexes, String bureauId) throws IParapheurException {
        return getRESTClient().archiver(dossierId, archiveTitle, withAnnexes, bureauId);
    }

    @Override
    public boolean envoiTdtHelios(String dossierId, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
        return getRESTClient().envoiTdtHelios(dossierId, annotPub, annotPriv, bureauId);
    }

    @Override
    public boolean envoiTdtActes(String dossierId, String nature, String classification, String numero, long dateActes, String objet, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
        return getRESTClient().envoiTdtActes(dossierId, nature, classification, numero, dateActes, objet, annotPub, annotPriv, bureauId);
    }

    @Override
    public boolean envoiMailSec(String dossierId, List<String> destinataires, List<String> destinatairesCC, List<String> destinatairesCCI, String sujet, String message, String password, boolean showPassword, boolean annexesIncluded, String bureauId) throws IParapheurException {
        // TODO : manage annexes
        return getRESTClient().envoiMailSec(dossierId, destinataires, destinatairesCC, destinatairesCCI, sujet, message, password, showPassword, annexesIncluded, bureauId);
    }

    @Override
    public boolean rejeter(String dossierId, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
        return getRESTClient().rejeter(dossierId, annotPub, annotPriv, bureauId);
    }
}
