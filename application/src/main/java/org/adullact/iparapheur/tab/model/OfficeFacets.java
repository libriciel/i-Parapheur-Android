package org.adullact.iparapheur.tab.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;

import android.content.Context;

import roboguice.inject.ContextSingleton;

import com.google.inject.Inject;

import org.codeartisans.java.toolbox.Couple;

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

    private final int typeRandomId;

    private final int subtypeRandomId;

    private final Context context;

    private final Map<Integer, String> stringsCache = new HashMap<Integer, String>();

    private final Map<OfficeFacet, String> titles = new EnumMap<OfficeFacet, String>( OfficeFacet.class );

    private final OfficeFacetChoices choices = new OfficeFacetChoices();

    @Inject
    public OfficeFacets( Context context )
    {
        this.context = context;
        Random rand = new Random();
        typeRandomId = rand.nextInt( Integer.MAX_VALUE - 100 );
        subtypeRandomId = rand.nextInt( Integer.MAX_VALUE - 100 );
        for ( OfficeFacet facet : OfficeFacet.values() ) {
            String facetTitle;
            List<OfficeFacetChoice> facetChoices = new ArrayList<OfficeFacetChoice>();
            switch ( facet ) {
                case ACTION:
                    facetTitle = string( R.string.office_facets_action_title );
                    facetChoices.add( new OfficeFacetChoice( string( R.string.office_facets_action_sign ), R.string.office_facets_action_sign ) );
                    facetChoices.add( new OfficeFacetChoice( string( R.string.office_facets_action_visa ), R.string.office_facets_action_visa ) );
                    break;
                case TYPE:
                    facetTitle = string( R.string.office_facets_type_title );
                    break;
                case SUBTYPE:
                    facetTitle = string( R.string.office_facets_subtype_title );
                    break;
                case SCHEDULE:
                    facetTitle = string( R.string.office_facets_schedule_title );
                    facetChoices.add( new OfficeFacetChoice( string( R.string.office_facets_schedule_today ), R.string.office_facets_schedule_today ) );
                    facetChoices.add( new OfficeFacetChoice( string( R.string.office_facets_schedule_week ), R.string.office_facets_schedule_week ) );
                    facetChoices.add( new OfficeFacetChoice( string( R.string.office_facets_schedule_nextweek ), R.string.office_facets_schedule_nextweek ) );
                    facetChoices.add( new OfficeFacetChoice( string( R.string.office_facets_schedule_month ), R.string.office_facets_schedule_month ) );
                    facetChoices.add( new OfficeFacetChoice( string( R.string.office_facets_schedule_nextmonth ), R.string.office_facets_schedule_nextmonth ) );
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

    public int iconId( OfficeFacet facet )
    {
        switch ( facet ) {
            case ACTION:
                return R.drawable.ic_facet_action;
            case TYPE:
                return R.drawable.ic_facet_type;
            case SUBTYPE:
                return R.drawable.ic_facet_subtype;
            case SCHEDULE:
                return R.drawable.ic_facet_schedule;
            default:
                return R.drawable.icon;
        }
    }

    public Couple<String[], Integer[]> rawChoices( OfficeFacet facet, SortedMap<String, List<String>> typology )
    {
        String[] choicesNames;
        Integer[] choicesIds;
        switch ( facet ) {
            case SUBTYPE:
                List<String> subtypes = new ArrayList<String>();
                for ( Map.Entry<String, List<String>> entry : typology.entrySet() ) {
                    for ( String subtype : entry.getValue() ) {
                        subtypes.add( subtype );
                    }
                }
                choicesNames = new String[ subtypes.size() ];
                choicesIds = new Integer[ choicesNames.length ];
                int subtypeIndex = 0;
                for ( String type : subtypes ) {
                    choicesNames[subtypeIndex] = type;
                    choicesIds[subtypeIndex] = subtypeRandomId + subtypeIndex;
                    subtypeIndex++;
                }
                break;
            case TYPE:
                choicesNames = new String[ typology.keySet().size() ];
                choicesIds = new Integer[ choicesNames.length ];
                int typeIndex = 0;
                for ( String type : typology.keySet() ) {
                    choicesNames[typeIndex] = type;
                    choicesIds[typeIndex] = typeRandomId + typeIndex;
                    typeIndex++;
                }
                break;
            default:
                choicesNames = new String[ choices.get( facet ).size() ];
                choicesIds = new Integer[ choicesNames.length ];
                int choiceIndex = 0;
                for ( OfficeFacetChoice choice : choices.get( facet ) ) {
                    choicesNames[choiceIndex] = choice.displayName;
                    choicesIds[choiceIndex] = choice.id;
                    choiceIndex++;
                }
        }
        return new Couple<String[], Integer[]>( choicesNames, choicesIds );
    }

    public String summary( OfficeFacetChoices facetSelection )
    {
        if ( facetSelection.isEmpty() ) {
            return context.getResources().getString( R.string.office_facets_summary_none );
        }
        StringBuilder summary = new StringBuilder();
        for ( Map.Entry<OfficeFacet, List<OfficeFacetChoice>> entry : facetSelection.entrySet() ) {
            switch ( entry.getKey() ) {
                case ACTION:
                    // TODO ACTION facet is unsupported for now, no filter summary generation
                    break;
                case TYPE:
                    if ( !entry.getValue().isEmpty() ) {
                        summary.append( "Dossiers du type métier " );
                        Iterator<OfficeFacetChoice> it = entry.getValue().iterator();
                        while ( it.hasNext() ) {
                            summary.append( it.next().displayName );
                            if ( it.hasNext() ) {
                                summary.append( " ou " );
                            }
                        }
                        summary.append( ".\n" );
                    }
                    break;
                case SUBTYPE:
                    if ( !entry.getValue().isEmpty() ) {
                        summary.append( "Dossiers du sous-type métier " );
                        Iterator<OfficeFacetChoice> it = entry.getValue().iterator();
                        while ( it.hasNext() ) {
                            summary.append( it.next().displayName );
                            if ( it.hasNext() ) {
                                summary.append( " ou " );
                            }
                        }
                        summary.append( ".\n" );
                    }
                    break;
                case SCHEDULE:
                    if ( !entry.getValue().isEmpty() ) {
                        summary.append( "Dossiers à traiter " );
                        Iterator<OfficeFacetChoice> it = entry.getValue().iterator();
                        while ( it.hasNext() ) {
                            summary.append( it.next().displayName.toLowerCase() );
                            if ( it.hasNext() ) {
                                summary.append( " ou " );
                            }
                        }
                        summary.append( ".\n" );
                    }
                    break;
            }
        }
        return summary.toString();
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
