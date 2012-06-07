package org.adullact.iparapheur.tab.model;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import roboguice.inject.ContextSingleton;

import com.google.inject.Inject;

import org.adullact.iparapheur.tab.R;

/**
 * OfficeFacet helper for views.
 * 
 * Load reference data from resources, handling localization, build
 * OfficeFacetChoice instances.
 */
@ContextSingleton
public class OfficeFacets
{

    private final Context context;

    private final Map<OfficeFacet, String> titles = new EnumMap<OfficeFacet, String>( OfficeFacet.class );

    private final Map<OfficeFacet, Map<String, Integer>> choices = new EnumMap<OfficeFacet, Map<String, Integer>>( OfficeFacet.class );

    private final Map<Integer, String> stringsCache = new HashMap<Integer, String>();

    @Inject
    public OfficeFacets( Context context )
    {
        this.context = context;
        for ( OfficeFacet facet : OfficeFacet.values() ) {
            String facetTitle;
            Map<String, Integer> facetChoices = new HashMap<String, Integer>();
            switch ( facet ) {
                case STATE:
                    facetTitle = string( R.string.office_facets_state_title );
                    facetChoices.put( string( R.string.office_facets_state_todo ), R.string.office_facets_state_todo );
                    facetChoices.put( string( R.string.office_facets_state_late ), R.string.office_facets_state_late );
                    facetChoices.put( string( R.string.office_facets_state_recoverable ), R.string.office_facets_state_recoverable );
                    facetChoices.put( string( R.string.office_facets_state_tocome ), R.string.office_facets_state_tocome );
                    facetChoices.put( string( R.string.office_facets_state_done ), R.string.office_facets_state_done );
                    break;
                case TYPE:
                    facetTitle = string( R.string.office_facets_type_title );
                    facetChoices.put( "Test", null );
                    facetChoices.put( "Actes", null );
                    facetChoices.put( "Demandes internes", null );
                    facetChoices.put( "Helios Fast", null );
                    break;
                case SUBTYPE:
                    facetTitle = string( R.string.office_facets_subtype_title );
                    facetChoices.put( "FAST", null );
                    facetChoices.put( "Arrêté du personnel", null );
                    facetChoices.put( "Commande de matériel", null );
                    break;
                case ACTION:
                    facetTitle = string( R.string.office_facets_action_title );
                    facetChoices.put( string( R.string.office_facets_action_sign ), R.string.office_facets_action_sign );
                    facetChoices.put( string( R.string.office_facets_action_visa ), R.string.office_facets_action_visa );
                    break;
                case SCHEDULE:
                    facetTitle = string( R.string.office_facets_schedule_title );
                    facetChoices.put( string( R.string.office_facets_schedule_today ), R.string.office_facets_schedule_today );
                    facetChoices.put( string( R.string.office_facets_schedule_week ), R.string.office_facets_schedule_week );
                    facetChoices.put( string( R.string.office_facets_schedule_nextweek ), R.string.office_facets_schedule_nextweek );
                    facetChoices.put( string( R.string.office_facets_schedule_month ), R.string.office_facets_schedule_month );
                    facetChoices.put( string( R.string.office_facets_schedule_nextmonth ), R.string.office_facets_schedule_nextmonth );
                    break;
                default:
                    throw new InternalError( "Unknown Facet " + facet + ", this should not happen." );
            }
            titles.put( facet, facetTitle );
            choices.put( facet, facetChoices );
        }
    }

    public String title( OfficeFacet facet )
    {
        return titles.get( facet );
    }

    public Collection<String> choices( OfficeFacet facet )
    {
        return choices.get( facet ).keySet();
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
