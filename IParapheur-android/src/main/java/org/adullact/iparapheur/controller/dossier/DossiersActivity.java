package org.adullact.iparapheur.controller.dossier;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
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
import org.adullact.iparapheur.model.Dossier;

import java.util.HashSet;


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
                                                          BureauxFragment.BureauSelectedListener {


    private static final String FRAGMENT_TAG_DETAILS = "Dossier_details";
    private static final String FRAGMENT_TAG_LIST = "Dossiers_list";
    private static final String FRAGMENT_TAG_BATCH = "Dossiers_batch";
    private static final String FRAGMENT_TAG_BUREAUX = "Bureaux_list";
    private static final int EDIT_PREFERENCE_REQUEST = 0;

    private HashSet<String> selectedDossiers;

    /** Main Layout off the screen */
    private DrawerLayout drawerLayout;

    /** Left panel acting as a menu */
    private LinearLayout drawerMenu;

    /** Used to control the drawer state. */
    private ActionBarDrawerToggle drawerToggle;

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

        selectedDossiers = new HashSet<String>();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
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
            selectedDossiers.clear();
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
     * if the dossier is newly selected, create or update the batchFragment
     * @param id
     */
    @Override
    public void onDossierChecked(String id) {
        DossierBatchFragment batchFragment = (DossierBatchFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_BATCH);
        DossierListFragment fragment = (DossierListFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_LIST);
        Dossier dossier = (fragment == null)? null : fragment.getDossier(id);
        if (dossier != null) {
            if (batchFragment == null) {
                batchFragment = new DossierBatchFragment();
                getFragmentManager().beginTransaction()
                        .replace(R.id.dossier_detail_container, batchFragment, FRAGMENT_TAG_BATCH)
                        .setTransition(android.R.anim.slide_in_left)
                        .commit();
            }
            batchFragment.addDossier(dossier);
        }
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
        DossierListFragment listFragment = (DossierListFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_LIST);
        if (listFragment != null) {
            // this method will reload dossiers fragments
            listFragment.setBureauId(id);
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

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        // show or hide specific menu actions depending on Drawer state
        // FIXME : actions appearance on dossiers list size?
        boolean actionsVisibility = !drawerLayout.isDrawerVisible(drawerMenu) && (MyAccounts.INSTANCE.getSelectedAccount() != null);
        menu.findItem(R.id.action_filtrer).setVisible(actionsVisibility);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle presses on the action bar items
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
