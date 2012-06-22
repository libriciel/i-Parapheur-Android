package org.adullact.iparapheur.tab.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.adullact.iparapheur.tab.IParapheurTabException;

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

        private JSONObject toJSON()
                throws JSONException
        {
            JSONObject json = new JSONObject();
            json.put( "url", url );
            return json;
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

    public final String getTitle()
    {
        return title;
    }

    public final Integer getSize()
    {
        return size;
    }

    public final String getUrl()
    {
        return url;
    }

    public final List<FolderFilePageImage> getPageImages()
    {
        return Collections.unmodifiableList( pageImages );
    }

    public final void setPageImages( List<FolderFilePageImage> pageImages )
    {
        this.pageImages.clear();
        this.pageImages.addAll( pageImages );
    }

    @Override
    public String toString()
    {
        return "FolderFile{" + "title=" + title + ", pageImages=" + pageImages + '}';
    }

    public JSONObject getPageImagesJSON()
    {
        try {
            JSONObject json = new JSONObject();
            JSONArray pages = new JSONArray();
            for ( FolderFilePageImage page : pageImages ) {
                pages.put( page.toJSON() );
            }
            json.put( "pages", pages );
            return json;
        } catch ( JSONException ex ) {
            throw new IParapheurTabException( "Unable to generate File Pages JSON", ex );
        }
    }

}
