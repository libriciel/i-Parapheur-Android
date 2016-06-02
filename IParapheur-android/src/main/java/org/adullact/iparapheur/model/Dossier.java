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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Dossier implements Parcelable {

	public static Creator<Dossier> CREATOR = new Creator<Dossier>() {

		public Dossier createFromParcel(Parcel source) {
			return new Dossier(source);
		}

		public Dossier[] newArray(int size) {
			return new Dossier[size];
		}
	};

	private final String mId;
	private final String mName;
	private final Action mActionDemandee;
	private final String mType;
	private final String mSousType;
	private final Date mDateCreation;
	private final Date mDateLimite;
	private final List<Document> mMainDocuments = new ArrayList<>();
	private final List<Document> mAnnexes = new ArrayList<>();
	private Set<Action> mActions;
	private Circuit mCircuit;
	private boolean mIsSignPapier;

	public Dossier(String id, String name, Action actionDemandee, Set<Action> actions, String type, String sousType, Date dateCreation, Date dateLimite,
				   boolean isSignPapier) {
		mId = id;
		mName = name;
		mActionDemandee = actionDemandee;
		mActions = actions;
		mType = type;
		mSousType = sousType;
		mDateCreation = dateCreation;
		mDateLimite = dateLimite;
		mIsSignPapier = isSignPapier;
	}

	private Dossier(Parcel in) {
		mId = in.readString();
		mName = in.readString();
		int tmpActionDemandee = in.readInt();
		mActionDemandee = tmpActionDemandee == -1 ? null : Action.values()[tmpActionDemandee];

		List<Action> actions = new ArrayList<>();
		in.readTypedList(actions, Action.CREATOR);
		mActions = new HashSet<>(actions);

		mType = in.readString();
		mSousType = in.readString();
		long tmpDateCreation = in.readLong();
		mDateCreation = tmpDateCreation == -1 ? null : new Date(tmpDateCreation);
		long tmpDateLimite = in.readLong();
		mDateLimite = tmpDateLimite == -1 ? null : new Date(tmpDateLimite);
		in.readTypedList(mMainDocuments, Document.CREATOR);
		in.readTypedList(mAnnexes, Document.CREATOR);
		mCircuit = in.readParcelable(Circuit.class.getClassLoader());
		mIsSignPapier = in.readByte() != 0;
	}

	// <editor-fold desc="Setters / Getters">

	public String getId() {
		return mId;
	}

	public String getName() {
		return mName;
	}

	public Set<Action> getActions() {
		return mActions;
	}

	public String getType() {
		return mType;
	}

	public String getSousType() {
		return mSousType;
	}

	public String getDateCreation() {
		return DateFormat.getDateInstance().format(mDateCreation);
	}

	public String getDateLimite() {
		return (mDateLimite == null) ? "" : DateFormat.getDateInstance().format(mDateLimite);
	}

	public List<Document> getMainDocuments() {
		return mMainDocuments;
	}

	public List<Document> getAnnexes() {
		return mAnnexes;
	}

	public Circuit getCircuit() {
		return mCircuit;
	}

	public void setCircuit(Circuit circuit) {
		mCircuit = circuit;
	}

	public Action getActionDemandee() {
		return mActionDemandee;
	}

	public boolean isSignPapier() {
		return mIsSignPapier;
	}

	// </editor-fold desc="Setters / Getters">

	public void addDocument(@Nullable Document document) {

		if (document == null)
			return;

		if (document.isMainDocument())
			mMainDocuments.add(document);
		else
			mAnnexes.add(document);
	}

	public void saveDetails(Dossier dossier) {
		mMainDocuments.addAll(dossier.getMainDocuments());
		mAnnexes.addAll(dossier.getAnnexes());
	}

	public void clearDetails() {
		mMainDocuments.clear();
		mAnnexes.clear();
		mCircuit = null;
	}

	public boolean isDetailsAvailable() {
		return (mCircuit != null) && (mCircuit.getEtapeCircuitList() != null) && (!mCircuit.getEtapeCircuitList().isEmpty()) && (!mMainDocuments.isEmpty());
	}

	public boolean hasActions() {
		return ((mActions != null) && (mActions.size() > 3)); // Pour ne pas compter EMAIL, JOURNAL et ENREGISTRER
	}

	// <editor-fold desc="Static utils">

	public static @Nullable Document findCurrentDocument(@Nullable Dossier dossier, @Nullable String documentId) {

		// Default case

		if (dossier == null)
			return null;

		// Finding doc

		List<Document> documents = new ArrayList<>();
		documents.addAll(dossier.getMainDocuments());
		documents.addAll(dossier.getAnnexes());

		if (!TextUtils.isEmpty(documentId))
			for (Document document : documents)
				if (TextUtils.equals(document.getId(), documentId))
					return document;

		return dossier.getMainDocuments().isEmpty() ? null : dossier.getMainDocuments().get(0);
	}

	/**
	 * Returns the main negative {@link Action} available, by coherent priority.
	 */
	public static @Nullable Action getPositiveAction(@NonNull Dossier dossier) {

		HashSet<Action> actions = new HashSet<>(Arrays.asList(Action.values()));
		actions.retainAll(dossier.getActions());

		if (dossier.getActionDemandee() != null)
			return dossier.getActionDemandee();

		if (actions.contains(Action.SIGNATURE))
			return Action.SIGNATURE;
		else if (actions.contains(Action.VISA))
			return Action.VISA;
		else if (actions.contains(Action.ARCHIVAGE))
			return Action.ARCHIVAGE;
		else if (actions.contains(Action.MAILSEC))
			return Action.MAILSEC;
		else if (actions.contains(Action.TDT_ACTES))
			return Action.TDT_ACTES;
		else if (actions.contains(Action.TDT_HELIOS))
			return Action.TDT_HELIOS;
		else if (actions.contains(Action.TDT))
			return Action.TDT;

		return null;
	}

	/**
	 * Returns the main negative {@link Action} available, by coherent priority.
	 */
	public static @Nullable Action getNegativeAction(@NonNull Dossier dossier) {

		HashSet<Action> actions = new HashSet<>(Arrays.asList(Action.values()));
		actions.retainAll(dossier.getActions());

		if (actions.contains(Action.REJET))
			return Action.REJET;

		return null;
	}

	// </editor-fold desc="Static utils">

	// <editor-fold desc="Parcelable">

	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mId);
		dest.writeString(mName);
		dest.writeInt(mActionDemandee == null ? -1 : mActionDemandee.ordinal());
		dest.writeTypedList(new ArrayList<Parcelable>(mActions));
		dest.writeString(mType);
		dest.writeString(mSousType);
		dest.writeLong(mDateCreation != null ? mDateCreation.getTime() : -1);
		dest.writeLong(mDateLimite != null ? mDateLimite.getTime() : -1);
		dest.writeTypedList(mMainDocuments);
		dest.writeTypedList(mAnnexes);
		dest.writeParcelable(mCircuit, 0);
		dest.writeByte(mIsSignPapier ? (byte) 1 : (byte) 0);
	}

	// </editor-fold desc="Parcelable">

	@Override public boolean equals(Object o) {

		if (o == null)
			return false;

		if (o instanceof Dossier)
			return TextUtils.equals(mId, ((Dossier) o).getId());

		else if (o instanceof String)
			return TextUtils.equals(mId, (String) o);

		return false;
	}

	@Override public String toString() {
		return mName;
	}

	@Override public int hashCode() {
		return mId.hashCode();
	}
}
