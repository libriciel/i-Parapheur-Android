package org.adullact.iparapheur.tab.model;

public abstract class FolderFile
{

    private String title;

    private String url;

    public FolderFile( String title, String url )
    {
        this.title = title;
        this.url = url;
    }

    public String getTitle()
    {
        return title;
    }

    public String getUrl()
    {
        return url;
    }

}
