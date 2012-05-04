package org.adullact.iparapheur.tab.model;

public class Office
{

    private final String identity;

    private final String title;

    private final Community community;

    private String description;

    private String logo; // TODO Who is responsible for the resource inflation ?

    private Integer todoFolderCount;

    private Integer lateFolderCount;

    public Office( String identity, String title, String community )
    {
        this.identity = identity;
        this.title = title;
        this.community = new Community( community );
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

    public String getLogo()
    {
        return logo;
    }

    public String getTitle()
    {
        return title;
    }

    public Integer getTodoFolderCount()
    {
        return todoFolderCount;
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
