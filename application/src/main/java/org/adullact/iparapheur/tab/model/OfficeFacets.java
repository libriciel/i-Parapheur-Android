package org.adullact.iparapheur.tab.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import roboguice.inject.ContextSingleton;

import com.google.inject.Inject;

/**
 * OfficeFacet helper for views.
 * 
 * Load reference data from resources and handle localization.
 * 
 * TODO Load resources from android values to support i18n.
 */
@ContextSingleton
public class OfficeFacets
{

    private final Context context;

    private final Map<OfficeFacet, String> titles = new EnumMap<OfficeFacet, String>( OfficeFacet.class );

    private final Map<OfficeFacet, List<String>> choices = new EnumMap<OfficeFacet, List<String>>( OfficeFacet.class );

    @Inject
    public OfficeFacets( Context context )
    {
        this.context = context;
        for ( OfficeFacet facet : OfficeFacet.values() ) {
            switch ( facet ) {
                case STATE:
                    titles.put( facet, "Filtrer par Étât" );
                    choices.put( facet, Arrays.asList( new String[]{ "A traiter", "En retard", "Récupérables", "A venir", "Déjà traités" } ) );
                    break;
                case TYPE:
                    titles.put( facet, "Filtrer par Type" );
                    choices.put( facet, Arrays.asList( new String[]{ "Foo", "Bar", "Baz" } ) );
                    break;
                case SUBTYPE:
                    titles.put( facet, "Filtrer par Sous-type" );
                    choices.put( facet, Arrays.asList( new String[]{ "Yo", "Howdy", "M8" } ) );
                    break;
                case ACTION:
                    titles.put( facet, "Filtrer par Action" );
                    choices.put( facet, Arrays.asList( new String[]{ "Signature", "Visa" } ) );
                    break;
                case SCHEDULE:
                    titles.put( facet, "Filtrer par Échéance" );
                    choices.put( facet, Arrays.asList( new String[]{ "Aujourd'hui", "Cette semaine", "La semaine prochaine", "Ce mois-ci", "Le mois prochain" } ) );
                    break;
                default:
                    throw new InternalError( "Unknown Facet " + facet + ", this should not happen." );
            }
        }
    }

    public String title( OfficeFacet facet )
    {
        return titles.get( facet );
    }

    public List<String> choices( OfficeFacet facet )
    {
        return choices.get( facet );
    }

    public String summary( Map<OfficeFacet, Collection<String>> facetSelection )
    {
        // TODO Implement filter summary generation
        if ( facetSelection.isEmpty() ) {
            return "Aucun filtre.";
        } else {
            return "Filtre: " + facetSelection.toString();
        }
    }

}
