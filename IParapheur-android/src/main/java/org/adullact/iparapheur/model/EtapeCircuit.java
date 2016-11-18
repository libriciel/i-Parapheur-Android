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

import org.adullact.iparapheur.utils.StringUtils;

import java.util.Date;


public class EtapeCircuit {

	@SerializedName("dateValidation") private Date mDateValidation;
	@SerializedName("approved") private boolean mIsApproved;
	@SerializedName("rejected") private boolean mIsRejected;
	@SerializedName("parapheurName") private String mBureauName;
	@SerializedName("signataire") private String mSignataire;
	@SerializedName("actionDemandee") private Action mAction;
	@SerializedName("annotPub") private String mPublicAnnotation;

	// <editor-fold desc="Setters / Getters">

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