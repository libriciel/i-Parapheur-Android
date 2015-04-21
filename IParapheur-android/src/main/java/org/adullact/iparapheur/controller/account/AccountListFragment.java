package org.adullact.iparapheur.controller.account;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.model.Account;

import java.util.ArrayList;
import java.util.List;

public class AccountListFragment extends Fragment implements AdapterView.OnItemClickListener {

	public static final String TAG = "account_fragment_tag";

	private AccountFragmentListener mListener;
	private List<Account> mAccounts;                // List of accounts displayed in the spinner
	private ListView mListView;                     // ListView used to show the bureaux of the currently selected account

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Called only once as retainInstance is set to true.
		setRetainInstance(true);

		mAccounts = new ArrayList<>();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof AccountFragmentListener))
			throw new IllegalStateException("Activity must implement AccountFragmentListener.");

		mListener = (AccountFragmentListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View content = inflater.inflate(R.layout.account_list_fragment, container, false);
		mListView = (ListView) content.findViewById(R.id.account_list);

		// Adding footers

		View separatorView = inflater.inflate(R.layout.account_list_fragment_footer_separator, mListView, false);
		mListView.addFooterView(separatorView, null, false);

		View footerView = inflater.inflate(R.layout.account_list_fragment_footer, mListView, false);
		footerView.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				if (mListener != null)
					mListener.onCreateAccountInvoked();
			}
		});
		mListView.addFooterView(footerView, null, false);

		return content;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mListView.setOnItemClickListener(this);
		mListView.setEmptyView(view.findViewById(android.R.id.empty));
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		updateAccounts();
		mListView.setAdapter(new AccountListAdapter(getActivity()));

		// Setting selected view

		Account selectedAccount = MyAccounts.INSTANCE.getSelectedAccount();
		if (selectedAccount == null)
			selectedAccount = MyAccounts.INSTANCE.getAccounts().get(0);

		for (int i = 0; i < mAccounts.size(); i++)
			if (selectedAccount.getId().contentEquals(mAccounts.get(i).getId()))
				mListView.setItemChecked(i, true);
	}

	private void updateAccounts() {
		mAccounts.clear();

		for (Account account : MyAccounts.INSTANCE.getAccounts())
			if (account.isValid())
				mAccounts.add(account);

		if ((mListView != null) && (mListView.getAdapter() != null))
			((AccountListAdapter) mListView.getAdapter()).notifyDataSetChanged();
	}

	public void accountsChanged() {
		updateAccounts();
	}

	// <editor-fold desc="OnItemClickListener">

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mListener.onAccountSelected(mAccounts.get(position));
	}

	// </editor-fold desc="OnItemClickListener">

	// <editor-fold desc="AccountSelectedListener">

	public interface AccountFragmentListener {

		public void onAccountSelected(@NonNull Account account);

		public void onCreateAccountInvoked();

	}

	// </editor-fold desc="AccountSelectedListener">

	private class AccountListAdapter extends ArrayAdapter<Account> {

		public AccountListAdapter(Context context) {
			super(context, R.layout.account_list_fragment_cell, android.R.id.text1);
		}

		@Override
		public int getCount() {
			return (mAccounts == null) ? 0 : mAccounts.size();
		}

		@Override
		public Account getItem(int position) {
			return mAccounts.get(position);
		}

		@Override
		public int getPosition(Account item) {
			return mAccounts.indexOf(item);
		}

		@Override
		public boolean isEmpty() {
			return (mAccounts == null) || mAccounts.isEmpty();
		}
	}

}