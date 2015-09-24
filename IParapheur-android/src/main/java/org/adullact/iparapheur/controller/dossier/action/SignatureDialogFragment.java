package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.utils.FileUtils;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.LoadingTask;
import org.adullact.iparapheur.utils.LoadingWithProgressTask;
import org.adullact.iparapheur.utils.PKCS7Signer;
import org.adullact.iparapheur.utils.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;


public class SignatureDialogFragment extends DialogFragment {

	private static final String LOG_TAG = "SignatureDialogFragment";
	private static final int RESULT_CODE_FILE_SELECT = 0;
	private static final String ARGUMENTS_DOSSIERS = "dossiers";
	private static final String ARGUMENTS_BUREAU_ID = "bureauId";

	protected EditText mPublicAnnotationEditText;
	protected EditText mPrivateAnnotationEditText;
	protected TextView mPublicAnnotationLabel;
	protected TextView mPrivateAnnotationLabel;

	private String mBureauId;
	private ArrayList<Dossier> mDossierList;
	private String signInfo;

	public static SignatureDialogFragment newInstance(ArrayList<Dossier> dossiers, String bureauId) {

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
	}

	@Override public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {

		// Create view

		View view = LayoutInflater.from(getActivity()).inflate(R.layout.action_dialog_signature, null);

		mPublicAnnotationEditText = (EditText) view.findViewById(R.id.action_import_password);
		mPrivateAnnotationEditText = (EditText) view.findViewById(R.id.action_dialog_private_annotation);
		mPublicAnnotationLabel = (TextView) view.findViewById(R.id.action_dialog_public_annotation_label);
		mPrivateAnnotationLabel = (TextView) view.findViewById(R.id.action_dialog_private_annotation_label);

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

		// Build Dialog

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Main_Dialog);
		builder.setView(view);
		builder.setPositiveButton(
				R.string.action_signer, new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						onSignButtonClicked();
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

		new AsyncTask<Void, Void, Void>() {
			@Override protected Void doInBackground(Void... params) {

				if ((mDossierList != null) && (!mDossierList.isEmpty())) {

					String dossierId = mDossierList.get(0).getId();
					try { signInfo = RESTClient.INSTANCE.getSignInfo(dossierId, mBureauId).getHash(); }
					catch (IParapheurException e) { e.printStackTrace(); }
				}

				return null;
			}
		}.execute();

	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
			case RESULT_CODE_FILE_SELECT:
				if (resultCode == Activity.RESULT_OK) {
					Uri uri = data.getData();
					Log.d(LOG_TAG, "File Uri: " + uri.toString());
				}
				break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	// </editor-fold desc="LifeCycle">

	private void onSignButtonClicked() {
		new SignTask(getActivity()).execute();
	}

	private void onCancelButtonClicked() {
		dismiss();
	}

	private class SignTask extends LoadingWithProgressTask {

		public SignTask(Activity activity) {
			super(activity, (LoadingTask.DataChangeListener) getActivity());
		}

		@Override protected void load(String... params) throws IParapheurException {

			if (isCancelled())
				return;

			String annotPub = mPublicAnnotationEditText.getText().toString();
			String annotPriv = mPrivateAnnotationEditText.getText().toString();

			publishProgress(0);

			int total = mDossierList.size();
			for (int i = 0; i < total; i++) {

				Dossier dossier = mDossierList.get(i);
				String signValue = "";

				// Sign data

				File certif = FileUtils.getBksFromCertificateFolder(getActivity());

				if (certif != null) {

					SharedPreferences settings = getActivity().getSharedPreferences(FileUtils.SHARED_PREFERENCES_CERTIFICATES_PASSWORDS, 0);
					String certificatePassword = settings.getString(certif.getName(), "");
					Log.i("Adrien", "certif found : " + certif.getAbsolutePath() + " " + certificatePassword);

					PKCS7Signer signer = new PKCS7Signer(certif.getAbsolutePath(), certificatePassword, "bma", "bma");

					try {
						signer.loadKeyStore();
						signer.loadPrivateKey();

						signValue = signer.sign(StringUtils.hexDecode(signInfo));
					}
					catch (FileNotFoundException e) {
						e.printStackTrace();
						Toast.makeText(getActivity(), R.string.signature_error_message_missing_bks_file, Toast.LENGTH_SHORT).show();
					}
					catch (NoSuchAlgorithmException | KeyStoreException | NoSuchProviderException e) {
						e.printStackTrace();
						Toast.makeText(getActivity(), R.string.signature_error_message_incompatible_device, Toast.LENGTH_SHORT).show();
					}
					catch (UnrecoverableKeyException e) {
						e.printStackTrace();
						Toast.makeText(getActivity(), R.string.signature_error_message_missing_alias, Toast.LENGTH_SHORT).show();
					}
					catch (InvalidKeyException e) {
						e.printStackTrace();
						Toast.makeText(getActivity(), R.string.signature_error_message_wrong_password, Toast.LENGTH_SHORT).show();
					}
					catch (IllegalArgumentException e) {
						e.printStackTrace();
						Toast.makeText(getActivity(), R.string.signature_error_message_no_data_to_sign, Toast.LENGTH_SHORT).show();
					}
					catch (SignatureException | IllegalStateException e) {
						e.printStackTrace();
						Toast.makeText(getActivity(), R.string.signature_error_message_unknown_error, Toast.LENGTH_SHORT).show();
					}
					catch (CertificateException | IOException e) {
						e.printStackTrace();
						Toast.makeText(getActivity(), R.string.signature_error_message_error_opening_bks_file, Toast.LENGTH_SHORT).show();
					}
				}

				// Send result, if any

				Log.d(LOG_TAG, "Signature... ");
				Log.d(LOG_TAG, "BKS   ? " + (certif != null));
				Log.d(LOG_TAG, "Data  : " + signInfo);
				Log.d(LOG_TAG, "Value : " + signValue);

				if (TextUtils.isEmpty(signValue))
					return; // TODO : Throw back error message

				if (isCancelled())
					return;

				// RESTClient.INSTANCE.signer(dossier.getId(), signValue, annotPub, annotPriv, bureauId);
				publishProgress(i * 100 / total);

				if (isCancelled())
					return;
			}
		}

	}
}
