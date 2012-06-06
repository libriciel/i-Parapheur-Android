package org.adullact.iparapheur.tab.services;

import org.adullact.iparapheur.tab.IParapheurTabException;

public class IParapheurHttpException
        extends IParapheurTabException
{

    public IParapheurHttpException( String string )
    {
        super( string );
    }

    public IParapheurHttpException( Throwable thrwbl )
    {
        super( thrwbl.getMessage(), thrwbl );
    }

    public IParapheurHttpException( String string, Throwable thrwbl )
    {
        super( string, thrwbl );
    }

}
