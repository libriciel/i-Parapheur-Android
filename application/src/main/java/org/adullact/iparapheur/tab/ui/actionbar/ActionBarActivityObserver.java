package org.adullact.iparapheur.tab.ui.actionbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import com.google.inject.Inject;
import de.akquinet.android.androlog.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.adullact.iparapheur.tab.IParapheurTabBuildInfo;
import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.ui.Refreshable;
import org.adullact.iparapheur.tab.ui.dashboard.DashboardActivity;
import org.adullact.iparapheur.tab.ui.folder.FolderActivity;
import org.adullact.iparapheur.tab.ui.office.OfficeActivity;
import org.adullact.iparapheur.tab.ui.settings.AccountsActivity;
import org.codeartisans.android.toolbox.activity.event.OnCreateOptionsMenuEvent;
import org.codeartisans.android.toolbox.activity.event.OnOptionsItemSelectedEvent;
import roboguice.event.Observes;
import roboguice.inject.ContextSingleton;

@ContextSingleton
public class ActionBarActivityObserver
{

    @Inject
    private Activity activity;

    /**
     * This one handles navigation only.
     */
    public void handleOnOptionsItemSelected( @Observes OnOptionsItemSelectedEvent event )
    {
        switch ( event.getItem().getItemId() ) {
            case android.R.id.home:
                if ( activity instanceof FolderActivity ) {
                    Bundle extras = activity.getIntent().getExtras();
                    String accountIdentity = extras.getString( FolderActivity.EXTRA_ACCOUNT_IDENTITY );
                    String officeIdentity = extras.getString( FolderActivity.EXTRA_OFFICE_IDENTITY );
                    String officeTitle = extras.getString( FolderActivity.EXTRA_OFFICE_TITLE );
                    Intent intent = new Intent( activity, OfficeActivity.class );
                    intent.putExtra( OfficeActivity.EXTRA_ACCOUNT_IDENTITY, accountIdentity );
                    intent.putExtra( OfficeActivity.EXTRA_OFFICE_IDENTITY, officeIdentity );
                    intent.putExtra( OfficeActivity.EXTRA_OFFICE_TITLE, officeTitle );
                    activity.startActivity( intent );
                } else {
                    // Default behavior: Dashboard
                    activity.startActivity( new Intent( activity, DashboardActivity.class ) );
                }
                break;
            default:
                Log.d( "Unknown Item selected in ActionBar: " + event.getItem() );
        }
    }

    /**
     * This one handles actions only.
     */
    public void handleOnCreateOptionsMenu( @Observes OnCreateOptionsMenuEvent event )
    {
        // Refresh
        Menu menu = event.getMenu();
        if ( activity instanceof Refreshable ) {
            addRefreshAction( menu );
        }

        // Accounts
        addAccountsAction( menu );

        // About
        addAboutAction( menu );
    }

    private void addRefreshAction( Menu menu )
    {
        MenuItem refresh = menu.add( activity.getResources().getString( R.string.words_refresh ) );
        refresh.setShowAsAction( MenuItem.SHOW_AS_ACTION_ALWAYS );
        refresh.setIcon( R.drawable.ic_refresh );
        refresh.setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener()
        {

            public boolean onMenuItemClick( MenuItem menuItem )
            {
                ( ( Refreshable ) activity ).refresh();
                return true;
            }

        } );
    }

    private void addAccountsAction( Menu menu )
    {
        MenuItem accounts = menu.add( activity.getResources().getString( R.string.accounts ) );
        accounts.setShowAsAction( MenuItem.SHOW_AS_ACTION_NEVER );
        accounts.setIcon( R.drawable.ic_accounts ); // Won't be shown as Android don't want icons in the overflow menu
        accounts.setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener()
        {

            public boolean onMenuItemClick( MenuItem mi )
            {
                activity.startActivity( new Intent( activity, AccountsActivity.class ) );
                return true;
            }

        } );
    }

    private void addAboutAction( Menu menu )
    {
        MenuItem about = menu.add( activity.getResources().getString( R.string.words_about ) );
        about.setShowAsAction( MenuItem.SHOW_AS_ACTION_NEVER );
        about.setIcon( R.drawable.icon );
        about.setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener()
        {

            public boolean onMenuItemClick( MenuItem mi )
            {
                View aboutLayout = activity.getLayoutInflater().inflate( R.layout.about_dialog, null );
                WebView aboutWebView = ( WebView ) aboutLayout.findViewById( R.id.about_webview );
                aboutWebView.loadUrl( "file:///android_asset/html/about.html" );

                StringBuilder title = new StringBuilder();
                title.append( IParapheurTabBuildInfo.DESCRIPTION ).
                        append( " - " ).
                        append( IParapheurTabBuildInfo.VERSION ).
                        append( " - " ).
                        append( new SimpleDateFormat( "dd/MM/yyyy" ).format( new Date( IParapheurTabBuildInfo.BUILD_TIMESTAMP ) ) );

                AlertDialog.Builder builder = new AlertDialog.Builder( activity );
                builder.setTitle( title.toString() );
                builder.setIcon( R.drawable.icon );
                builder.setView( aboutLayout );
                builder.setPositiveButton( activity.getResources().getString( R.string.words_close ),
                                           new DialogInterface.OnClickListener()
                {

                    public void onClick( DialogInterface dialog, int id )
                    {
                        dialog.cancel();
                    }

                } );
                builder.show();
                return true;
            }

        } );
    }

}
