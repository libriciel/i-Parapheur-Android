package org.adullact.iparapheur.tab.model;

public abstract class AbstractFolderFile
{

    private final String title;

    private final Integer size;

    private final String url;

    public AbstractFolderFile( String title, Integer size, String url )
    {
        this.title = title;
        this.size = size;
        this.url = url;
    }

    public String getTitle()
    {
        return title;
    }

    public Integer getSize()
    {
        return size;
    }

    public String getUrl()
    {
        return url;
    }

}
