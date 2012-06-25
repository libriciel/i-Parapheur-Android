package org.adullact.iparapheur.tab.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.format.DateUtils;

import roboguice.inject.ContextSingleton;

import com.google.inject.Inject;

import de.akquinet.android.androlog.Log;

import org.json.JSONArray;
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

    private static final String TYPE_FILTER = "ph:typeMetier";

    private static final String SUBTYPE_FILTER = "ph:sousTypeMetier";

    private static final String DUE_DATE_FILTER = "ph:dateLimite";

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
            JSONArray and = new JSONArray();
            for ( Map.Entry<OfficeFacet, List<OfficeFacetChoice>> entry : facetSelection.entrySet() ) {
                JSONObject orWrapper = new JSONObject();
                JSONArray or = new JSONArray();
                OfficeFacet facet = entry.getKey();
                List<OfficeFacetChoice> selection = entry.getValue();
                switch ( facet ) {
                    case ACTION:
                        applyActionSelection( or, selection );
                        break;
                    case TYPE:
                        applyGenericSelection( or, selection, TYPE_FILTER );
                        break;
                    case SUBTYPE:
                        applyGenericSelection( or, selection, SUBTYPE_FILTER );
                        break;
                    case SCHEDULE:
                        applyScheduleSelection( or, selection );
                        break;
                    default:
                        throw new InternalError( "Unknown Facet " + facet + ", this should not happen." );
                }
                orWrapper.put( "or", or );
                and.put( orWrapper );
            }
            filters.put( "and", and );
            return filters;
        } catch ( JSONException ex ) {
            throw new IParapheurHttpException( "Unable to build filters: " + ex.getMessage(), ex );
        }
    }

    private void applyActionSelection( JSONArray filterHolder, List<OfficeFacetChoice> selection )
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

    private void applyGenericSelection( JSONArray filterHolder, List<OfficeFacetChoice> selection, String filterName )
            throws JSONException
    {
        for ( OfficeFacetChoice filter : selection ) {
            JSONObject jsonfilter = new JSONObject();
            jsonfilter.put( filterName, filter.displayName );
            filterHolder.put( jsonfilter );
        }
    }

    private void applyScheduleSelection( JSONArray filterHolder, List<OfficeFacetChoice> selection )
            throws JSONException
    {
        long now = System.currentTimeMillis();
        DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        JSONObject jsonFilter = new JSONObject();
        for ( OfficeFacetChoice choice : selection ) {
            switch ( choice.id ) {
                case R.string.office_facets_schedule_today:
                    String filter = "[" + dateFormat.format( new Date( now - ( DateUtils.WEEK_IN_MILLIS * 4 ) ) )
                                    + " TO " + dateFormat.format( new Date() ) + "]";
                    jsonFilter.put( DUE_DATE_FILTER, filter );
                    break;
                case R.string.office_facets_schedule_week:
                    filter = "[NOW TO " + dateFormat.format( new Date( now + DateUtils.WEEK_IN_MILLIS ) ) + "]";
                    jsonFilter.put( DUE_DATE_FILTER, filter );
                    break;
                case R.string.office_facets_schedule_nextweek:
                    filter = "[" + dateFormat.format( new Date( now + DateUtils.WEEK_IN_MILLIS ) )
                             + " TO " + dateFormat.format( new Date( now + DateUtils.WEEK_IN_MILLIS * 2 ) ) + "]";
                    jsonFilter.put( DUE_DATE_FILTER, filter );
                    break;
                case R.string.office_facets_schedule_month:
                    filter = "[NOW TO " + dateFormat.format( new Date( now + DateUtils.WEEK_IN_MILLIS * 4 ) ) + "]";
                    jsonFilter.put( DUE_DATE_FILTER, filter );
                    break;
                case R.string.office_facets_schedule_nextmonth:
                    filter = "[" + dateFormat.format( new Date( now + DateUtils.WEEK_IN_MILLIS * 4 ) )
                             + " TO " + dateFormat.format( new Date( now + DateUtils.WEEK_IN_MILLIS * 8 ) ) + "]";
                    jsonFilter.put( DUE_DATE_FILTER, filter );
                    break;
                default:
                    Log.w( this, "Ignored unknown OfficeFacetChoice: " + choice );
            }
        }
        filterHolder.put( jsonFilter );
    }

}
