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
import java.util.List;
import java.util.UUID;


public class Filter implements Parcelable {

	public static final String REQUEST_JSON_FILTER_TYPE_METIER = "ph:typeMetier";
	public static final String REQUEST_JSON_FILTER_SOUS_TYPE_METIER = "ph:soustypeMetier";
	public static final String REQUEST_JSON_FILTER_TITLE = "cm:title";
	public static final String REQUEST_JSON_FILTER_AND = "and";
	public static final String REQUEST_JSON_FILTER_OR = "or";
	public static final String EDIT_FILTER_ID = "edit-filter";

	private static final State DEFAULT_STATE = State.A_TRAITER;
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
	private State mState;
	private Date mBeginDate;
	private Date mEndDate;

	public Filter() {
		mId = UUID.randomUUID().toString();
		mName = null;
		mState = DEFAULT_STATE;
		mTypeList = new ArrayList<>();
		mSubTypeList = new ArrayList<>();
	}

	public Filter(String id) {
		mId = id;
		mName = null;
		mState = DEFAULT_STATE;
		mTypeList = new ArrayList<>();
		mSubTypeList = new ArrayList<>();
	}

	private Filter(Parcel in) {
		mId = in.readString();
		mName = in.readString();
		mTitle = in.readString();
		mTypeList = new ArrayList<>();
		in.readList(mTypeList, String.class.getClassLoader());
		mSubTypeList = new ArrayList<>();
		in.readList(mSubTypeList, String.class.getClassLoader());
		mState = State.values()[in.readInt()];
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

	public State getState() {
		return mState;
	}

	public void setState(State state) {
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
		dest.writeInt(mState.ordinal());
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
