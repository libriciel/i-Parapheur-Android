package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;

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

	protected TextView publicAnnotationTextView;
	protected TextView privateAnnotationTextView;
	private TextView certInfo;
	private TextView errorInfo;

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

		publicAnnotationTextView = (TextView) view.findViewById(R.id.action_dialog_annotation_publique);
		privateAnnotationTextView = (TextView) view.findViewById(R.id.action_dialog_annotation_privee);

		view.findViewById(R.id.action_dialog_signature_certificate_button).setOnClickListener(
				new View.OnClickListener() {
					@Override public void onClick(View v) {
						onChooseCertificateButtonClicked();
					}
				}
		);
		certInfo = (TextView) view.findViewById(R.id.action_dialog_signature_cert_info);

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

	private void onChooseCertificateButtonClicked() {

	}

	private class SignTask extends LoadingWithProgressTask {

		public SignTask(Activity activity) {
			super(activity, (LoadingTask.DataChangeListener) getActivity());
		}

		@Override protected void load(String... params) throws IParapheurException {

			if (isCancelled())
				return;

			String annotPub = publicAnnotationTextView.getText().toString();
			String annotPriv = privateAnnotationTextView.getText().toString();

			publishProgress(0);

			int total = mDossierList.size();
			for (int i = 0; i < total; i++) {

				Dossier dossier = mDossierList.get(i);
				String signValue = "";

				// Sign data

				File certif = FileUtils.getBksFromDownloadFolder();

				if (certif != null) {
					PKCS7Signer signer = new PKCS7Signer(certif.getAbsolutePath(), "bmabma", "bma", "bma");

					try {
						signer.loadKeyStore();
						signer.loadPrivateKey();

						signValue = signer.sign(StringUtils.hexDecode(signInfo));
					}
					catch (FileNotFoundException e) {
						e.printStackTrace();
						errorInfo.setText(R.string.signature_error_message_missing_bks_file);
					}
					catch (NoSuchAlgorithmException | KeyStoreException | NoSuchProviderException e) {
						e.printStackTrace();
						errorInfo.setText(R.string.signature_error_message_incompatible_device);
					}
					catch (UnrecoverableKeyException e) {
						e.printStackTrace();
						errorInfo.setText(R.string.signature_error_message_missing_alias);
					}
					catch (InvalidKeyException e) {
						e.printStackTrace();
						errorInfo.setText(R.string.signature_error_message_wrong_password);
					}
					catch (IllegalArgumentException e) {
						e.printStackTrace();
						errorInfo.setText(R.string.signature_error_message_no_data_to_sign);
					}
					catch (SignatureException | IllegalStateException e) {
						e.printStackTrace();
						errorInfo.setText(R.string.signature_error_message_unknown_error);
					}
					catch (CertificateException | IOException e) {
						e.printStackTrace();
						errorInfo.setText(R.string.signature_error_message_error_opening_bks_file);
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
