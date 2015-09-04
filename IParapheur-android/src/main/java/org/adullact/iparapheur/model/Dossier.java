package org.adullact.iparapheur.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Dossier implements Parcelable {

	public static Creator<Dossier> CREATOR = new Creator<Dossier>() {
		public Dossier createFromParcel(Parcel source) {
			return new Dossier(source);
		}

		public Dossier[] newArray(int size) {
			return new Dossier[size];
		}
	};

	private final String id;
	private final String name;
	private final Action actionDemandee;
	private final String type;
	private final String sousType;
	private final Date dateCreation;
	private final Date dateLimite;
	private final List<Document> mainDocuments = new ArrayList<Document>();
	private final List<Document> annexes = new ArrayList<Document>();
	private List<Action> actions;
	private List<EtapeCircuit> circuit = new ArrayList<EtapeCircuit>();

	// TODO : remove
	public Dossier(int i) {
		this(
				UUID.randomUUID().toString(),
				"Dossier " + i,
				Action.VISA,
				new ArrayList<Action>(),
				"Type",
				"SousType",
				Calendar.getInstance().getTime(),
				Calendar.getInstance().getTime()
		);
		getActions().add(Action.VISA);
	}

	/**
	 * Constructor used to search a dossier in a list (only the id is used for comparisons).
	 *
	 * @param id
	 */
	public Dossier(String id) {
		this.id = id;
		this.name = this.type = this.sousType = null;
		this.dateCreation = this.dateLimite = null;
		this.actionDemandee = null;
		this.actions = new ArrayList<>();
	}

	public Dossier(String id, String name, Action actionDemandee, List<Action> actions, String type, String sousType, Date dateCreation, Date dateLimite) {
		this.id = id;
		this.name = name;
		this.actionDemandee = actionDemandee;
		this.actions = actions;
		this.type = type;
		this.sousType = sousType;
		this.dateCreation = dateCreation;
		this.dateLimite = dateLimite;
	}

	private Dossier(Parcel in) {
		this.id = in.readString();
		this.name = in.readString();
		int tmpActionDemandee = in.readInt();
		this.actionDemandee = tmpActionDemandee == -1 ? null : Action.values()[tmpActionDemandee];
		in.readTypedList(actions, Action.CREATOR);
		this.type = in.readString();
		this.sousType = in.readString();
		long tmpDateCreation = in.readLong();
		this.dateCreation = tmpDateCreation == -1 ? null : new Date(tmpDateCreation);
		long tmpDateLimite = in.readLong();
		this.dateLimite = tmpDateLimite == -1 ? null : new Date(tmpDateLimite);
		in.readTypedList(mainDocuments, Document.CREATOR);
		in.readTypedList(annexes, Document.CREATOR);
		in.readTypedList(circuit, EtapeCircuit.CREATOR);
	}

	// <editor-fold desc="Setters / Getters">

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<Action> getActions() {
		return actions;
	}

	public String getType() {
		return type;
	}

	public String getSousType() {
		return sousType;
	}

	public String getDateCreation() {
		return DateFormat.getDateInstance().format(dateCreation);
	}

	public String getDateLimite() {
		return (dateLimite == null) ? "" : DateFormat.getDateInstance().format(dateLimite);
	}

	public List<Document> getMainDocuments() {
		return mainDocuments;
	}

	public List<Document> getAnnexes() {
		return annexes;
	}

	public List<EtapeCircuit> getCircuit() {
		return circuit;
	}

	public void setCircuit(List<EtapeCircuit> circuit) {
		this.circuit = circuit;
	}

	public Action getActionDemandee() {
		return actionDemandee;
	}

	// </editor-fold desc="Setters / Getters">

	public void addDocument(@Nullable Document document) {

		if (document == null)
			return;

		if (document.isMainDocument())
			mainDocuments.add(document);
		else
			annexes.add(document);
	}

	public void saveDetails(Dossier dossier) {
		this.mainDocuments.addAll(dossier.getMainDocuments());
		this.annexes.addAll(dossier.getAnnexes());
	}

	public void clearDetails() {
		this.mainDocuments.clear();
		this.annexes.clear();
		this.circuit.clear();
	}

	public boolean isDetailsAvailable() {
		return (!circuit.isEmpty() && !mainDocuments.isEmpty());
	}

	public boolean hasActions() {
		return ((actions != null) && (actions.size() > 3)); // Pour ne pas compter EMAIL, JOURNAL et ENREGISTRER
	}

	// <editor-fold desc="Parcelable">

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.id);
		dest.writeString(this.name);
		dest.writeInt(this.actionDemandee == null ? -1 : this.actionDemandee.ordinal());
		dest.writeTypedList(actions);
		dest.writeString(this.type);
		dest.writeString(this.sousType);
		dest.writeLong(dateCreation != null ? dateCreation.getTime() : -1);
		dest.writeLong(dateLimite != null ? dateLimite.getTime() : -1);
		dest.writeTypedList(mainDocuments);
		dest.writeTypedList(annexes);
		dest.writeTypedList(circuit);
	}

	// </editor-fold desc="Parcelable">

	/**
	 * Equals and hashCode overriding, so we can find dossier with its id.
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Dossier) {
			Dossier toCompare = (Dossier) o;
			return this.id.equals(toCompare.id);
		}
		else if (o instanceof String) {
			return this.id.equals(o);
		}
		return false;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
