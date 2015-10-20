package org.adullact.iparapheur.model;

import android.text.TextUtils;

import org.adullact.iparapheur.controller.rest.api.IParapheurAPI;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;


public class Account implements Serializable {

	public static final long _serialVersionUID = 1L;

	private final String id;
	private String title;
	private String url;
	private String login;
	private String tenant;
	private String password;
	private String ticket;
	private Integer apiVersion;
	private Long lastRequest;

	public Account(String id) {
		this.id = id;
		this.title = "";
		this.url = "";
		this.login = "";
		this.password = "";
		this.tenant = null;
		this.lastRequest = 0L;
	}

	// <editor-fold desc="Getters / Setters">

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;

		if (login != null)
			if (login.contains("@"))
				this.tenant = login.substring(login.indexOf("@") + 1);
	}

	public String getTenant() {
		return tenant;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public Integer getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(Integer apiVersion) {
		this.apiVersion = apiVersion;
	}

	public Long getLastRequest() {
		return lastRequest;
	}

	public void setLastRequest(Long lastRequest) {
		this.lastRequest = lastRequest;
	}

	// </editor-fold desc="Getters / Setters">

	private static boolean isURLValid(String url) {

		if (TextUtils.isEmpty(url))
			return false;

		try {
			new URL(IParapheurAPI.BASE_PATH + url);
			return true;
		}
		catch (MalformedURLException ignored) {
			return false;
		}
	}

	public boolean isValid() {
		return (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(login) && !TextUtils.isEmpty(password) && isURLValid(url));
	}

	public static boolean validateAccount(String title, String url, String login, String password) {
		return (title != null && !title.isEmpty() && login != null && !login.isEmpty() && password != null && !password.isEmpty() && isURLValid(url));
	}

	@Override public boolean equals(Object o) {
		if (o instanceof Account) {
			Account toCompare = (Account) o;
			return TextUtils.equals(id, toCompare.id);
		}
		return false;
	}

	@Override public int hashCode() {
		return id.hashCode();
	}

	@Override public String toString() {
		return title;
	}
}
