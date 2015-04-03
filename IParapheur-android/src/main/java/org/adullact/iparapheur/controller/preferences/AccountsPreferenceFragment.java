package org.adullact.iparapheur.controller.preferences;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.LoadingTask;

public class AccountsPreferenceFragment extends PreferenceFragment implements ActionsAccountPreference.ActionsAccountPreferenceListener, Preference.OnPreferenceChangeListener, LoadingTask.DataChangeListener {

	private PreferenceScreen accountsScreen;
	private String testResponse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.settings_accounts, null);
		Button b = (Button) v.findViewById(R.id.settings_accounts_button);
		b.setText(getActivity().getResources().getString(R.string.account_new));
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Account account = MyAccounts.INSTANCE.addAccount();
				buildAccountPrefScreen(accountsScreen, account);
			}
		});

		accountsScreen = getPreferenceManager().createPreferenceScreen(getActivity());
		String title = getResources().getString(R.string.pref_header_accounts) + " " + getResources().getString(R.string.app_name);
		accountsScreen.setTitle(title);

		for (Account account : MyAccounts.INSTANCE.getAccounts())
			if (!account.getId().contentEquals("AccountTest0"))
				buildAccountPrefScreen(accountsScreen, account);

		setPreferenceScreen(accountsScreen);

		return v;
	}

	/**
	 * Add a preferenceCategory related to the account passed as parameter.
	 *
	 * @param parent  the preference screen where to add the category
	 * @param account
	 */
	private void buildAccountPrefScreen(PreferenceScreen parent, Account account) {
		final PreferenceCategory accountPref = new PreferenceCategory(getActivity());
		parent.addPreference(accountPref);
		accountPref.setKey(account.getId());
		accountPref.setTitle(buildAccountHeader(account));

		// Account Title
		final EditTextPreference titlePref = new EditTextPreference(getActivity());
		titlePref.setDialogTitle(getResources().getString(R.string.pref_account_title));
		titlePref.setTitle(account.getTitle());
		titlePref.setIcon(android.R.drawable.ic_menu_info_details);
		titlePref.setText(account.getTitle());
		titlePref.getEditText().setTextColor(Color.RED);
		titlePref.setSummary(getResources().getString(R.string.pref_account_title));
		titlePref.setKey(MyAccounts.PREFS_ACCOUNT_PREFIX + account.getId() + MyAccounts.PREFS_TITLE_SUFFIX);
		titlePref.setOnPreferenceChangeListener(this);
		accountPref.addPreference(titlePref);

		// IParapheur URL
		final EditTextPreference urlPref = new EditTextPreference(getActivity());
		urlPref.setDialogTitle(getResources().getString(R.string.pref_account_url) + " " + getResources().getString(R.string.app_name));
		urlPref.setTitle(account.getUrl());
		urlPref.setIcon(android.R.drawable.ic_menu_compass);
		urlPref.setText(account.getUrl());
		urlPref.setSummary(getResources().getString(R.string.pref_account_url) + " " + getResources().getString(R.string.app_name));
		urlPref.setKey(MyAccounts.PREFS_ACCOUNT_PREFIX + account.getId() + MyAccounts.PREFS_URL_SUFFIX);
		urlPref.setOnPreferenceChangeListener(this);
		accountPref.addPreference(urlPref);

		// Login
		final EditTextPreference loginPref = new EditTextPreference(getActivity());
		loginPref.setDialogTitle(getResources().getString(R.string.pref_account_login));
		loginPref.setTitle(account.getLogin());
		loginPref.setIcon(android.R.drawable.ic_menu_myplaces);
		loginPref.setText(account.getLogin());
		loginPref.setSummary(getResources().getString(R.string.pref_account_login));
		loginPref.setKey(MyAccounts.PREFS_ACCOUNT_PREFIX + account.getId() + MyAccounts.PREFS_LOGIN_SUFFIX);
		loginPref.setOnPreferenceChangeListener(this);
		accountPref.addPreference(loginPref);

		// Password
		final EditTextPreference passwordPref = new EditTextPreference(getActivity());
		passwordPref.getEditText().setTransformationMethod(PasswordTransformationMethod.getInstance());
		passwordPref.setDialogTitle(getResources().getString(R.string.pref_account_password));
		passwordPref.setTitle("************");
		passwordPref.setIcon(android.R.drawable.ic_menu_preferences);
		passwordPref.setText(account.getPassword());
		passwordPref.setSummary(getResources().getString(R.string.pref_account_password));
		passwordPref.setKey(MyAccounts.PREFS_ACCOUNT_PREFIX + account.getId() + MyAccounts.PREFS_PASSWORD_SUFFIX);
		passwordPref.setOnPreferenceChangeListener(this);
		accountPref.addPreference(passwordPref);

		accountPref.addPreference(new ActionsAccountPreference(getActivity(), this, account));
	}

	/**
	 * Used to indicate if the account is complete in the title of the account's preference category
	 *
	 * @param account
	 * @return The title of the account's preference category
	 */
	private String buildAccountHeader(Account account) {
		//Log.d("debug", "buildAccountHeader for account : " + account.getTitle());
		if (account.isValid()) {
			return account.getLogin() + " @ " + account.getUrl();
		}
		else {
			return getResources().getString(R.string.pref_account_incomplete);
		}
	}

	@Override
	public void onAccountDeleted(Account deleted) {
		PreferenceCategory accountPreferenceScreen = (PreferenceCategory) getPreferenceScreen().findPreference(deleted.getId());
		accountsScreen.removePreference(accountPreferenceScreen);
		MyAccounts.INSTANCE.delete(deleted);
	}

	@Override
	public void onAccountTested(Account toTest) {
		new TestTask(this, toTest).execute();
	}

	/**
	 * Applelé quand le test du compte est terminé (LoadinkTask)
	 */
	@Override
	public void onDataChanged() {
		Toast.makeText(getActivity(), this.testResponse, Toast.LENGTH_LONG).show();
		this.testResponse = null;
	}

	/**
	 * Find the modified account and update the related category title and also the modified
	 * preference title
	 *
	 * @param preference the modified preference
	 * @param o          the value that has been modified
	 * @return true
	 */
	@Override
	public boolean onPreferenceChange(Preference preference, Object o) {
		String key = MyAccounts.INSTANCE.getAccountIdFromPreferenceKey(preference.getKey());
		// We have to modify the account manually as the preference will change only after this
		//methods returns...
		Account modifiedAccount = MyAccounts.INSTANCE.getAccount(key);
		// Don't update the password title
		if (preference.getKey().contains(MyAccounts.PREFS_PASSWORD_SUFFIX)) {
			modifiedAccount.setPassword(o.toString());
		}
		else {
			if (preference.getKey().contains(MyAccounts.PREFS_TITLE_SUFFIX)) {
				modifiedAccount.setTitle(o.toString());
			}
			else if (preference.getKey().contains(MyAccounts.PREFS_URL_SUFFIX)) {
				modifiedAccount.setUrl(o.toString());
			}
			else if (preference.getKey().contains(MyAccounts.PREFS_LOGIN_SUFFIX)) {
				modifiedAccount.setLogin(o.toString());
			}
			preference.setTitle(o.toString());
		}
		accountsScreen.findPreference(key).setTitle(buildAccountHeader(modifiedAccount));
		return true;
	}

	private class TestTask extends LoadingTask {

		private Account account;

		public TestTask(DataChangeListener listener, Account account) {
			super(getActivity(), listener);
			this.account = account;
		}

		@Override
		protected void load(String... params) throws IParapheurException {
			testResponse = getActivity().getResources().getString(RESTClient.INSTANCE.test(MyAccounts.INSTANCE.getAccount(this.account.getId())));
		}
	}
}
