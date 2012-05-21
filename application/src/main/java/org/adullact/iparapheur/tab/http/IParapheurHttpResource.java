package org.adullact.iparapheur.tab.http;

public enum IParapheurHttpResource
{

    login( "/parapheur/api/login" ),
    logout( "/parapheur/api/logout" ),
    offices( "/parapheur/api/getBureaux" ),
    folders( "/parapheur/api/getDossiersHeaders" ),
    folder( "/parapheur/api/getDossier" );

    private final String path;

    private IParapheurHttpResource( String path )
    {
        this.path = path;
    }

    public String path()
    {
        return path;
    }

}
