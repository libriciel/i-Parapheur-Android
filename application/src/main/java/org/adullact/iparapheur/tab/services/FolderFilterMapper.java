package org.adullact.iparapheur.tab.services;

import java.util.Collection;
import java.util.Map;

import android.content.Context;

import roboguice.inject.ContextSingleton;

import com.google.inject.Inject;

import org.json.JSONException;
import org.json.JSONObject;

import org.adullact.iparapheur.tab.model.OfficeFacet;
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

    public JSONObject buildFilters( Map<OfficeFacet, Collection<String>> facetSelection )
    {
        if ( facetSelection.isEmpty() ) {
            return new JSONObject();
        }

        // Map OfficeFacet selection to iParapheur FolderFilters
        try {
            JSONObject filters = new JSONObject();
            if ( false ) { // TODO Activate OfficeFacets / FolderFilters mapping
                for ( Map.Entry<OfficeFacet, Collection<String>> entry : facetSelection.entrySet() ) {
                    OfficeFacet facet = entry.getKey();
                    Collection<String> selection = entry.getValue();
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

    private void applyGenericSelection( JSONObject filters, Collection<String> selection, String filterName )
            throws JSONException
    {
        for ( String filter : selection ) {
            filters.put( filterName, filter );
        }
    }

    private void applyStateSelection( JSONObject filters, Collection<String> selection )
            throws JSONException
    {
        // TODO Map State OfficeFacets / FolderFilters
    }

    private void applyActionSelection( JSONObject filters, Collection<String> selection )
            throws JSONException
    {
        for ( String item : selection ) {
            
            
        }
        // TODO Map Action OfficeFacets / FolderFilters
    }

    private void applyScheduleSelection( JSONObject filters, Collection<String> selection )
            throws JSONException
    {
        // TODO Map Schedule OfficeFacets / FolderFilters
    }

}
