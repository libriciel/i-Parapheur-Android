package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.security.KeyChainException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.LoadingWithProgressTask;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;

public class SignatureDialogFragment extends ActionDialogFragment implements View.OnClickListener {

	private static final String LOG_TAG = "SignatureDialogFragment";
	private static final String ARGUMENTS_DOSSIERS = "dossiers";
	private static final String ARGUMENTS_BUREAU_ID = "bureauId";

	protected TextView annotationPublique;
	protected TextView annotationPrivee;

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

	// Cert chooser button click
	@Override public void onClick(View v) {
		KeyChain.choosePrivateKeyAlias(
				getActivity(), new KeyChainAliasCallback() {
					public void alias(final String alias) {
						selectedCertAlias = alias;
						getActivity().runOnUiThread(
								new Runnable() {
									public void run() {
										certInfo.setText(alias);
									}
								}
						);
					}
				},
				// FIXME :  only RSA?
				new String[]{"RSA"}, // List of acceptable key types. null for any
				null,                // issuer, null for any
				null,                // host name of server requesting the cert, null if unavailable
				-1,                  // port of server requesting the cert, -1 if unavailable
				null
		);               // alias to preselect, null if unavailable
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

				try { signValue = getSignature(signInfo); }
				catch (NullPointerException | InterruptedException | KeyChainException | NoSuchAlgorithmException
						| SignatureException | InvalidKeyException | UnsupportedEncodingException e) { e.printStackTrace(); }

				// Send result, if any

				Log.i("Adrien", "Signature...");
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

	/**
	 * https://developer.android.com/training/articles/keystore.html#SigningAndVerifyingData
	 */
	private @NonNull String getSignature(@Nullable String textToSign) throws KeyChainException, InterruptedException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException {

		// Default case

		if (TextUtils.isEmpty(textToSign))
			return "";

		// Use a PrivateKey in the KeyStore to create a signature over some data.

		PrivateKey privateKey = KeyChain.getPrivateKey(getActivity(), selectedCertAlias);

		Signature s = Signature.getInstance("SHA1withRSA");
		s.initSign(privateKey);
		s.update(textToSign.getBytes("UTF-8"));
		byte[] signature = s.sign();

		return Base64.encodeToString(signature, 0);
	}
}
