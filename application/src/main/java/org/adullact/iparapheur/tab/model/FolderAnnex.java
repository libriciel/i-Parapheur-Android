package org.adullact.iparapheur.tab.model;

import java.io.Serializable;

public class FolderAnnex
        extends AbstractFolderFile
        implements Serializable
{

    public static final long _serialVersionUID = 1L;

    public FolderAnnex( String title, String url )
    {
        super( title, url );
    }

}
