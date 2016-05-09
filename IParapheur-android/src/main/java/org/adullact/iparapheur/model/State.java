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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import org.adullact.iparapheur.R;


public enum State {

	EN_PREPARATION("en-preparation", R.string.en_preparation),
	A_TRAITER("a-traiter", R.string.a_traiter),
	EN_FIN_DE_CIRCUIT("a-archiver", R.string.a_archiver),
	RETOURNES("retournes", R.string.retournes),
	EN_COURS("en-cours", R.string.en_cours),
	A_VENIR("a-venir", R.string.a_venir),
	RECUPERABLE("recuperables", R.string.recuperables),
	EN_RETARD("en-retard", R.string.en_retard),
	TRAITES("traites", R.string.traites),
	DOSSIERS_DELEGUES("dossiers-delegues", R.string.dossiers_delegues),
	TOUTES_LES_BANETTES("no-corbeille", R.string.no_corbeille),
	TOUT_IPARAPHEUR("no-bureau", R.string.no_bureau);

	private String mServerValue;
	private @StringRes int mNameRes;

	State(@NonNull String serverValue, @StringRes int nameRes) {
		mServerValue = serverValue;
		mNameRes = nameRes;
	}

	public @NonNull String getServerValue() {
		return mServerValue;
	}

	public int getNameRes() {
		return mNameRes;
	}

	public static @Nullable State fromServerValue(@NonNull String serverValue) {

		for (State tray : State.values())
			if (TextUtils.equals(serverValue, tray.getServerValue()))
				return tray;

		return null;
	}

	public static @Nullable State fromName(@NonNull Context context, @NonNull String name) {

		for (State tray : State.values())
			if (TextUtils.equals(name, context.getString(tray.getNameRes())))
				return tray;

		return null;
	}
}
