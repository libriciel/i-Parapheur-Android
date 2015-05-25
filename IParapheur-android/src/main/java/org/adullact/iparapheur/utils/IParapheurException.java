package org.adullact.iparapheur.utils;

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
