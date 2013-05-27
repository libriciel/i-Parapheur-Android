package org.adullact.iparapheur.tab.util;


import android.util.Log;

import java.security.*;

/**
 * Created with IntelliJ IDEA.
 * User: jmaire
 * Date: 26/03/13
 * Time: 10:14
 */
public class SignUtils {

    static final String DIGEST_ALG = "SHA1";

    /*static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }*/



    public static String CMSSign(String hash, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Log.i("debug", "[CMSSign] hash : " + hash);
        byte[] data = TransformUtils.hexStringToByteArray(hash);
        Signature sig = Signature.getInstance("NONEwithRSA"); // try RSA, NONEwithRSA, SHA1withRSA
        sig.initSign(privateKey);
        sig.update(data);
        byte[] signed = sig.sign();
        Log.i("debug", "[CMSSign] final signature String : " + TransformUtils.encode(TransformUtils.der2pem(signed)));
        return TransformUtils.encode(TransformUtils.der2pem(signed));
    }

}
