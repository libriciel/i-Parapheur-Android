package org.adullact.iparapheur.tab.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import roboguice.inject.ContextSingleton;

import com.google.inject.Inject;

import org.adullact.iparapheur.tab.R;

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

    private final Map<Integer, String> stringsCache = new HashMap<Integer, String>();

    @Inject
    public OfficeFacets( Context context )
    {
        this.context = context;
        for ( OfficeFacet facet : OfficeFacet.values() ) {
            switch ( facet ) {
                case STATE:
                    titles.put( facet, string( R.string.office_facets_state_title ) );
                    choices.put( facet, Arrays.asList( new String[]{ "A traiter", "En retard", "Récupérables", "A venir", "Déjà traités" } ) );
                    break;
                case TYPE:
                    titles.put( facet, string( R.string.office_facets_type_title ) );
                    choices.put( facet, Arrays.asList( new String[]{ "Test", "Actes", "Demandes internes", "Helios Fast" } ) );
                    break;
                case SUBTYPE:
                    titles.put( facet, string( R.string.office_facets_subtype_title ) );
                    choices.put( facet, Arrays.asList( new String[]{ "FAST", "Arrêté du personnel", "Commande de matériel" } ) );
                    break;
                case ACTION:
                    titles.put( facet, string( R.string.office_facets_action_title ) );
                    choices.put( facet, Arrays.asList( new String[]{ "Signature", "Visa" } ) );
                    break;
                case SCHEDULE:
                    titles.put( facet, string( R.string.office_facets_schedule_title ) );
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
        if ( facetSelection.isEmpty() ) {
            return context.getResources().getString( R.string.office_facets_summary_none );
        }
        // TODO Implement filter summary generation
        return "Filtre: " + facetSelection.toString();
    }

    private String string( int id )
    {
        if ( stringsCache.containsKey( id ) ) {
            return stringsCache.get( id );
        }
        String string = context.getResources().getString( id );
        stringsCache.put( id, string );
        return string;
    }

}
