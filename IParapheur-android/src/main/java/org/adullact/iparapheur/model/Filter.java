package org.adullact.iparapheur.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.adullact.iparapheur.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class Filter implements Parcelable {

	public static final String REQUEST_JSON_FILTER_TYPE_METIER = "ph:typeMetier";
	public static final String REQUEST_JSON_FILTER_SOUS_TYPE_METIER = "ph:soustypeMetier";
	public static final String REQUEST_JSON_FILTER_TITLE = "cm:title";
	public static final String REQUEST_JSON_FILTER_AND = "and";
	public static final String REQUEST_JSON_FILTER_OR = "or";

	public static final String DEFAULT_ID = "default-filter";
	public static final String EDIT_FILTER_ID = "edit-filter";
	public static final ArrayList<String> states;
	public static final HashMap<String, String> statesTitles;

	static {
		states = new ArrayList<String>(12);
		states.add("en-preparation");
		states.add("a-traiter");
		states.add("a-archiver");
		states.add("retournes");
		states.add("en-cours");
		states.add("a-venir");
		states.add("recuperables");
		states.add("en-retard");
		states.add("traites");
		states.add("dossiers-delegues");
		states.add("no-corbeille");
		states.add("no-bureau");

        /*statesTitles = new HashMap<String, String>();
		statesTitles.put("À transmettre", "en-preparation");
        statesTitles.put("À traiter", "a-traiter");
        statesTitles.put("En fin de circuit", "a-archiver");
        statesTitles.put("Retournés", "retournes");
        statesTitles.put("En cours", "en-cours");
        statesTitles.put("À venir", "a-venir");
        statesTitles.put("Récupérables", "recuperables");
        statesTitles.put("En retard", "en-retard");
        statesTitles.put("Traités", "traites");
        statesTitles.put("Dossiers en délégation", "dossiers-delegues");
        statesTitles.put("Toutes les banettes", "no-corbeille");
        statesTitles.put("Tout i-P arapheur", "no-bureau");*/
		statesTitles = new LinkedHashMap<String, String>();
		statesTitles.put("en-preparation", "À transmettre");
		statesTitles.put("a-traiter", "À traiter");
		statesTitles.put("a-archiver", "En fin de circuit");
		statesTitles.put("retournes", "Retournés");
		statesTitles.put("en-cours", "En cours");
		statesTitles.put("a-venir", "À venir");
		statesTitles.put("recuperables", "Récupérables");
		statesTitles.put("en-retard", "En retard");
		statesTitles.put("traites", "Traités");
		statesTitles.put("dossiers-delegues", "Dossiers en délégation");
		statesTitles.put("no-corbeille", "Toutes les banettes");
		statesTitles.put("no-bureau", "Tout i-Parapheur");
	}

	private static final String DEFAULT_ETAT = "a-traiter";
	private static final String DEFAULT_NOM = "Dossiers à traiter";
	public static Parcelable.Creator<Filter> CREATOR = new Parcelable.Creator<Filter>() {
		public Filter createFromParcel(Parcel source) {
			return new Filter(source);
		}

		public Filter[] newArray(int size) {
			return new Filter[size];
		}
	};
	private String id;
	/**
	 * Nom du filtre sauvegardé
	 */
	private String name;
	/**
	 * Valeurs du filtre
	 */
	private String title;
	private List<String> types;
	private List<String> subTypes;
	private String state;
	private Date beginDate;
	private Date endDate;

	public Filter() {
		this.id = DEFAULT_ID;
		this.name = DEFAULT_NOM;
		this.state = DEFAULT_ETAT;
		this.types = new ArrayList<String>();
		this.subTypes = new ArrayList<String>();
	}

	public Filter(String id) {
		this.id = id;
		this.name = DEFAULT_NOM;
		this.state = DEFAULT_ETAT;
		this.types = new ArrayList<String>();
		this.subTypes = new ArrayList<String>();
	}

	public Filter(Filter filter) {
		if (filter.id.equals(DEFAULT_ID) || filter.id.equals(EDIT_FILTER_ID)) {
			this.id = UUID.randomUUID().toString();
		}
		else {
			this.id = filter.id;
		}
		this.name = filter.name;
		this.title = filter.title;
		this.state = filter.state;
		this.types = filter.types;
		this.subTypes = filter.subTypes;
		this.beginDate = filter.beginDate;
		this.endDate = filter.endDate;
	}

	private Filter(Parcel in) {
		this.id = in.readString();
		this.name = in.readString();
		this.title = in.readString();
		this.types = new ArrayList<String>();
		in.readList(this.types, String.class.getClassLoader());
		this.subTypes = new ArrayList<String>();
		in.readList(this.subTypes, String.class.getClassLoader());
		this.state = in.readString();
		long tmpDateDebut = in.readLong();
		this.beginDate = tmpDateDebut == -1 ? null : new Date(tmpDateDebut);
		long tmpDateFin = in.readLong();
		this.endDate = tmpDateFin == -1 ? null : new Date(tmpDateFin);
	}

	public String getJSONFilter() {

		JSONObject jsonFilter = new JSONObject();
		try {

			// TYPES
			JSONArray jsonTypes = new JSONArray();
			if (types != null) {
				for (String type : types) {
					jsonTypes.put(new JSONObject().put(REQUEST_JSON_FILTER_TYPE_METIER, StringUtils.urlEncode(type)));
				}
			}
			// SOUSTYPES
			JSONArray jsonSousTypes = new JSONArray();
			if (subTypes != null) {
				for (String sousType : subTypes) {
					jsonSousTypes.put(new JSONObject().put(REQUEST_JSON_FILTER_SOUS_TYPE_METIER, StringUtils.urlEncode(sousType)));
				}
			}
			//TITRE

			JSONArray jsonTitre = new JSONArray();
			if ((title != null) && (!title.trim().isEmpty())) {
				jsonTitre.put(new JSONObject().put(REQUEST_JSON_FILTER_TITLE, "*" + title.trim() + "*"));
			}

			// FILTRE FINAL
			jsonFilter.put(REQUEST_JSON_FILTER_AND, new JSONArray().
					put(new JSONObject().put(REQUEST_JSON_FILTER_OR, jsonTypes)).
					put(new JSONObject().put(REQUEST_JSON_FILTER_OR, jsonSousTypes)).
					put(new JSONObject().put(REQUEST_JSON_FILTER_OR, jsonTitre)));

		}
		catch (JSONException e) {
			//Log.w(Filter.class.getSimpleName(), "Erreur lors de la conversion du filtre", e);
		}
		return jsonFilter.toString();
	}

	// <editor-fold desc="Setters / Getters">

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getTypes() {
		return types;
	}

	public void setTypes(List<String> types) {
		this.types = types;
	}

	public List<String> getSubTypes() {
		return subTypes;
	}

	public void setSubTypes(List<String> subTypes) {
		this.subTypes = subTypes;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(long beginDate) {
		this.beginDate = new Date(beginDate);
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(long endDate) {
		this.endDate = new Date(endDate);
	}

	// </editor-fold desc="Setters / Getters">

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.id);
		dest.writeString(this.name);
		dest.writeString(this.title);
		dest.writeList(this.types);
		dest.writeList(this.subTypes);
		dest.writeString(this.state);
		dest.writeLong(beginDate != null ? beginDate.getTime() : -1);
		dest.writeLong(endDate != null ? endDate.getTime() : -1);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Filter) {
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
	public String toString() {
		return name;
	}
}
