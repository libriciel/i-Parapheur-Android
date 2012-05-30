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
import org.adullact.iparapheur.tab.ui.actionbar.ActionBarActivityObserver;

public class OfficeActivity
        extends RoboFragmentActivity
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

    private final AdapterView.OnItemClickListener folderClickListener = new AdapterView.OnItemClickListener()
    {

        public void onItemClick( AdapterView<?> parentView, View childView, int position, long id )
        {
            folderDetailVisibility( true );
            Log.i( "CLICKED ITEM: " + officeFolderListFragment.getListAdapter().getItem( position ) );
            Folder folder = ( Folder ) officeFolderListFragment.getListAdapter().getItem( position );
            folderTitleView.setText( folder.getTitle() );
            switch ( folder.getRequestedAction() ) {
                case SIGNATURE:
                    folderPositiveButton.setText( "Signer" );
                    break;
                case VISA:
                    folderPositiveButton.setText( "Viser" );
                    break;
            }
            folderNegativeButton.setText( "Rejeter" );
        }

    };

    private void folderDetailVisibility( boolean visible )
    {
        for ( int idx = 0; idx < folderLayout.getChildCount(); idx++ ) {
            folderLayout.getChildAt( idx ).setVisibility( visible ? View.VISIBLE : View.INVISIBLE );

        }
    }

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        String accountIdentity = getIntent().getExtras().getString( EXTRA_ACCOUNT_IDENTITY );
        String officeIdentity = getIntent().getExtras().getString( EXTRA_OFFICE_IDENTITY );
        String officeTitle = getIntent().getExtras().getString( EXTRA_OFFICE_TITLE );
        Log.i( "onCreate for office: " + accountIdentity + " / " + officeIdentity + " / " + officeTitle );
        super.onCreate( savedInstanceState );
        setContentView( R.layout.office );
        folderDetailVisibility( false );
        officeTitleView.setText( officeTitle );
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
                    officeFolderListFragment.getListView().setOnItemClickListener( folderClickListener );
                }
            }

        }.execute( new OfficeLoadingTask.Params( accountIdentity, officeIdentity, 1, 10 ) );
    }

}
