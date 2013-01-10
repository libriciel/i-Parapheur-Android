package org.adullact.iparapheur.tab.model;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import org.adullact.iparapheur.tab.services.StaticHttpClient;
import org.codeartisans.java.toolbox.Strings;

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

    public boolean validates()
    {
        return !( Strings.isEmpty( title )
                  || Strings.isEmpty( url )
                  || Strings.isEmpty( login )
                  || Strings.isEmpty( password ) )
               && validUrl( url );
    }

    private static boolean validUrl( String url )
    {
        try {
            new URL(StaticHttpClient.BASE_PATH + url );
            return true;
        } catch ( MalformedURLException ignored ) {
            return false;
        }
    }

}
