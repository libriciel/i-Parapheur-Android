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
package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.adullact.iparapheur.R;


public class AskPasswordDialogFragment extends DialogFragment {

	public static final String FRAGMENT_TAG = "ask_password_dialog_fragment";
	public static final int REQUEST_CODE_ASK_PASSWORD = 110911;   // Because A-S-K = 19-09-11
	public static final String RESULT_BUNDLE_EXTRA_PASSWORD = "password";

	private static final String ARGUMENTS_SELECTED_ALIAS = "selected_alias";

	private EditText mPasswordEditText;
	private TextView mPasswordLabel;

	private String mSelectedAlias;

	public static AskPasswordDialogFragment newInstance(String alias) {

		AskPasswordDialogFragment fragment = new AskPasswordDialogFragment();

		Bundle args = new Bundle();
		args.putString(ARGUMENTS_SELECTED_ALIAS, alias);

		fragment.setArguments(args);

		return fragment;
	}

	// <editor-fold desc="LifeCycle">

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			mSelectedAlias = getArguments().getString(ARGUMENTS_SELECTED_ALIAS);
		}
	}

	@Override public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {

		// Create view

		View view = View.inflate(getActivity(), R.layout.action_dialog_ask_password, null);

		mPasswordEditText = (EditText) view.findViewById(R.id.action_ask_password_edittext);
		mPasswordLabel = (TextView) view.findViewById(R.id.action_ask_password_label);

		// Set listeners

		mPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override public void onFocusChange(View v, boolean hasFocus) {
				mPasswordLabel.setActivated(hasFocus);
			}
		});

		mPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				onValidButtonClicked();
				dismissAllowingStateLoss();
				return true;
			}
		});

		// Build Dialog

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Main_Dialog);
		builder.setView(view);
		builder.setTitle(mSelectedAlias);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				onValidButtonClicked();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				onCancelButtonClicked();
			}
		});

		return builder.create();
	}

	// </editor-fold desc="LifeCycle">

	private void onValidButtonClicked() {

		Intent intent = new Intent();
		intent.putExtra(RESULT_BUNDLE_EXTRA_PASSWORD, mPasswordEditText.getText().toString());

		getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
	}

	private void onCancelButtonClicked() {
		dismissAllowingStateLoss();
	}

}
