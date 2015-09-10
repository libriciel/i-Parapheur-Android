package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.jcajce.JcaCertStore;
import org.spongycastle.cms.CMSException;
import org.spongycastle.cms.CMSProcessableByteArray;
import org.spongycastle.cms.CMSSignedData;
import org.spongycastle.cms.CMSSignedDataGenerator;
import org.spongycastle.cms.CMSTypedData;
import org.spongycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.spongycastle.util.Store;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class SignatureDialogFragment extends ActionDialogFragment implements View.OnClickListener {

	private static final String LOG_TAG = "SignatureDialogFragment";
	private static final int RESULT_CODE_FILE_SELECT = 0;
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
		KeyChain.choosePrivateKeyAlias(
				getActivity(), new KeyChainAliasCallback() {
					public void alias(final String alias) {

						selectedCertAlias = alias;

						try { addCertificateToKeyStore(alias); }
						catch (KeyStoreException | InterruptedException | NoSuchAlgorithmException | CertificateException
								| KeyChainException | IOException e) {e.printStackTrace();}

					}
				}, new String[]{"RSA"}, // List of acceptable key types. null for any
				null,                // issuer, null for any
				null,                // host name of server requesting the cert, null if unavailable
				-1,                  // port of server requesting the cert, -1 if unavailable
				null
		);               // alias to preselect, null if unavailable
	}

	private void addCertificateToKeyStore(@Nullable String alias) throws KeyStoreException, KeyChainException, InterruptedException, CertificateException, NoSuchAlgorithmException, IOException {

		// Load app's KeyStore

		KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
		keyStore.load(null);

		// Check if already imported, cancels the operation if any

		boolean alreadyExists = false;
		Enumeration<String> aliases = keyStore.aliases();

		while (aliases.hasMoreElements())
			if (TextUtils.equals(alias, aliases.nextElement()))
				alreadyExists = true;

		// Cancels if not valid data

//		if (TextUtils.isEmpty(alias) || alreadyExists)
//			return;

		// Import

		X509Certificate[] certificates = KeyChain.getCertificateChain(getActivity(), alias);
		PrivateKey privateKey = KeyChain.getPrivateKey(getActivity(), alias);

		if ((certificates != null) && (privateKey != null))
			keyStore.setKeyEntry(alias, privateKey.getEncoded(), certificates);
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
				catch (Exception e) {
					e.printStackTrace();
				}

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
	private @NonNull String getSignature(@Nullable String textToSign) throws GeneralSecurityException, IOException, CMSException, OperatorCreationException {

		// Default case

		if (TextUtils.isEmpty(textToSign))
			return "";

		// Use a PrivateKey in the KeyStore to create a signature over some data.

		KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
		keyStore.load(null);

		byte[] signedData = sign(keyStore, textToSign);
		return Base64.encodeToString(signedData, 0);
	}

	private PrivateKey getPrivateKey(KeyStore keystore) throws GeneralSecurityException, IOException {

		PrivateKey privateKey = (PrivateKey) keystore.getKey(selectedCertAlias, "123".toCharArray());
		Log.e("Adrien", "privateKey " + keystore.size());

		Enumeration<String> aliases = keystore.aliases();
		while (aliases.hasMoreElements())
			Log.i("Adrien", "alias... -" + aliases.nextElement() + "- | -" + selectedCertAlias + "-");

		Log.e("Adrien", "privateKey " + privateKey);
		return privateKey;
	}

	/**
	 * Retrieve the X509 Certificate form the KeyStore
	 *
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	private X509CertificateHolder getCert(KeyStore keyStore) throws GeneralSecurityException, IOException {
		java.security.cert.Certificate c = keyStore.getCertificate(selectedCertAlias);
		return new X509CertificateHolder(c.getEncoded());
	}

	public byte[] sign(KeyStore keystore, String data) throws GeneralSecurityException, CMSException, IOException, OperatorCreationException {

		List<X509CertificateHolder> certList = new ArrayList<>();
		CMSTypedData msg = new CMSProcessableByteArray(data.getBytes()); //Data to sign

		certList.add(getCert(keystore)); //Adding the X509 Certificate

		Store certs = new JcaCertStore(certList);

		CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
		//Initializing the the BC's Signer
		ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(getPrivateKey(keystore));

		gen.addSignerInfoGenerator(
				new JcaSignerInfoGeneratorBuilder(
						new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()
				).build(sha1Signer, getCert(keystore))
		);

		//adding the certificate

		gen.addCertificates(certs);

		//Getting the signed data

		CMSSignedData sigData = gen.generate(msg, false);
		return sigData.getEncoded();
	}

}
