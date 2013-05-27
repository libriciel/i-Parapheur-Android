package org.adullact.iparapheur.tab.util;

import org.spongycastle.asn1.x509.Certificate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jmaire
 * Date: 26/03/13
 * Time: 15:01
 */
public class CertUtils {

    public static List<Certificate> getCertificates() {
        String selectedAlias = null;

        return new ArrayList<Certificate>();
    }

    /*public static X509Certificate getCertificate(String ac, String id, String cn) throws Exception {

        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);

        KeyPair keyPair = CertUtils.loadKey("bma.p12");
        PrivateKey privateKey = keyPair.getPrivate();

        // TODO : choose .p12


        for (Enumeration<String> enumAlias = store.aliases(); enumAlias.hasMoreElements();) {
            String actualAlias = enumAlias.nextElement();
            if (ks.entryInstanceOf(actualAlias, KeyStore.PrivateKeyEntry.class)) {
                X509Certificate actualCert = (X509Certificate) ks.getCertificate(actualAlias);
                HashMap<String, String> certInfos = CertificateInfosExtractor.makeSubjectInfos(actualCert);
                if (certInfos.get("CN").equalsIgnoreCase(ac) && certInfos.get("ID").equalsIgnoreCase(id) && certInfos.get("CN").equalsIgnoreCase(cn)) {
                    if (isValidCertificate(actualCert)) {
                        return actualCert;
                    } else {
                        throw new CertificateException("Invalid Certificate");
                    }
                }
            }
        }

        throw new CertificateException("No matching certificate in user KeyStore");

    }

    private static KeyPair loadKey(String fileName) {
        try {
            File privateKeyFile = new File(Environment.getExternalStorageDirectory(), "bma.p12");
            PEMReader r = new PEMReader(new InputStreamReader(new FileInputStream(privateKeyFile)));
            return (KeyPair) r.readObject();
        } catch (IOException e) {
            Log.e("CertUtils", "Failed to load key from " + fileName, e);
            throw new RuntimeException(e);
        }
    }*/
}
