package org.adullact.iparapheur.controller.rest.api;

import android.util.SparseArray;

import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Annotation;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.EtapeCircuit;
import org.adullact.iparapheur.model.PageAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IParapheurAPI {

    public static final String BASE_PATH = "https://m.";

    /**
     * Test si un compte est accessible.
     * @param account le compte à tester.
     * @return l'id d'une ressource (string) indiquant l'état d'accessibilité du compte.
     */
    int test(Account account) throws IParapheurException;

    /**
     * Retourne un ticket d'authentification associé au compte passé en paramètre
     * @param account le compte sur lequel se connecter
     * @return un ticket d'authentification
     * @throws IParapheurException
     */
    String getTicket(Account account) throws IParapheurException;

    /**
     *
     * @return
     */
    List<Bureau> getBureaux() throws IParapheurException;

    /**
     *
     * @param bureauId
     * @param dossierId
     * @return
     */
    Dossier getDossier(String bureauId, String dossierId) throws IParapheurException;

    /**
     *
     * @param bureauId
     * @return
     */
    List<Dossier> getDossiers(String bureauId) throws IParapheurException;

    /**
     *
     * @return
     */
    Map<String, ArrayList<String>> getTypologie() throws IParapheurException;

    /**
     *
     * @param dossierId
     * @return
     */
    List<EtapeCircuit> getCircuit(String dossierId) throws IParapheurException;

    /**
     *
     * @param dossierId
     * @return les annotations graphiques déposées sur le document principal du dossier
     * @throws IParapheurException
     */
    SparseArray<PageAnnotations> getAnnotations(String dossierId) throws IParapheurException;

    /**
     *
     * @param dossierId
     * @return l'id de l'annotation crééeles annotations graphiques déposées sur le document principal du dossier
     * @throws IParapheurException
     */
    String createAnnotation(String dossierId, Annotation annotation, int page) throws IParapheurException;

    /**
     *
     * @param dossierId
     * @param annotation
     * @param page
     * @throws IParapheurException
     */
    void updateAnnotation(String dossierId, Annotation annotation, int page) throws IParapheurException;

    /**
     *
     * @param dossierId
     * @param annotationId
     * @throws IParapheurException
     */
    void deleteAnnotation(String dossierId, String annotationId, int page) throws IParapheurException;

    /**
     *
     * @param url
     * @param path
     * @return
     * @throws IParapheurException
     */
    boolean downloadFile(String url, String path) throws IParapheurException;

    /**
     *
     * @param dossier
     * @param annotPub
     * @param annotPriv
     * @param bureauId
     * @return
     * @throws IParapheurException
     */
    boolean viser(Dossier dossier, String annotPub, String annotPriv, String bureauId) throws IParapheurException;

    /**
     *
     * @param dossierId
     * @param signValue
     * @param annotPub
     * @param annotPriv
     * @param bureauId
     * @return
     * @throws IParapheurException
     */
    boolean signer(String dossierId, String signValue, String annotPub, String annotPriv, String bureauId) throws IParapheurException;

    /**
     *
     * @param dossierId
     * @param archiveTitle
     * @param withAnnexes
     * @param bureauId
     * @return
     * @throws IParapheurException
     */
    boolean archiver(String dossierId, String archiveTitle, boolean withAnnexes, String bureauId) throws IParapheurException;

    /**
     *
     * @param dossierId
     * @param annotPub
     * @param annotPriv
     * @param bureauId
     * @return
     * @throws IParapheurException
     */
    boolean envoiTdtHelios(String dossierId, String annotPub, String annotPriv, String bureauId) throws IParapheurException;

    /**
     *
     * @param dossierId
     * @param classification
     * @param annotPub
     * @param annotPriv
     * @param bureauId
     * @return
     * @throws IParapheurException
     */
    boolean envoiTdtActes(String dossierId, String nature, String classification, String numero, long dateActes, String objet, String annotPub, String annotPriv, String bureauId) throws IParapheurException;

    /**
     *
     * @param dossierId
     * @param destinataires
     * @param sujet
     * @param message
     * @param showPassword
     * @param bureauId
     * @return
     * @throws IParapheurException
     */
    boolean envoiMailSec(String dossierId,
                         List<String> destinataires,
                         List<String> destinatairesCC,
                         List<String> destinatairesCCI,
                         String sujet,
                         String message,
                         String password,
                         boolean showPassword,
                         boolean annexesIncluded,
                         String bureauId) throws IParapheurException;

    /**
     *
     * @param dossierId
     * @param annotPub
     * @param annotPriv
     * @param bureauId
     * @return
     * @throws IParapheurException
     */
    boolean rejeter(String dossierId, String annotPub, String annotPriv, String bureauId) throws IParapheurException;
}