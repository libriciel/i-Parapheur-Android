package org.adullact.iparapheur.controller.dossier;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.Spinner;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.AccountListFragment;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.controller.bureau.BureauxListFragment;
import org.adullact.iparapheur.controller.dossier.action.ArchivageDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.MailSecDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.RejetDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.SignatureDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.TdtHeliosDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.VisaDialogFragment;
import org.adullact.iparapheur.controller.dossier.filter.FilterAdapter;
import org.adullact.iparapheur.controller.dossier.filter.FilterDialog;
import org.adullact.iparapheur.controller.dossier.filter.MyFilters;
import org.adullact.iparapheur.controller.preferences.AccountsPreferenceFragment;
import org.adullact.iparapheur.controller.preferences.SettingsActivity;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.Filter;
import org.adullact.iparapheur.utils.DeviceUtils;
import org.adullact.iparapheur.utils.LoadingTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * An activity representing a list of Dossiers.
 * The activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link DossierListFragment} and the item details
 * is a {@link DossierDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link org.adullact.iparapheur.controller.dossier.DossierListFragment.DossierListFragmentListener} interface
 * to listen for item selections.
 */
public class MainActivity extends ActionBarActivity implements DossierListFragment.DossierListFragmentListener, BureauxListFragment.BureauListFragmentListener, AccountListFragment.AccountFragmentListener, AdapterView.OnItemSelectedListener, LoadingTask.DataChangeListener, FilterDialog.FilterDialogListener, ActionMode.Callback {

	private static final int EDIT_PREFERENCE_REQUEST = 0;

	private DrawerLayout mDrawerLayout;                                 // Main Layout off the screen
	private FrameLayout mDrawerMenu;                                    // Left panel acting as a menu
	private ActionBarDrawerToggle mDrawerToggle;                        // Used to control the drawer state.
	private boolean mOpenDrawerWhenFinishedLoading = false;
	private boolean mManageDrawerWhenFinishedLoading = false;
	private FilterAdapter mFilterAdapter;                               // Adapter for action bar, used to display user's filters
	private Spinner mFiltersSpinner;
	private ActionMode mActionMode;                                     // The actionMode used when dossiers are checked

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// To have a transparent StatusBar, and a background color behind

		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

		// Loading indicator

		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_dossiers);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_dossiers_drawer_layout);
		mDrawerMenu = (FrameLayout) findViewById(R.id.activity_dossiers_left_drawer);
		mFiltersSpinner = (Spinner) findViewById(R.id.activity_dossiers_toolbar_spinner);

		mFiltersSpinner.setOnItemSelectedListener(this);

		Toolbar toolbar = (Toolbar) findViewById(R.id.home_toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Used to listen open and close events on the Drawer Layout
		mDrawerToggle = new DossiersActionBarDrawerToggle(this, mDrawerLayout);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	protected void onStart() {
		super.onStart();

		Fragment fragmentToDisplay = getSupportFragmentManager().findFragmentByTag(BureauxListFragment.TAG);

		if (fragmentToDisplay != null)
			return;

		fragmentToDisplay = new BureauxListFragment();

		// Replace whatever is in the fragment_container view with this fragment.

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.setCustomAnimations(0, 0, R.anim.push_center_to_right, R.anim.push_left_to_center);
		transaction.replace(R.id.left_fragment, fragmentToDisplay, BureauxListFragment.TAG);
		transaction.commit();

		// We select the first account by default, the demo one
		Account selectedAccount = MyAccounts.INSTANCE.getSelectedAccount();
		if (selectedAccount == null)
			MyAccounts.INSTANCE.selectAccount(MyAccounts.INSTANCE.getAccounts().get(0).getId());
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mManageDrawerWhenFinishedLoading) {
			if (mDrawerLayout != null) {
				if (mOpenDrawerWhenFinishedLoading)
					mDrawerLayout.openDrawer(mDrawerMenu);
				else
					mDrawerLayout.closeDrawer(mDrawerMenu);
			}
			mManageDrawerWhenFinishedLoading = false;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Save accounts state for later use. In our case, the latest selected account
		// will be automatically selected if the application is killed and relaunched.
		MyAccounts.INSTANCE.saveState();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == EDIT_PREFERENCE_REQUEST) {
			// Don't check if result is ok as the user can press back after modifying an Account
			// only notify BureauxFragments to update accounts list (the bureau will update back this Activity if needed)
			AccountListFragment accountListFragment = (AccountListFragment) getSupportFragmentManager().findFragmentByTag(AccountListFragment.TAG);
			if (accountListFragment != null)
				accountListFragment.accountsChanged();
		}
	}

	// <editor-fold desc="ActionBar">

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.dossiers_menu, menu);

		// Alignment in TopBar isn't working on XML, but works programmatically

		Toolbar.LayoutParams spinnerContainerLayoutParams = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.END);
		spinnerContainerLayoutParams.rightMargin = Math.round(DeviceUtils.dipsToPixels(this, 20));
		mFiltersSpinner.setLayoutParams(spinnerContainerLayoutParams);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		// Show or hide specific menu actions depending on displayed fragment

		Fragment dossierFragment = getSupportFragmentManager().findFragmentByTag(DossierListFragment.TAG);
		mFiltersSpinner.setVisibility((dossierFragment == null) ? View.GONE : View.VISIBLE);

		// Show or hide specific menu actions depending on Drawer state.

		if ((mDrawerLayout != null) && (mDrawerMenu != null)) {
			boolean actionsVisibility = !mDrawerLayout.isDrawerVisible(mDrawerMenu) && (MyAccounts.INSTANCE.getSelectedAccount() != null);
			menu.setGroupVisible(R.id.dossiers_menu_actions, actionsVisibility);
			return super.onPrepareOptionsMenu(menu);
		}

		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
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

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

		if (position < mFilterAdapter.getCount() - 1) {
			Filter filter = mFilterAdapter.getItem(position);
			if (!filter.equals(MyFilters.INSTANCE.getSelectedFilter())) {
				MyFilters.INSTANCE.selectFilter(filter);
				onDataChanged();
			}
		}
		else {
			Filter filter = MyFilters.INSTANCE.getSelectedFilter();
			if (filter == null)
				filter = new Filter();

			FilterDialog.newInstance(filter).show(getSupportFragmentManager(), FilterDialog.TAG);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	// </editor-fold desc="ActionBar">

	// <editor-fold desc="ActionMode">

	@Override
	public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
		actionMode.setTitleOptionalHint(true);
		MenuInflater inflater = actionMode.getMenuInflater();
		inflater.inflate(R.menu.dossiers_list_menu_actions, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
		DossierListFragment fragment = (DossierListFragment) getSupportFragmentManager().findFragmentByTag(DossierListFragment.TAG);
		if (fragment != null) {
			HashSet<Dossier> checkedDossiers = fragment.getCheckedDossiers();
			if ((checkedDossiers != null) && (!checkedDossiers.isEmpty())) {
				actionMode.setTitle(getResources().getString(R.string.action_mode_nb_dossiers, checkedDossiers.size()));
				// Get the intersection of all possible actions on checked dossiers and update the menu
				menu.setGroupVisible(R.id.dossiers_menu_main_actions, false);
				menu.setGroupVisible(R.id.dossiers_menu_other_actions, false);

				HashSet<Action> actions = new HashSet<>(Arrays.asList(Action.values()));

				boolean sign = false;
				for (Dossier dossier : checkedDossiers) {
					actions.retainAll(dossier.getActions());
					sign = sign || dossier.getActions().contains(Action.SIGNATURE);
				}

				for (Action action : actions) {
					MenuItem item;
					int menuItemId;
					String menuTitle;
					// Si c'est le visa, et qu'on a aussi de la signature dans le lot, on prend
					// la signature (possible en API v3).
					if (action.equals(Action.VISA) && (sign)) {
						menuItemId = Action.SIGNATURE.getMenuItemId();
						menuTitle = getResources().getString(Action.VISA.getTitle()) + "/" +
								getResources().getString(Action.SIGNATURE.getTitle());
					}
					else {
						menuItemId = action.getMenuItemId();
						menuTitle = getResources().getString(action.getTitle());
					}

					item = menu.findItem(menuItemId);
					if (item != null) {
						item.setTitle(menuTitle);
						item.setVisible(true);
					}
				}
				return true;
			}
			else {
				actionMode.finish();
			}
		}
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
		DossierListFragment fragment = (DossierListFragment) getSupportFragmentManager().findFragmentByTag(DossierListFragment.TAG);

		if (fragment != null) {
			String bureauId = fragment.getBureauId();

			DialogFragment actionDialog;
			switch (menuItem.getItemId()) {
				case R.id.action_visa:
					actionDialog = VisaDialogFragment.newInstance(new ArrayList<>(fragment.getCheckedDossiers()), bureauId);
					actionDialog.show(getSupportFragmentManager(), "VisaDialogFragment");
					return true;
				case R.id.action_signature:
					actionDialog = SignatureDialogFragment.newInstance(new ArrayList<>(fragment.getCheckedDossiers()), bureauId);
					actionDialog.show(getSupportFragmentManager(), "SignatureDialogFragment");
					return true;
				case R.id.action_mailsec:
					actionDialog = MailSecDialogFragment.newInstance(new ArrayList<>(fragment.getCheckedDossiers()), bureauId);
					actionDialog.show(getSupportFragmentManager(), "MailSecDialogFragment");
					return true;
				case R.id.action_tdt_actes:
				case R.id.action_tdt_helios:
					actionDialog = TdtHeliosDialogFragment.newInstance(new ArrayList<>(fragment.getCheckedDossiers()), bureauId);
					actionDialog.show(getSupportFragmentManager(), "TdtHeliosDialogFragment");
					return true;
				case R.id.action_archivage:
					actionDialog = ArchivageDialogFragment.newInstance(new ArrayList<>(fragment.getCheckedDossiers()), bureauId);
					actionDialog.show(getSupportFragmentManager(), "ArchivageDialogFragment");
					return true;
				case R.id.action_rejet:
					actionDialog = RejetDialogFragment.newInstance(new ArrayList<>(fragment.getCheckedDossiers()), bureauId);
					actionDialog.show(getSupportFragmentManager(), "RejetDialogFragment");
					return true;
				default:
					return false;
			}
		}

		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode actionMode) {
		DossierListFragment fragment = (DossierListFragment) getSupportFragmentManager().findFragmentByTag(DossierListFragment.TAG);

		if (fragment != null)
			fragment.clearSelection();

		mActionMode = null;
	}

	// </editor-fold desc="ActionMode">

	public void onFilterSave(Filter filter) {
		MyFilters.INSTANCE.selectFilter(filter);
		MyFilters.INSTANCE.save(filter);

		mFiltersSpinner.setSelection(mFilterAdapter.getPosition(filter), true);

		mFilterAdapter.notifyDataSetChanged();
		onDataChanged();
	}

	private void replaceLeftFragment(@NonNull String bureauId) {

		// Create fragment and give it an argument specifying the Bureau it should show

		DossierListFragment dossierFragment = new DossierListFragment();
		Bundle args = new Bundle();
		args.putString(DossierListFragment.ARG_BUREAU_ID, bureauId);
		dossierFragment.setArguments(args);

		// Replace whatever is in the fragment_container view with this fragment.

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.push_right_to_center, R.anim.push_center_to_left, R.anim.push_center_to_right, R.anim.push_left_to_center);
		transaction.replace(R.id.left_fragment, dossierFragment, DossierListFragment.TAG);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	// <editor-fold desc="AccountFragmentListener">

	@Override
	public void onAccountSelected(@NonNull Account account) {
		mDrawerLayout.closeDrawer(mDrawerMenu);
		MyAccounts.INSTANCE.selectAccount(account.getId());

		DossierListFragment dossierListFragment = (DossierListFragment) getSupportFragmentManager().findFragmentByTag(DossierListFragment.TAG);
		if (dossierListFragment != null)
			onBackPressed();

		BureauxListFragment bureauxFragment = (BureauxListFragment) getSupportFragmentManager().findFragmentByTag(BureauxListFragment.TAG);
		if (bureauxFragment != null)
			bureauxFragment.updateBureaux(true);
	}

	@Override
	public void onCreateAccountInvoked() {
		Intent preferencesIntent = new Intent(this, SettingsActivity.class);
		preferencesIntent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, AccountsPreferenceFragment.class.getName());
		startActivityForResult(preferencesIntent, EDIT_PREFERENCE_REQUEST);
	}

	// </editor-fold desc="AccountFragmentListener">

	// <editor-fold desc="BureauListFragmentListener">

	@Override
	public void onBureauListFragmentSelected(String id) {
		if (id != null)
			replaceLeftFragment(id);
	}

	// </editor-fold desc="BureauListFragmentListener">

	// <editor-fold desc="FilterDialogListener">

	public void onFilterChange(Filter filter) {

		mFiltersSpinner.setSelection(mFilterAdapter.getPosition(filter));

		MyFilters.INSTANCE.selectFilter(filter);
		onDataChanged();
	}

	public void onFilterCancel() {
		mFiltersSpinner.setSelection(mFilterAdapter.getPosition(MyFilters.INSTANCE.getSelectedFilter()));
	}

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

	// </editor-fold desc="FilterDialogListener">

	// <editor-fold desc="DossierListFragmentListener">

	@Override
	public void onDossierSelected(@Nullable Dossier dossier, @Nullable String bureauId) {

		Fragment fragment = getSupportFragmentManager().findFragmentByTag(DossierDetailFragment.TAG);
		if (fragment != null)
			((DossierDetailFragment) fragment).update(dossier, bureauId);
	}

	@Override
	public void onDossierCheckedChanged() {
		if (mActionMode == null)
			mActionMode = startSupportActionMode(this);
		else
			mActionMode.invalidate();
	}

	@Override
	public void onDossiersLoaded(int size) {
		onDossierSelected(null, null);

		if (mFilterAdapter == null)
			mFilterAdapter = new FilterAdapter(this);

		mFiltersSpinner.setAdapter(mFilterAdapter);
		mFiltersSpinner.setVisibility(View.VISIBLE);
	}

	@Override
	public void onDossiersNotLoaded() {
		mFiltersSpinner.setVisibility(View.INVISIBLE);
	}

	// </editor-fold desc="DossierListFragmentListener">

	/**
	 * Listener used on the Drawer Layout used to control the Action Bar content depending
	 * on the Drawer state.
	 */
	private class DossiersActionBarDrawerToggle extends ActionBarDrawerToggle {

		public DossiersActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout) {
			super(activity, drawerLayout, (Toolbar) activity.findViewById(R.id.home_toolbar), R.string.drawer_open, R.string.drawer_close);
		}

		@Override
		public void onDrawerClosed(View view) {
			if ((getSupportActionBar() != null) && (MyAccounts.INSTANCE.getSelectedAccount() != null))
				getSupportActionBar().setTitle(MyAccounts.INSTANCE.getSelectedAccount().getTitle());

			// calls onPrepareOptionMenu to show context specific actions
			invalidateOptionsMenu();
		}

		@Override
		/** Called when a drawer has settled in a completely open state. */
		public void onDrawerOpened(View drawerView) {
			if (getSupportActionBar() != null)
				getSupportActionBar().setTitle(R.string.app_name);

			// calls onPrepareOptionMenu to hide context specific actions
			invalidateOptionsMenu();
		}
	}

}