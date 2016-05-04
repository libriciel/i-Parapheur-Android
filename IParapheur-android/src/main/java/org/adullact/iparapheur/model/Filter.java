/*
 * <p>iParapheur Android<br/>
 * Copyright (C) 2016 Adullact-Projet.</p>
 *
 * <p>This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.</p>
 *
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.</p>
 *
 * <p>You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.</p>
 */
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
		states = new ArrayList<>();
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
		statesTitles = new LinkedHashMap<>();
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

	private String mId;
	private String mName;

	// Filter values
	private String mTitle;
	private List<String> mTypeList;
	private List<String> mSubTypeList;
	private String mState;
	private Date mBeginDate;
	private Date mEndDate;

	public Filter() {
		mId = DEFAULT_ID;
		mName = DEFAULT_NOM;
		mState = DEFAULT_ETAT;
		mTypeList = new ArrayList<>();
		mSubTypeList = new ArrayList<>();
	}

	public Filter(String id) {
		mId = id;
		mName = DEFAULT_NOM;
		mState = DEFAULT_ETAT;
		mTypeList = new ArrayList<>();
		mSubTypeList = new ArrayList<>();
	}

//	public Filter(Filter filter) {
//		if (filter.mId.equals(DEFAULT_ID) || filter.mId.equals(EDIT_FILTER_ID)) {
//			mId = UUID.randomUUID().toString();
//		}
//		else {
//			mId = filter.mId;
//		}
//		mName = filter.mName;
//		mTitle = filter.mTitle;
//		mState = filter.mState;
//		mTypeList = filter.mTypeList;
//		mSubTypeList = filter.mSubTypeList;
//		mBeginDate = filter.mBeginDate;
//		mEndDate = filter.mEndDate;
//	}

	private Filter(Parcel in) {
		mId = in.readString();
		mName = in.readString();
		mTitle = in.readString();
		mTypeList = new ArrayList<>();
		in.readList(mTypeList, String.class.getClassLoader());
		mSubTypeList = new ArrayList<>();
		in.readList(mSubTypeList, String.class.getClassLoader());
		mState = in.readString();
		long tmpDateDebut = in.readLong();
		mBeginDate = tmpDateDebut == -1 ? null : new Date(tmpDateDebut);
		long tmpDateFin = in.readLong();
		mEndDate = tmpDateFin == -1 ? null : new Date(tmpDateFin);
	}

	public String getJSONFilter() {

		JSONObject jsonFilter = new JSONObject();
		try {

			// TYPES
			JSONArray jsonTypes = new JSONArray();
			if (mTypeList != null) {
				for (String type : mTypeList) {
					jsonTypes.put(new JSONObject().put(REQUEST_JSON_FILTER_TYPE_METIER, StringUtils.urlEncode(type)));
				}
			}
			// SOUSTYPES
			JSONArray jsonSousTypes = new JSONArray();
			if (mSubTypeList != null) {
				for (String sousType : mSubTypeList) {
					jsonSousTypes.put(new JSONObject().put(REQUEST_JSON_FILTER_SOUS_TYPE_METIER, StringUtils.urlEncode(sousType)));
				}
			}
			//TITRE

			JSONArray jsonTitre = new JSONArray();
			if ((mTitle != null) && (!mTitle.trim().isEmpty())) {
				jsonTitre.put(new JSONObject().put(REQUEST_JSON_FILTER_TITLE, "*" + mTitle.trim() + "*"));
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
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public List<String> getTypeList() {
		return mTypeList;
	}

	public void setTypeList(List<String> typeList) {
		mTypeList = typeList;
	}

	public List<String> getSubTypeList() {
		return mSubTypeList;
	}

	public void setSubTypeList(List<String> subTypeList) {
		mSubTypeList = subTypeList;
	}

	public String getState() {
		return mState;
	}

	public void setState(String state) {
		mState = state;
	}

	public Date getBeginDate() {
		return mBeginDate;
	}

	public void setBeginDate(long beginDate) {
		mBeginDate = new Date(beginDate);
	}

	public Date getEndDate() {
		return mEndDate;
	}

	public void setEndDate(long endDate) {
		mEndDate = new Date(endDate);
	}

	// </editor-fold desc="Setters / Getters">

	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mId);
		dest.writeString(mName);
		dest.writeString(mTitle);
		dest.writeList(mTypeList);
		dest.writeList(mSubTypeList);
		dest.writeString(mState);
		dest.writeLong(mBeginDate != null ? mBeginDate.getTime() : -1);
		dest.writeLong(mEndDate != null ? mEndDate.getTime() : -1);
	}

	@Override public boolean equals(Object o) {
		if (o instanceof Filter) {
			Filter toCompare = (Filter) o;
			return mId.equals(toCompare.mId);
		}
		return false;
	}

	@Override public int hashCode() {
		return mId.hashCode();
	}

	@Override public String toString() {
		return "{Filter name=" + mName + "}";
	}
}
