package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.LoadingWithProgressTask;
import org.adullact.iparapheur.utils.PKCS7Signer;
import org.spongycastle.cert.X509CertificateHolder;

import java.io.File;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.ArrayList;

public class SignatureDialogFragment extends ActionDialogFragment implements View.OnClickListener {

	private static final String LOG_TAG = "SignatureDialogFragment";
	private static final int RESULT_CODE_FILE_SELECT = 0;
	private static final String ARGUMENTS_DOSSIERS = "dossiers";
	private static final String ARGUMENTS_BUREAU_ID = "bureauId";

	protected TextView annotationPublique;
	protected TextView annotationPrivee;

	private KeyStore mKeystore;
	private PrivateKey mPrivateKey;
	private X509CertificateHolder mCertificateHolder;
	private TextView certInfo;
	private String selectedCertAlias;
	private String signInfo;

	public SignatureDialogFragment() {}

	public static SignatureDialogFragment newInstance(@NonNull ArrayList<Dossier> dossiers, @NonNull String bureauId) {
		SignatureDialogFragment fragment = new SignatureDialogFragment();

		// Supply parameters as an arguments.
		Bundle args = new Bundle();
		args.putParcelableArrayList(ARGUMENTS_DOSSIERS, dossiers);
		args.putString(ARGUMENTS_BUREAU_ID, bureauId);
		fragment.setArguments(args);

		return fragment;
	}

	// <editor-fold desc="LifeCycle">

	@Override protected View createView() {

		View layout = super.createView();

		annotationPublique = (TextView) layout.findViewById(R.id.action_dialog_annotation_publique);
		annotationPrivee = (TextView) layout.findViewById(R.id.action_dialog_annotation_privee);

		layout.findViewById(R.id.action_dialog_signature_certificate_button).setOnClickListener(this);
		certInfo = (TextView) layout.findViewById(R.id.action_dialog_signature_cert_info);

		return layout;
	}

	@Override public void onStart() {
		super.onStart();

		new AsyncTask<Void, Void, Void>() {
			@Override protected Void doInBackground(Void... params) {

				if ((dossiers != null) && (!dossiers.isEmpty())) {

					String dossierId = dossiers.get(0).getId();
					try { signInfo = RESTClient.INSTANCE.getSignInfo(dossierId, bureauId).getHash(); }
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

	@Override protected int getTitle() {
		return Action.SIGNATURE.getTitle();
	}

	@Override protected int getViewId() {
		return R.layout.action_dialog_signature;
	}

	@Override protected void executeTask() {
		new SignTask(getActivity()).execute();
	}

	@Override public void onClick(View v) {

	}

	private class SignTask extends LoadingWithProgressTask {

		public SignTask(Activity activity) {
			super(activity, listener);
		}

		@Override protected void load(String... params) throws IParapheurException {

			if (isCancelled())
				return;

			String annotPub = annotationPublique.getText().toString();
			String annotPriv = annotationPrivee.getText().toString();

			publishProgress(0);

			int total = dossiers.size();
			for (int i = 0; i < total; i++) {

				Dossier dossier = dossiers.get(i);
				String signValue = "";

				// Sign data

				File certif = getBksFromDownloadFolder();

				if (certif != null) {
					PKCS7Signer signer = new PKCS7Signer(certif.getAbsolutePath());
					try { signValue = signer.sign(hexDecode(signInfo)); }
					catch (Exception e) { e.printStackTrace(); }
				}

				// Send result, if any

				Log.i("Adrien", "Signature... ");
				Log.i("Adrien", "Data  : " + signInfo);
				Log.i("Adrien", "Value : " + signValue);

				if (TextUtils.isEmpty(signValue))
					return; // TODO : Throw back error message

				RESTClient.INSTANCE.signer(dossier.getId(), signValue, annotPub, annotPriv, bureauId);
				publishProgress(i * 100 / total);

				if (isCancelled())
					return;
			}
		}

	}

	// <editor-fold desc="Signature">

	private @Nullable File getBksFromDownloadFolder() {

		File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		File jks = null;

		if (folder.listFiles() != null)
			for (File file : folder.listFiles())
				if (file.getName().endsWith("bks"))
					jks = file;

		return jks;
	}

	/**
	 * Decode the Hexadecimal char sequence (as string) into Byte Array.
	 *
	 * @param data The Hex encoded sequence to be decoded.
	 * @return Decoded byte array.
	 * @throws IllegalArgumentException <var>data</var> when wrong number of chars is given or invalid chars.
	 */
	private static byte[] hexDecode(String data) {

		int length = data.length();
		if ((length % 2) != 0)
			throw new IllegalArgumentException("Odd number of characters.");

		try {
			byte[] bytes = new byte[length / 2];

			for (int i = 0, j = 0; i < length; i = i + 2)
				bytes[j++] = (byte) Integer.parseInt(data.substring(i, i + 2), 16);

			return bytes;
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("Illegal hexadecimal character.", e);
		}
	}

	// </editor-fold desc="Signature">
}
