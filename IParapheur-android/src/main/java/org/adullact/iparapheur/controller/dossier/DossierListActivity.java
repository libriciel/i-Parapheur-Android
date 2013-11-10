package org.adullact.iparapheur.controller.dossier;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.preferences.SettingsActivity;
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
public class DossierListActivity extends Activity implements DossierListFragment.DossierSelectedListener,
                                                             DossierDetailFragment.DossierDetailListener {


    private static final String FRAGMENT_TAG_DETAILS = "Dossier_details";
    private static final String FRAGMENT_TAG_LIST = "Dossiers_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Loading indicator
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_dossier_list);
        getActionBar().setDisplayHomeAsUpEnabled(true);
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

    // DossierDetailListener implementation
    @Override
    public Dossier getDossier(String id) {
        DossierListFragment fragment = (DossierListFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_LIST);
        return (fragment == null)? null : fragment.getDossier(id);
    }

    // ACTIONBAR METHODS
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                // TODO
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
