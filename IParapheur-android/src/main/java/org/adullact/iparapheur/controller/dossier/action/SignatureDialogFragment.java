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
import org.spongycastle.cms.CMSProcessableByteArray;
import org.spongycastle.cms.CMSSignedData;
import org.spongycastle.cms.CMSSignedDataGenerator;
import org.spongycastle.cms.CMSTypedData;
import org.spongycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.spongycastle.util.Store;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
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

		File bks = getBksFromDownloadFolder();

		if (bks == null)
			return;

		try { mKeystore = getKeystore(bks, "bmabma".toCharArray()); }
		catch (GeneralSecurityException | IOException e) { e.printStackTrace(); }

		Log.i("Adrien", "keystore : " + KeyStore.getDefaultType() + " " + (mKeystore != null));

		if (mKeystore == null)
			return;

		PrivateKey privateKey = null;
		try { privateKey = getPrivateKey(mKeystore); }
		catch (GeneralSecurityException | IOException e) { e.printStackTrace(); }

		X509CertificateHolder certficateHolder = null;
		try { certficateHolder = getCert(mKeystore); }
		catch (GeneralSecurityException | IOException e) { e.printStackTrace(); }

		Log.i("Adrien", "privateKey : " + (privateKey != null));
		Log.i("Adrien", "certficateHolder : " + (certficateHolder != null));

		mPrivateKey = privateKey;
		mCertificateHolder = certficateHolder;
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
				catch (Exception e) { e.printStackTrace(); }

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
	private @NonNull String getSignature(@Nullable String textToSign) throws Exception {

		// Default case

		if (TextUtils.isEmpty(textToSign))
			return "";

		// Use a PrivateKey in the KeyStore to create a signature over some data.

		CMSSignedDataGenerator signatureGenerator = setUpProvider(mKeystore);
		byte[] signedBytes = signPkcs7(hexDecode(textToSign), signatureGenerator);
		Log.e("Adrien", "sign + " + Base64.encodeToString(signedBytes, 0));

		return Base64.encodeToString(signedBytes, 0);
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

	private @Nullable KeyStore getKeystore(@NonNull File bks, char[] password) throws GeneralSecurityException, IOException {

		KeyStore keystore = KeyStore.getInstance("BKS");
		InputStream input = new FileInputStream(bks);

		try { keystore.load(input, password); }
		catch (IOException e) { e.printStackTrace(); }
		finally { input.close(); }

		return keystore;
	}

	private @Nullable PrivateKey getPrivateKey(@NonNull KeyStore keystore) throws GeneralSecurityException, IOException {

		PrivateKey privateKey = (PrivateKey) keystore.getKey("bma", "bma".toCharArray());
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
		java.security.cert.Certificate c = keyStore.getCertificate("bma");
		return new X509CertificateHolder(c.getEncoded());
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

	private CMSSignedDataGenerator setUpProvider(final KeyStore keystore) throws Exception {

		Security.addProvider(new BouncyCastleProvider());

		Certificate[] certchain = keystore.getCertificateChain("bma");

		final List<Certificate> certlist = new ArrayList<>();

		for (int i = 0, length = certchain == null ? 0 : certchain.length; i < length; i++) {
			certlist.add(certchain[i]);
		}

		Store certstore = new JcaCertStore(certlist);

		Certificate cert = keystore.getCertificate("bma");

		ContentSigner signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").
				build((PrivateKey) (keystore.getKey("bma", "bma".toCharArray())));

		CMSSignedDataGenerator generator = new CMSSignedDataGenerator();

		generator.addSignerInfoGenerator(
				new JcaSignerInfoGeneratorBuilder(
						new JcaDigestCalculatorProviderBuilder().setProvider("BC").
								build()
				).build(signer, (X509Certificate) cert)
		);

		generator.addCertificates(certstore);

		return generator;
	}

	private byte[] signPkcs7(final byte[] content, final CMSSignedDataGenerator generator) throws Exception {

		CMSTypedData cmsdata = new CMSProcessableByteArray(content);
		CMSSignedData signeddata = generator.generate(cmsdata, true);
		return signeddata.getEncoded();
	}

	// </editor-fold desc="Signature">
}
