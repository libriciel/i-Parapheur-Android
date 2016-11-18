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