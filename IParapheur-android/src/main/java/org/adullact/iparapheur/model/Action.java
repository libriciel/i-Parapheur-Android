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
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import org.adullact.iparapheur.R;


public enum Action implements Parcelable {

	// TODO : all possible actions (secretariat, supprimer, ...)
	VISA(R.string.action_viser, R.id.action_visa, R.drawable.iw_visa, R.drawable.ip_visa),
	SIGNATURE(R.string.action_signer, R.id.action_signature, R.drawable.iw_signature, R.drawable.ip_signature),
	TDT(R.string.action_tdt, R.id.action_tdt, R.drawable.iw_tdt, R.drawable.ip_tdt),
	TDT_ACTES(R.string.action_tdt_actes, R.id.action_tdt_actes, R.drawable.iw_tdt, R.drawable.ip_tdt),
	TDT_HELIOS(R.string.action_tdt_helios, R.id.action_tdt_helios, R.drawable.iw_tdt, R.drawable.ip_tdt),
	ARCHIVAGE(R.string.action_archiver, R.id.action_archivage, R.drawable.iw_archivage, R.drawable.iw_archivage),
	MAILSEC(R.string.action_mailsec, R.id.action_mailsec, R.drawable.iw_mailsec, R.drawable.ip_mailsec),
	REJET(R.string.action_rejeter, R.id.action_rejet, R.drawable.ic_action_reject, R.drawable.ic_action_reject),
	SECRETARIAT(R.string.action_secretariat, R.id.action_secretariat, -1, -1),
	REMORD(R.string.action_remord, R.id.action_remord, -1, -1),
	AVIS_COMPLEMENTAIRE(R.string.action_avis, R.id.action_avis, -1, -1),
	TRANSFERT_SIGNATURE(R.string.action_transfertsign, R.id.action_transfert_signature, -1, -1),
	AJOUT_SIGNATURE(R.string.action_ajoutsign, R.id.action_ajout_signature, -1, -1),
	EMAIL(R.string.action_mail, R.id.action_mail, -1, -1),
	ENREGISTRER(R.string.action_enregistrer, R.id.action_enregistrer, -1, -1),
	SUPPRESSION(R.string.action_supprimer, R.id.action_supprimer, -1, -1),
	JOURNAL(R.string.action_journal, R.id.action_journal, -1, -1),
	TRANSFERT_ACTION(R.string.action_transfert, R.id.action_avis, -1, -1), // TODO

	//non implementées :
	GET_ATTEST,
	RAZ,
	EDITION,
	ENCHAINER_CIRCUIT;

	public static final Creator<Action> CREATOR = new Creator<Action>() {

		@Override public Action createFromParcel(final Parcel source) {
			return Action.values()[source.readInt()];
		}

		@Override public Action[] newArray(final int size) {
			return new Action[size];
		}
	};

	private final int mIcon;
	private final int mApprovedIcon;
	private final int mTitle;
	private final int mMenuItemId;

	Action(@StringRes int title, @IdRes int menuItemId, @DrawableRes int icon, @DrawableRes int approvedIcon) {
		mTitle = title;
		mMenuItemId = menuItemId;
		mIcon = icon;
		mApprovedIcon = approvedIcon;
	}

	Action() {
		mTitle = R.string.action_non_implementee;
		mMenuItemId = -1;
		mIcon = -1;
		mApprovedIcon = -1;
	}

	public @DrawableRes int getIcon(boolean approved) {
		return approved ? mApprovedIcon : mIcon;
	}

	public @StringRes int getTitle() {
		return mTitle;
	}

	public @IdRes int getMenuItemId() {
		return mMenuItemId;
	}

	public static @Nullable Action fromId(@IdRes int id) {

		for (Action action : Action.values())
			if (action.getMenuItemId() == id)
				return action;

		return null;
	}

	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(ordinal());
	}

}
