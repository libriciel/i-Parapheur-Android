package org.adullact.iparapheur.tab.model;

public class OfficeFacetChoice
{

    public final String displayName;

    public final int id;

    public OfficeFacetChoice( String displayName, int id )
    {
        this.displayName = displayName;
        this.id = id;
    }

    @Override
    public String toString()
    {
        return displayName;
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
        final OfficeFacetChoice other = ( OfficeFacetChoice ) obj;
        if ( ( this.displayName == null ) ? ( other.displayName != null ) : !this.displayName.equals( other.displayName ) ) {
            return false;
        }
        if ( this.id != other.id ) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 37 * hash + ( this.displayName != null ? this.displayName.hashCode() : 0 );
        hash = 37 * hash + this.id;
        return hash;
    }

}
