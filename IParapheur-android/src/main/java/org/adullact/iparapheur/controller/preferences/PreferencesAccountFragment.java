package org.adullact.iparapheur.controller.preferences;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.model.Account;

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

		mAccountData = buildAccountDataMap();
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
				R.id.preferences_accounts_fragment_title_edittext,
				R.id.preferences_accounts_fragment_url_edittext,
				R.id.preferences_accounts_fragment_login_edittext,
				R.id.preferences_accounts_fragment_password_edittext
		};

		SimpleAdapter accountAdapter = new SimpleAdapter(
				getActivity(), mAccountData, R.layout.preferences_accounts_fragment_cell, orderedFieldNames, orderedFieldIds
		);
		mAccountList.setAdapter(accountAdapter);

		//

		return v;
	}

	// </editor-fold desc="LifeCycle">

	private ArrayList<Map<String, String>> buildAccountDataMap() {
		ArrayList<Map<String, String>> dataList = new ArrayList<>();

		List<Account> accountList = MyAccounts.INSTANCE.getAccounts();
		for (Account account : accountList) {

			Map<String, String> accountData = new HashMap<>();
			accountData.put(LIST_FIELD_TITLE, account.getTitle());
			accountData.put(LIST_FIELD_URL, account.getUrl());
			accountData.put(LIST_FIELD_LOGIN, account.getLogin());
			accountData.put(LIST_FIELD_PASSWORD, account.getPassword());
			dataList.add(accountData);
		}

		return dataList;
	}

	private void onAddFloatingButtonClicked() {

		Map<String, String> accountData = new HashMap<>();
		accountData.put(LIST_FIELD_TITLE, null);
		accountData.put(LIST_FIELD_URL, null);
		accountData.put(LIST_FIELD_LOGIN, null);
		accountData.put(LIST_FIELD_PASSWORD, null);

		mAccountData.add(accountData);
		mAccountList.deferNotifyDataSetChanged();
	}
}
