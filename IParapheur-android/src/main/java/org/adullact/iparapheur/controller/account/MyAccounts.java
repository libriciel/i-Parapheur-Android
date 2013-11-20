package org.adullact.iparapheur.controller.account;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.adullact.iparapheur.controller.IParapheur;
import org.adullact.iparapheur.model.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by jmaire on 23/10/13.
 */
public enum MyAccounts implements SharedPreferences.OnSharedPreferenceChangeListener {
    INSTANCE;

    public static final String PREFS_ACCOUNT_PREFIX = "account_";
    public static final String PREFS_TITLE_SUFFIX = "_title";
    public static final String PREFS_URL_SUFFIX = "_url";
    public static final String PREFS_LOGIN_SUFFIX = "_login";
    public static final String PREFS_PASSWORD_SUFFIX = "_password";

    private ArrayList<Account> accounts = null;
    private Account selectedAccount;

    public List<Account> getAccounts() {
        if (accounts == null) {
            accounts = new ArrayList<Account>();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(IParapheur.getContext());

            for (String pref : sharedPreferences.getAll().keySet()) {
                if (pref.startsWith(PREFS_ACCOUNT_PREFIX))
                {
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
        }
        return accounts;
    }

    public Account addAccount()
    {
        Account account = new Account(UUID.randomUUID().toString());
        save(account);
        return account;
    }

    public void save(Account account) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(IParapheur.getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString( PREFS_ACCOUNT_PREFIX + account.getId() + PREFS_TITLE_SUFFIX, account.getTitle());
        editor.putString( PREFS_ACCOUNT_PREFIX + account.getId() + PREFS_URL_SUFFIX, account.getUrl());
        editor.putString( PREFS_ACCOUNT_PREFIX + account.getId() + PREFS_LOGIN_SUFFIX, account.getLogin());
        editor.putString( PREFS_ACCOUNT_PREFIX + account.getId() + PREFS_PASSWORD_SUFFIX, account.getPassword());
        editor.apply();
        editor.commit();
    }

    public void delete(Account account) {
        String id = account.getId();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(IParapheur.getContext());
        Set<String> keySet = sharedPreferences.getAll().keySet();
        if (keySet.contains( PREFS_ACCOUNT_PREFIX + id + PREFS_TITLE_SUFFIX))
        {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(PREFS_ACCOUNT_PREFIX + id + PREFS_TITLE_SUFFIX);
            editor.remove(PREFS_ACCOUNT_PREFIX + id + PREFS_URL_SUFFIX);
            editor.remove(PREFS_ACCOUNT_PREFIX + id + PREFS_LOGIN_SUFFIX);
            editor.remove(PREFS_ACCOUNT_PREFIX + id + PREFS_PASSWORD_SUFFIX);
            editor.apply();
            editor.commit();
        }
        if ((selectedAccount != null) && (selectedAccount.getId().equals(id))) {
            selectedAccount = null;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.startsWith(PREFS_ACCOUNT_PREFIX)) {
            accounts = null;
            getAccounts();
            // if an Account was previously selected, update it with the new one
            if (selectedAccount != null)
            {
                selectedAccount = getAccount(selectedAccount.getId());
            }
        }
    }

    public Account getAccount(String id) {
        int index = accounts.indexOf(new Account(id));
        return (index != -1)? accounts.get(index) : null;
    }

    public String getAccountIdFromPreferenceKey(String key) {
        return key.substring(key.indexOf('_') + 1, key.lastIndexOf('_'));
    }

    public Account getSelectedAccount() {
        return selectedAccount;
    }

    public void selectAccount(String id) {
        selectedAccount = getAccount(id);
    }
}
