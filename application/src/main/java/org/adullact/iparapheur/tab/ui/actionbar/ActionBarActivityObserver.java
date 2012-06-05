package org.adullact.iparapheur.tab.ui.actionbar;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import roboguice.event.Observes;
import roboguice.inject.ContextSingleton;

import com.google.inject.Inject;

import de.akquinet.android.androlog.Log;

import org.codeartisans.android.toolbox.activity.event.OnCreateOptionsMenuEvent;
import org.codeartisans.android.toolbox.activity.event.OnOptionsItemSelectedEvent;

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
        Menu menu = event.getMenu();
        if ( activity instanceof Refreshable ) {
            MenuItem refresh = menu.add( "Rafraichir" );
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
        MenuItem accounts = menu.add( "Comptes" );
        accounts.setShowAsAction( MenuItem.SHOW_AS_ACTION_NEVER );
        accounts.setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener()
        {

            public boolean onMenuItemClick( MenuItem mi )
            {
                activity.startActivity( new Intent( activity, AccountsActivity.class ) );
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
