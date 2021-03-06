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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.adullact.iparapheur.utils.ActionUtils;
import org.adullact.iparapheur.utils.DossierUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@DatabaseTable(tableName = "Folder")
public class Dossier {

	public static final String DB_FIELD_ID = "Id";
	private static final String DB_FIELD_NAME = "Name";
	private static final String DB_FIELD_ACTION_ASKED = "ActionAsked";
	private static final String DB_FIELD_TYPE = "Type";
	private static final String DB_FIELD_SUBTYPE = "SubType";
	private static final String DB_FIELD_CREATION_DATE = "CreationDate";
	private static final String DB_FIELD_LATE_DATE = "LateDate";
	private static final String DB_FIELD_ACTIONS = "Actions";
	private static final String DB_FIELD_IS_PAPER_SIGN = "IsPaperSign";
	private static final String DB_FIELD_DOCUMENTS = "Documents";
	private static final String DB_FIELD_SYNC = "Sync";
	private static final String DB_FIELD_DESK = "Desk";

	@DatabaseField(columnName = DB_FIELD_ID, id = true, index = true)  //
	@SerializedName("id")  //
	private String mId;

	@DatabaseField(columnName = DB_FIELD_NAME, canBeNull = false, defaultValue = "")  //
	@SerializedName("title")  //
	private String mName;

	@DatabaseField(columnName = DB_FIELD_ACTION_ASKED, dataType = DataType.ENUM_STRING, canBeNull = false, defaultValue = "VISA")  //
	@SerializedName("actionDemandee")  //
	private Action mActionDemandee;

	@DatabaseField(columnName = DB_FIELD_TYPE)  //
	@SerializedName("type")  //
	private String mType;

	@DatabaseField(columnName = DB_FIELD_SUBTYPE)  //
	@SerializedName("sousType")  //
	private String mSousType;

	@DatabaseField(columnName = DB_FIELD_CREATION_DATE)  //
	@SerializedName("dateEmission")  //
	private Date mDateCreation;

	@DatabaseField(columnName = DB_FIELD_LATE_DATE)  //
	@SerializedName("dateLimite")  //
	private Date mDateLimite;

	@DatabaseField(columnName = DB_FIELD_ACTIONS, dataType = DataType.SERIALIZABLE)  //
	@SerializedName("actions")  //
	private HashSet<Action> mActions;

	@DatabaseField(columnName = DB_FIELD_IS_PAPER_SIGN)  //
	@SerializedName("isSignPapier")  //
	private boolean mIsSignPapier;

	@SerializedName("documents")  //
	private List<Document> mDocumentList = new ArrayList<>();

	@ForeignCollectionField(columnName = DB_FIELD_DOCUMENTS)  //
	private transient ForeignCollection<Document> mChildrenDocuments;

	@DatabaseField(columnName = DB_FIELD_SYNC)  //
	private Date mSyncDate;

	@DatabaseField(columnName = DB_FIELD_DESK, foreign = true, foreignAutoRefresh = true)  //
	private transient Bureau mParent;

	private Circuit mCircuit;

	public Dossier() {}

	public Dossier(String id, String name, Action actionDemandee, HashSet<Action> actions, String type, String sousType, Date dateCreation, Date dateLimite,
				   boolean isSignPapier) {
		mId = id;
		mName = name;
		mActionDemandee = actionDemandee;
		mActions = actions;
		mType = type;
		mSousType = sousType;
		mDateCreation = dateCreation;
		mDateLimite = dateLimite;
		mIsSignPapier = isSignPapier;
	}

	/**
	 * Static parser, useful for Unit tests
	 *
	 * @param jsonArrayString data as a Json array, serialized with some {@link org.json.JSONArray#toString}.
	 * @param gson            passed statically to prevent re-creating it.
	 */
	public static @Nullable List<Dossier> fromJsonArray(@NonNull String jsonArrayString, @NonNull Gson gson) {

		Type listDossierType = new TypeToken<ArrayList<Dossier>>() {}.getType();

		try {
			ArrayList<Dossier> dossiersParsed = gson.fromJson(jsonArrayString, listDossierType);

			// Fix default value on parse.
			// There is no easy way (@annotation) to do it with Gson,
			// So we're doing it here instead of overriding everything.
			for (Dossier dossier : dossiersParsed)
				ActionUtils.fixActions(dossier);

			return dossiersParsed;
		}
		catch (JsonSyntaxException e) {
			return null;
		}
	}

	/**
	 * Static parser, useful for Unit tests
	 *
	 * @param jsonObjectString data as a Json array, serialized with some {@link org.json.JSONArray#toString}.
	 * @param gson             passed statically to prevent re-creating it.
	 */
	public static @Nullable Dossier fromJsonObject(@NonNull String jsonObjectString, @NonNull Gson gson) {

		try {
			Dossier dossierParsed = gson.fromJson(jsonObjectString, Dossier.class);

			// Fix default value on parse.
			// There is no easy way (@annotation) to do it with Gson,
			// So we're doing it here instead of overriding everything.
			if (dossierParsed != null)
				ActionUtils.fixActions(dossierParsed);

			return dossierParsed;
		}
		catch (JsonSyntaxException e) {
			return null;
		}
	}

	// <editor-fold desc="Setters / Getters">

	public String getId() {
		return mId;
	}

	public String getName() {
		return mName;
	}

	public Set<Action> getActions() {
		return mActions;
	}

	public void setActions(@NonNull HashSet<Action> actions) {
		mActions = actions;
	}

	public String getType() {
		return mType;
	}

	public String getSousType() {
		return mSousType;
	}

	public Circuit getCircuit() {
		return mCircuit;
	}

	public void setCircuit(Circuit circuit) {
		mCircuit = circuit;
	}

	public Action getActionDemandee() {
		return mActionDemandee;
	}

	public void setActionDemandee(@NonNull Action action) {
		mActionDemandee = action;
	}

	public boolean isSignPapier() {
		return mIsSignPapier;
	}

	public List<Document> getDocumentList() {
		return mDocumentList;
	}

	public Date getDateLimite() {
		return mDateLimite;
	}

	public Date getDateCreation() {
		return mDateCreation;
	}

	public void setDocumentList(@NonNull List<Document> documentList) {
		mDocumentList.addAll(documentList);
	}

	public Date getSyncDate() {
		return mSyncDate;
	}

	public void setSyncDate(Date date) {
		mSyncDate = date;
	}

	public Bureau getParent() {
		return mParent;
	}

	public void setParent(Bureau parent) {
		mParent = parent;
	}

	public ForeignCollection<Document> getChildrenDocuments() {
		return mChildrenDocuments;
	}

	public void setChildrenDocuments(ForeignCollection<Document> childrenDocuments) {
		mChildrenDocuments = childrenDocuments;
	}

	// </editor-fold desc="Setters / Getters">

	@Override public boolean equals(Object o) {

		if (o == null)
			return false;

		if (o instanceof Dossier)
			return TextUtils.equals(mId, ((Dossier) o).getId());

		else if (o instanceof String)
			return TextUtils.equals(mId, (String) o);

		return false;
	}

	@Override public String toString() {
		return "{Dossier id=" + mId + " name=" + mName + " actionsDemandees=" + mActionDemandee + " type=" + mType + " subType=" + mSousType     //
				+ " dateCrea=" + mDateCreation + " dateLimite=" + mDateLimite + " docs=" + mDocumentList                                         //
				+ " actions=" + (mActions == null ? "null" : mActions.size()) + " circuit=" + mCircuit + " isSignPapier=" + mIsSignPapier + "}";
	}

	@Override public int hashCode() {
		return (mId != null) ? mId.hashCode() : -1;
	}

}
