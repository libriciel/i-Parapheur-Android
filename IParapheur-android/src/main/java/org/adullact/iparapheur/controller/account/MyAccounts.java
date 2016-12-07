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
/**
 * @return
 * @coveredInInstrumentedUnitTest
 */
package org.adullact.iparapheur.controller.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.adullact.iparapheur.model.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public enum MyAccounts implements SharedPreferences.OnSharedPreferenceChangeListener {

	INSTANCE;

	public static final String PREFS_ACCOUNT_PREFIX = "account_";
	public static final String PREFS_TITLE_SUFFIX = "_title";
	public static final String PREFS_URL_SUFFIX = "_url";
	public static final String PREFS_LOGIN_SUFFIX = "_login";
	public static final String PREFS_PASSWORD_SUFFIX = "_password";
	public static final String PREFS_ACTIVATED_SUFFIX = "_activated";
	public static final String PREFS_SELECTED_ACCOUNT = "selected_account";

	private ArrayList<Account> mAccounts = null;
	private Account mSelectedAccount;

	public @NonNull List<Account> getAccounts(@NonNull Context context) {

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return getAccounts(sharedPreferences);
	}

	public @NonNull List<Account> getAccounts(@NonNull SharedPreferences sharedPreferences) {
		if (mAccounts == null) {
			mAccounts = new ArrayList<>();

			for (String pref : sharedPreferences.getAll().keySet()) {
				if (pref.startsWith(PREFS_ACCOUNT_PREFIX)) {

					String id = pref.substring(pref.indexOf("_") + 1);
					id = id.substring(0, id.lastIndexOf("_"));
					Account account = new Account(id);

					if (!mAccounts.contains(account)) {
						account.setTitle(sharedPreferences.getString(PREFS_ACCOUNT_PREFIX + id + PREFS_TITLE_SUFFIX, ""));
						account.setLogin(sharedPreferences.getString(PREFS_ACCOUNT_PREFIX + id + PREFS_LOGIN_SUFFIX, ""));
						account.setServerBaseUrl(sharedPreferences.getString(PREFS_ACCOUNT_PREFIX + id + PREFS_URL_SUFFIX, ""));
						account.setPassword(sharedPreferences.getString(PREFS_ACCOUNT_PREFIX + id + PREFS_PASSWORD_SUFFIX, ""));
						account.setActivated(sharedPreferences.getBoolean(PREFS_ACCOUNT_PREFIX + id + PREFS_ACTIVATED_SUFFIX, true));
						mAccounts.add(account);
					}
				}
			}
			if (mSelectedAccount == null) {
				String selectedDossierId = (sharedPreferences.getString(PREFS_SELECTED_ACCOUNT, null));
				if (selectedDossierId != null)
					mSelectedAccount = getAccount(selectedDossierId);
			}
		}
		return mAccounts;
	}

	public @NonNull Account addAccount(@NonNull Context context) {

		Account account = new Account(UUID.randomUUID().toString());
		mAccounts.add(account);
		save(context, account);

		return account;
	}

	public void save(@NonNull Context context, @NonNull Account account) {

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PREFS_ACCOUNT_PREFIX + account.getId() + PREFS_TITLE_SUFFIX, account.getTitle());
		editor.putString(PREFS_ACCOUNT_PREFIX + account.getId() + PREFS_URL_SUFFIX, account.getServerBaseUrl());
		editor.putString(PREFS_ACCOUNT_PREFIX + account.getId() + PREFS_LOGIN_SUFFIX, account.getLogin());
		editor.putString(PREFS_ACCOUNT_PREFIX + account.getId() + PREFS_PASSWORD_SUFFIX, account.getPassword());
		editor.putBoolean(PREFS_ACCOUNT_PREFIX + account.getId() + PREFS_ACTIVATED_SUFFIX, account.isActivated());
		editor.apply();
	}

	public void delete(@NonNull Context context, @NonNull Account account) {

		String id = account.getId();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Set<String> keySet = sharedPreferences.getAll().keySet();

		if (keySet.contains(PREFS_ACCOUNT_PREFIX + id + PREFS_TITLE_SUFFIX)) {
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.remove(PREFS_ACCOUNT_PREFIX + id + PREFS_TITLE_SUFFIX);
			editor.remove(PREFS_ACCOUNT_PREFIX + id + PREFS_URL_SUFFIX);
			editor.remove(PREFS_ACCOUNT_PREFIX + id + PREFS_LOGIN_SUFFIX);
			editor.remove(PREFS_ACCOUNT_PREFIX + id + PREFS_PASSWORD_SUFFIX);
			editor.remove(PREFS_ACCOUNT_PREFIX + id + PREFS_ACTIVATED_SUFFIX);
			editor.apply();
		}

		mAccounts.remove(account);

		if ((mSelectedAccount != null) && (TextUtils.equals(mSelectedAccount.getId(), id)))
			mSelectedAccount = null;
	}

	public @Nullable Account getAccount(@NonNull String id) {
		int index = mAccounts.indexOf(new Account(id));
		return (index != -1) ? mAccounts.get(index) : null;
	}

	public Account getSelectedAccount() {
		return mSelectedAccount;
	}

	public void selectAccount(@NonNull String id) {
		mSelectedAccount = getAccount(id);
	}

	public void saveState(@NonNull Context context) {
		if (mSelectedAccount != null) {

			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = sharedPreferences.edit();

			editor.putString(PREFS_SELECTED_ACCOUNT, mSelectedAccount.getId());
			editor.apply();
		}
	}

	// <editor-fold desc="OnSharedPreferenceChangeListener">

	@Override public void onSharedPreferenceChanged(@NonNull SharedPreferences sharedPreferences, @NonNull String s) {

		if (s.startsWith(PREFS_ACCOUNT_PREFIX)) {

			mAccounts = null;
			getAccounts(sharedPreferences);

			// if an Account was previously selected, update it with the new one

			if (mSelectedAccount != null)
				mSelectedAccount = getAccount(mSelectedAccount.getId());
		}
	}

	// </editor-fold desc="OnSharedPreferenceChangeListener">
}
