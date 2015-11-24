package org.adullact.iparapheur.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.List;


public class Circuit implements Parcelable {

	public static Parcelable.Creator<Circuit> CREATOR = new Parcelable.Creator<Circuit>() {

		public Circuit createFromParcel(Parcel source) {
			return new Circuit(source);
		}

		public Circuit[] newArray(int size) {
			return new Circuit[size];
		}
	};

	private List<EtapeCircuit> mEtapeCircuitList;
	private String mSigFormat;
	private boolean mIsDigitalSignatureMandatory;
	private boolean mHasSelectionScript;

	public Circuit(List<EtapeCircuit> etapeCircuitList, String sigFormat, boolean isDigitalSignatureMandatory, boolean hasSelectionScript) {
		mEtapeCircuitList = etapeCircuitList;
		mSigFormat = sigFormat;
		mIsDigitalSignatureMandatory = isDigitalSignatureMandatory;
		mHasSelectionScript = hasSelectionScript;
	}

	private Circuit(Parcel in) {
		EtapeCircuit[] etapesParcelableArray = (EtapeCircuit[]) in.readParcelableArray(EtapeCircuit.class.getClassLoader());
		mEtapeCircuitList = Arrays.asList(etapesParcelableArray);
		mSigFormat = in.readString();
		mIsDigitalSignatureMandatory = in.readByte() != 0;
		mHasSelectionScript = in.readByte() != 0;
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

	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel dest, int flags) {

		Parcelable[] parcelableArray = new Parcelable[mEtapeCircuitList.size()];
		dest.writeParcelableArray(mEtapeCircuitList.toArray(parcelableArray), 0);
		dest.writeString(mSigFormat);
		dest.writeByte(mIsDigitalSignatureMandatory ? (byte) 1 : (byte) 0);
		dest.writeByte(mHasSelectionScript ? (byte) 1 : (byte) 0);
	}

}