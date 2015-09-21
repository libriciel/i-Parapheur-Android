package org.adullact.iparapheur.utils;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.pkcs.PKCS9Attributes;
import sun.security.pkcs.SignerInfo;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X500Name;

public final class PKCS7Signer {

	private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

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

	public KeyStore loadKeyStore(@NonNull String path) throws Exception {

		mKeystore = KeyStore.getInstance("BKS");
		InputStream is = new FileInputStream(path);
		mKeystore.load(is, mKeystorePassword.toCharArray());

		return mKeystore;
	}

	public PrivateKey loadPrivateKey() throws Exception {

		mPrivateKey = (PrivateKey) mKeystore.getKey(mAlias, mAliasPassword.toCharArray());
		return mPrivateKey;
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
		CMSSignedData signeddata = generator.generate(cmsdata, false);

		return signeddata.getEncoded();
	}

	public String sign(byte[] dataToSign) throws Exception {

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
//		Log.i("Adrien", ">> " + signedAttributes);

		SignerInfo signerInfo = new SignerInfo(

				new X500Name("EMAIL=systeme@adullact.org,CN=AC ADULLACT Projet g2,OU=ADULLACT-Projet,O=ADULLACT-Projet,ST=Herault,C=FR"),
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