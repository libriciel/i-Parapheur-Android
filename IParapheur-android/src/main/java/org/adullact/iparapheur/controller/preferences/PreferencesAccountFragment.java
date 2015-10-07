package org.adullact.iparapheur.controller.preferences;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PreferencesAccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreferencesAccountFragment extends Fragment {

	public static final String FRAGMENT_TAG = "PreferencesAccountFragment";

	private static final String LIST_FIELD_TITLE = "list_field_title";
	private static final String LIST_FIELD_URL = "list_field_url";
	private static final String LIST_FIELD_LOGIN = "list_field_login";
	private static final String LIST_FIELD_PASSWORD = "list_field_password";

	private ListView mAccountList;
	private ArrayList<Map<String, String>> mAccountData;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment PreferencesMenuFragment.
	 */
	public static PreferencesAccountFragment newInstance() {
		return new PreferencesAccountFragment();
	}

	public PreferencesAccountFragment() {
		// Required empty public constructor
	}

	// <editor-fold desc="LifeCycle">

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAccountData = new ArrayList<>();
		buildAccountDataMap();
	}

	@Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.preferences_accounts_fragment, container, false);

		mAccountList = (ListView) v.findViewById(R.id.preferences_accounts_fragment_main_list);
		View floatingButtonAction = v.findViewById(R.id.preferences_accounts_fragment_add_parapheur_floatingactionbutton);

		// Set listeners

		floatingButtonAction.setOnClickListener(
				new View.OnClickListener() {
					@Override public void onClick(View v) {
						onAddFloatingButtonClicked();
					}
				}
		);

		// Building List adapter

		String[] orderedFieldNames = new String[]{LIST_FIELD_TITLE, LIST_FIELD_URL, LIST_FIELD_LOGIN, LIST_FIELD_PASSWORD};
		int[] orderedFieldIds = new int[]{
				R.id.preferences_accounts_fragment_cell_title_edittext,
				R.id.preferences_accounts_fragment_cell_url_edittext,
				R.id.preferences_accounts_fragment_cell_login_edittext,
				R.id.preferences_accounts_fragment_cell_password_edittext
		};

		SimpleAdapter accountAdapter = new SimpleAdapter(
				getActivity(), mAccountData, R.layout.preferences_accounts_fragment_cell, orderedFieldNames, orderedFieldIds
		) {
			@Override public View getView(final int position, View convertView, ViewGroup parent) {
				final View v = super.getView(position, convertView, parent);

				// Cell buttons listener

				v.findViewById(R.id.preferences_accounts_fragment_cell_save_button).setOnClickListener(
						new View.OnClickListener() {
							@Override public void onClick(View arg0) {
								onSaveButtonClicked(position);
							}
						}
				);

				v.findViewById(R.id.preferences_accounts_fragment_cell_save_button).setOnClickListener(
						new View.OnClickListener() {
							@Override public void onClick(View arg0) {
								onDeleteButtonClicked(position);
							}
						}
				);

				v.findViewById(R.id.preferences_accounts_fragment_cell_test_button).setOnClickListener(
						new View.OnClickListener() {
							@Override public void onClick(View arg0) {

								String login = ((EditText) v.findViewById(R.id.preferences_accounts_fragment_cell_login_edittext)).getText().toString();
								String password = ((EditText) v.findViewById(R.id.preferences_accounts_fragment_cell_password_edittext)).getText().toString();

								// Apply regex on entered URL

								EditText urlEditText = ((EditText) v.findViewById(R.id.preferences_accounts_fragment_cell_url_edittext));
								String entryUrl = urlEditText.getText().toString();
								String fixedUrl = StringUtils.fixUrl(entryUrl);
								urlEditText.setText(fixedUrl);

								//

								onTestButtonClicked(fixedUrl, login, password);
							}
						}
				);

				//

				return v;
			}
		};
		mAccountList.setAdapter(accountAdapter);

		//

		return v;
	}

	// </editor-fold desc="LifeCycle">

	private void onSaveButtonClicked(int position) {
		Log.e("Adrien", "Save " + position);
	}

	private void onDeleteButtonClicked(int position) {
		Log.e("Adrien", "Delete " + position);
	}

	private void onTestButtonClicked(@Nullable String url, @Nullable String login, @Nullable String password) {

		new TestTask().execute(url, login, password);
	}

	private void onAddFloatingButtonClicked() {

		Map<String, String> accountData = new HashMap<>();
		accountData.put(LIST_FIELD_TITLE, "");
		accountData.put(LIST_FIELD_URL, "");
		accountData.put(LIST_FIELD_LOGIN, "");
		accountData.put(LIST_FIELD_PASSWORD, "");

		mAccountData.add(accountData);
		((SimpleAdapter) mAccountList.getAdapter()).notifyDataSetChanged();
	}

	private void buildAccountDataMap() {

		mAccountData.clear();

		List<Account> accountList = MyAccounts.INSTANCE.getAccounts();
		for (Account account : accountList) {

			Map<String, String> accountData = new HashMap<>();
			accountData.put(LIST_FIELD_TITLE, account.getTitle());
			accountData.put(LIST_FIELD_URL, account.getUrl());
			accountData.put(LIST_FIELD_LOGIN, account.getLogin());
			accountData.put(LIST_FIELD_PASSWORD, account.getPassword());
			mAccountData.add(accountData);
		}
	}

	private class TestTask extends AsyncTask<String, Void, Void> {

		private int mResultMessageRes;

		@Override protected Void doInBackground(String... params) {

			Account testAccount = new Account("test");
			testAccount.setUrl(params[0]);
			testAccount.setLogin(params[1]);
			testAccount.setPassword(params[2]);

			try {
				mResultMessageRes = RESTClient.INSTANCE.test(testAccount);
			}
			catch (IParapheurException e) {
				Crashlytics.logException(e);
				mResultMessageRes = e.getResId();
				e.printStackTrace();
			}

			return null;
		}

		@Override protected void onPostExecute(Void aVoid) {

			if (getActivity() != null)
				Toast.makeText(getActivity(), mResultMessageRes, Toast.LENGTH_SHORT).show();

			super.onPostExecute(aVoid);
		}
	}
}
