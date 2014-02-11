package org.adullact.iparapheur.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;

/**
 * Created by jmaire on 06/02/2014.
 */
public class Filter {

    public static final String DEFAULT_ID = "default-filter";
    public static final String EDIT_FILTER_NOM = "Éditer le filtre courant";
    public static final String EDIT_FILTER_ID = "edit-filter";

    private static final String DEFAULT_ETAT = "a-traiter";
    private static final String DEFAULT_NOM = "Dossiers à traiter";

    public static final ArrayList<String> etats;
    public static final HashMap<String, String> etatsTitres;
    static
    {
        etats = new ArrayList<String>(12);
        etats.add("en-preparation");
        etats.add("a-traiter");
        etats.add("a-archiver");
        etats.add("retournes");
        etats.add("en-cours");
        etats.add("a-venir");
        etats.add("recuperables");
        etats.add("en-retard");
        etats.add("traites");
        etats.add("dossiers-delegues");
        etats.add("no-corbeille");
        etats.add("no-bureau");

        /*etatsTitres = new HashMap<String, String>();
        etatsTitres.put("À transmettre", "en-preparation");
        etatsTitres.put("À traiter", "a-traiter");
        etatsTitres.put("En fin de circuit", "a-archiver");
        etatsTitres.put("Retournés", "retournes");
        etatsTitres.put("En cours", "en-cours");
        etatsTitres.put("À venir", "a-venir");
        etatsTitres.put("Récupérables", "recuperables");
        etatsTitres.put("En retard", "en-retard");
        etatsTitres.put("Traités", "traites");
        etatsTitres.put("Dossiers en délégation", "dossiers-delegues");
        etatsTitres.put("Toutes les banettes", "no-corbeille");
        etatsTitres.put("Tout i-P arapheur", "no-bureau");*/
        etatsTitres = new LinkedHashMap<String, String>();
        etatsTitres.put("en-preparation", "À transmettre");
        etatsTitres.put("a-traiter", "À traiter");
        etatsTitres.put("a-archiver", "En fin de circuit");
        etatsTitres.put("retournes", "Retournés");
        etatsTitres.put("en-cours", "En cours");
        etatsTitres.put("a-venir", "À venir");
        etatsTitres.put("recuperables", "Récupérables");
        etatsTitres.put("en-retard", "En retard");
        etatsTitres.put("traites", "Traités");
        etatsTitres.put("dossiers-delegues", "Dossiers en délégation");
        etatsTitres.put("no-corbeille", "Toutes les banettes");
        etatsTitres.put("no-bureau", "Tout i-Parapheur");
    }

    /**
     * id du filtre utilisé pour les préférences
     */
    private String id;
    /**
     * Nom du filtre sauvegardé
     */
    private String nom;
    /**
     * Valeurs du filtre
     */
    private String titre;
    private Set<String> types;
    private Set<String> sousTypes;
    private String etat;
    private Date dateDebut;
    private Date dateFin;

    public Filter() {
        this.id = DEFAULT_ID;
        this.nom = DEFAULT_NOM;
        this.etat = DEFAULT_ETAT;
        this.types = new HashSet<String>();
        this.sousTypes = new HashSet<String>();
    }

    public Filter(String id) {
        this.id = id;
        this.nom = DEFAULT_NOM;
        this.etat = DEFAULT_ETAT;
        this.types = new HashSet<String>();
        this.sousTypes = new HashSet<String>();
    }

    public Filter(Filter filter) {
        if (filter.id.equals(DEFAULT_ID) || filter.id.equals(EDIT_FILTER_ID)) {
            this.id = UUID.randomUUID().toString();
        }
        else {
            this.id = filter.id;
        }
        this.nom = filter.nom;
        this.titre = filter.titre;
        this.etat = filter.etat;
        this.types = filter.types;
        this.sousTypes = filter.sousTypes;
        this.dateDebut = filter.dateDebut;
        this.dateFin = filter.dateFin;
    }

    public String getJSONFilter() {
        JSONObject jsonFilter = new JSONObject();
        try {
            // TYPES
            JSONArray jsonTypes = new JSONArray();
            if (types != null) {
                for (String type : types) {
                    jsonTypes.put(new JSONObject().put("ph:typeMetier", type));
                }
            }
            // SOUSTYPES
            JSONArray jsonSousTypes = new JSONArray();
            if (sousTypes != null) {
                for (String sousType : sousTypes) {
                    jsonSousTypes.put(new JSONObject().put("ph:soustypeMetier", sousType));
                }
            }
            //TITRE

            JSONArray jsonTitre = new JSONArray();
            if ((titre != null) && (!titre.trim().isEmpty())) {
                jsonTitre.put(new JSONObject().put("cm:title", "*" + titre.trim() + "*"));
            }

            // FILTRE FINAL
            jsonFilter.put("and", new JSONArray()
                    .put(new JSONObject().put("or", jsonTypes))
                    .put(new JSONObject().put("or", jsonSousTypes))
                    .put(new JSONObject().put("or", jsonTitre))).toString();
        }
        catch (JSONException e) {
            Log.w(Filter.class.getSimpleName(), "Erreur lors de la conversion du filtre", e);
        }
        return jsonFilter.toString();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Filter){
            Filter toCompare = (Filter) o;
            return this.id.equals(toCompare.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString()
    {
        return nom;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public Set<String> getTypes() {
        return types;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }

    public Set<String> getSousTypes() {
        return sousTypes;
    }

    public void setSousTypes(Set<String> sousTypes) {
        this.sousTypes = sousTypes;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(long dateDebut) {
        this.dateDebut = new Date(dateDebut);
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(long dateFin) {
        this.dateFin = new Date(dateFin);
    }
}
