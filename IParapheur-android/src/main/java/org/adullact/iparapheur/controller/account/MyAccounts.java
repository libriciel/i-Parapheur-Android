package org.adullact.iparapheur.controller.account;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.adullact.iparapheur.controller.IParapheurApplication;
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
	public static final String PREFS_SELECTED_ACCOUNT = "selected_account";

	private ArrayList<Account> accounts = null;
	private Account selectedAccount;

	public List<Account> getAccounts() {
		if (accounts == null) {
			accounts = new ArrayList<>();
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(IParapheurApplication.getContext());

			for (String pref : sharedPreferences.getAll().keySet()) {
				if (pref.startsWith(PREFS_ACCOUNT_PREFIX)) {

					String id = pref.substring(pref.indexOf("_") + 1);
					id = id.substring(0, id.lastIndexOf("_"));
					Account account = new Account(id);

					if (!accounts.contains(account)) {
						account.setTitle(sharedPreferences.getString(PREFS_ACCOUNT_PREFIX + id + PREFS_TITLE_SUFFIX, ""));
						account.setLogin(sharedPreferences.getString(PREFS_ACCOUNT_PREFIX + id + PREFS_LOGIN_SUFFIX, ""));
						account.setUrl(sharedPreferences.getString(PREFS_ACCOUNT_PREFIX + id + PREFS_URL_SUFFIX, ""));
						account.setPassword(sharedPreferences.getString(PREFS_ACCOUNT_PREFIX + id + PREFS_PASSWORD_SUFFIX, ""));
						accounts.add(account);
					}
				}
			}
			if (selectedAccount == null) {
				String selectedDossierId = (sharedPreferences.getString(PREFS_SELECTED_ACCOUNT, null));
				if (selectedDossierId != null)
					selectedAccount = getAccount(selectedDossierId);
			}
		}
		return accounts;
	}

	public Account addAccount() {
		Account account = new Account(UUID.randomUUID().toString());
		save(account);
		return account;
	}

	public void save(Account account) {

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(IParapheurApplication.getContext());

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PREFS_ACCOUNT_PREFIX + account.getId() + PREFS_TITLE_SUFFIX, account.getTitle());
		editor.putString(PREFS_ACCOUNT_PREFIX + account.getId() + PREFS_URL_SUFFIX, account.getUrl());
		editor.putString(PREFS_ACCOUNT_PREFIX + account.getId() + PREFS_LOGIN_SUFFIX, account.getLogin());
		editor.putString(PREFS_ACCOUNT_PREFIX + account.getId() + PREFS_PASSWORD_SUFFIX, account.getPassword());
		editor.apply();
		editor.commit();
	}

	public void delete(Account account) {

		String id = account.getId();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(IParapheurApplication.getContext());
		Set<String> keySet = sharedPreferences.getAll().keySet();

		if (keySet.contains(PREFS_ACCOUNT_PREFIX + id + PREFS_TITLE_SUFFIX)) {
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.remove(PREFS_ACCOUNT_PREFIX + id + PREFS_TITLE_SUFFIX);
			editor.remove(PREFS_ACCOUNT_PREFIX + id + PREFS_URL_SUFFIX);
			editor.remove(PREFS_ACCOUNT_PREFIX + id + PREFS_LOGIN_SUFFIX);
			editor.remove(PREFS_ACCOUNT_PREFIX + id + PREFS_PASSWORD_SUFFIX);
			editor.apply();
			editor.commit();
		}

		if ((selectedAccount != null) && (selectedAccount.getId().equals(id)))
			selectedAccount = null;
	}

	@Override public void onSharedPreferenceChanged(@NonNull SharedPreferences sharedPreferences, @NonNull String s) {

		if (s.startsWith(PREFS_ACCOUNT_PREFIX)) {

			accounts = null;
			getAccounts();

			// if an Account was previously selected, update it with the new one

			if (selectedAccount != null)
				selectedAccount = getAccount(selectedAccount.getId());
		}
	}

	public @Nullable Account getAccount(@NonNull String id) {
		int index = accounts.indexOf(new Account(id));
		return (index != -1) ? accounts.get(index) : null;
	}

	public @NonNull String getAccountIdFromPreferenceKey(@NonNull String key) {
		return key.substring(key.indexOf('_') + 1, key.lastIndexOf('_'));
	}

	public @Nullable Account getSelectedAccount() {
		return selectedAccount;
	}

	public void selectAccount(@NonNull String id) {
		selectedAccount = getAccount(id);
	}

	public void saveState() {
		if (selectedAccount != null) {

			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(IParapheurApplication.getContext());
			SharedPreferences.Editor editor = sharedPreferences.edit();

			editor.putString(PREFS_SELECTED_ACCOUNT, selectedAccount.getId());
			editor.apply();
			editor.commit();
		}
	}
}
