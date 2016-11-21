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
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Dossier {

	@SerializedName("id") private String mId;
	@SerializedName("title") private String mName;
	@SerializedName("actionDemandee") private Action mActionDemandee;
	@SerializedName("type") private String mType;
	@SerializedName("sousType") private String mSousType;
	@SerializedName("dateEmission") private Date mDateCreation;
	@SerializedName("dateLimite") private Date mDateLimite;
	@SerializedName("actions") private Set<Action> mActions;
	@SerializedName("isSignPapier") private boolean mIsSignPapier;
	@SerializedName("documents") private List<Document> mDocumentList = new ArrayList<>();
	//@SerializedName("total") private int mTotal;
	//@SerializedName("protocol", alternate = {"protocole"}) private String mProtocol;
	//@SerializedName("isSent") private boolean mIsSent;
	//@SerializedName("creator") private String mCreator;
	//@SerializedName("bureauName") private String mBureauName;
	//@SerializedName("pendingFile") private int mPendingFile;
	//@SerializedName("banetteName") private String mBanetteName;
	//@SerializedName("skipped") private int mSkipped;
	//@SerializedName("isXemEnabled") private boolean mIsXemEnabled;
	//@SerializedName("hasRead") private boolean mHasRead;
	//@SerializedName("readingMandatory") private boolean mIsReadingMandatory;
	//@SerializedName("isRead") private boolean mIsRead;
	//@SerializedName("locked") private boolean mIsLocked;
	//@SerializedName("includeAnnexes") private boolean mInclueAnnexes;
	//@SerializedName("nomTdT") private String mNomTDT;
	//@SerializedName("visibility") private String mVisibility;
	//@SerializedName("status") private String mStatus;
	//@SerializedName("canAdd") private boolean mCanAdd;
	//@SerializedName("metadatas") private HashMap<String, Object> mMetadataMap;
	//@SerializedName("xPathSignature") private String mSignatureXPath;
	private Circuit mCircuit;

	public Dossier() {}

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

	public List<Document> getDocumentList() {
		return mDocumentList;
	}

	// </editor-fold desc="Setters / Getters">

	public void saveDetails(Dossier dossier) {
		mDocumentList.addAll(dossier.getDocumentList());
	}

	public void clearDetails() {
		mDocumentList.clear();
		mCircuit = null;
	}

	public boolean isDetailsAvailable() {
		return (mCircuit != null) && (mCircuit.getEtapeCircuitList() != null) && (!mCircuit.getEtapeCircuitList().isEmpty()) && (!mDocumentList.isEmpty());
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

		if (!TextUtils.isEmpty(documentId))
			for (Document document : dossier.getDocumentList())
				if (TextUtils.equals(document.getId(), documentId))
					return document;

		return dossier.getDocumentList().isEmpty() ? null : dossier.getDocumentList().get(0);
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

	/**
	 * Patching a weird Signature case :
	 * "actionDemandee" can have any "actions" value...
	 * ... Except when "actionDemandee=SIGNATURE", where "actions" only contains VISA, for some reason
	 *
	 * A SIGNATURE action is acceptable in VISA too...
	 *
	 * @param dossier , the dossier to fix
	 */
	public static void fixActionsDemandees(@NonNull Dossier dossier) {

		if (dossier.getActionDemandee() == Action.SIGNATURE) {
			dossier.getActions().remove(Action.VISA);
			dossier.getActions().add(Action.SIGNATURE);
		}

		if (dossier.getActionDemandee() == Action.VISA)
			dossier.getActions().add(Action.SIGNATURE);
	}

	public static @NonNull List<Document> getMainDocuments(@Nullable Dossier dossier) {

		// Default case

		if ((dossier == null) || (dossier.getDocumentList()) == null || (dossier.getDocumentList().isEmpty()))
			return new ArrayList<>();

		//

		ArrayList<Document> result = new ArrayList<>();
		for (Document document : dossier.getDocumentList())
			if (Document.isMainDocument(dossier, document))
				result.add(document);

		return result;
	}

	public static @NonNull List<Document> getAnnexes(@Nullable Dossier dossier) {

		// Default case

		if ((dossier == null) || (dossier.getDocumentList()) == null || (dossier.getDocumentList().isEmpty()))
			return new ArrayList<>();

		//

		ArrayList<Document> result = new ArrayList<>();
		for (Document document : dossier.getDocumentList())
			if (!Document.isMainDocument(dossier, document))
				result.add(document);

		return result;
	}

	// </editor-fold desc="Static utils">

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
		return "{Dossier id=" + mId + " name=" + mName + " actionsDemandees=" + mActionDemandee + " type=" + mType + " subType=" + mSousType     //
				+ " dateCrea=" + mDateCreation + " dateLimite=" + mDateLimite + " docs=" + mDocumentList + " actions=" + mActions                //
				+ " circuit=" + mCircuit + " isSignPapier=" + mIsSignPapier + "}";
	}

	@Override public int hashCode() {
		return mId.hashCode();
	}
}
