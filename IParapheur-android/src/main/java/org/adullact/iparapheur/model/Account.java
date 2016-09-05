/*
 * <p>iParapheur Android<br/>
 * Copyright (C) 2016 Adullact-Projet.</p>
 *
 * <p>This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.</p>
 *
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.</p>
 *
 * <p>You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.</p>
 */
package org.adullact.iparapheur.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.adullact.iparapheur.utils.StringUtils;

import java.io.Serializable;


public class Account implements Serializable {

	private final String mId;
	private String mTitle;
	private String mName;
	private String mServerBaseUrl;
	private String mLogin;
	private String mTenant;
	private String mPassword;
	private String mTicket;
	private Integer mApiVersion;
	private Long mLastRequest;
	private boolean mActivated;

	public Account(String id) {
		mId = id;
		mTitle = "";
		mServerBaseUrl = "";
		mLogin = "";
		mPassword = "";
		mTenant = null;
		mLastRequest = 0L;
		mActivated = true;
	}

	// <editor-fold desc="Getters / Setters">

	public @NonNull String getId() {
		return mId;
	}

	public @NonNull String getTitle() {
		return mTitle;
	}

	public void setTitle(@NonNull String title) {
		mTitle = title;
	}

	public @NonNull String getServerBaseUrl() {
		return mServerBaseUrl;
	}

	public void setServerBaseUrl(@NonNull String url) {
		mServerBaseUrl = url;
	}

	public @NonNull String getLogin() {
		return mLogin;
	}

	public void setLogin(@NonNull String login) {

		mLogin = login;

		if (login.contains("@"))
			mTenant = login.substring(login.indexOf("@") + 1);
	}

	public @Nullable String getTenant() {
		return mTenant;
	}

	public @NonNull String getPassword() {
		return mPassword;
	}

	public void setPassword(@NonNull String password) {
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

	public String getUserName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public boolean isActivated() {
		return mActivated;
	}

	public void setActivated(boolean activated) {
		mActivated = activated;
	}

	// </editor-fold desc="Getters / Setters">

	/**
	 *
	 * @return
	 * @coveredInInstrumentedUnitTest
	 */
	public boolean isValid() {
		return validateAccount(mTitle, mLogin, mPassword, mServerBaseUrl);
	}

	public static boolean validateAccount(String title, String url, String login, String password) {
		return StringUtils.areNotEmpty(title, login, password) && StringUtils.isUrlValid(url);
	}

	@Override public boolean equals(Object o) {
		if (o instanceof Account) {
			Account toCompare = (Account) o;
			return TextUtils.equals(mId, toCompare.mId);
		}
		return false;
	}

	@Override public int hashCode() {
		return mId.hashCode();
	}

	@Override public String toString() {
		return mTitle;
	}
}
