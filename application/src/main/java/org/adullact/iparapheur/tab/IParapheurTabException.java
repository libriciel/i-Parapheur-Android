package org.adullact.iparapheur.tab;

public class IParapheurTabException
        extends RuntimeException
{

    public IParapheurTabException( String string )
    {
        super( string );
    }

    public IParapheurTabException( Throwable thrwbl )
    {
        super( thrwbl );
    }

    public IParapheurTabException( String string, Throwable thrwbl )
    {
        super( string, thrwbl );
    }

}
