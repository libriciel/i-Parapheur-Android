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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class ParapheurType {

	@SerializedName("id") private String mName;
	@SerializedName("sousTypes") private List<String> mSubTypes;

	public ParapheurType() {}

	/**
	 * Static parser, useful for Unit tests
	 *
	 * @param jsonArrayString data as a Json array, serialized with some {@link org.json.JSONArray#toString}.
	 * @param gson            passed statically to prevent re-creating it.
	 * @coveredInLocalUnitTest ModelTest
	 */
	public static @Nullable List<ParapheurType> fromJsonArray(@NonNull String jsonArrayString, @NonNull Gson gson) {

		Type typologyType = new TypeToken<ArrayList<ParapheurType>>() {}.getType();

		try {
			return gson.fromJson(jsonArrayString, typologyType);
		}
		catch (JsonSyntaxException e) {
			return null;
		}
	}

	// <editor-fold desc="Setters / Getters">

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public List<String> getSubTypes() {
		return mSubTypes;
	}

	public void setSubTypes(List<String> subTypes) {
		mSubTypes = subTypes;
	}

	// </editor-fold desc="Setters / Getters">

	@Override public String toString() {
		return "{Type id=" + mName + " sousType=" + mSubTypes + "}";
	}
}
