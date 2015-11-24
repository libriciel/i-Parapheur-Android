package org.adullact.iparapheur.model;

import android.os.Parcel;
import android.os.Parcelable;

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

	private final Date dateValidation;
	private final boolean isApproved;
	private final boolean isRejected;
	private final String bureauName;
	private final String signataire;
	private final Action action;
	private final String publicAnnotation;

	// <editor-fold desc="Setters / Getters">

	public EtapeCircuit(Long dateValidation, boolean isApproved, boolean isRejected, String bureauName, String signataire, String action, String publicAnnotation) {
		this.dateValidation = new Date(dateValidation);
		this.isApproved = isApproved;
		this.isRejected = isRejected;
		this.bureauName = bureauName;
		this.signataire = signataire == null ? "" : signataire;
		this.action = Action.valueOf(action);
		this.publicAnnotation = publicAnnotation;
	}

	public EtapeCircuit(String dateValidation, boolean isApproved, boolean isRejected, String bureauName, String signataire, String action, String publicAnnotation) {
		this.dateValidation = StringUtils.parseISO8601Date(dateValidation);
		this.isApproved = isApproved;
		this.isRejected = isRejected;
		this.bureauName = bureauName;
		this.signataire = signataire == null ? "" : signataire;
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

	public String getPublicAnnotation() {
		return publicAnnotation;
	}

	// </editor-fold desc="Setters / Getters">

	@Override public String toString() {
		return bureauName;
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