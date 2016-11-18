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

import java.util.List;


public class Circuit {

	@SerializedName("sigFormat") private String mSigFormat;
	@SerializedName("isDigitalSignatureMandatory") private boolean mIsDigitalSignatureMandatory;
	@SerializedName("hasSelectionScript") private boolean mHasSelectionScript;
	@SerializedName("etapes") private List<EtapeCircuit> mEtapeCircuitList;

	public Circuit(List<EtapeCircuit> etapeCircuitList, String sigFormat, boolean isDigitalSignatureMandatory, boolean hasSelectionScript) {
		mEtapeCircuitList = etapeCircuitList;
		mSigFormat = sigFormat;
		mIsDigitalSignatureMandatory = isDigitalSignatureMandatory;
		mHasSelectionScript = hasSelectionScript;
	}

	/**
	 * Static parser, useful for Unit tests
	 *
	 * @param jsonArrayString data as a Json array, serialized with some {@link org.json.JSONArray#toString}.
	 * @param gson            passed statically to prevent re-creating it.
	 * @coveredInLocalUnitTest Bureau
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

	public boolean hasSelectionScript() {
		return mHasSelectionScript;
	}

	// </editor-fold desc="Setters / Getters">

	@Override public String toString() {
		return "{Circuit etapeCircuitList=" + mEtapeCircuitList + " sigFormat=" + mSigFormat + " isDigitalsignMandatory=" + isDigitalSignatureMandatory()  //
				+ "hasSelectScript=" + mHasSelectionScript + "}";
	}

}