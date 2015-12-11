package org.adullact.iparapheur.utils;

/**
 * There should be an official one like this,
 * TODO : find it, and remove this class
 */
public class HttpException extends IParapheurException {

	private int responseCode;

	public HttpException(final int responseCode) {
		super(-1, null);
		this.responseCode = responseCode;
	}

	public int getResponseCode() {
		return this.responseCode;
	}
}
