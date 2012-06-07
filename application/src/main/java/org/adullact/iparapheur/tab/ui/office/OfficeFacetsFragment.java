package org.adullact.iparapheur.tab.ui.office;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

import com.google.inject.Inject;

import org.codeartisans.java.toolbox.ObjectHolder;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.OfficeFacet;
import org.adullact.iparapheur.tab.model.OfficeFacets;

public class OfficeFacetsFragment
        extends RoboFragment
{

    public static interface OnSelectionChangeListener
    {

        void facetSelectionChanged( Map<OfficeFacet, Collection<String>> selection );

    }

    private class OnFacetClickListener
            implements View.OnClickListener
    {

        private final OfficeFacet facet;

        public OnFacetClickListener( OfficeFacet facet )
        {
            this.facet = facet;
        }

        public void onClick( View view )
        {
            buildDialog( facet ).show();
        }

    }

    private final Map<OfficeFacet, Collection<String>> facetSelection = new EnumMap<OfficeFacet, Collection<String>>( OfficeFacet.class );

    private OnSelectionChangeListener selectionChangeListener;

    @Inject
    private OfficeFacets officeFacets;

    @InjectView( R.id.office_facet_state )
    private Button stateButton;

    @InjectView( R.id.office_facet_type )
    private Button typeButton;

    @InjectView( R.id.office_facet_subtype )
    private Button subtypeButton;

    @InjectView( R.id.office_facet_action )
    private Button actionButton;

    @InjectView( R.id.office_facet_schedule )
    private Button scheduleButton;

    @InjectView( R.id.office_facet_summary )
    private TextView facetSummary;

    public Map<OfficeFacet, Collection<String>> getFacetSelection()
    {
        return Collections.unmodifiableMap( facetSelection );
    }

    public void setOnSelectionChangedListener( OnSelectionChangeListener listener )
    {
        selectionChangeListener = listener;
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        return inflater.inflate( R.layout.office_facets, container, false );
    }

    @Override
    public void onViewCreated( View view, Bundle savedInstanceState )
    {
        super.onViewCreated( view, savedInstanceState );
        stateButton.setOnClickListener( new OnFacetClickListener( OfficeFacet.STATE ) );
        typeButton.setOnClickListener( new OnFacetClickListener( OfficeFacet.TYPE ) );
        subtypeButton.setOnClickListener( new OnFacetClickListener( OfficeFacet.SUBTYPE ) );
        actionButton.setOnClickListener( new OnFacetClickListener( OfficeFacet.ACTION ) );
        scheduleButton.setOnClickListener( new OnFacetClickListener( OfficeFacet.SCHEDULE ) );
    }

    private synchronized Dialog buildDialog( final OfficeFacet facet )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );

        // Titles & Choices
        builder.setTitle( officeFacets.title( facet ) );
        final String[] choices = officeFacets.choices( facet ).toArray( new String[]{} );

        // Dialog State
        final List<String> selectedItems = new ArrayList<String>();
        final ObjectHolder<Boolean> dirtyHolder = new ObjectHolder<Boolean>( Boolean.FALSE );

        // Initial Selection
        boolean[] initial = new boolean[ choices.length ];
        for ( int idx = 0; idx < choices.length; idx++ ) {
            String choice = choices[idx];
            if ( facetSelection.containsKey( facet ) && facetSelection.get( facet ).contains( choice ) ) {
                initial[idx] = true;
                selectedItems.add( choice );
            } else {
                initial[idx] = false;
            }
        }

        // Dialog Control
        builder.setMultiChoiceItems( choices, initial, new DialogInterface.OnMultiChoiceClickListener()
        {

            public void onClick( DialogInterface dialog, int index, boolean selected )
            {
                String choice = choices[index];
                if ( selected ) {
                    if ( !selectedItems.contains( choice ) ) {
                        dirtyHolder.setHolded( Boolean.TRUE );
                        selectedItems.add( choice );
                    }
                } else {
                    if ( selectedItems.contains( choice ) ) {
                        dirtyHolder.setHolded( Boolean.TRUE );
                        selectedItems.remove( choice );
                    }
                }
            }

        } );

        // Apply
        builder.setPositiveButton( "Appliquer", new DialogInterface.OnClickListener()
        {

            public void onClick( DialogInterface dialog, int id )
            {
                if ( dirtyHolder.getHolded() ) {
                    fireFacetSelectionChanged( facet, selectedItems );
                }
            }

        } );

        // Cancel
        // TODO Change Facet Cancel button to Facet Reset
        builder.setNegativeButton( "Annuler", new DialogInterface.OnClickListener()
        {

            public void onClick( DialogInterface dialog, int id )
            {
                dialog.cancel();
            }

        } );

        AlertDialog alert = builder.create();
        return alert;
    }

    private void fireFacetSelectionChanged( OfficeFacet facet, List<String> facetSelectedItems )
    {
        if ( facetSelectedItems.isEmpty() ) {
            facetSelection.remove( facet );
        } else {
            facetSelection.put( facet, new ArrayList<String>( facetSelectedItems ) );
        }

        updateFilterSummary();

        if ( selectionChangeListener != null ) {
            // Use getter so we pass a reference to an unmodifiable Map
            selectionChangeListener.facetSelectionChanged( getFacetSelection() );
        }
    }

    private void updateFilterSummary()
    {
        facetSummary.setText( officeFacets.summary( facetSelection ) );
    }

}
