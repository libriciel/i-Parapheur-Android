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
import android.widget.Toast;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

import org.codeartisans.java.toolbox.ObjectHolder;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.OfficeFacet;

public class OfficeFacetsFragment
        extends RoboFragment
{

    public static interface OnSelectionChangeListener
    {

        void facetSelectionChanged( Map<OfficeFacet, Collection<String>> selection );

    }

    private final Map<OfficeFacet, Collection<String>> facetSelection = new EnumMap<OfficeFacet, Collection<String>>( OfficeFacet.class );

    private OnSelectionChangeListener selectionChangeListener;

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
        stateButton.setOnClickListener( new View.OnClickListener()
        {

            public void onClick( View view )
            {
                getDialog( OfficeFacet.STATE ).show();
            }

        } );
        typeButton.setOnClickListener( new View.OnClickListener()
        {

            public void onClick( View view )
            {
                getDialog( OfficeFacet.TYPE ).show();
            }

        } );
        subtypeButton.setOnClickListener( new View.OnClickListener()
        {

            public void onClick( View view )
            {
                getDialog( OfficeFacet.SUBTYPE ).show();
            }

        } );
        actionButton.setOnClickListener( new View.OnClickListener()
        {

            public void onClick( View view )
            {
                getDialog( OfficeFacet.ACTION ).show();
            }

        } );
        scheduleButton.setOnClickListener( new View.OnClickListener()
        {

            public void onClick( View view )
            {
                getDialog( OfficeFacet.SCHEDULE ).show();
            }

        } );
    }

    @Override
    public void onDestroy()
    {
        facetSelection.clear();
        super.onDestroy();
    }

    private synchronized Dialog getDialog( final OfficeFacet facet )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );

        // Titles & Choices
        final String[] choices;
        switch ( facet ) {
            case STATE:
                builder.setTitle( "Filtrer par Etât" );
                choices = new String[]{ "A traiter", "En retard", "Récupérables", "A venir", "Déjà traités" };
                break;
            case TYPE:
                builder.setTitle( "Filtrer par Type" );
                choices = new String[]{ "Foo", "Bar", "Baz" };
                break;
            case SUBTYPE:
                builder.setTitle( "Filtrer par Sous-type" );
                choices = new String[]{ "Yo", "Howdy", "M8" };
                break;
            case ACTION:
                builder.setTitle( "Filtrer par Action" );
                choices = new String[]{ "Signature", "Visa" };
                break;
            case SCHEDULE:
                builder.setTitle( "Filtrer par Échéance" );
                choices = new String[]{ "Aujourd'hui", "Cette semaine", "La semaine prochaine", "Ce mois-ci", "Le mois prochain" };
                break;
            default:
                throw new InternalError( "Unknown Facet " + facet + ", this should not happen." );
        }

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
                    Toast.makeText( getActivity(), selectedItems.toString(), Toast.LENGTH_SHORT ).show();
                }
            }

        } );

        // Cancel
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
            selectionChangeListener.facetSelectionChanged( facetSelection );
        }
    }

    private void updateFilterSummary()
    {
        if ( facetSelection.isEmpty() ) {
            facetSummary.setText( "Aucun filtre." );
        } else {
            facetSummary.setText( "Filtre: " + facetSelection.toString() );
        }
    }

}
