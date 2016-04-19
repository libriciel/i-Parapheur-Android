package org.adullact.iparapheur.controller.preferences;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.crashlytics.android.Crashlytics;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PreferencesAccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreferencesAccountFragment extends Fragment {

	public static final String FRAGMENT_TAG = "preferences_account_fragment";
	public static final String LOG_TAG = "PreferencesAccountFrag";

	private static final String LIST_FIELD_TITLE = "list_field_title";
	private static final String LIST_FIELD_ID = "list_field_id";
	private static final String LIST_FIELD_URL = "list_field_url";
	private static final String LIST_FIELD_LOGIN = "list_field_login";
	private static final String LIST_FIELD_PASSWORD = "list_field_password";
	private static final String LIST_FIELD_ACTIVATED = "list_field_activated";
	private static final int LIST_CELL_TAG_POSITION = 1615190920;    // Because P-O-S-I-T = 16-15-19-09-20

	private ListView mAccountList;
	private List<Map<String, String>> mAccountData;

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
		setRetainInstance(true);

		mAccountData = new ArrayList<>();
		buildAccountDataMap();
	}

	@Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.preferences_accounts_fragment, container, false);

		mAccountList = (ListView) v.findViewById(R.id.preferences_accounts_fragment_main_list);
		View floatingButtonAction = v.findViewById(R.id.preferences_accounts_fragment_add_parapheur_floatingactionbutton);

		// Set listeners

		floatingButtonAction.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				onAddFloatingButtonClicked();
			}
		});

		// Building ListAdapter

		String[] orderedFieldNames = new String[]{LIST_FIELD_TITLE, LIST_FIELD_URL, LIST_FIELD_LOGIN, LIST_FIELD_PASSWORD};
		int[] orderedFieldIds = new int[]{
				R.id.preferences_accounts_fragment_cell_title_edittext,
				R.id.preferences_accounts_fragment_cell_server_edittext,
				R.id.preferences_accounts_fragment_cell_login_edittext,
				R.id.preferences_accounts_fragment_cell_password_edittext
		};

		SimpleAdapter accountAdapter = new AccountSimpleAdapter(getActivity(),
																mAccountData,
																R.layout.preferences_accounts_fragment_cell,
																orderedFieldNames,
																orderedFieldIds
		);
		mAccountList.setAdapter(accountAdapter);

		//

		return v;
	}

	@Override public void onResume() {
		super.onResume();

		if (getActivity() instanceof AppCompatActivity) {
			AppCompatActivity parentActivity = (AppCompatActivity) getActivity();
			if (parentActivity.getSupportActionBar() != null)
				parentActivity.getSupportActionBar().setTitle(R.string.pref_header_accounts);
		}
	}

	// </editor-fold desc="LifeCycle">

	private void onSaveButtonClicked(@NonNull EditText urlEditText, int position) {

		cleanupUrlEditText(urlEditText);

		// Retrieve existing account, or create it

		String currentId = mAccountData.get(position).get(LIST_FIELD_ID);
		Account currentAccount = MyAccounts.INSTANCE.getAccount(currentId);

		if (currentAccount == null) {
			currentAccount = MyAccounts.INSTANCE.addAccount();
			mAccountData.get(position).put(LIST_FIELD_ID, currentAccount.getId());
		}

		// Edit

		currentAccount.setServerBaseUrl(urlEditText.getText().toString());
		currentAccount.setTitle(mAccountData.get(position).get(LIST_FIELD_TITLE));
		currentAccount.setLogin(mAccountData.get(position).get(LIST_FIELD_LOGIN));
		currentAccount.setPassword(mAccountData.get(position).get(LIST_FIELD_PASSWORD));

		// Save

		Log.i(LOG_TAG, "Save account " + currentAccount);
		MyAccounts.INSTANCE.save(currentAccount);

		Toast.makeText(getActivity(), R.string.pref_account_message_save_success, Toast.LENGTH_SHORT).show();
	}

	private void onDeleteButtonClicked(int position) {

		// Retrieve existing account

		String currentId = mAccountData.get(position).get(LIST_FIELD_ID);
		Account currentAccount;

		currentAccount = MyAccounts.INSTANCE.getAccount(currentId);

		if (currentAccount == null)
			currentAccount = MyAccounts.INSTANCE.addAccount();

		// Delete

		mAccountData.remove(position);
		Log.i(LOG_TAG, "Delete account " + currentAccount);
		MyAccounts.INSTANCE.delete(currentAccount);

		((SimpleAdapter) mAccountList.getAdapter()).notifyDataSetChanged();
		Toast.makeText(getActivity(), R.string.pref_account_message_delete_success, Toast.LENGTH_SHORT).show();
	}

	private void onTestButtonClicked(@NonNull EditText urlEditText, @Nullable String login, @Nullable String password) {

		cleanupUrlEditText(urlEditText);
		new TestTask().execute(urlEditText.getText().toString(), login, password);
	}

	private void onAddFloatingButtonClicked() {

		Map<String, String> accountData = new HashMap<>();
		accountData.put(LIST_FIELD_TITLE, "");
		accountData.put(LIST_FIELD_URL, "");
		accountData.put(LIST_FIELD_LOGIN, "");
		accountData.put(LIST_FIELD_PASSWORD, "");

		mAccountData.add(accountData);
		((SimpleAdapter) mAccountList.getAdapter()).notifyDataSetChanged();

		// Scroll down to the last element programmatically, to make it visible
		mAccountList.setSelection(mAccountList.getCount() - 1);
	}

	private void cleanupUrlEditText(@NonNull EditText urlEditText) {

		String entryUrl = urlEditText.getText().toString();
		String fixedUrl = StringUtils.fixUrl(entryUrl);

		urlEditText.setText(fixedUrl);
	}

	public void buildAccountDataMap() {

		mAccountData.clear();

		// Retrieve and sort Account list (by titles, alphabetically)

		List<Account> accountList = MyAccounts.INSTANCE.getAccounts();
		Collections.sort(accountList, StringUtils.buildAccountAlphabeticalComparator(getActivity()));

		// Build map

		for (Account account : accountList) {

			Map<String, String> accountData = new HashMap<>();
			accountData.put(LIST_FIELD_TITLE, account.getTitle());
			accountData.put(LIST_FIELD_ID, account.getId());
			accountData.put(LIST_FIELD_URL, account.getServerBaseUrl());
			accountData.put(LIST_FIELD_LOGIN, account.getLogin());
			accountData.put(LIST_FIELD_PASSWORD, account.getPassword());
			accountData.put(LIST_FIELD_ACTIVATED, String.valueOf(account.isActivated()));

			mAccountData.add(accountData);
		}
	}

	private class AccountSimpleAdapter extends SimpleAdapter {

		/**
		 * Constructor
		 *
		 * @param context  The context where the View associated with this SimpleAdapter is running
		 * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
		 *                 Maps contain the data for each row, and should include all the entries specified in
		 *                 "from"
		 * @param resource Resource identifier of a view layout that defines the views for this list
		 *                 item. The layout file should include at least those named views defined in "to"
		 * @param from     A list of column names that will be added to the Map associated with each
		 *                 item.
		 * @param to       The views that should display column in the "from" parameter. These should all be
		 *                 TextViews. The first N views in this list are given the values of the first N columns
		 */
		public AccountSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
		}

		@Override public View getView(final int position, View convertView, ViewGroup parent) {

			// We reset the Tag before recycling the view, with super, then reassign it
			// because we don't want to trigger the EditText TextChangedListeners
			// when the system recycles the views.

			if (convertView != null)
				convertView.setTag(LIST_CELL_TAG_POSITION, -1);

			final View v = super.getView(position, convertView, parent);

			v.setTag(LIST_CELL_TAG_POSITION, position);

			// Retrieve entries (a Holder might be overkill for 7 subviews...)

			final EditText titleEditText = ((EditText) v.findViewById(R.id.preferences_accounts_fragment_cell_title_edittext));
			final EditText urlEditText = ((EditText) v.findViewById(R.id.preferences_accounts_fragment_cell_server_edittext));
			final EditText loginEditText = ((EditText) v.findViewById(R.id.preferences_accounts_fragment_cell_login_edittext));
			final EditText passwordEditText = ((EditText) v.findViewById(R.id.preferences_accounts_fragment_cell_password_edittext));
			final ToggleButton enableToggleButton = ((ToggleButton) v.findViewById(R.id.preferences_accounts_fragment_cell_enabled_togglebutton));
			final Button saveButton = (Button) v.findViewById(R.id.preferences_accounts_fragment_cell_save_button);
			final Button deleteButton = (Button) v.findViewById(R.id.preferences_accounts_fragment_cell_delete_button);
			final Button testButton = (Button) v.findViewById(R.id.preferences_accounts_fragment_cell_test_button);

			// Since we can't easily remove lambda functions with TextView's TextChangeListeners,
			// We have to store a tag to know if the EditText already have a listener
			// (otherwise it will be called one more times every time the view is recycled)

			if (convertView == null) {
				setEditTextListenerToDataMap(v, titleEditText, LIST_FIELD_TITLE);
				setEditTextListenerToDataMap(v, urlEditText, LIST_FIELD_URL);
				setEditTextListenerToDataMap(v, loginEditText, LIST_FIELD_LOGIN);
				setEditTextListenerToDataMap(v, passwordEditText, LIST_FIELD_PASSWORD);
			}

			// Cell buttons listener

			saveButton.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View arg0) {
					onSaveButtonClicked(urlEditText, position);
				}
			});

			deleteButton.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View arg0) {
					onDeleteButtonClicked(position);
				}
			});

			testButton.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View arg0) {
					String login = loginEditText.getText().toString();
					String password = passwordEditText.getText().toString();

					onTestButtonClicked(urlEditText, login, password);
				}
			});

			// Demo case

			boolean isDemoAccount = TextUtils.equals(mAccountData.get(position).get(LIST_FIELD_ID), getString(R.string.demo_account_id));
			boolean isActivated = Boolean.valueOf(mAccountData.get(position).get(LIST_FIELD_ACTIVATED));

			lockEditText(titleEditText, !isDemoAccount);
			lockEditText(urlEditText, !isDemoAccount);
			lockEditText(loginEditText, !isDemoAccount);
			lockEditText(passwordEditText, !isDemoAccount);

			deleteButton.setVisibility(isDemoAccount ? View.GONE : View.VISIBLE);
			saveButton.setVisibility(isDemoAccount ? View.GONE : View.VISIBLE);
			enableToggleButton.setVisibility(isDemoAccount ? View.VISIBLE : View.GONE);

			enableToggleButton.setOnCheckedChangeListener(null);
			enableToggleButton.setChecked(isActivated);
			enableToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

					boolean isDemoAccount = TextUtils.equals(mAccountData.get(position).get(LIST_FIELD_ID), getString(R.string.demo_account_id));
					if (isDemoAccount) {

						int currentPosition = (Integer) v.getTag(LIST_CELL_TAG_POSITION);
						mAccountData.get(currentPosition).put(LIST_FIELD_ACTIVATED, String.valueOf(isChecked));

						String currentAccountId = mAccountData.get(currentPosition).get(LIST_FIELD_ID);
						Account currentAccount = MyAccounts.INSTANCE.getAccount(currentAccountId);
						if (currentAccount != null) {
							currentAccount.setActivated(isChecked);
							MyAccounts.INSTANCE.save(currentAccount);
						}
					}
				}
			});

			//

			return v;
		}

		private void lockEditText(EditText editText, boolean lock) {
			editText.setFocusable(lock);
			editText.setFocusableInTouchMode(lock);
		}

		private void setEditTextListenerToDataMap(@NonNull final View parentView, @NonNull final EditText editText, @NonNull final String dataMapField) {

			editText.addTextChangedListener(

					new TextWatcher() {

						@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

						@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

						@Override public void afterTextChanged(Editable s) {

							int currentPosition = (Integer) parentView.getTag(LIST_CELL_TAG_POSITION);
							if (currentPosition != -1)
								mAccountData.get(currentPosition).put(dataMapField, editText.getText().toString());
						}
					});
		}

	}

	private class TestTask extends AsyncTask<String, Void, Void> {

		private int mResultMessageRes;

		@Override protected Void doInBackground(String... params) {

			Account testAccount = new Account("test");
			testAccount.setServerBaseUrl(params[0]);
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