package org.adullact.iparapheur.utils;

/**
 * Created by jmaire on 20/08/2014.
 */
public class IParapheurException extends Exception {

    private int resId;
    private String complement;

    public IParapheurException(int resId, String complement) {
        super();
        this.resId = resId;
        this.complement = complement;
    }

    public int getResId() {
        return resId;
    }

    public String getComplement() {
        return complement;
    }
}
