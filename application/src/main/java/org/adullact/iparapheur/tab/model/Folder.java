package org.adullact.iparapheur.tab.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Folder
        implements Serializable
{

    public static final long _serialVersionUID = 1L;

    private final String identity;

    private final String title;

    private final FolderRequestedAction requestedAction;

    private final String businessType;

    private final String businessSubType;

    private final List<FolderDocument> documents = new ArrayList<FolderDocument>();

    private final List<FolderAnnex> annexes = new ArrayList<FolderAnnex>();

    public Folder( String identity, String title, FolderRequestedAction requestedAction, String businessType, String businessSubType )
    {
        this.identity = identity;
        this.title = title;
        this.requestedAction = requestedAction;
        this.businessType = businessType;
        this.businessSubType = businessSubType;
    }

    public String getIdentity()
    {
        return identity;
    }

    public String getTitle()
    {
        return title;
    }

    public String getBusinessType()
    {
        return businessType;
    }

    public String getBusinessSubType()
    {
        return businessSubType;
    }

    public FolderRequestedAction getRequestedAction()
    {
        return requestedAction;
    }

    public boolean addDocument( FolderDocument folderDocument )
    {
        return documents.add( folderDocument );
    }

    public boolean addAllDocuments( FolderDocument... folderDocuments )
    {
        return documents.addAll( Arrays.asList( folderDocuments ) );
    }

    public void clearDocuments()
    {
        documents.clear();
    }

    public List<FolderDocument> getDocuments()
    {
        return Collections.unmodifiableList( documents );
    }

    public boolean addAnnex( FolderAnnex folderAnnex )
    {
        return annexes.add( folderAnnex );
    }

    public boolean addAllAnnexes( FolderAnnex... folderAnnexes )
    {
        return annexes.addAll( Arrays.asList( folderAnnexes ) );
    }

    public void clearAnnexes()
    {
        annexes.clear();
    }

    public List<FolderAnnex> getAnnexes()
    {
        return Collections.unmodifiableList( annexes );
    }

    public List<AbstractFolderFile> getAllFiles()
    {
        List<AbstractFolderFile> allFiles = new ArrayList<AbstractFolderFile>( documents );
        allFiles.addAll( annexes );
        return Collections.unmodifiableList( allFiles );
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder( "Folder{" );
        sb.append( "identity=" ).append( identity ).append( ", " );
        sb.append( "title=" ).append( title ).append( ", " );
        sb.append( "requestedAction=" ).append( requestedAction );
        if ( !documents.isEmpty() ) {
            sb.append( ", document=[" );
            Iterator<FolderDocument> it = documents.iterator();
            while ( it.hasNext() ) {
                sb.append( it.next().getTitle() );
                if ( it.hasNext() ) {
                    sb.append( ", " );
                }
            }
            sb.append( ']' );
        }
        sb.append( '}' );
        return sb.toString();
    }

}
