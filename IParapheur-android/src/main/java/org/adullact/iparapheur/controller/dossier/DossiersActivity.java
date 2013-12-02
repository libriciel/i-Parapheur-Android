package org.adullact.iparapheur.controller.dossier;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.controller.bureau.BureauxFragment;
import org.adullact.iparapheur.controller.preferences.SettingsActivity;
import org.adullact.iparapheur.controller.utils.LoadingTask;
import org.adullact.iparapheur.model.Dossier;


/**
 * An activity representing a list of Dossiers.
 * The activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link DossierListFragment} and the item details
 * is a {@link DossierDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link org.adullact.iparapheur.controller.dossier.DossierListFragment.DossierSelectedListener} interface
 * to listen for item selections.
 */
public class DossiersActivity extends Activity implements DossierListFragment.DossierSelectedListener,
                                                          DossierDetailFragment.DossierDetailListener,
                                                          BureauxFragment.BureauSelectedListener,
                                                          LoadingTask.DataChangeListener {


    private static final String FRAGMENT_TAG_DETAILS = "Dossier_details";
    private static final String FRAGMENT_TAG_LIST = "Dossiers_list";
    private static final String FRAGMENT_TAG_BUREAUX = "Bureaux_list";
    private static final int EDIT_PREFERENCE_REQUEST = 0;

    /** Main Layout off the screen */
    private DrawerLayout drawerLayout;

    /** Left panel acting as a menu */
    private LinearLayout drawerMenu;

    /** Used to control the drawer state. */
    private ActionBarDrawerToggle drawerToggle;
    private boolean openDrawerwhenFinishedLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Loading indicator
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_dossiers);

        drawerLayout = (DrawerLayout) findViewById(R.id.activity_dossiers_drawer_layout);
        drawerMenu = (LinearLayout) findViewById(R.id.activity_dossiers_left_drawer);

        // Used to listen open and close events on the Drawer Layout
        drawerToggle = new DossiersActionBarDrawerToggle(this, drawerLayout);
        drawerLayout.setDrawerListener(drawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (openDrawerwhenFinishedLoading) {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(drawerMenu);
            }
            openDrawerwhenFinishedLoading = false;
        }
    }

    /**
     * Save accounts state for later use. In our case, the latest selected account
     * will be automatically selected if the application is killed and relaunched.
     */
    @Override
    protected void onPause() {
        super.onPause();
        MyAccounts.INSTANCE.saveState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    // DossierSelectedListener implementations
    @Override
    public void onDossierSelected(String id)
    {
        if (id == null) {
            Fragment fragment = getFragmentManager().findFragmentByTag(FRAGMENT_TAG_DETAILS);
            if (fragment != null) {
                // hide is used so that we can do a relace when a dossier is selected after
                getFragmentManager().beginTransaction().hide(fragment);
            }
        }
        else {
            Bundle arguments = new Bundle();
            arguments.putString(DossierDetailFragment.DOSSIER_ID, id);
            DossierDetailFragment fragment = new DossierDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.dossier_detail_container, fragment, FRAGMENT_TAG_DETAILS)
                    .commit();
        }
    }

    /**
     * Update actionBar actions on dossier
     * @param id
     */
    @Override
    public void onDossierChecked(String id) {
        invalidateOptionsMenu();
    }

    // DossierDetailListener implementation
    @Override
    public Dossier getDossier(String id) {
        DossierListFragment fragment = (DossierListFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_LIST);
        return (fragment == null)? null : fragment.getDossier(id);
    }

    // BureauSelectedListener implementation
    @Override
    public void onBureauSelected(String id) {
        // Can happen when there is no network! no bureau can be loaded and we are notified.
        if (drawerLayout == null) {
            // TODO : use it!
            openDrawerwhenFinishedLoading = true;
        }
        else {
            if (id == null) {
                drawerLayout.openDrawer(drawerMenu);
            }
            else {
                drawerLayout.closeDrawer(drawerMenu);
            }
        }
        DossierListFragment listFragment = (DossierListFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_LIST);
        if (listFragment != null) {
            // this method will reload dossiers fragments
            listFragment.setBureauId(id);
        }
    }

    //LoadingTask implementation
    /**
     * Called when an action is done on a dossier. We have to force reload dossier list and
     * dismiss details.
     */
    @Override
    public void onDataChanged() {
        DossierListFragment listFragment = (DossierListFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_LIST);
        if (listFragment != null) {
            // this method will reload dossiers fragments
            listFragment.reload();
        }
    }

    // ACTIONBAR METHODS
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dossiers_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Will also call onPrepareOptionsMenu on each Fragments.
     * DossierListFragment will update possible actions on checked dossiers.
     */
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        // show or hide specific menu actions depending on Drawer state
        // FIXME : actions appearance on dossiers list size?
        Log.d("debug", "onPrepareOptionsMenu in Activity");
        boolean actionsVisibility = !drawerLayout.isDrawerVisible(drawerMenu) && (MyAccounts.INSTANCE.getSelectedAccount() != null);
        menu.setGroupVisible(R.id.dossiers_menu_actions, actionsVisibility);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // TODO : handle dossier(s) actions
        // Handle presses on the action bar items
        DialogFragment actionDialog;
        switch (item.getItemId()) {
            case R.id.action_filtrer :
                Toast.makeText(this, "Filtrer", Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_search:
                // TODO
                return true;
            case R.id.action_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), EDIT_PREFERENCE_REQUEST);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Return of the settings Activity.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_PREFERENCE_REQUEST) {
            /* Don't check if result is ok as the user can press back after modifying an Account
               only notify BureauxFragments to update accounts list (the bureau will update back this
               Activity if needed).*/
            BureauxFragment bureauxFragment = (BureauxFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_BUREAUX);
            if (bureauxFragment != null) {
                bureauxFragment.accountsChanged();
            }
        }
    }

    /**
     * Listener used on the Drawer Layout used to control the Action Bar content depending
     * on the Drawer state.
     */
    private class DossiersActionBarDrawerToggle extends ActionBarDrawerToggle {
        public DossiersActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout) {
            super(activity, drawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close);
        }

        @Override
        public void onDrawerClosed(View view) {
            if (MyAccounts.INSTANCE.getSelectedAccount() != null) {
                getActionBar().setTitle(MyAccounts.INSTANCE.getSelectedAccount().getTitle());
            }
            // calls onPrepareOptionMenu to show context specific actions
            invalidateOptionsMenu();
        }

        @Override
        /** Called when a drawer has settled in a completely open state. */
        public void onDrawerOpened(View drawerView) {
            getActionBar().setTitle(R.string.app_name);
            // calls onPrepareOptionMenu to hide context specific actions
            invalidateOptionsMenu();
        }
    }
}
