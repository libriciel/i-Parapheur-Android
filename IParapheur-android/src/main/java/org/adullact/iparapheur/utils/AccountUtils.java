/*
 * iParapheur Android
 * Copyright (C) 2016-2019 Libriciel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.adullact.iparapheur.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.adullact.iparapheur.model.Account;

import java.util.Comparator;


public class AccountUtils {

	public static final String DEMO_ID = "AccountTest0";
	public static final String DEMO_TITLE = "iParapheur demo";
	public static final String DEMO_BASE_URL = "iparapheur-partenaires.libriciel.fr";
	public static final String DEMO_LOGIN = "admin@demo";
	public static final String DEMO_PASSWORD = "admin";

	private static final String PREFS_SELECTED_ACCOUNT = "selected_account";

	public static Account SELECTED_ACCOUNT = null;

	public static String loadSelectedAccountId(@NonNull Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPrefs.getString(PREFS_SELECTED_ACCOUNT, null);
	}

	public static boolean isValid(@NonNull Account account) {
		return StringUtils.areNotEmpty(account.getTitle(), account.getLogin(), account.getPassword()) && StringUtils.isUrlValid(account.getServerBaseUrl());
	}

	public static @NonNull Comparator<Account> buildAlphabeticalComparator() {

		return new Comparator<Account>() {
			@Override public int compare(Account lhs, Account rhs) {

				if (TextUtils.equals(lhs.getId(), AccountUtils.DEMO_ID))
					return Integer.MIN_VALUE;

				if (TextUtils.equals(rhs.getId(), AccountUtils.DEMO_ID))
					return Integer.MAX_VALUE;

				return lhs.getTitle().compareTo(rhs.getTitle());
			}
		};
	}

}
