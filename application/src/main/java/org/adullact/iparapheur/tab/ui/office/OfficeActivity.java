package org.adullact.iparapheur.tab.ui.office;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import roboguice.inject.InjectFragment;
import roboguice.inject.InjectView;

import com.google.inject.Inject;

import de.akquinet.android.androlog.Log;

import org.codeartisans.android.toolbox.activity.RoboFragmentActivity;
import org.codeartisans.android.toolbox.logging.AndrologInitOnCreateObserver;
import org.codeartisans.android.toolbox.os.AsyncTaskResult;

import org.adullact.iparapheur.tab.IParapheurTabException;
import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.Folder;
import org.adullact.iparapheur.tab.model.OfficeFacet;
import org.adullact.iparapheur.tab.model.OfficeFacetChoice;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.ui.Refreshable;
import org.adullact.iparapheur.tab.ui.actionbar.ActionBarActivityObserver;
import org.adullact.iparapheur.tab.ui.dashboard.DashboardActivity;
import org.adullact.iparapheur.tab.ui.folder.FolderActivity;
import org.adullact.iparapheur.tab.ui.office.OfficeFacetsFragment.OnSelectionChangeListener;
import org.adullact.iparapheur.tab.ui.office.OfficeFolderListFragment.OfficeFolderListAdapter;
import org.adullact.iparapheur.tab.ui.office.OfficeFolderListFragment.OnFolderDisplayRequestListener;
import org.adullact.iparapheur.tab.ui.office.OfficeFolderListFragment.OnFolderSelectionChange;

public class OfficeActivity
        extends RoboFragmentActivity
        implements Refreshable
{

    public static final String EXTRA_ACCOUNT_IDENTITY = "account:identity";

    public static final String EXTRA_OFFICE_IDENTITY = "office:identity";

    public static final String EXTRA_OFFICE_TITLE = "office:title";

    @Inject
    private AndrologInitOnCreateObserver andrologInitOnCreateObserver;

    @Inject
    private ActionBarActivityObserver actionBarObserver;

    @Inject
    private AccountsRepository accountsRepository;

    @Inject
    private IParapheurHttpClient iParapheurClient;

    @InjectFragment( R.id.office_facet_fragment )
    private OfficeFacetsFragment facetsFragment;

    @InjectFragment( R.id.office_list_fragment )
    private OfficeFolderListFragment listFragment;

    @InjectView( R.id.office_folder_layout )
    private RelativeLayout folderLayout;

    @InjectView( R.id.office_folder_title )
    private TextView folderTitleView;

    @InjectView( R.id.office_folder_positive_button )
    private Button folderPositiveButton;

    @InjectView( R.id.office_folder_negative_button )
    private Button folderNegativeButton;

    @InjectView( R.id.office_folder_open_button )
    private Button folderOpenButton;

    private final OnFolderDisplayRequestListener folderDisplayRequestListener = new OnFolderDisplayRequestListener()
    {

        public void onFolderDisplayRequest( final Folder folder )
        {
            System.out.println( "FOLDER DISPLAY REQUEST FOR: " + folder );
            listFragment.shadeFolder( folder );
            folderTitleView.setText( folder.getTitle() );
            folderTitleView.setVisibility( View.VISIBLE );
            if ( folder.getRequestedAction() != null ) {
                switch ( folder.getRequestedAction() ) {
                    case SIGNATURE:
                        folderPositiveButton.setText( "Signer" );
                        break;
                    case VISA:
                        folderPositiveButton.setText( "Viser" );
                        break;
                }
                folderPositiveButton.setOnClickListener( new View.OnClickListener()
                {

                    public void onClick( View view )
                    {
                        positiveAction( folder );
                    }

                } );
                folderPositiveButton.setVisibility( View.VISIBLE );
                folderNegativeButton.setText( "Rejeter" );
                folderNegativeButton.setOnClickListener( new View.OnClickListener()
                {

                    public void onClick( View view )
                    {
                        negativeAction( folder );
                    }

                } );
                folderNegativeButton.setVisibility( View.VISIBLE );
            }
            folderOpenButton.setVisibility( View.VISIBLE );
            folderOpenButton.setOnClickListener( new View.OnClickListener()
            {

                public void onClick( View view )
                {
                    openAction( folder );
                }

            } );
        }

    };

    private OnFolderSelectionChange onFolderSelectionChange = new OnFolderSelectionChange()
    {

        public void onFolderSelectionChange( List<Folder> selectedFolders )
        {
            System.out.println( "SELECTED FOLDERS: " + selectedFolders );
        }

    };

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.office );
        facetsFragment.setOnSelectionChangedListener( new OnSelectionChangeListener()
        {

            public void facetSelectionChanged( Map<OfficeFacet, List<OfficeFacetChoice>> selection )
            {
                // TODO Activate refresh when FacetSelection changed
                // refresh();
            }

        } );
        listFragment.setOnFolderDisplayRequestListener( folderDisplayRequestListener );
        listFragment.setOnFolderSelectionChangeListener( onFolderSelectionChange );
        refresh();
    }

    public void refresh()
    {
        String accountIdentity = getIntent().getExtras().getString( EXTRA_ACCOUNT_IDENTITY );
        String officeIdentity = getIntent().getExtras().getString( EXTRA_OFFICE_IDENTITY );
        String officeTitle = getIntent().getExtras().getString( EXTRA_OFFICE_TITLE );
        Log.i( "Refresh for office: " + accountIdentity + " / " + officeIdentity + " / " + officeTitle );

        // Reset View
        resetViews();

        // Load Data
        new OfficeLoadingTask( this, accountsRepository, iParapheurClient )
        {

            @Override
            protected void beforeDialogDismiss( AsyncTaskResult<List<Folder>, IParapheurTabException> result )
            {
                Log.d( OfficeActivity.this, "Got result: AsynkTaskResult[ result: " + result.getResult() + ", errors: " + result.getErrors() + "]" );
                List<Folder> folders = result.getResult();
                if ( folders == null ) {
                    folders = Collections.emptyList();
                }

                listFragment.setListAdapter( new OfficeFolderListAdapter( listFragment, folders ) );
            }

            @Override
            protected void afterDialogDismiss( AsyncTaskResult<List<Folder>, IParapheurTabException> result )
            {
                if ( result.hasError() ) {
                    AlertDialog.Builder builder = new AlertDialog.Builder( context );
                    builder.setTitle( "Le chargement de ce bureau a échoué" ).
                            setMessage( result.buildErrorMessages() ).
                            setCancelable( false );
                    builder.setPositiveButton( "Réessayer", new DialogInterface.OnClickListener()
                    {

                        public void onClick( DialogInterface dialog, int id )
                        {
                            refresh();
                        }

                    } );
                    builder.setNegativeButton( "Tableau de bord", new DialogInterface.OnClickListener()
                    {

                        public void onClick( DialogInterface dialog, int id )
                        {
                            startActivity( new Intent( context, DashboardActivity.class ) );
                        }

                    } );
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }

        }.execute( new OfficeLoadingTask.Params( accountIdentity,
                                                 officeIdentity,
                                                 facetsFragment.getFacetSelection(),
                                                 1, 10 ) );
    }

    private void resetViews()
    {
        for ( int idx = 0; idx < folderLayout.getChildCount(); idx++ ) {
            folderLayout.getChildAt( idx ).setVisibility( View.INVISIBLE );
        }
        folderPositiveButton.setOnClickListener( null );
        folderNegativeButton.setOnClickListener( null );
        folderOpenButton.setOnClickListener( null );
    }

    private void positiveAction( Folder folder )
    {
        Log.i( "POSITIVE ACTION on " + folder ); // TODO Implement positive action
    }

    private void negativeAction( Folder folder )
    {
        Log.i( "NEGATIVE ACTION on " + folder ); // TODO Implement negative action
    }

    private void openAction( Folder folder )
    {
        Intent intent = new Intent( this, FolderActivity.class );
        intent.putExtra( FolderActivity.EXTRA_ACCOUNT_IDENTITY, getIntent().getExtras().getString( EXTRA_ACCOUNT_IDENTITY ) );
        intent.putExtra( FolderActivity.EXTRA_FOLDER_IDENTITY, folder.getIdentity() );
        intent.putExtra( FolderActivity.EXTRA_FOLDER_TITLE, folder.getTitle() );
        startActivity( intent );
    }

}
