/*
 * iParapheur Android
 * Copyright (C) 2016-2019 Libriciel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.adullact.iparapheur.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class Circuit {

	@SerializedName("sigFormat") private String mSigFormat;
	@SerializedName("isDigitalSignatureMandatory") private boolean mIsDigitalSignatureMandatory;
	@SerializedName("etapes") private List<EtapeCircuit> mEtapeCircuitList;

	public Circuit(List<EtapeCircuit> etapeCircuitList, String sigFormat, boolean isDigitalSignatureMandatory) {
		mEtapeCircuitList = etapeCircuitList;
		mSigFormat = sigFormat;
		mIsDigitalSignatureMandatory = isDigitalSignatureMandatory;
	}

	/**
	 * Static parser, useful for Unit tests
	 *
	 * @param jsonArrayString data as a Json array, serialized with some {@link org.json.JSONArray#toString}.
	 * @param gson            passed statically to prevent re-creating it.
	 */
	public static @Nullable Circuit fromJsonObject(@NonNull String jsonArrayString, @NonNull Gson gson) {

		try {
			Circuit circuit = gson.fromJson(jsonArrayString, Circuit.class);

			// Fix default value on parse.
			// There is no easy way (@annotation) to do it with Gson,
			// So we're doing it here instead of overriding everything.
			for (EtapeCircuit etape : circuit.getEtapeCircuitList())
				if (etape.getAction() == null)
					etape.setAction(Action.VISA);

			return circuit;
		}
		catch (JsonSyntaxException e) {
			return null;
		}
	}

	// <editor-fold desc="Setters / Getters">

	public List<EtapeCircuit> getEtapeCircuitList() {
		return mEtapeCircuitList;
	}

	public String getSigFormat() {
		return mSigFormat;
	}

	public boolean isDigitalSignatureMandatory() {
		return mIsDigitalSignatureMandatory;
	}

	// </editor-fold desc="Setters / Getters">

	@Override public String toString() {
		return "{Circuit etapeList=" + mEtapeCircuitList + " sigFormat=" + mSigFormat + " isDigitalSignMandatory=" + isDigitalSignatureMandatory() + "}";
	}

}