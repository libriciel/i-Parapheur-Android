package org.adullact.iparapheur.tab.ui.actionbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import roboguice.event.Observes;
import roboguice.inject.ContextSingleton;

import com.google.inject.Inject;

import de.akquinet.android.androlog.Log;

import org.codeartisans.android.toolbox.activity.event.OnCreateOptionsMenuEvent;
import org.codeartisans.android.toolbox.activity.event.OnOptionsItemSelectedEvent;

import org.adullact.iparapheur.tab.IParapheurTabBuildInfo;
import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.ui.Refreshable;
import org.adullact.iparapheur.tab.ui.dashboard.DashboardActivity;
import org.adullact.iparapheur.tab.ui.settings.AccountsActivity;

@ContextSingleton
public class ActionBarActivityObserver
{

    @Inject
    private Activity activity;

    /**
     * This one handles actions only.
     */
    public void handleOnCreateOptionsMenu( @Observes OnCreateOptionsMenuEvent event )
    {
        // Refresh
        Menu menu = event.getMenu();
        if ( activity instanceof Refreshable ) {
            MenuItem refresh = menu.add( "Rafraichir" );
            refresh.setIcon( R.drawable.ic_refresh );
            refresh.setShowAsAction( MenuItem.SHOW_AS_ACTION_ALWAYS );
            refresh.setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener()
            {

                public boolean onMenuItemClick( MenuItem menuItem )
                {
                    ( ( Refreshable ) activity ).refresh();
                    return true;
                }

            } );
        }

        // Accounts
        MenuItem accounts = menu.add( "Comptes" );
        accounts.setIcon( R.drawable.ic_accounts ); // Won't be shown as Android don't want icons in the overflow menu
        accounts.setShowAsAction( MenuItem.SHOW_AS_ACTION_NEVER );
        accounts.setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener()
        {

            public boolean onMenuItemClick( MenuItem mi )
            {
                activity.startActivity( new Intent( activity, AccountsActivity.class ) );
                return true;
            }

        } );

        // About
        MenuItem about = menu.add( "À propos" );
        about.setIcon( R.drawable.icon );
        about.setShowAsAction( MenuItem.SHOW_AS_ACTION_NEVER );
        about.setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener()
        {

            public boolean onMenuItemClick( MenuItem mi )
            {
                View aboutLayout = activity.getLayoutInflater().inflate( R.layout.about_dialog, null );
                WebView aboutWebView = ( WebView ) aboutLayout.findViewById( R.id.about_webview );
                aboutWebView.loadUrl( "file:///android_asset/about/index.html" );

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
                builder.setPositiveButton( "Fermer", new DialogInterface.OnClickListener()
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

    /**
     * This one handles navigation only.
     */
    public void handleOnOptionsItemSelected( @Observes OnOptionsItemSelectedEvent event )
    {
        switch ( event.getItem().getItemId() ) {
            case android.R.id.home:
                activity.startActivity( new Intent( activity, DashboardActivity.class ) );
                break;
            default:
                Log.d( "Unknown Item selected in ActionBar: " + event.getItem() );
        }
    }

}
