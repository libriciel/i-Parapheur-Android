package org.adullact.iparapheur.tab.model;

import java.io.Serializable;

public class Account
        implements Serializable
{

    public static final long _serialVersionUID = 1L;

    private final String identity;

    private final String title;

    private final String url;

    private final String login;

    private final String password;

    public Account( String identity, String title, String url, String login, String password )
    {
        this.identity = identity;
        this.title = title;
        this.url = url;
        this.login = login;
        this.password = password;
    }

    public String getIdentity()
    {
        return identity;
    }

    public String getTitle()
    {
        return title;
    }

    public String getUrl()
    {
        return url;
    }

    public String getLogin()
    {
        return login;
    }

    public String getPassword()
    {
        return password;
    }

    @Override
    public String toString()
    {
        return "Account[ title: " + title + ", url: " + url + ", login: " + login + " ]";
    }

}
