package org.adullact.iparapheur.tab.ui.office;

import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import roboguice.inject.InjectFragment;
import roboguice.inject.InjectView;

import com.google.inject.Inject;

import de.akquinet.android.androlog.Log;

import org.codeartisans.android.toolbox.activity.RoboFragmentActivity;
import org.codeartisans.android.toolbox.logging.AndrologInitOnCreateObserver;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.Folder;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.ui.Refreshable;
import org.adullact.iparapheur.tab.ui.actionbar.ActionBarActivityObserver;

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

    @InjectView( R.id.office_title )
    private TextView officeTitleView;

    @InjectFragment( R.id.office_list_view )
    private OfficeFolderListFragment officeFolderListFragment;

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

    private final AdapterView.OnItemClickListener folderListItemClickListener = new AdapterView.OnItemClickListener()
    {

        public void onItemClick( AdapterView<?> parentView, View childView, int position, long id )
        {
            Log.i( "CLICKED FOLDER: " + officeFolderListFragment.getListAdapter().getItem( position ) );
            final Folder folder = ( Folder ) officeFolderListFragment.getListAdapter().getItem( position );
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

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.office );
        refresh();
    }

    public void refresh()
    {
        String accountIdentity = getIntent().getExtras().getString( EXTRA_ACCOUNT_IDENTITY );
        String officeIdentity = getIntent().getExtras().getString( EXTRA_OFFICE_IDENTITY );
        String officeTitle = getIntent().getExtras().getString( EXTRA_OFFICE_TITLE );
        Log.i( "Refresh for office: " + accountIdentity + " / " + officeIdentity + " / " + officeTitle );
        folderDetailReset();
        officeTitleView.setText( officeTitle );
        loadData( accountIdentity, officeIdentity );
    }

    private void loadData( String accountIdentity, String officeIdentity )
    {
        new OfficeLoadingTask( this, accountsRepository, iParapheurClient )
        {

            // This method is called on the UI thread
            // Populates UI views
            @Override
            protected void beforeDialogDismiss( List<Folder> folders )
            {
                Log.d( OfficeActivity.this, "Got result: " + folders );
                if ( folders != null && !folders.isEmpty() ) {
                    officeFolderListFragment.setListAdapter( new OfficeFolderListFragment.OfficeFolderListAdapter( context, folders ) );
                    officeFolderListFragment.getListView().setOnItemClickListener( folderListItemClickListener );
                }
            }

        }.execute( new OfficeLoadingTask.Params( accountIdentity, officeIdentity, 1, 10 ) );
    }

    private void folderDetailReset()
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
        Log.i( "POSITIVE ACTION on " + folder ); // TODO Implement
    }

    private void negativeAction( Folder folder )
    {
        Log.i( "NEGATIVE ACTION on " + folder ); // TODO Implement
    }

    private void openAction( Folder folder )
    {
        Log.i( "OPEN ACTION on " + folder ); // TODO Implement
    }

}
