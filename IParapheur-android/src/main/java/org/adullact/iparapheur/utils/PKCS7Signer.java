package org.adullact.iparapheur.utils;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.pkcs.PKCS9Attributes;
import sun.security.pkcs.SignerInfo;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X500Name;

public final class PKCS7Signer {

	private String mCertificatePath;
	static final String DIGEST_ALG = "SHA1";
	private String mKeystorePassword = "bmabma";
	private String mAlias = "bma";
	private String mAliasPassword = "bma";
	private PrivateKey mPrivateKey;
	private KeyStore mKeystore;

	public PKCS7Signer(@NonNull String certificatePath) {
		mCertificatePath = certificatePath;
	}

	public KeyStore loadKeyStore(@NonNull String path) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {

		mKeystore = KeyStore.getInstance("BKS");
		InputStream is = new FileInputStream(path);
		mKeystore.load(is, mKeystorePassword.toCharArray());

		return mKeystore;
	}

	public PrivateKey loadPrivateKey() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {

		mPrivateKey = (PrivateKey) mKeystore.getKey(mAlias, mAliasPassword.toCharArray());
		return mPrivateKey;
	}

	public String sign(byte[] dataToSign) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, NoSuchProviderException, InvalidKeyException, SignatureException {

		Log.e("Adrien", "Here ? " + (mPrivateKey != null));

		KeyStore keyStore = loadKeyStore(mCertificatePath);
		X509Certificate certificate = (X509Certificate) keyStore.getCertificate(mAlias);

//		String content = "some bytes to be signed";
//		dataToSign = content.getBytes("UTF-8");
//		CMSSignedDataGenerator signatureGenerator = setUpProvider(keyStore);
//		byte[] signedBytes = signPkcs7(dataToSign, signatureGenerator);

		//

		Security.addProvider(new BouncyCastleProvider());
		AlgorithmId digestAlg = AlgorithmId.get(DIGEST_ALG);

		// signature alg
		Signature sig = Signature.getInstance("SHA1WithRSA", "BC");

		// p9 attributes
		PKCS9Attribute[] authenticatedAttributeList = {
				new PKCS9Attribute(PKCS9Attribute.SIGNING_TIME_OID, new Date()),
				new PKCS9Attribute(PKCS9Attribute.CONTENT_TYPE_OID, ContentInfo.DATA_OID),
				new PKCS9Attribute(PKCS9Attribute.MESSAGE_DIGEST_OID, dataToSign)
		};

		PKCS9Attributes authenticatedAttributes = new PKCS9Attributes(authenticatedAttributeList);

		// signed attributes computation
		sig.initSign(mPrivateKey);
		sig.update(authenticatedAttributes.getDerEncoding());
		byte[] signedAttributes = sig.sign();

		// detached signature
		ContentInfo contentInfo = new ContentInfo(ContentInfo.DATA_OID, null);

		// signer serial
		java.math.BigInteger serial = certificate.getSerialNumber();

//		Log.i("Adrien", ">> " + certificate.getIssuerDN().getName());
		Log.i("Adrien", ">> " + certificate.getIssuerDN().getName());
//		Log.i("Adrien", ">> " + serial);
//		Log.i("Adrien", ">> " + digestAlg);
//		Log.i("Adrien", ">> " + authenticatedAttributes);
//		Log.i("Adrien", ">> " + new AlgorithmId(AlgorithmId.RSAEncryption_oid));
//		Log.i("Adrien", ">> " + signedAttributes); "EMAIL=systeme@adullact.org,CN=AC ADULLACT Projet g2,OU=ADULLACT-Projet,O=ADULLACT-Projet,ST=Herault,C=FR"

		SignerInfo signerInfo = new SignerInfo(

				new X500Name(StringUtils.fixIssuerDnX500NameStringOrder(certificate.getIssuerDN().getName())),
				serial,
				digestAlg,
				authenticatedAttributes,
				new AlgorithmId(AlgorithmId.RSAEncryption_oid),
				signedAttributes,
				null
		);

		X509Certificate[] certificateList = {certificate};

		AlgorithmId[] algs = {digestAlg};
		SignerInfo[] infos = {signerInfo};
		PKCS7 p7Signature = new PKCS7(algs, contentInfo, certificateList, infos);
		ByteArrayOutputStream nbaos = new ByteArrayOutputStream();
		p7Signature.encodeSignedData(nbaos);

		return Base64.encodeToString(nbaos.toByteArray(), Base64.DEFAULT);
	}
}