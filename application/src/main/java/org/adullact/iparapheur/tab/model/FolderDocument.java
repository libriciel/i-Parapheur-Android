package org.adullact.iparapheur.tab.model;

import java.io.Serializable;

public class FolderDocument
        extends AbstractFolderFile
        implements Serializable
{

    public static final long _serialVersionUID = 1L;

    public FolderDocument( String title, String url )
    {
        super( title, url );
    }

}
