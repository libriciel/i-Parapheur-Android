package org.adullact.iparapheur.tab.ui.office;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import roboguice.inject.InjectFragment;
import roboguice.inject.InjectView;

import com.google.inject.Inject;

import de.akquinet.android.androlog.Log;

import org.codeartisans.android.toolbox.activity.RoboFragmentActivity;
import org.codeartisans.android.toolbox.app.UserErrorDialogFactory;
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
import org.adullact.iparapheur.tab.ui.actions.ActionsDialogFactory;
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

    @Inject
    private ActionsDialogFactory actionsDialogFactory;

    @InjectFragment( R.id.office_facet_fragment )
    private OfficeFacetsFragment facetsFragment;

    @InjectFragment( R.id.office_list_fragment )
    private OfficeFolderListFragment listFragment;

    @InjectView( R.id.office_details_flipper )
    private ViewFlipper detailsFlipper;

    @InjectView( R.id.office_folder_layout )
    private RelativeLayout folderLayout;

    @InjectView( R.id.office_folder_icon )
    private ImageView folderIconView;

    @InjectView( R.id.office_folder_title )
    private TextView folderTitleView;

    @InjectView( R.id.office_folder_type_details )
    private TextView folderTypeDetails;

    @InjectView( R.id.office_folder_positive_button )
    private Button folderPositiveButton;

    @InjectView( R.id.office_folder_negative_button )
    private Button folderNegativeButton;

    @InjectView( R.id.office_folder_open_button )
    private Button folderOpenButton;

    @InjectView( R.id.office_folder_date_details )
    private TextView folderDateDetails;

    @InjectView( R.id.office_batch_layout )
    private RelativeLayout batchLayout;

    @InjectView( R.id.office_batch_title )
    private TextView batchTitle;

    private Folder currentFolder;

    private RelativeLayout currentDetailLayout;

    private void flipToFolderDetail()
    {
        if ( folderLayout != currentDetailLayout ) {
            detailsFlipper.setInAnimation( this, R.anim.in_from_right );
            detailsFlipper.setOutAnimation( this, R.anim.out_to_left );
            detailsFlipper.showPrevious();
            currentDetailLayout = folderLayout;
        }
    }

    private void flipToBatchDetail()
    {
        if ( batchLayout != currentDetailLayout ) {
            detailsFlipper.setInAnimation( this, R.anim.in_from_left );
            detailsFlipper.setOutAnimation( this, R.anim.out_to_right );
            detailsFlipper.showNext();
            currentDetailLayout = batchLayout;
        }
    }

    private final OnFolderDisplayRequestListener folderDisplayRequestListener = new OnFolderDisplayRequestListener()
    {

        public void onFolderDisplayRequest( final Folder folder )
        {
            // System.out.println( "FOLDER DISPLAY REQUEST FOR: " + folder ); // TODO Make it a DEBUG level log
            listFragment.shadeFolder( folder );
            folderIconView.setVisibility( View.VISIBLE );
            folderTitleView.setText( folder.getTitle() );
            folderTitleView.setVisibility( View.VISIBLE );
            if ( folder.requestedActionSupported() ) {
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
            StringBuilder typeDetails = new StringBuilder();
            typeDetails.append( "<p><b>Type</b> " ).append( folder.getBusinessType() ).append( "</p>" );
            typeDetails.append( "<p><b>Sous-type</b> " ).append( folder.getBusinessSubType() ).append( "</p>" );
            folderTypeDetails.setText( Html.fromHtml( typeDetails.toString() ) );
            folderTypeDetails.setVisibility( View.VISIBLE );
            StringBuilder dateDetails = new StringBuilder();
            dateDetails.append( "<p><b>Date de création</b> " ).append( folder.getDisplayCreationDate() ).append( "</p>" );
            if ( folder.getDueDate() != null ) {
                dateDetails.append( "<p><b>Date limite</b> " ).append( folder.getDisplayDueDate() ).append( "</p>" );
            }
            folderDateDetails.setText( Html.fromHtml( dateDetails.toString() ) );
            folderDateDetails.setVisibility( View.VISIBLE );
            flipToFolderDetail();
            currentFolder = folder;
        }

    };

    private OnFolderSelectionChange onFolderSelectionChange = new OnFolderSelectionChange()
    {

        public void onFolderSelectionChange( List<Folder> selectedFolders )
        {
            if ( selectedFolders.isEmpty() ) {
                listFragment.shadeFolder( currentFolder );
                flipToFolderDetail();
            } else {
                listFragment.shadeFolder( selectedFolders.toArray( new Folder[ selectedFolders.size() ] ) );
                flipToBatchDetail();
            }
        }

    };

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setTitle( getIntent().getExtras().getString( EXTRA_OFFICE_TITLE ) );
        getActionBar().setDisplayHomeAsUpEnabled( true );
        setContentView( R.layout.office );
        currentDetailLayout = folderLayout;
        facetsFragment.setOnSelectionChangedListener( new OnSelectionChangeListener()
        {

            public void facetSelectionChanged( Map<OfficeFacet, List<OfficeFacetChoice>> selection )
            {
                refresh();
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

        // Reset Views
        resetViews();

        // Load Data
        new OfficeLoadingTask( this, accountsRepository, iParapheurClient )
        {

            @Override
            protected void beforeDialogDismiss( AsyncTaskResult<OfficeData, IParapheurTabException> result )
            {
                List<Folder> folders = result.getResult().getFolders();
                if ( folders == null ) {
                    folders = Collections.emptyList();
                }
                facetsFragment.setOfficeTypology( result.getResult().getTypology() );
                listFragment.setListAdapter( new OfficeFolderListAdapter( listFragment, folders ) );
            }

            private DialogInterface.OnClickListener refresh = new DialogInterface.OnClickListener()
            {

                public void onClick( DialogInterface dialog, int id )
                {
                    refresh();
                }

            };

            private DialogInterface.OnClickListener dashboard = new DialogInterface.OnClickListener()
            {

                public void onClick( DialogInterface dialog, int id )
                {
                    startActivity( new Intent( context, DashboardActivity.class ) );
                }

            };

            @Override
            protected void afterDialogDismiss( AsyncTaskResult<OfficeData, IParapheurTabException> result )
            {
                if ( result.hasError() ) {
                    UserErrorDialogFactory.show( context, "Le chargement de ce bureau a échoué", result.getErrors(),
                                                 "Réessayer", refresh, "Tableau de bord", dashboard );
                }
            }

        }.execute( new OfficeLoadingTask.Params( accountIdentity,
                                                 officeIdentity,
                                                 facetsFragment.getFacetSelection(),
                                                 0, 20 ) );
    }

    private void resetViews()
    {
        for ( int idx = 0; idx < folderLayout.getChildCount(); idx++ ) {
            folderLayout.getChildAt( idx ).setVisibility( View.INVISIBLE );
        }
        folderPositiveButton.setOnClickListener( null );
        folderNegativeButton.setOnClickListener( null );
        folderOpenButton.setOnClickListener( null );
        flipToFolderDetail();
    }

    private void positiveAction( Folder folder )
    {
        String accountIdentity = getIntent().getExtras().getString( EXTRA_ACCOUNT_IDENTITY );
        actionsDialogFactory.buildActionDialog( accountIdentity, folder ).show();
    }

    private void negativeAction( Folder folder )
    {
        String accountIdentity = getIntent().getExtras().getString( EXTRA_ACCOUNT_IDENTITY );
        actionsDialogFactory.buildRejectDialog( accountIdentity, folder ).show();
    }

    private void openAction( Folder folder )
    {
        Intent intent = new Intent( this, FolderActivity.class );
        intent.putExtra( FolderActivity.EXTRA_ACCOUNT_IDENTITY, getIntent().getExtras().getString( EXTRA_ACCOUNT_IDENTITY ) );
        intent.putExtra( FolderActivity.EXTRA_OFFICE_IDENTITY, getIntent().getExtras().getString( EXTRA_OFFICE_IDENTITY ) );
        intent.putExtra( FolderActivity.EXTRA_OFFICE_TITLE, getIntent().getExtras().getString( EXTRA_OFFICE_TITLE ) );
        intent.putExtra( FolderActivity.EXTRA_FOLDER_IDENTITY, folder.getIdentity() );
        intent.putExtra( FolderActivity.EXTRA_FOLDER_TITLE, folder.getTitle() );
        startActivity( intent );
    }

}
