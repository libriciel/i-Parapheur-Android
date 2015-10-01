package org.adullact.iparapheur.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

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

	private static final String DIGEST_ALG = "SHA1";
	private static final String SIGN_ALGO = "SHA1WithRSA";

	private KeyStore mKeystore;
	private PrivateKey mPrivateKey;
	private String mCertificatePath;
	private String mKeystorePassword;
	private String mAlias;
	private String mAliasPassword;

	public PKCS7Signer(@NonNull String certificatePath, @NonNull String keystorePassword, @NonNull String alias, @NonNull String aliasPassword) {
		mCertificatePath = certificatePath;
		mKeystorePassword = keystorePassword;
		mAlias = alias;
		mAliasPassword = aliasPassword;
	}

	public @Nullable KeyStore loadKeyStore() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {

		mKeystore = KeyStore.getInstance("BKS");
		InputStream is = new FileInputStream(mCertificatePath);
		mKeystore.load(is, mKeystorePassword.toCharArray());

		return mKeystore;
	}

	public @Nullable PrivateKey loadPrivateKey() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {

		mPrivateKey = (PrivateKey) mKeystore.getKey(mAlias, mAliasPassword.toCharArray());
		return mPrivateKey;
	}

	public String sign(byte[] dataToSign) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException, IllegalStateException, IllegalArgumentException, KeyStoreException, IOException {

		// Init checking

		if (mKeystore == null)
			throw new IllegalStateException("PKCS7Signer not properly initialized, call PKCS7Signer#loakKeystore() before signature");

		if (mPrivateKey == null)
			throw new IllegalStateException("PKCS7Signer not properly initialized, call PKCS7Signer#loakPrivateKey() before signature");

		if (dataToSign == null)
			throw new IllegalArgumentException("PKCS7Signer exception : Data to sign cannot be null");

		// Signed attributes computation

		Security.addProvider(new BouncyCastleProvider());

		PKCS9Attribute[] authenticatedAttributeList = {
				new PKCS9Attribute(PKCS9Attribute.SIGNING_TIME_OID, new Date()),
				new PKCS9Attribute(PKCS9Attribute.CONTENT_TYPE_OID, ContentInfo.DATA_OID),
				new PKCS9Attribute(PKCS9Attribute.MESSAGE_DIGEST_OID, dataToSign)
		};
		PKCS9Attributes authenticatedAttributes = new PKCS9Attributes(authenticatedAttributeList);

		Signature sig = Signature.getInstance(SIGN_ALGO, "BC");
		sig.initSign(mPrivateKey);
		sig.update(authenticatedAttributes.getDerEncoding());
		byte[] signedAttributes = sig.sign();

		// Signer serial info

		X509Certificate certificate = (X509Certificate) mKeystore.getCertificate(mAlias);
		java.math.BigInteger serial = certificate.getSerialNumber();
		AlgorithmId digestAlg = AlgorithmId.get(DIGEST_ALG);

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

		// Signature

		ContentInfo contentInfo = new ContentInfo(ContentInfo.DATA_OID, null);
		AlgorithmId[] algs = {digestAlg};
		SignerInfo[] infos = {signerInfo};
		PKCS7 p7Signature = new PKCS7(algs, contentInfo, certificateList, infos);
		ByteArrayOutputStream signedBytesStream = new ByteArrayOutputStream();
		p7Signature.encodeSignedData(signedBytesStream);

		return Base64.encodeToString(signedBytesStream.toByteArray(), Base64.DEFAULT);
	}

}