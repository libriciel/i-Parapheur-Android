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

import org.adullact.iparapheur.utils.StringUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class EtapeCircuit {

	@SerializedName("dateValidation") private Date mDateValidation;
	@SerializedName("approved") private boolean mIsApproved;
	@SerializedName("rejected") private boolean mIsRejected;
	@SerializedName("parapheurName") private String mBureauName;
	@SerializedName("signataire") private String mSignataire;
	@SerializedName("actionDemandee") private Action mAction;
	@SerializedName("annotPub") private String mPublicAnnotation;

	public EtapeCircuit(String dateValidation, boolean isApproved, boolean isRejected, String bureauName, String signataire, String action,
						String publicAnnotation) {

		mDateValidation = StringUtils.parseIso8601Date(dateValidation);
		mIsApproved = isApproved;
		mIsRejected = isRejected;
		mBureauName = bureauName;
		mSignataire = signataire;
		mAction = (action != null) ? Action.valueOf(action) : Action.VISA;
		mPublicAnnotation = publicAnnotation;
	}

	/**
	 * Static parser, useful for Unit tests
	 *
	 * @param jsonArrayString data as a Json array, serialized with some {@link org.json.JSONArray#toString}.
	 * @param gson            passed statically to prevent re-creating it.
	 */
	public static @Nullable List<EtapeCircuit> fromJsonArray(@NonNull String jsonArrayString, @NonNull Gson gson) {

		Type typologyType = new TypeToken<ArrayList<EtapeCircuit>>() {}.getType();

		try {
			List<EtapeCircuit> etapeCircuitList = gson.fromJson(jsonArrayString, typologyType);

			// Fix default value on parse.
			// There is no easy way (@annotation) to do it with Gson,
			// So we're doing it here instead of overriding everything.
			for (EtapeCircuit etapeCircuit : etapeCircuitList)
				if (etapeCircuit.getAction() == null)
					etapeCircuit.setAction(Action.VISA);

			return etapeCircuitList;
		}
		catch (JsonSyntaxException e) {
			return null;
		}
	}

	// <editor-fold desc="Setters / Getters">

	public Date getDateValidation() {
		return mDateValidation;
	}

	public boolean isApproved() {
		return mIsApproved;
	}

	public boolean isRejected() {
		return mIsRejected;
	}

	public String getBureauName() {
		return mBureauName;
	}

	public String getSignataire() {
		return mSignataire;
	}

	public Action getAction() {
		return mAction;
	}

	public void setAction(Action action) {
		mAction = action;
	}

	// </editor-fold desc="Setters / Getters">

	@Override public String toString() {
		return "{EtapeCircuit dateValidation=" + mDateValidation + " isApproved=" + mIsApproved + " isRejected=" + mIsRejected + //
				" bureauName=" + mBureauName + " signataire=" + mSignataire + " action=" + mAction + " publicAnnot=" + mPublicAnnotation + "}";
	}
}