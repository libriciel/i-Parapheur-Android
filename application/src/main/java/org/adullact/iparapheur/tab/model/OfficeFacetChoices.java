package org.adullact.iparapheur.tab.model;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class OfficeFacetChoices
        extends EnumMap<OfficeFacet, List<OfficeFacetChoice>>
        implements Map<OfficeFacet, List<OfficeFacetChoice>>
{

    public OfficeFacetChoices()
    {
        super( OfficeFacet.class );
    }

    public boolean contains( OfficeFacet facet, int id )
    {
        if ( containsKey( facet ) ) {
            for ( OfficeFacetChoice choice : get( facet ) ) {
                if ( choice.id == id ) {
                    return true;
                }
            }
        }
        return false;
    }

    public OfficeFacetChoice get( OfficeFacet facet, int id )
    {
        if ( containsKey( facet ) ) {
            for ( OfficeFacetChoice choice : get( facet ) ) {
                if ( choice.id == id ) {
                    return choice;
                }
            }
        }
        return null;
    }

}
