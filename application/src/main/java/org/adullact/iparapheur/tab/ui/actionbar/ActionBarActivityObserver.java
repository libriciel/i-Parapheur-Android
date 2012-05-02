package org.adullact.iparapheur.tab.ui.actionbar;

import android.app.Activity;
import android.content.Intent;

import roboguice.event.Observes;
import roboguice.inject.ContextSingleton;

import com.google.inject.Inject;

import de.akquinet.android.androlog.Log;

import org.codeartisans.android.toolbox.activity.event.OnCreateOptionsMenuEvent;
import org.codeartisans.android.toolbox.activity.event.OnOptionsItemSelectedEvent;
import org.codeartisans.android.toolbox.activity.event.OnOptionsMenuClosedEvent;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.ui.settings.AccountsActivity;
import org.adullact.iparapheur.tab.ui.settings.SettingsActivity;

@ContextSingleton
public class ActionBarActivityObserver
{

    @Inject
    private Activity activity;

    public void handleOnCreateOptionsMenu( @Observes OnCreateOptionsMenuEvent event )
    {
        activity.getMenuInflater().inflate( R.menu.actionbar, event.getMenu() );
    }

    public void handleOnOptionsItemSelected( @Observes OnOptionsItemSelectedEvent event )
    {
        switch ( event.getItem().getItemId() ) {
            case R.id.accounts_menu_item:
                activity.startActivity( new Intent( activity, AccountsActivity.class ) );
                break;
            case R.id.settings_menu_item:
                activity.startActivity( new Intent( activity, SettingsActivity.class ) );
                break;
            default:
                Log.d( "Unknown Item selected in ActionBar: " + event.getItem() );
        }
    }

    public void handleOnOptionsMenuClosed( @Observes OnOptionsMenuClosedEvent event )
    {
    }

}
