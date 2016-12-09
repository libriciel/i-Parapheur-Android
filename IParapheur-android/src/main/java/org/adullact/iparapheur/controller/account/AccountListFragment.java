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
package org.adullact.iparapheur.controller.account;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.database.DatabaseHelper;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.utils.AccountUtils;
import org.adullact.iparapheur.utils.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AccountListFragment extends Fragment implements AdapterView.OnItemClickListener {

	public static final String FRAGMENT_TAG = "account_list_fragment";

	private List<Account> mAccounts;                // List of accounts displayed in the spinner
	private ListView mListView;                     // ListView used to show the bureaux of the currently selected account
	private AccountListAdapter mAccountListAdapter;

	// <editor-fold desc="LifeCycle">

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		mAccounts = new ArrayList<>();
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View content = inflater.inflate(R.layout.account_list_fragment, container, false);

		mListView = (ListView) content.findViewById(R.id.account_list);

		// UI tuning

		mListView.setDivider(new ColorDrawable(ContextCompat.getColor(getActivity(), android.R.color.background_light)));
		mListView.setDividerHeight(1);
		mListView.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.background_light));

		// Adding footers

		View separatorView = inflater.inflate(R.layout.account_list_fragment_footer_separator, mListView, false);
		mListView.addFooterView(separatorView, null, false);

		View footerView = inflater.inflate(R.layout.account_list_fragment_footer, mListView, false);
		footerView.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				((AccountListFragmentListener) getActivity()).onCreateAccountInvoked();
			}
		});
		mListView.addFooterView(footerView, null, false);

		return content;
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mListView.setOnItemClickListener(this);
		mListView.setEmptyView(view.findViewById(android.R.id.empty));
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAccountListAdapter = new AccountListAdapter(getActivity());
		mListView.setAdapter(mAccountListAdapter);
		updateAccounts();

		// Setting selected view

		Account selectedAccount = AccountUtils.SELECTED_ACCOUNT;
		if (selectedAccount == null)
			selectedAccount = AccountUtils.getDemoAccount();

		for (int i = 0; i < mAccounts.size(); i++)
			if (selectedAccount.getId().contentEquals(mAccounts.get(i).getId()))
				mListView.setItemChecked(i, true);
	}

	// </editor-fold desc="LifeCycle">

	private void updateAccounts() {
		mAccounts.clear();

		ArrayList<Account> accountList = new ArrayList<>();
		try { accountList.addAll(new DatabaseHelper(getActivity()).getAccountDao().queryForAll()); }
		catch (SQLException e) { e.printStackTrace(); }

		for (Account account : accountList)
			if (AccountUtils.isValid(account))
				if (account.isActivated())
					mAccounts.add(account);

		Collections.sort(mAccounts, StringUtils.buildAccountAlphabeticalComparator());

		if (mAccountListAdapter != null)
			mAccountListAdapter.notifyDataSetChanged();
	}

	public void accountsChanged() {
		updateAccounts();
	}

	// <editor-fold desc="OnItemClickListener">

	@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (isAdded())
			((AccountListFragmentListener) getActivity()).onAccountSelected(mAccounts.get(position));
	}

	// </editor-fold desc="OnItemClickListener">

	// <editor-fold desc="AccountSelectedListener">

	public interface AccountListFragmentListener {

		void onAccountSelected(@NonNull Account account);

		void onCreateAccountInvoked();

	}

	// </editor-fold desc="AccountSelectedListener">

	private class AccountListAdapter extends ArrayAdapter<Account> {

		private AccountListAdapter(Context context) {
			super(context, R.layout.account_list_fragment_cell, android.R.id.text1);
		}

		@Override public int getCount() {
			return (mAccounts == null) ? 0 : mAccounts.size();
		}

		@Override public Account getItem(int position) {
			return mAccounts.get(position);
		}

		@Override public int getPosition(Account item) {
			return mAccounts.indexOf(item);
		}

		@Override public boolean isEmpty() {
			return (mAccounts == null) || mAccounts.isEmpty();
		}
	}

}