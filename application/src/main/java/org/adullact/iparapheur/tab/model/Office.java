package org.adullact.iparapheur.tab.model;

public class Office
{

    private String identity;

    private String title;

    private String description;

    private String community;

    private String logo; // TODO Who is responsible for the resource inflation ?

    private Integer todoFolderCount;

    private Integer lateFolderCount;

    public Office( String title, String community )
    {
        this.title = title;
        this.community = community;
    }

    public String getCommunity()
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

}
