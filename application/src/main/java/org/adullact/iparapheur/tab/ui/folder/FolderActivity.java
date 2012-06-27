package org.adullact.iparapheur.tab.ui.folder;

import java.util.Collections;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import roboguice.inject.InjectFragment;
import roboguice.inject.InjectView;

import com.google.inject.Inject;

import org.codeartisans.android.toolbox.activity.RoboFragmentActivity;
import org.codeartisans.android.toolbox.app.UserErrorDialogFactory;
import org.codeartisans.android.toolbox.logging.AndrologInitOnCreateObserver;
import org.codeartisans.android.toolbox.os.AsyncTaskResult;
import org.codeartisans.android.toolbox.webkit.WebChromeSupport.AutoQuotaGrowWebChromeClient;
import org.codeartisans.android.toolbox.webkit.WebChromeSupport.ChainedWebChromeClient;
import org.codeartisans.android.toolbox.webkit.WebChromeSupport.ConsoleAndrologWebChromeClient;
import org.codeartisans.android.toolbox.webkit.WebViewSupport.JSInjectWebViewClient;
import org.json.JSONObject;

import org.adullact.iparapheur.tab.IParapheurTabException;
import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.AbstractFolderFile;
import org.adullact.iparapheur.tab.model.Folder;
import org.adullact.iparapheur.tab.model.FolderRequestedAction;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.ui.Refreshable;
import org.adullact.iparapheur.tab.ui.actionbar.ActionBarActivityObserver;
import org.adullact.iparapheur.tab.ui.actions.ActionsDialogFactory;
import org.adullact.iparapheur.tab.ui.dashboard.DashboardActivity;
import org.adullact.iparapheur.tab.ui.folder.FolderFileListFragment.FolderListAdapter;
import org.adullact.iparapheur.tab.ui.folder.FolderFileListFragment.OnFileDisplayRequestListener;

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

    private String accountIdentity;

    private String folderIdentity;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        // Gather Intent Extras
        accountIdentity = getIntent().getExtras().getString( EXTRA_ACCOUNT_IDENTITY );
        String officeTitle = getIntent().getExtras().getString( EXTRA_OFFICE_TITLE );
        String folderTitle = getIntent().getExtras().getString( EXTRA_FOLDER_TITLE );
        folderIdentity = getIntent().getExtras().getString( EXTRA_FOLDER_IDENTITY );

        // Setup Activity
        setTitle( officeTitle + " > " + folderTitle );
        getActionBar().setDisplayHomeAsUpEnabled( true );
        setContentView( R.layout.folder );

        // Set title
        title.setText( folderTitle );

        // Setup WebView
        fileWebView.getSettings().setJavaScriptEnabled( true );
        fileWebView.getSettings().setSupportZoom( true );
        fileWebView.getSettings().setBuiltInZoomControls( true );
        fileWebView.getSettings().setDomStorageEnabled( true );
        fileWebView.getSettings().setAppCachePath( getApplicationContext().getCacheDir().getAbsolutePath() );
        fileWebView.getSettings().setAllowFileAccess( true );
        fileWebView.getSettings().setAppCacheMaxSize( 1024 * 1024 * 12 ); // 12MB
        fileWebView.getSettings().setAppCacheEnabled( true );
        fileWebView.getSettings().setCacheMode( WebSettings.LOAD_CACHE_ELSE_NETWORK );
        fileWebView.setWebChromeClient( new ChainedWebChromeClient( new ConsoleAndrologWebChromeClient(),
                                                                    new AutoQuotaGrowWebChromeClient( 2 ) ) );

        // Create button listeners
        positiveButton.setOnClickListener( new View.OnClickListener()
        {

            public void onClick( View view )
            {
                String accountIdentity = getIntent().getExtras().getString( FolderActivity.this.accountIdentity );
                actionsDialogFactory.buildActionDialog( accountIdentity, Collections.singletonList( currentFolder ) ).show();
            }

        } );
        negativeButton.setOnClickListener( new View.OnClickListener()
        {

            public void onClick( View view )
            {
                String accountIdentity = getIntent().getExtras().getString( FolderActivity.this.accountIdentity );
                actionsDialogFactory.buildRejectDialog( accountIdentity, Collections.singletonList( currentFolder ) ).show();
            }

        } );

        refresh();
    }

    public void refresh()
    {
        // Clear View
        topSummaryLeft.setText( "" );
        topSummaryRight.setText( "" );
        positiveButton.setVisibility( View.INVISIBLE );
        negativeButton.setVisibility( View.INVISIBLE );
        fileWebView.clearView();
        fileWebView.freeMemory();

        // Load Folder
        new FolderLoadingTask( this, accountsRepository, iParapheurClient )
        {

            @Override
            protected void beforeDialogDismiss( AsyncTaskResult<Folder, IParapheurTabException> result )
            {
                if ( result.getResult() != null ) {

                    // Update view state
                    Folder folder = result.getResult();
                    if ( folder.getRequestedAction() != FolderRequestedAction.UNSUPPORTED ) {
                        positiveButton.setVisibility( View.VISIBLE );
                        negativeButton.setVisibility( View.VISIBLE );
                    }
                    topSummaryLeft.setText( Html.fromHtml( "<b>" + folder.getBusinessType() + "</b><br/>" + folder.getBusinessSubType() ) );
                    StringBuilder summaryRight = new StringBuilder();
                    summaryRight.append( "<b>Créé le</b> " ).append( folder.getDisplayCreationDate() );
                    if ( folder.getDueDate() != null ) {
                        summaryRight.append( "<br/><b>A traiter avant le</b> " ).append( folder.getDisplayDueDate() );
                    }
                    topSummaryRight.setText( Html.fromHtml( summaryRight.toString() ) );
                    folderFileListFragment.setListAdapter( new FolderListAdapter( context, folder.getAllFiles() ) );
                    folderFileListFragment.setOnFileDisplayRequestListener( new OnFileDisplayRequestListener()
                    {

                        public void onFileDisplayRequest( AbstractFolderFile file )
                        {
                            displayFile( file );
                        }

                    } );
                    if ( !folder.getAllFiles().isEmpty() ) {
                        displayFile( folder.getAllFiles().get( 0 ) );
                    }
                    currentFolder = folder;

                } else {
                    currentFolder = null;
                }

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
            protected void afterDialogDismiss( AsyncTaskResult<Folder, IParapheurTabException> result )
            {
                if ( result.hasError() ) {
                    UserErrorDialogFactory.show( context, "Le chargement de ce dossier a échoué", result.getErrors(),
                                                 "Réessayer", refresh, "Tableau de bord", dashboard );
                }
            }

        }.execute( new FolderLoadingTask.Params( accountIdentity, folderIdentity ) );
    }

    private void displayFile( AbstractFolderFile file )
    {
        folderFileListFragment.shadeFiles( file );
        JSONObject json = file.getPageImagesJSON();
        fileWebView.setWebViewClient( new JSInjectWebViewClient( "injectData(" + json.toString() + ");" ) );
        fileWebView.loadUrl( file.getUrl() );
    }

}
