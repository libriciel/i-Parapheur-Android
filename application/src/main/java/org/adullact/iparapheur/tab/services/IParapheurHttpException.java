package org.adullact.iparapheur.tab.services;

public class IParapheurHttpException
        extends Exception
{

    public IParapheurHttpException( String string )
    {
        super( string );
    }

    public IParapheurHttpException( String string, Throwable thrwbl )
    {
        super( string, thrwbl );
    }

}
