package org.adullact.iparapheur.controller.dossier;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.controller.bureau.BureauxFragment;
import org.adullact.iparapheur.controller.dossier.filter.FilterAdapter;
import org.adullact.iparapheur.controller.dossier.filter.FilterDialog;
import org.adullact.iparapheur.controller.dossier.filter.MyFilters;
import org.adullact.iparapheur.controller.preferences.SettingsActivity;
import org.adullact.iparapheur.controller.utils.LoadingTask;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.Filter;


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
 * {@link org.adullact.iparapheur.controller.dossier.DossierListFragment.DossierListFragmentListener} interface
 * to listen for item selections.
 */
public class DossiersActivity extends FragmentActivity implements DossierListFragment.DossierListFragmentListener,
                                                          DossierDetailFragment.DossierDetailListener,
                                                          BureauxFragment.BureauSelectedListener,
                                                          LoadingTask.DataChangeListener,
                                                          FilterDialog.FilterDialogListener,
                                                          ActionBar.OnNavigationListener {

    public static final String DOSSIER_ID = "dossier_id";
    public static final String BUREAU_ID = "bureau_id";
    private static final int EDIT_PREFERENCE_REQUEST = 0;

    /** Main Layout off the screen */
    private DrawerLayout drawerLayout;

    /** Left panel acting as a menu */
    private FrameLayout drawerMenu;

    /** Used to control the drawer state. */
    private ActionBarDrawerToggle drawerToggle;
    private boolean openDrawerwhenFinishedLoading = false;
    private boolean manageDrawerwhenFinishedLoading = false;

    /** Adapter for action bar, used to display user's filters */
    private FilterAdapter filterAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Loading indicator
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_dossiers);

        drawerLayout = (DrawerLayout) findViewById(R.id.activity_dossiers_drawer_layout);
        drawerMenu = (FrameLayout) findViewById(R.id.activity_dossiers_left_drawer);

        // Used to listen open and close events on the Drawer Layout
        drawerToggle = new DossiersActionBarDrawerToggle(this, drawerLayout);
        drawerLayout.setDrawerListener(drawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
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
        if (manageDrawerwhenFinishedLoading) {
            if (drawerLayout != null) {
                if (openDrawerwhenFinishedLoading) {
                    drawerLayout.openDrawer(drawerMenu);
                }
                else {
                    drawerLayout.closeDrawer(drawerMenu);
                }
            }
            manageDrawerwhenFinishedLoading = false;
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

    // DossierListFragmentListener implementations
    @Override
    public void onDossierSelected(String dossierId, String bureauId)
    {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(DossierDetailFragment.TAG);
        if (fragment != null) {
            ((DossierDetailFragment) fragment).update(bureauId, dossierId);
        }
    }

    /**
     * Update actionBar actions on dossier
     * @param id
     */
    @Override
    public void onDossierChecked(String id) {
        Dossier dossier = getDossier(id);
        getActionBar().setSubtitle((dossier != null)? getDossier(id).getName() : "");
        invalidateOptionsMenu();
    }

    @Override
    public void onDossiersLoaded(int size) {
        onDossierSelected(null, null);

        if (getActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_LIST) {
            getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            if (filterAdapter == null) {
                filterAdapter = new FilterAdapter(this);
            }
            getActionBar().setListNavigationCallbacks(filterAdapter, this);
        }
    }

    @Override
    public void onDossiersNotLoaded() {
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    // DossierDetailListener implementation
    @Override
    public Dossier getDossier(String id) {
        DossierListFragment fragment = (DossierListFragment) getSupportFragmentManager().findFragmentByTag(DossierListFragment.TAG);
        return (fragment == null)? null : fragment.getDossier(id);
    }

    // BureauSelectedListener implementation
    @Override
    public void onBureauSelected(String id) {
        if (drawerLayout == null) {
            manageDrawerwhenFinishedLoading = true;
            openDrawerwhenFinishedLoading = (id == null);
        }
        else {
            if (id == null) {
                drawerLayout.openDrawer(drawerMenu);
            }
            else {
                drawerLayout.closeDrawer(drawerMenu);
            }
        }
        DossierListFragment listFragment = (DossierListFragment) getSupportFragmentManager().findFragmentByTag(DossierListFragment.TAG);
        if (listFragment != null) {
            // this method will reload dossiers fragments
            listFragment.setBureauId(id);
        }
    }

    // FilterDialogListener methods passed by the parent Activity

    public void onFilterSave(Filter filter) {
        MyFilters.INSTANCE.selectFilter(filter);
        MyFilters.INSTANCE.save(filter);
        getActionBar().setSelectedNavigationItem(filterAdapter.getPosition(filter));
        filterAdapter.notifyDataSetChanged();
        onDataChanged();
    }

    public void onFilterChange(Filter filter) {
        getActionBar().setSelectedNavigationItem(filterAdapter.getPosition(filter));
        MyFilters.INSTANCE.selectFilter(filter);
        onDataChanged();
    }

    public void onFilterCancel() {
        getActionBar().setSelectedNavigationItem(filterAdapter.getPosition(MyFilters.INSTANCE.getSelectedFilter()));
    }

    // OnNavigationListener implementation

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        if (itemPosition < filterAdapter.getCount() - 1) {
            Filter filter = filterAdapter.getItem(itemPosition);
            if (!filter.equals(MyFilters.INSTANCE.getSelectedFilter())) {
                MyFilters.INSTANCE.selectFilter(filter);
                onDataChanged();
            }
        }
        else {
            Filter filter = MyFilters.INSTANCE.getSelectedFilter();
            if (filter == null) {
                filter = new Filter();
            }
            FilterDialog.newInstance(filter).show(getSupportFragmentManager(), FilterDialog.TAG);

        }
        return false;
    }

    //LoadingTask implementation
    /**
     * Called when an action is done on a dossier. We have to force reload dossier list and
     * dismiss details.
     */
    @Override
    public void onDataChanged() {
        DossierListFragment listFragment = (DossierListFragment) getSupportFragmentManager().findFragmentByTag(DossierListFragment.TAG);
        if (listFragment != null) {
            // this method will reload dossiers fragments
            listFragment.reload();
        }
        onDossierSelected(null, null);
        invalidateOptionsMenu();
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
        if ((drawerLayout != null) && (drawerMenu != null)) {
            boolean actionsVisibility = !drawerLayout.isDrawerVisible(drawerMenu) && (MyAccounts.INSTANCE.getSelectedAccount() != null);
            menu.setGroupVisible(R.id.dossiers_menu_actions, actionsVisibility);
            return super.onPrepareOptionsMenu(menu);
        }
        return false;
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
        switch (item.getItemId()) {
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
            BureauxFragment bureauxFragment = (BureauxFragment) getSupportFragmentManager().findFragmentByTag(BureauxFragment.TAG);
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
