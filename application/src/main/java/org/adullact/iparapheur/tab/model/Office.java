package org.adullact.iparapheur.tab.model;

import java.io.Serializable;

public class Office
        implements Serializable
{

    public static final long _serialVersionUID = 1L;

    private final String identity;

    private final String title;

    private final Community community;

    private String description;

    private String logoUrl;

    private Integer todoFolderCount;

    private Integer lateFolderCount;

    private final String accountIdentity;

    public Office( String identity, String title, String community, String accountIdentity )
    {
        this.identity = identity;
        this.title = title;
        this.community = new Community( community );
        this.accountIdentity = accountIdentity;
    }

    public String getAccountIdentity()
    {
        return accountIdentity;
    }

    public Community getCommunity()
    {
        return community;
    }

    public String getDescription()
    {
        return description;
    }

    public String getIdentity()
    {
        return identity;
    }

    public Integer getLateFolderCount()
    {
        return lateFolderCount;
    }

    public String getLogoUrl()
    {
        return logoUrl;
    }

    public String getTitle()
    {
        return title;
    }

    public Integer getTodoFolderCount()
    {
        return todoFolderCount;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public void setLogoUrl( String logoUrl )
    {
        this.logoUrl = logoUrl;
    }

    public void setTodoFolderCount( Integer todoFolderCount )
    {
        this.todoFolderCount = todoFolderCount;
    }

    public void setLateFolderCount( Integer lateFolderCount )
    {
        this.lateFolderCount = lateFolderCount;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == null || o instanceof Office == false ) {
            return false;
        }
        return identity.equals( ( ( Office ) o ).identity );
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 53 * hash + ( this.identity != null ? this.identity.hashCode() : 0 );
        return hash;
    }

    @Override
    public String toString()
    {
        return "Office{" + "identity=" + identity + ", title=" + title + ", community=" + community + ", todoFolderCount=" + todoFolderCount + ", lateFolderCount=" + lateFolderCount + '}';
    }

}
