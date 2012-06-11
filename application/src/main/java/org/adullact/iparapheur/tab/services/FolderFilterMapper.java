package org.adullact.iparapheur.tab.services;

import java.util.List;
import java.util.Map;

import android.content.Context;

import roboguice.inject.ContextSingleton;

import com.google.inject.Inject;

import de.akquinet.android.androlog.Log;

import org.json.JSONException;
import org.json.JSONObject;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.OfficeFacet;
import org.adullact.iparapheur.tab.model.OfficeFacetChoice;
import org.adullact.iparapheur.tab.model.OfficeFacetChoices;
import org.adullact.iparapheur.tab.model.OfficeFacets;

@ContextSingleton
public class FolderFilterMapper
{

    private final Context context;

    private final OfficeFacets officeFacets;

    @Inject
    public FolderFilterMapper( Context context, OfficeFacets officeFacets )
    {
        this.context = context;
        this.officeFacets = officeFacets;
    }

    public JSONObject buildFilters( OfficeFacetChoices facetSelection )
    {
        if ( facetSelection == null || facetSelection.isEmpty() ) {
            return new JSONObject();
        }

        // Map OfficeFacet selection to iParapheur FolderFilters
        try {
            JSONObject filters = new JSONObject();
            if ( false ) { // TODO Activate OfficeFacets / FolderFilters mapping
                for ( Map.Entry<OfficeFacet, List<OfficeFacetChoice>> entry : facetSelection.entrySet() ) {
                    OfficeFacet facet = entry.getKey();
                    List<OfficeFacetChoice> selection = entry.getValue();
                    switch ( facet ) {
                        case STATE:
                            applyStateSelection( filters, selection );
                            break;
                        case TYPE:
                            applyGenericSelection( filters, selection, "cm:type" );
                            break;
                        case SUBTYPE:
                            applyGenericSelection( filters, selection, "cm:sousType" );
                            break;
                        case ACTION:
                            applyActionSelection( filters, selection );
                            break;
                        case SCHEDULE:
                            applyScheduleSelection( filters, selection );
                            break;
                        default:
                            throw new InternalError( "Unknown Facet " + facet + ", this should not happen." );
                    }
                }
            }
            return filters;
        } catch ( JSONException ex ) {
            throw new IParapheurHttpException( "Unable to build filters: " + ex.getMessage(), ex );
        }
    }

    private void applyGenericSelection( JSONObject filters, List<OfficeFacetChoice> selection, String filterName )
            throws JSONException
    {
        for ( OfficeFacetChoice filter : selection ) {
            filters.put( filterName, filter.displayName );
        }
    }

    private void applyStateSelection( JSONObject filters, List<OfficeFacetChoice> selection )
            throws JSONException
    {
        // TODO Map State OfficeFacets / FolderFilters
        for ( OfficeFacetChoice choice : selection ) {
            switch ( choice.id ) {
                case R.string.office_facets_state_todo:
                    break;
                case R.string.office_facets_state_late:
                    break;
                case R.string.office_facets_state_recoverable:
                    break;
                case R.string.office_facets_state_tocome:
                    break;
                case R.string.office_facets_state_done:
                    break;
                default:
                    Log.w( this, "Ignored unknown OfficeFacetChoice: " + choice );
            }
        }
    }

    private void applyActionSelection( JSONObject filters, List<OfficeFacetChoice> selection )
            throws JSONException
    {
        // TODO Map Action OfficeFacets / FolderFilters
        for ( OfficeFacetChoice choice : selection ) {
            switch ( choice.id ) {
                case R.string.office_facets_action_sign:
                    break;
                case R.string.office_facets_action_visa:
                    break;
                default:
                    Log.w( this, "Ignored unknown OfficeFacetChoice: " + choice );
            }
        }
    }

    private void applyScheduleSelection( JSONObject filters, List<OfficeFacetChoice> selection )
            throws JSONException
    {
        // TODO Map Schedule OfficeFacets / FolderFilters
        for ( OfficeFacetChoice choice : selection ) {
            switch ( choice.id ) {
                case R.string.office_facets_schedule_today:
                    break;
                case R.string.office_facets_schedule_week:
                    break;
                case R.string.office_facets_schedule_nextweek:
                    break;
                case R.string.office_facets_schedule_month:
                    break;
                case R.string.office_facets_schedule_nextmonth:
                    break;
                default:
                    Log.w( this, "Ignored unknown OfficeFacetChoice: " + choice );
            }
        }
    }

}
