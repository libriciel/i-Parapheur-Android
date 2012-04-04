package org.adullact.iparapheur.tab.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Folder
{

    private String title;

    private FolderRequestedAction requestedAction;

    private String businessType;

    private String businessSubType;

    private final List<Document> documents = new ArrayList<Document>();

    private final List<Annex> annexes = new ArrayList<Annex>();

    public Folder( String title, FolderRequestedAction requestedAction, String businessType, String businessSubType )
    {
        this.title = title;
        this.requestedAction = requestedAction;
        this.businessType = businessType;
        this.businessSubType = businessSubType;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle( String title )
    {
        this.title = title;
    }

    public String getBusinessType()
    {
        return businessType;
    }

    public void setBusinessType( String businessType )
    {
        this.businessType = businessType;
    }

    public String getBusinessSubType()
    {
        return businessSubType;
    }

    public void setBusinessSubType( String businessSubType )
    {
        this.businessSubType = businessSubType;
    }

    public FolderRequestedAction getRequestedAction()
    {
        return requestedAction;
    }

    public void setRequestedAction( FolderRequestedAction requestedAction )
    {
        this.requestedAction = requestedAction;
    }

    public boolean addDocument( Document folderDocument )
    {
        return documents.add( folderDocument );
    }

    public boolean addAllDocuments( Document... folderDocuments )
    {
        return documents.addAll( Arrays.asList( folderDocuments ) );
    }

    public void clearDocuments()
    {
        documents.clear();
    }

    public List<Document> getDocuments()
    {
        return Collections.unmodifiableList( documents );
    }

    public boolean addAnnex( Annex folderAnnex )
    {
        return annexes.add( folderAnnex );
    }

    public boolean addAllAnnexes( Annex... folderAnnexes )
    {
        return annexes.addAll( Arrays.asList( folderAnnexes ) );
    }

    public void clearAnnexes()
    {
        annexes.clear();
    }

    public List<Annex> getAnnexes()
    {
        return Collections.unmodifiableList( annexes );
    }

    public List<FolderFile> getAllFiles()
    {
        List<FolderFile> allFiles = new ArrayList<FolderFile>( documents );
        allFiles.addAll( annexes );
        return Collections.unmodifiableList( allFiles );
    }

}
