package org.adullact.iparapheur.model;

import org.adullact.iparapheur.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by jmaire on 01/11/2013.
 */
public class Dossier {

    public enum Action
    {
        // TODO : mailsec and TdT icons
        VISA(R.drawable.ic_circuit_iw_visa, R.drawable.ic_circuit_ip_visa),
        SIGNATURE(R.drawable.ic_circuit_iw_signature, R.drawable.ic_circuit_ip_signature),
        TDT(R.drawable.ic_circuit_iw_visa, R.drawable.ic_circuit_ip_visa),
        ARCHIVAGE(R.drawable.ic_circuit_iw_archivage, R.drawable.ic_circuit_iw_archivage),
        MAILSEC(R.drawable.ic_circuit_iw_visa, R.drawable.ic_circuit_ip_visa);

        private final int icon;
        private final int approvedIcon;

        Action(int icon, int rejectedIcon) {
            this.icon = icon;
            this.approvedIcon = rejectedIcon;
        }

        public int getIcon(boolean approved) {
            return approved? approvedIcon : icon;
        }
    }

    private final String id;

    private final String name;

    private final Action action;

    private final String type;

    private final String sousType;

    private final Date dateCreation;

    private final Date dateLimite;

    private final List<Document> mainDocuments = new ArrayList<Document>();

    private final List<Document> annexes = new ArrayList<Document>();

    private ArrayList<EtapeCircuit> circuit;

    // TODO : remove
    public Dossier(int i) {
        this(UUID.randomUUID().toString(),
                "Dossier " + i,
                Action.VISA,
                "Type",
                "SousType",
                Calendar.getInstance().getTime(),
                Calendar.getInstance().getTime());
    }

    /**
     * Constructor used to search a dossier in a list (only the id is used for comparisons).
     * @param id
     */
    public Dossier(String id) {
        this.id = id;
        this.name = this.type = this.sousType = null;
        this.dateCreation = this.dateLimite = null;
        this.action = null;
    }

    public Dossier(String id, String name, Action action, String type, String sousType, Date dateCreation, Date dateLimite) {
        this.id = id;
        this.name = name;
        this.action = action;
        this.type = type;
        this.sousType = sousType;
        this.dateCreation = dateCreation;
        this.dateLimite = dateLimite;
    }

    public boolean isDetailsAvailable() {
        return (circuit != null) && !mainDocuments.isEmpty();
    }

    @Override
    public String toString() {
        return name;
    }

    // Equals and hashCode overriding, so we can find dossier with its id.
    @Override
    public boolean equals(Object o){
        if(o instanceof Dossier){
            Dossier toCompare = (Dossier) o;
            return this.id.equals(toCompare.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    // Setters (only for documents and details)

    // FIXME plus tard: pour le moment, il n'y a QU'UN SEUL DOCUMENT, les autres pieces sont necessairement des ANNEXES !

    public void addDocument(Document document) {
        if (mainDocuments.isEmpty()) {
            this.mainDocuments.add(document);
        }
        else {
            this.annexes.add(document);
        }
    }

    public void saveDetails(Dossier dossier) {
        this.mainDocuments.addAll(dossier.getMainDocuments());
        this.annexes.addAll(dossier.getAnnexes());
    }

    // Getters

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Action getAction() {
        return action;
    }

    public String getType() {
        return type;
    }

    public String getSousType() {
        return sousType;
    }

    public String getDateCreation() {
        return DateFormat.getDateInstance().format(dateCreation);
    }

    public String getDateLimite() {
        return (dateLimite == null)? "" : DateFormat.getDateInstance().format(dateLimite);
    }

    public List<Document> getMainDocuments() {
        return mainDocuments;
    }

    public List<Document> getAnnexes() {
        return annexes;
    }

    /**
     * Return all documents (main and annexes).
     * @return main documents and annexes
     */
    public List<Document> getDocuments() {
        List<Document> documents = new ArrayList<Document>(mainDocuments);
        documents.addAll(annexes);
        return documents;
    }

    public void setCircuit(ArrayList<EtapeCircuit> circuit) {
        this.circuit = circuit;
    }

    public ArrayList<EtapeCircuit> getCircuit() {
        return circuit;
    }

}
