package org.adullact.iparapheur.tab.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractFolderFile
{

    public static class FolderFilePageImage
    {

        public final String url;

        public FolderFilePageImage( String url )
        {
            this.url = url;
        }

        @Override
        public String toString()
        {
            return "FolderFilePageImage{" + "url=" + url + '}';
        }

    }

    private final String title;

    private final Integer size;

    private final String url;

    private final List<FolderFilePageImage> pageImages = new ArrayList<FolderFilePageImage>();

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

    public List<FolderFilePageImage> getPageImages()
    {
        return Collections.unmodifiableList( pageImages );
    }

    public void setPageImages( List<FolderFilePageImage> pageImages )
    {
        this.pageImages.clear();
        this.pageImages.addAll( pageImages );
    }

    @Override
    public String toString()
    {
        return "FolderFile{" + "title=" + title + ", pageImages=" + pageImages + '}';
    }

}
