package org.adullact.iparapheur.model;

import android.text.TextUtils;
import android.webkit.URLUtil;

import org.adullact.iparapheur.utils.StringUtils;

import java.io.Serializable;


public class Account implements Serializable {

	@SuppressWarnings("unused") public static final long _serialVersionUID = 1L;

	private final String mId;
	private String mTitle;
	private String mUrl;
	private String mLogin;
	private String mTenant;
	private String mPassword;
	private String mTicket;
	private Integer mApiVersion;
	private Long mLastRequest;

	public Account(String id) {
		mId = id;
		mTitle = "";
		mUrl = "";
		mLogin = "";
		mPassword = "";
		mTenant = null;
		mLastRequest = 0L;
	}

	// <editor-fold desc="Getters / Setters">

	public String getId() {
		return mId;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		mUrl = url;
	}

	public String getLogin() {
		return mLogin;
	}

	public void setLogin(String login) {

		mLogin = login;

		if (login != null) {
			int separatorIndex = login.indexOf("@");

			if (separatorIndex != -1)
				mTenant = login.substring(separatorIndex + 1);
		}
	}

	public String getTenant() {
		return mTenant;
	}

	public String getPassword() {
		return mPassword;
	}

	public void setPassword(String password) {
		mPassword = password;
	}

	public String getTicket() {
		return mTicket;
	}

	public void setTicket(String ticket) {
		mTicket = ticket;
	}

	public Integer getApiVersion() {
		return mApiVersion;
	}

	public void setApiVersion(Integer apiVersion) {
		mApiVersion = apiVersion;
	}

	public Long getLastRequest() {
		return mLastRequest;
	}

	public void setLastRequest(Long lastRequest) {
		mLastRequest = lastRequest;
	}

	// </editor-fold desc="Getters / Setters">

	public static boolean validateAccount(String title, String url, String login, String password) {
		return StringUtils.areNotEmpty(title, url, login, password) && URLUtil.isValidUrl(url);
	}

	public boolean isValid() {
		return validateAccount(mTitle, mUrl, mLogin, mPassword);
	}

	@Override public boolean equals(Object o) {

		if (o instanceof Account) {
			Account toCompare = (Account) o;
			return TextUtils.equals(mId, toCompare.getId());
		}

		return false;
	}

	@Override public int hashCode() {
		return mId.hashCode();
	}

	@Override public String toString() {
		return "{Account title:" + mTitle + " id:" + mId + "}";
	}
}
