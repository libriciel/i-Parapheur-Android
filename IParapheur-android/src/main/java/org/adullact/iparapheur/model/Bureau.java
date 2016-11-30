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
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@DatabaseTable(tableName = "Desk")
public class Bureau {

	@DatabaseField(columnName = "Id", id = true, index = true)  //
	@SerializedName(value = "id", alternate = {"nodeRef"})  //
	private String mId;

	@DatabaseField(columnName = "Title", canBeNull = false, defaultValue = "")  //
	@SerializedName("name")  //
	private String mTitle;

	@DatabaseField(columnName = "Todo", defaultValue = "0")  //
	@SerializedName("a-traiter") //
	private int mTodoCount;

	@DatabaseField(columnName = "Late", defaultValue = "0")  //
	@SerializedName("en-retard")  //
	private int mLateCount;

	@DatabaseField(columnName = "Sync")  //
	private Date mSyncDate;

	@ForeignCollectionField(columnName = "Folders")  //
	private ForeignCollection<Dossier> mChildrenDossiers;

	// <editor-fold desc="Static utils">

	public static @Nullable Bureau findInList(@Nullable List<Bureau> bureauList, @Nullable String bureauId) {

		// Default case

		if ((bureauList == null) || (bureauId == null))
			return null;

		//

		for (Bureau bureau : bureauList)
			if (bureau != null)
				if (TextUtils.equals(bureau.getId(), bureauId))
					return bureau;

		return null;
	}

	// </editor-fold desc="Static utils">

	/**
	 * Static parser, useful for Unit tests
	 *
	 * @param jsonArrayString data as a Json array, serialized with some {@link org.json.JSONArray#toString}.
	 * @param gson            passed statically to prevent re-creating it.
	 */
	public static @Nullable List<Bureau> fromJsonArray(@NonNull String jsonArrayString, @NonNull Gson gson) {

		Type typologyType = new TypeToken<ArrayList<Bureau>>() {}.getType();

		try {
			return gson.fromJson(jsonArrayString, typologyType);
		}
		catch (JsonSyntaxException e) {
			return null;
		}
	}

	public Bureau(String id, String title, int todo, int late) {

		if (id.contains("workspace://SpacesStore/"))
			id = id.substring("workspace://SpacesStore/".length());

		mId = id;
		mTitle = title;
		mTodoCount = todo;
		mLateCount = late;
		mSyncDate = null;
	}

	public Bureau() {}

	// <editor-fold desc="Setters / Getters">

	public String getId() {
		return mId;
	}

	public String getTitle() {
		return mTitle;
	}

	public int getTodoCount() {
		return mTodoCount;
	}

	public int getLateCount() {
		return mLateCount;
	}

	public Date getSyncDate() {
		return mSyncDate;
	}

	public void setSyncDate(Date date) {
		mSyncDate = date;
	}

	public ForeignCollection<Dossier> getChildrenDossiers() {
		return mChildrenDossiers;
	}

	public void setChildrenDossiers(ForeignCollection<Dossier> childrenDossiers) {
		mChildrenDossiers = childrenDossiers;
	}

	// </editor-fold desc="Setters / Getters">

	@Override public String toString() {
		return "{Bureau id=" + mId + " title=" + mTitle + " todo=" + mTodoCount + " late=" + mLateCount + " sync=" + mSyncDate + "}";
	}

	@Override public boolean equals(Object o) {
		return (o != null) && (o instanceof Bureau) && TextUtils.equals(mId, ((Bureau) o).getId());
	}

}
