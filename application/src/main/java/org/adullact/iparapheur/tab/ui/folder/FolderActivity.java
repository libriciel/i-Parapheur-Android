package org.adullact.iparapheur.tab.ui.folder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import roboguice.inject.InjectFragment;
import roboguice.inject.InjectView;

import com.google.inject.Inject;

import de.akquinet.android.androlog.Log;

import org.codeartisans.android.toolbox.activity.RoboFragmentActivity;
import org.codeartisans.android.toolbox.logging.AndrologInitOnCreateObserver;
import org.codeartisans.android.toolbox.os.AsyncTaskResult;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.Folder;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.services.IParapheurHttpException;
import org.adullact.iparapheur.tab.ui.Refreshable;
import org.adullact.iparapheur.tab.ui.actionbar.ActionBarActivityObserver;
import org.adullact.iparapheur.tab.ui.actions.ActionsDialogFactory;
import org.adullact.iparapheur.tab.ui.dashboard.DashboardActivity;
import org.adullact.iparapheur.tab.ui.folder.FolderFileListFragment.FolderListAdapter;

public class FolderActivity
        extends RoboFragmentActivity
        implements Refreshable
{

    public static final String EXTRA_ACCOUNT_IDENTITY = "account:identity";

    public static final String EXTRA_OFFICE_IDENTITY = "office:identity";

    public static final String EXTRA_OFFICE_TITLE = "office:title";

    public static final String EXTRA_FOLDER_IDENTITY = "folder:identity";

    public static final String EXTRA_FOLDER_TITLE = "folder:title";

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

    @InjectFragment( R.id.folder_list_fragment )
    private FolderFileListFragment folderFileListFragment;

    @InjectView( R.id.folder_top_title )
    private TextView title;

    @InjectView( R.id.folder_top_summary_left )
    private TextView topSummaryLeft;

    @InjectView( R.id.folder_top_summary_right )
    private TextView topSummaryRight;

    @InjectView( R.id.folder_button_positive )
    private Button positiveButton;

    @InjectView( R.id.folder_button_negative )
    private Button negativeButton;

    @InjectView( R.id.folder_details_webview )
    private WebView fileWebView;

    private Folder currentFolder;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setTitle( getIntent().getExtras().getString( EXTRA_FOLDER_TITLE ) );
        getActionBar().setDisplayHomeAsUpEnabled( true );
        setContentView( R.layout.folder );

        // Set title
        title.setText( getIntent().getExtras().getString( EXTRA_FOLDER_TITLE ) );

        // Set HTML to views from ressources
        topSummaryLeft.setText( Html.fromHtml( getString( R.string.demo_summary_left ) ) );
        topSummaryRight.setText( Html.fromHtml( getString( R.string.demo_summary_right ) ) );

        // Handle buttons
        positiveButton.setOnClickListener( new View.OnClickListener()
        {

            public void onClick( View view )
            {
                String accountIdentity = getIntent().getExtras().getString( EXTRA_ACCOUNT_IDENTITY );
                actionsDialogFactory.buildActionDialog( accountIdentity, currentFolder ).show();
            }

        } );
        negativeButton.setOnClickListener( new View.OnClickListener()
        {

            public void onClick( View view )
            {
                String accountIdentity = getIntent().getExtras().getString( EXTRA_ACCOUNT_IDENTITY );
                actionsDialogFactory.buildRejectDialog( accountIdentity, currentFolder ).show();
            }

        } );
        refresh();
    }

    public void refresh()
    {
        // TODO FolderActivity story: Clear view state
        fileWebView.clearView();
        new FolderLoadingTask( this, accountsRepository, iParapheurClient )
        {

            @Override
            protected void beforeDialogDismiss( AsyncTaskResult<Folder, IParapheurHttpException> result )
            {
                Log.d( context, "Got result: " + result );
                if ( result.getResult() != null ) {
                    // Update view state
                    Folder folder = result.getResult();
                    folderFileListFragment.setListAdapter( new FolderListAdapter( context, folder.getAllFiles() ) );
                    if ( !folder.getAllFiles().isEmpty() ) {
                        fileWebView.loadUrl( folder.getAllFiles().get( 0 ).getUrl() );
                    }
                    currentFolder = folder;
                } else {
                    currentFolder = null;
                }
            }

            @Override
            protected void afterDialogDismiss( AsyncTaskResult<Folder, IParapheurHttpException> result )
            {
                if ( result.hasError() ) {
                    AlertDialog.Builder builder = new AlertDialog.Builder( context );
                    builder.setTitle( "Le chargement de ce dossier a échoué" ).
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

        }.execute( new FolderLoadingTask.Params( getIntent().getExtras().getString( EXTRA_ACCOUNT_IDENTITY ),
                                                 getIntent().getExtras().getString( EXTRA_FOLDER_IDENTITY ) ) );
    }

}
