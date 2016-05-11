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
package org.adullact.iparapheur.controller.preferences;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.utils.FileUtils;

import java.io.File;


public class ImportCertificatesDialogFragment extends DialogFragment {

	public static final String FRAGMENT_TAG = "import_certificates_dialog_fragment";

	private static final String ARGUMENT_CERTIFICATE_PATH = "certificatePath";
	private static final String LOG_TAG = "ImportCertificatesDialogFragment";

	protected File mCertificateFile;

	protected EditText mPasswordEditText;
	protected TextView mPasswordLabel;

	public static ImportCertificatesDialogFragment newInstance(@NonNull File certificate) {

		ImportCertificatesDialogFragment fragment = new ImportCertificatesDialogFragment();

		Bundle args = new Bundle();
		args.putString(ARGUMENT_CERTIFICATE_PATH, certificate.getAbsolutePath());
		fragment.setArguments(args);

		return fragment;
	}

	// <editor-fold desc="LifeCycle">

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {

			String path = getArguments().getString(ARGUMENT_CERTIFICATE_PATH);
			if (path != null)
				mCertificateFile = new File(path);
		}
	}

	@Override public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {

		// Create view

		View view = LayoutInflater.from(getActivity()).inflate(R.layout.action_dialog_import, null);

		mPasswordEditText = (EditText) view.findViewById(R.id.dialog_import_password);
		mPasswordLabel = (TextView) view.findViewById(R.id.dialog_import_password_label);

		// Set listeners

		mPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override public void onFocusChange(View v, boolean hasFocus) {
				mPasswordLabel.setActivated(hasFocus);
			}
		});

		mPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				onImportButtonClicked();
				return true;
			}
		});

		// Build Dialog

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Main_Dialog);
		builder.setTitle(mCertificateFile.getName());
		builder.setView(view);
		builder.setPositiveButton(R.string.import_certificate, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				onImportButtonClicked();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				onCancelButtonClicked();
			}
		});

		final AlertDialog alertDialog = builder.create();
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override public void onShow(DialogInterface dialog) {

				Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				positiveButton.setOnClickListener(new View.OnClickListener() {
					@Override public void onClick(View v) {
						onImportButtonClicked();
					}
				});
			}
		});

		return alertDialog;
	}

	// </editor-fold desc="LifeCycle">

	private void onImportButtonClicked() {

		boolean success = FileUtils.importCertificate(getActivity(), mCertificateFile, mPasswordEditText.getText().toString());

		if (success)
			dismiss();
	}

	private void onCancelButtonClicked() {
		dismiss();
	}

}
