package org.adullact.iparapheur.tab.model;

/**
 * Created with IntelliJ IDEA.
 * User: jmaire
 * Date: 26/04/13
 * Time: 14:17
 */
public class SignInfo {
    public enum Format {
        CMS
    };

    private String dossierRef;
    private String hash;
    private Format format;

    //public SignInfo() {}

    public SignInfo(String dossierRef, String hash, String format) {
        this.dossierRef = dossierRef;
        this.hash = hash;
        this.format = Format.valueOf(format);
    }

    public String getDossierRef() {
        return dossierRef;
    }

    public void setDossierRef(String dossierRef) {
        this.dossierRef = dossierRef;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = Format.valueOf(format);
    }
}
