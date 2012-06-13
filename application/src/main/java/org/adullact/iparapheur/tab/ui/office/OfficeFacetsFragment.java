package org.adullact.iparapheur.tab.ui.office;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

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

import org.codeartisans.java.toolbox.Couple;
import org.codeartisans.java.toolbox.ObjectHolder;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.OfficeFacet;
import org.adullact.iparapheur.tab.model.OfficeFacetChoice;
import org.adullact.iparapheur.tab.model.OfficeFacetChoices;
import org.adullact.iparapheur.tab.model.OfficeFacets;

public class OfficeFacetsFragment
        extends RoboFragment
{

    public static interface OnSelectionChangeListener
    {

        void facetSelectionChanged( Map<OfficeFacet, List<OfficeFacetChoice>> selection );

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

    private final OfficeFacetChoices facetSelection = new OfficeFacetChoices();

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

    private SortedMap<String, List<String>> typology;

    void setOfficeTypology( SortedMap<String, List<String>> typology )
    {
        this.typology = typology;
    }

    public OfficeFacetChoices getFacetSelection()
    {
        return facetSelection;
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
        builder.setIcon( officeFacets.iconId( facet ) );
        Couple<String[], Integer[]> rawChoices = officeFacets.rawChoices( facet, typology );
        final String[] choicesNames = rawChoices.left();
        final Integer[] choicesIds = rawChoices.right();

        // Dialog State
        final List<OfficeFacetChoice> selectedItems = new ArrayList<OfficeFacetChoice>();
        final ObjectHolder<Boolean> dirtyHolder = new ObjectHolder<Boolean>( Boolean.FALSE );

        // Initial Selection
        boolean[] initial = new boolean[ choicesNames.length ];
        for ( int index = 0; index < choicesNames.length; index++ ) {
            int choiceId = choicesIds[index];
            if ( facetSelection.contains( facet, choiceId ) ) {
                initial[index] = true;
                selectedItems.add( facetSelection.get( facet, choiceId ) );
            } else {
                initial[index] = false;
            }
        }

        // Dialog Control
        builder.setMultiChoiceItems( choicesNames, initial, new DialogInterface.OnMultiChoiceClickListener()
        {

            public void onClick( DialogInterface dialog, int index, boolean selected )
            {
                OfficeFacetChoice newChoice = new OfficeFacetChoice( choicesNames[index], choicesIds[index] );
                if ( selected ) {
                    if ( !selectedItems.contains( newChoice ) ) {
                        dirtyHolder.setHolded( Boolean.TRUE );
                        selectedItems.add( newChoice );
                    }
                } else {
                    if ( selectedItems.contains( newChoice ) ) {
                        dirtyHolder.setHolded( Boolean.TRUE );
                        selectedItems.remove( newChoice );
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

    private void fireFacetSelectionChanged( OfficeFacet facet, List<OfficeFacetChoice> facetSelectedItems )
    {
        if ( facetSelectedItems.isEmpty() ) {
            facetSelection.remove( facet );
        } else {
            facetSelection.put( facet, new ArrayList<OfficeFacetChoice>( facetSelectedItems ) );
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
