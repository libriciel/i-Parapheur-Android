package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.utils.FileUtils;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.PKCS7Signer;
import org.adullact.iparapheur.utils.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SignatureDialogFragment extends DialogFragment {

	private static final String LOG_TAG = "SignatureDialogFragment";
	private static final String ARGUMENTS_DOSSIERS = "dossiers";
	private static final String ARGUMENTS_BUREAU_ID = "bureau_id";

	protected EditText mPublicAnnotationEditText;
	protected EditText mPrivateAnnotationEditText;
	protected TextView mPublicAnnotationLabel;
	protected TextView mPrivateAnnotationLabel;
	protected Spinner mCertificateSpinner;
	protected Spinner mAliasesSpinner;
	protected TextView mCertificateLabel;
	protected TextView mAliasesLabel;

	private List<File> mCertificateFileList;
	private List<String> mCertificateNameList;
	private List<String> mAliasList;
	private String mBureauId;
	private ArrayList<Dossier> mDossierList;
	private String signInfo;

	private File mSelectedCertificateFile;
	private String mSelectedCertificatePassword;

	public static @NonNull SignatureDialogFragment newInstance(@NonNull ArrayList<Dossier> dossiers, @NonNull String bureauId) {

		SignatureDialogFragment fragment = new SignatureDialogFragment();

		Bundle args = new Bundle();
		args.putParcelableArrayList(ARGUMENTS_DOSSIERS, dossiers);
		args.putString(ARGUMENTS_BUREAU_ID, bureauId);

		fragment.setArguments(args);

		return fragment;
	}

	// <editor-fold desc="LifeCycle">

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			mDossierList = getArguments().getParcelableArrayList(ARGUMENTS_DOSSIERS);
			mBureauId = getArguments().getString(ARGUMENTS_BUREAU_ID);
		}

		// Init values

		mCertificateNameList = new ArrayList<>();
		mAliasList = new ArrayList<>();
	}

	@Override public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {

		// Create view

		View view = View.inflate(getActivity(), R.layout.action_dialog_signature, null);

		mPublicAnnotationEditText = (EditText) view.findViewById(R.id.action_signature_public_annotation);
		mPrivateAnnotationEditText = (EditText) view.findViewById(R.id.action_signature_private_annotation);
		mPublicAnnotationLabel = (TextView) view.findViewById(R.id.action_signature_public_annotation_label);
		mPrivateAnnotationLabel = (TextView) view.findViewById(R.id.action_signature_private_annotation_label);
		mCertificateSpinner = (Spinner) view.findViewById(R.id.action_signature_choose_certificate_spinner);
		mCertificateLabel = (TextView) view.findViewById(R.id.action_signature_choose_certificate_label);
		mAliasesSpinner = (Spinner) view.findViewById(R.id.action_signature_choose_alias_spinner);
		mAliasesLabel = (TextView) view.findViewById(R.id.action_signature_choose_alias_label);

		// Set Spinner adapters

		ArrayAdapter<String> certificatesArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mCertificateNameList);
		certificatesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCertificateSpinner.setAdapter(certificatesArrayAdapter);

		ArrayAdapter<String> aliasesArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mAliasList);
		aliasesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAliasesSpinner.setAdapter(aliasesArrayAdapter);

		// Set listeners

		mPublicAnnotationEditText.setOnFocusChangeListener(
				new View.OnFocusChangeListener() {
					@Override public void onFocusChange(View v, boolean hasFocus) {
						mPublicAnnotationLabel.setActivated(hasFocus);
					}
				}
		);

		mPrivateAnnotationEditText.setOnFocusChangeListener(
				new View.OnFocusChangeListener() {
					@Override public void onFocusChange(View v, boolean hasFocus) {
						mPrivateAnnotationLabel.setActivated(hasFocus);
					}
				}
		);

		mCertificateSpinner.setOnItemSelectedListener(
				new AdapterView.OnItemSelectedListener() {
					@Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						refreshAliasesSpinner();
					}

					@Override public void onNothingSelected(AdapterView<?> parent) {
						refreshAliasesSpinner();
					}
				}
		);

		// Build Dialog

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Main_Dialog);
		builder.setView(view);
		builder.setPositiveButton(
				R.string.action_signer, new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						// Do nothing here because we override this button in the onStart() to change the close behaviour.
						// However, we still need this because on older versions of Android :
						// unless we pass a handler the button doesn't get instantiated
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

		return builder.create();
	}

	@Override public void onStart() {
		super.onStart();

		AlertDialog dialog = (AlertDialog) getDialog();
		if (dialog != null) {
			Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(
					new View.OnClickListener() {
						@Override public void onClick(View v) {
							onSignButtonClicked();
						}
					}
			);
		}

		retrieveSignData();
		refreshCertificatesSpinner();
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if ((requestCode == AskPasswordDialogFragment.REQUEST_CODE_ASK_PASSWORD) && (resultCode == Activity.RESULT_OK)) {

			String password = data.getStringExtra(AskPasswordDialogFragment.RESULT_BUNDLE_EXTRA_PASSWORD);
			new SignTask().execute(password);

			return;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	// </editor-fold desc="LifeCycle">

	private void onSignButtonClicked() {

		AskPasswordDialogFragment askFragment = AskPasswordDialogFragment.newInstance(mAliasesSpinner.getSelectedItem().toString());
		askFragment.setTargetFragment(this, AskPasswordDialogFragment.REQUEST_CODE_ASK_PASSWORD);
		askFragment.show(getActivity().getSupportFragmentManager(), AskPasswordDialogFragment.FRAGMENT_TAG);

		// See #onActivityResult() for password retrieve and signature launch.
	}

	private void onCancelButtonClicked() {
		dismiss();
	}

	private void retrieveSignData() {

		new AsyncTask<Void, Void, Void>() {
			@Override protected Void doInBackground(Void... params) {

				if ((mDossierList != null) && (!mDossierList.isEmpty())) {

					String dossierId = mDossierList.get(0).getId();
					try { signInfo = RESTClient.INSTANCE.getSignInfo(dossierId, mBureauId).getHash(); }
					catch (IParapheurException e) {

						e.printStackTrace();
					}
				}

				return null;
			}
		}.execute();
	}

	private void refreshCertificatesSpinner() {

		new AsyncTask<Void, Void, Void>() {
			@Override protected Void doInBackground(Void... params) {

				// Generate String List

				List<File> certificateList = FileUtils.getBksFromCertificateFolder(getActivity());
				mCertificateNameList.clear();

				for (File certificateFile : certificateList)
					mCertificateNameList.add(certificateFile.getName());

				return null;
			}

			@SuppressWarnings("unchecked") @Override protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				((ArrayAdapter<String>) mCertificateSpinner.getAdapter()).notifyDataSetChanged();

				refreshAliasesSpinner();
			}
		}.execute();

	}

	private void refreshAliasesSpinner() {

		// Default case

		if (mCertificateSpinner.getSelectedItem() == null)
			return;

		// Inflate aliases

		final String selectedCertificateName = mCertificateSpinner.getSelectedItem().toString();

		new AsyncTask<Void, Void, Void>() {
			@Override protected Void doInBackground(Void... params) {

				// Default cases

				mCertificateFileList = FileUtils.getBksFromCertificateFolder(getActivity());
				if (mCertificateFileList.isEmpty())
					return null;

				mSelectedCertificateFile = null;
				for (File certificateFile : mCertificateFileList)
					if (TextUtils.equals(certificateFile.getName(), selectedCertificateName))
						mSelectedCertificateFile = certificateFile;

				if (mSelectedCertificateFile == null)
					return null;

				// Retrieving Certificate password.

				SharedPreferences settings = getActivity().getSharedPreferences(FileUtils.SHARED_PREFERENCES_CERTIFICATES_PASSWORDS, 0);
				mSelectedCertificatePassword = settings.getString(mSelectedCertificateFile.getName(), "");

				// Retrieving aliases from Certificate file.

				PKCS7Signer signer = new PKCS7Signer(mSelectedCertificateFile.getAbsolutePath(), mSelectedCertificatePassword, "", "");
				ArrayList<String> aliasList = new ArrayList<>();

				try {
					KeyStore keystore = signer.loadKeyStore();

					if (keystore != null)
						aliasList.addAll(Collections.list(keystore.aliases()));
				}
				catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) { e.printStackTrace(); }

				// Inflate spinner

				mAliasList.clear();
				mAliasList.addAll(aliasList);

				return null;
			}

			@SuppressWarnings("unchecked") @Override protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				((ArrayAdapter<String>) mAliasesSpinner.getAdapter()).notifyDataSetChanged();
			}
		}.execute();

	}

	private class SignTask extends AsyncTask<String, Void, Boolean> {

		private String mAnnotPub;
		private String mAnnotPriv;
		private String mSelectedAlias;
		private int mErrorMessage;

		@Override protected void onPreExecute() {
			super.onPreExecute();

			mAnnotPub = mPublicAnnotationEditText.getText().toString();
			mAnnotPriv = mPrivateAnnotationEditText.getText().toString();
			mSelectedAlias = mAliasesSpinner.getSelectedItem().toString();
			mErrorMessage = -1;
		}

		@Override protected Boolean doInBackground(String... passwordArg) {

			if (isCancelled())
				return false;

			String password = passwordArg[0];
			int total = mDossierList.size();
			for (int i = 0; i < total; i++) {

				String signValue = "";

				// Sign data

				if (mSelectedCertificateFile != null) {

					PKCS7Signer signer = new PKCS7Signer(mSelectedCertificateFile.getAbsolutePath(), mSelectedCertificatePassword, mSelectedAlias, password);

					try {
						signer.loadKeyStore();
						signer.loadPrivateKey();
						signValue = signer.sign(StringUtils.hexDecode(signInfo));
					}
					catch (FileNotFoundException e) {
						e.printStackTrace();
						Crashlytics.logException(e);
						mErrorMessage = R.string.signature_error_message_missing_bks_file;
					}
					catch (NoSuchAlgorithmException | KeyStoreException | NoSuchProviderException e) {
						e.printStackTrace();
						Crashlytics.logException(e);
						mErrorMessage = R.string.signature_error_message_incompatible_device;
					}
					catch (UnrecoverableKeyException e) {
						e.printStackTrace();
						Crashlytics.logException(e);
						mErrorMessage = R.string.signature_error_message_missing_alias_or_wrong_password;
					}
					catch (InvalidKeyException e) {
						e.printStackTrace();
						Crashlytics.logException(e);
						mErrorMessage = R.string.signature_error_message_wrong_password;
					}
					catch (IllegalArgumentException e) {
						e.printStackTrace();
						Crashlytics.logException(e);
						mErrorMessage = R.string.signature_error_message_no_data_to_sign;
					}
					catch (SignatureException | IllegalStateException e) {
						e.printStackTrace();
						Crashlytics.logException(e);
						mErrorMessage = R.string.signature_error_message_unknown_error;
					}
					catch (CertificateException | IOException e) {
						e.printStackTrace();
						Crashlytics.logException(e);
						mErrorMessage = R.string.signature_error_message_error_opening_bks_file;
					}
				}

				// Send result, if any

				Log.d(LOG_TAG, "Signature... ");
				Log.d(LOG_TAG, "Data  : " + signInfo);
				Log.d(LOG_TAG, "Value : " + signValue);

				if (TextUtils.isEmpty(signValue))
					return false;

				if (isCancelled())
					return false;

				try { RESTClient.INSTANCE.signer(mDossierList.get(i).getId(), signValue, mAnnotPub, mAnnotPriv, mBureauId); }
				catch (IParapheurException e) {
					e.printStackTrace();
					Crashlytics.logException(e);
					mErrorMessage = R.string.signature_error_message_not_sent_to_server;
				}

				if (isCancelled())
					return false;
			}

			// If no error message, then the signature is successful.
			return (mErrorMessage == -1);
		}

		@Override protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);

			if (success) {
				dismiss();
			}
			else if (getActivity() != null) {
				Toast.makeText(getActivity(), mErrorMessage, Toast.LENGTH_SHORT).show();
			}
		}
	}
}
