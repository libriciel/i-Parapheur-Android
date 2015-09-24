package org.adullact.iparapheur.controller.preferences;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.utils.FileUtils;
import org.adullact.iparapheur.utils.PKCS7Signer;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;


public class ImportCertificatesDialogFragment extends DialogFragment {

	public static final String FRAGMENT_TAG = "ImportCertificatesDialogFragment";

	private static final String ARGUMENT_CERTIFICATE_PATH = "certificatePath";
	private static final String LOG_TAG = "ImportCertificatesDialogFragment";

	protected File mCertificateFile;
	protected EditText mPasswordEditText;
	protected TextView mPasswordLabel;

	public static ImportCertificatesDialogFragment newInstance(@NonNull File certificateFound) {

		ImportCertificatesDialogFragment fragment = new ImportCertificatesDialogFragment();

		Bundle args = new Bundle();
		args.putString(ARGUMENT_CERTIFICATE_PATH, certificateFound.getAbsolutePath());
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

		mPasswordEditText.setOnFocusChangeListener(
				new View.OnFocusChangeListener() {
					@Override public void onFocusChange(View v, boolean hasFocus) {
						mPasswordLabel.setActivated(hasFocus);
					}
				}
		);

		mPasswordEditText.setOnEditorActionListener(
				new TextView.OnEditorActionListener() {
					@Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
						onImportButtonClicked();
						return true;
					}
				}
		);

		// Build Dialog

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Main_Dialog);
		builder.setTitle(mCertificateFile.getName());
		builder.setView(view);
		builder.setPositiveButton(
				R.string.import_certificate, new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						onImportButtonClicked();
					}
				}
		);
		builder.setNegativeButton(
				android.R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						onCancelButtonClicked();
					}
				}
		);

		final AlertDialog alertDialog = builder.create();
		alertDialog.setOnShowListener(
				new DialogInterface.OnShowListener() {
					@Override public void onShow(DialogInterface dialog) {

						Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
						positiveButton.setOnClickListener(
								new View.OnClickListener() {
									@Override public void onClick(View v) {
										onImportButtonClicked();
									}
								}
						);
					}
				}
		);

		return alertDialog;
	}

	// </editor-fold desc="LifeCycle">

	private void onImportButtonClicked() {

		// Test password

		boolean bksOpeningSuccess = false;
		try {
			PKCS7Signer pkcs7signer = new PKCS7Signer(mCertificateFile.getAbsolutePath(), mPasswordEditText.getText().toString(), "", "");
			pkcs7signer.loadKeyStore();
			bksOpeningSuccess = true;
		}
		catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(getActivity(), R.string.import_error_message_error_opening_bks_file, Toast.LENGTH_SHORT).show();
		}
		catch (NoSuchAlgorithmException | KeyStoreException | CertificateException e) {
			e.printStackTrace();
			Toast.makeText(getActivity(), R.string.import_error_message_incompatible_device, Toast.LENGTH_SHORT).show();
		}

		// Stop on error

		if (!bksOpeningSuccess)
			return;

		// Import to intern memory

		boolean movedSuccessfully;

		File from = mCertificateFile;
		File to = new File(FileUtils.getInternalCertificateStoragePath(getContext()), mCertificateFile.getName());
		movedSuccessfully = from.renameTo(to);

		if (movedSuccessfully) {

			SharedPreferences settings = getActivity().getSharedPreferences(FileUtils.SHARED_PREFERENCES_CERTIFICATES_PASSWORDS, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(mCertificateFile.getName(), mPasswordEditText.getText().toString());
			editor.apply();

			Toast.makeText(getActivity(), R.string.import_successful, Toast.LENGTH_SHORT).show();
			dismiss();
		}
		else {
			Toast.makeText(getActivity(), R.string.import_error_cant_copy_certificate, Toast.LENGTH_SHORT).show();
		}
	}

	private void onCancelButtonClicked() {
		dismiss();
	}

}
