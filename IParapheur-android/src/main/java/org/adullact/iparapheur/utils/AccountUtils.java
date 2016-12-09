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
package org.adullact.iparapheur.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import org.adullact.iparapheur.model.Account;


public class AccountUtils {

	public static final String DEMO_ID = "AccountTest0";
	private static final String DEMO_TITLE = "iParapheur demo";
	private static final String DEMO_BASE_URL = "parapheur.demonstrations.adullact.org";
	private static final String DEMO_LOGIN = "bma";
	private static final String DEMO_PASSWORD = "secret";

	private static final String PREFS_SELECTED_ACCOUNT = "selected_account";

	public static Account SELECTED_ACCOUNT = null;

	public static String loadSelectedAccountId(@NonNull Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPrefs.getString(PREFS_SELECTED_ACCOUNT, null);
	}

	public static boolean isValid(@NonNull Account account) {
		return StringUtils.areNotEmpty(account.getTitle(), account.getLogin(), account.getPassword()) && StringUtils.isUrlValid(account.getServerBaseUrl());
	}

	public static @NonNull Account getDemoAccount() {
		return new Account(DEMO_ID, DEMO_TITLE, DEMO_BASE_URL, DEMO_LOGIN, DEMO_PASSWORD, null, null);
	}

}
