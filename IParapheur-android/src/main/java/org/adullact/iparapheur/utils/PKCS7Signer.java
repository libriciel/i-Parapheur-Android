package org.adullact.iparapheur.utils;

import android.support.annotation.NonNull;
import android.util.Base64;

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

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public final class PKCS7Signer {

	private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

	private String mCertificatePath;
	private String mKeystorePassword = "bmabma";
	private String mAlias = "bma";
	private String mAliasPassword = "bma";

	public PKCS7Signer(@NonNull String certificatePath) {
		mCertificatePath = certificatePath;
	}

	public KeyStore loadKeyStore(@NonNull String path) throws Exception {

		KeyStore keystore = KeyStore.getInstance("BKS");
		InputStream is = new FileInputStream(path);
		keystore.load(is, mKeystorePassword.toCharArray());
		return keystore;
	}

	public CMSSignedDataGenerator setUpProvider(@NonNull final KeyStore keystore) throws Exception {

		Security.addProvider(new BouncyCastleProvider());

		Certificate[] certchain = keystore.getCertificateChain(mAlias);
		final List<Certificate> certlist = new ArrayList<>();

		if ((certchain != null) && (certchain.length > 0))
			certlist.add(certchain[0]);

		Store certstore = new JcaCertStore(certlist);

		Certificate cert = keystore.getCertificate(mAlias);

		ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).setProvider("BC").
				build((PrivateKey) (keystore.getKey(mAlias, mAliasPassword.toCharArray())));

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

	public byte[] signPkcs7(final byte[] content, final CMSSignedDataGenerator generator) throws Exception {

		CMSTypedData cmsdata = new CMSProcessableByteArray(content);
		CMSSignedData signeddata = generator.generate(cmsdata, true);
		return signeddata.getEncoded();
	}

	public String sign(byte[] dataToSign) throws Exception {

		KeyStore keyStore = loadKeyStore(mCertificatePath);
		CMSSignedDataGenerator signatureGenerator = setUpProvider(keyStore);
		byte[] signedBytes = signPkcs7(dataToSign, signatureGenerator);
		return Base64.encodeToString(signedBytes, Base64.DEFAULT);
	}
}