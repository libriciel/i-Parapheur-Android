package org.adullact.iparapheur.tab;

public class IParapheurException
        extends RuntimeException
{

    public IParapheurException( String string )
    {
        super( string );
    }

    public IParapheurException( Throwable thrwbl )
    {
        super( thrwbl );
    }

    public IParapheurException( String string, Throwable thrwbl )
    {
        super( string, thrwbl );
    }

}
