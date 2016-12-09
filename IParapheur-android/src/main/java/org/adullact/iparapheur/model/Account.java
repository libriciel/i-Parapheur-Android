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

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.adullact.iparapheur.utils.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@DatabaseTable(tableName = "Account")
public class Account implements Serializable {

	@DatabaseField(columnName = "Id", id = true, index = true)  //
	private String mId;

	@DatabaseField(columnName = "Title", canBeNull = false, defaultValue = "")  //
	private String mTitle;

	@DatabaseField(columnName = "BaseUrl", canBeNull = false, defaultValue = "")  //
	private String mServerBaseUrl;

	@DatabaseField(columnName = "Login", canBeNull = false, defaultValue = "")  //
	private String mLogin;

	@DatabaseField(columnName = "Password", canBeNull = false, defaultValue = "")  //
	private String mPassword;

	@DatabaseField(columnName = "UserFullName")  //
	private String mUserFullName;

	@DatabaseField(columnName = "Tenant")  //
	private String mTenant;

	@DatabaseField(columnName = "ApiVersion")  //
	private Integer mApiVersion;

	@DatabaseField(columnName = "Activated")  //
	private boolean mActivated;

	@DatabaseField(columnName = "LastRequest")  //
	private Date mLastRequest;

	@ForeignCollectionField(columnName = "Desks")  //
	private ForeignCollection<Bureau> mChildrenBureaux;

	private String mTicket;

	public Account() {}

	public Account(String id) {
		mId = id;
		mTitle = "";
		mServerBaseUrl = "";
		mLogin = "";
		mPassword = "";
		mTenant = null;
		mActivated = true;
	}

	public Account(@NonNull String id, @NonNull String title, @NonNull String serverBaseUrl, @NonNull String login, @NonNull String password,
				   @Nullable String tenant, @Nullable Integer apiVersion) {
		mId = id;
		mTitle = title;
		mServerBaseUrl = serverBaseUrl;
		mLogin = login;
		mPassword = password;
		mTenant = tenant;
		mApiVersion = apiVersion;
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

	public String getUserFullName() {
		return mUserFullName;
	}

	public void setUserFullName(String userFullName) {
		mUserFullName = userFullName;
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

	public boolean isActivated() {
		return mActivated;
	}

	public void setActivated(boolean activated) {
		mActivated = activated;
	}

	public Date getLastRequest() {
		return mLastRequest;
	}

	public void setLastRequest(Date lastRequest) {
		mLastRequest = lastRequest;
	}

	public ForeignCollection<Bureau> getChildrenBureaux() {
		return mChildrenBureaux;
	}

	// </editor-fold desc="Getters / Setters">

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
