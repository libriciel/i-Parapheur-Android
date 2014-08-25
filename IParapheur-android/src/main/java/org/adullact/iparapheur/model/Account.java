package org.adullact.iparapheur.model;

import org.adullact.iparapheur.controller.rest.api.IParapheurAPI;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by jmaire on 23/10/13.
 */
public class Account implements Serializable
{

    public static final long _serialVersionUID = 1L;

    private final String id;
    private String title;
    private String url;
    private String login;
    private String tenant;
    private String password;
    private String ticket;
    private Integer apiVersion;

    public Account(String id)
    {
        this.id = id;
        this.title = "";
        this.url = "";
        this.login = "";
        this.password = "";
        this.tenant = null;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Account){
            Account toCompare = (Account) o;
            return this.id.equals(toCompare.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString()
    {
        return title;
    }

    public boolean isValid()
    {
        return (title != null && !title.isEmpty()
                && login != null && !login.isEmpty()
                && password != null && !password.isEmpty()
                && isURLValid(url));
    }

    public static boolean validateAccount(String title, String url, String login, String password)
    {
        return (title != null && !title.isEmpty()
                && login != null && !login.isEmpty()
                && password != null && !password.isEmpty()
                && isURLValid(url));
    }

    private static boolean isURLValid(String url)
    {
        if (url == null || url.isEmpty()) {
            return false;
        }
        try {
            new URL(IParapheurAPI.BASE_PATH + url );
            return true;
        } catch ( MalformedURLException ignored ) {
            return false;
        }
    }


    // GETTERS and SETTERS

    public String getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
        if (login != null) {
            int separatorIndex = login.indexOf("@");
            if (separatorIndex != -1) {
                this.tenant = login.substring(separatorIndex + 1);
            }
        }
    }

    public String getTenant() {
        return tenant;
    }

    public String getPassword()
    {
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

    public void setApiVersion(Integer apiVersion) {
        this.apiVersion = apiVersion;
    }

    public Integer getApiVersion() {
        return apiVersion;
    }
}
