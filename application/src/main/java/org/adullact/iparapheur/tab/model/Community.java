package org.adullact.iparapheur.tab.model;

import java.io.Serializable;

public class Community
        implements Serializable
{

    public static final long _serialVersionUID = 1L;

    private final String name;

    public Community( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final Community other = ( Community ) obj;
        if ( ( this.name == null ) ? ( other.name != null ) : !this.name.equals( other.name ) ) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        return hash;
    }

}
