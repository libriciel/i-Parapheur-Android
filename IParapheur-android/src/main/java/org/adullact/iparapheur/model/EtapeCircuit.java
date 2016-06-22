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
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import org.adullact.iparapheur.utils.StringUtils;

import java.util.Date;


public class EtapeCircuit implements Parcelable {

	public static Parcelable.Creator<EtapeCircuit> CREATOR = new Parcelable.Creator<EtapeCircuit>() {

		public EtapeCircuit createFromParcel(Parcel source) {
			return new EtapeCircuit(source);
		}

		public EtapeCircuit[] newArray(int size) {
			return new EtapeCircuit[size];
		}
	};

	@SerializedName("dateValidation") private Date dateValidation;
	@SerializedName("approved") private boolean isApproved;
	@SerializedName("rejected") private boolean isRejected;
	@SerializedName("parapheurName") private String bureauName;
	@SerializedName("signataire") private String signataire;
	@SerializedName("actionDemandee") private Action action;
	@SerializedName("annotPub") private String publicAnnotation;

	public EtapeCircuit() {}

	// <editor-fold desc="Setters / Getters">

	public EtapeCircuit(String dateValidation, boolean isApproved, boolean isRejected, String bureauName, String signataire, String action,
						String publicAnnotation) {

		this.dateValidation = TextUtils.isEmpty(dateValidation) ? null : StringUtils.parseIso8601Date(dateValidation);
		this.isApproved = isApproved;
		this.isRejected = isRejected;
		this.bureauName = bureauName;
		this.signataire = signataire;
		this.action = Action.valueOf(action);
		this.publicAnnotation = publicAnnotation;
	}

	private EtapeCircuit(Parcel in) {
		long tmpDateValidation = in.readLong();
		this.dateValidation = tmpDateValidation == -1 ? null : new Date(tmpDateValidation);
		this.isApproved = in.readByte() != 0;
		this.isRejected = in.readByte() != 0;
		this.bureauName = in.readString();
		this.signataire = in.readString();
		int tmpAction = in.readInt();
		this.action = tmpAction == -1 ? null : Action.values()[tmpAction];
		this.publicAnnotation = in.readString();
	}

	public Date getDateValidation() {
		return dateValidation;
	}

	public boolean isApproved() {
		return isApproved;
	}

	public boolean isRejected() {
		return isRejected;
	}

	public String getBureauName() {
		return bureauName;
	}

	public String getSignataire() {
		return signataire;
	}

	public Action getAction() {
		return action;
	}

	// </editor-fold desc="Setters / Getters">

	@Override public String toString() {
		return "{EtapeCircuit dateValidation=" + dateValidation + " isApproved=" + isApproved + " isRejected=" + isRejected + " bureauName=" + bureauName //
				+ " signataire=" + signataire + " action=" + action + " publicAnnot=" + publicAnnotation + "}";
	}

	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(dateValidation != null ? dateValidation.getTime() : -1);
		dest.writeByte(isApproved ? (byte) 1 : (byte) 0);
		dest.writeByte(isRejected ? (byte) 1 : (byte) 0);
		dest.writeString(this.bureauName);
		dest.writeString(this.signataire);
		dest.writeInt(this.action == null ? -1 : this.action.ordinal());
		dest.writeString(this.publicAnnotation);
	}
}