package org.adullact.iparapheur.controller.dossier;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.IParapheurApplication;
import org.adullact.iparapheur.controller.account.AccountListFragment;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.controller.bureau.BureauxListFragment;
import org.adullact.iparapheur.controller.dossier.action.ArchivageDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.MailSecDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.RejectDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.SignatureDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.TdtHeliosDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.VisaDialogFragment;
import org.adullact.iparapheur.controller.dossier.filter.FilterAdapter;
import org.adullact.iparapheur.controller.dossier.filter.FilterDialog;
import org.adullact.iparapheur.controller.dossier.filter.MyFilters;
import org.adullact.iparapheur.controller.preferences.ImportCertificatesDialogFragment;
import org.adullact.iparapheur.controller.preferences.PreferencesAccountFragment;
import org.adullact.iparapheur.controller.preferences.PreferencesActivity;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.Filter;
import org.adullact.iparapheur.utils.DeviceUtils;
import org.adullact.iparapheur.utils.FileUtils;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.LoadingTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


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
 * {@link DossierListFragment.DossierListFragmentListener} interface
 * to listen for item selections.
 */
public class MainActivity extends AppCompatActivity implements DossierListFragment.DossierListFragmentListener, BureauxListFragment.BureauListFragmentListener,
		AccountListFragment.AccountFragmentListener, AdapterView.OnItemSelectedListener, LoadingTask.DataChangeListener, FilterDialog.FilterDialogListener,
		ActionMode.Callback, DossierDetailFragment.DossierDetailsFragmentListener {

	private static final String SHARED_PREFERENCES_MAIN = ":iparapheur:shared_preferences_main";
	private static final String SHARED_PREFERENCES_IS_DRAWER_KNOWN = "is_drawer_known";

	private static final String SCHEME_URI = "iparapheur";
	private static final String SCHEME_URI_IMPORTCERTIFICATE = "importCertificate";
	private static final String SCHEME_URI_IMPORTCERTIFICATE_URL = "AndroidUrl";
	private static final String SCHEME_URI_IMPORTCERTIFICATE_PASSWORD = "AndroidPwd";

	private DrawerLayout mLeftDrawerLayout;
	private DrawerLayout mRightDrawerLayout;
	private FrameLayout mLeftDrawerMenu;
	private ActionBarDrawerToggle mLeftDrawerToggle;
	private FilterAdapter mFilterAdapter;                      // Adapter for action bar, used to display user's filters
	private Spinner mFiltersSpinner;
	private ActionMode mActionMode;                            // The actionMode used when dossiers are checked

	// <editor-fold desc="LifeCycle">

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// To have a transparent StatusBar, and a background color behind

		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

		// Loading indicator

		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main_activity);

		mLeftDrawerLayout = (DrawerLayout) findViewById(R.id.activity_dossiers_drawer_layout);
		mRightDrawerLayout = (DrawerLayout) findViewById(R.id.activity_dossiers_right_drawer_layout);
		mLeftDrawerMenu = (FrameLayout) findViewById(R.id.activity_dossiers_left_drawer);
		mFiltersSpinner = (Spinner) findViewById(R.id.activity_dossiers_toolbar_spinner);

		if (mFiltersSpinner != null)
			mFiltersSpinner.setOnItemSelectedListener(this);

		Toolbar toolbar = (Toolbar) findViewById(R.id.home_toolbar);
		setSupportActionBar(toolbar);

		if (getSupportActionBar() != null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Drawers

		mLeftDrawerToggle = new DossiersActionBarDrawerToggle(this, mLeftDrawerLayout);
		mLeftDrawerLayout.addDrawerListener(mLeftDrawerToggle);
		mRightDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
	}

	@Override protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mLeftDrawerToggle.syncState();
	}

	@Override protected void onStart() {
		super.onStart();

		// Starting checks

		List<File> certificatesFoundList = FileUtils.getBksFromDownloadFolder();
		if (!certificatesFoundList.isEmpty()) {
			File certificateFound = certificatesFoundList.get(0);
			DialogFragment actionDialog = ImportCertificatesDialogFragment.newInstance(certificateFound);
			actionDialog.show(getSupportFragmentManager(), ImportCertificatesDialogFragment.FRAGMENT_TAG);
		}

		// Clear backStack (wrong backStack can stay after rotation)

		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

		// ContentView Fragment restore

		Fragment contentFragment = getSupportFragmentManager().findFragmentByTag(DossierDetailFragment.FRAGMENT_TAG);
		if (contentFragment == null)
			contentFragment = new DossierDetailFragment();

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.dossier_detail_layout, contentFragment, DossierDetailFragment.FRAGMENT_TAG);
		transaction.commit();

		//

		Fragment fragmentToDisplay = getSupportFragmentManager().findFragmentByTag(BureauxListFragment.FRAGMENT_TAG);

		if (fragmentToDisplay == null)
			fragmentToDisplay = new BureauxListFragment();

		// Replace whatever is in the fragment_container view with this fragment.

		fragmentToDisplay.setRetainInstance(true);
		if (findViewById(R.id.left_fragment) != null)
			replaceLeftFragment(fragmentToDisplay, BureauxListFragment.FRAGMENT_TAG, false);

		// Selecting the first account by default, the demo one

		Account selectedAccount = MyAccounts.INSTANCE.getSelectedAccount();
		if (selectedAccount == null)
			MyAccounts.INSTANCE.selectAccount(MyAccounts.INSTANCE.getAccounts().get(0).getId());
	}

	@Override public void onResume() {
		super.onResume();

		// Check possible Scheme URI call.
		// Waiting arguments like :
		//
		// iparapheur://importCertificate?AndroidUrl=https%3A%2F%2Fcurl.adullact.org%2FsC4VU%2Fbma.p12      (mandatory)
		//                               &AndroidPwd=bma                                                    (optional)
		//                               &iOsUrl=https%3A%2F%2Fcurl.adullact.org%2FSUZI2%2Fbma.p12          (ignored)
		//                               &iOsPwd=bma                                                        (ignored)
		Uri schemeUri = getIntent().getData();
		boolean isValidUriScheme = (schemeUri != null && TextUtils.equals(schemeUri.getScheme(), SCHEME_URI));

		if (isValidUriScheme) {
			if (TextUtils.equals(schemeUri.getHost(), SCHEME_URI_IMPORTCERTIFICATE)) {

				String certifUrl = schemeUri.getQueryParameter(SCHEME_URI_IMPORTCERTIFICATE_URL);
				String certifPassword = schemeUri.getQueryParameter(SCHEME_URI_IMPORTCERTIFICATE_PASSWORD);

				if (!TextUtils.isEmpty(certifUrl))
					importCertificate(certifUrl, certifPassword);
				else
					Toast.makeText(this, R.string.import_error_message_incorrect_scheme, Toast.LENGTH_SHORT).show();
			}

			// If we let the intent data, it can try to re-import certificate, back from history.
			getIntent().setData(null);
		}

		// On first launch, we have to open the NavigationDrawer.
		// It's in the Android guidelines, the user have to know it's here.
		// (And we want to open it in portrait in any case, otherwise the user sees a weird grey panel)

		SharedPreferences settings = getSharedPreferences(SHARED_PREFERENCES_MAIN, 0);
		boolean isDrawerKnown = settings.getBoolean(SHARED_PREFERENCES_IS_DRAWER_KNOWN, false);
		boolean isDeviceInPortrait = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);

		if ((!isDrawerKnown) && isDeviceInPortrait) {

			mLeftDrawerLayout.openDrawer(mLeftDrawerMenu);

			// Registering the fact that the user knows the drawer

			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(SHARED_PREFERENCES_IS_DRAWER_KNOWN, true);
			editor.apply();
		}
	}

	@Override protected void onPause() {
		super.onPause();

		// Save accounts state for later use. In our case, the latest selected account
		// will be automatically selected if the application is killed and relaunched.
		MyAccounts.INSTANCE.saveState();
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == PreferencesActivity.PREFERENCES_ACTIVITY_REQUEST_CODE) {

				// Notify BureauxFragments to update accounts list (the bureau will update back this Activity if needed)
				AccountListFragment accountListFragment = (AccountListFragment) getSupportFragmentManager().findFragmentByTag(AccountListFragment.FRAGMENT_TAG);

			if (accountListFragment != null) {
					accountListFragment.accountsChanged();
			}
		}
	}

	@Override public void onBackPressed() {

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

			// First, try to pop backStack (and open the drawer to show it)

			if (getSupportFragmentManager().getBackStackEntryCount() > 0) {

				if (!mLeftDrawerLayout.isDrawerOpen(mLeftDrawerMenu))
					mLeftDrawerLayout.openDrawer(mLeftDrawerMenu);

				getSupportFragmentManager().popBackStack();
				return;
			}

			// Then, close the drawer

			if (mLeftDrawerLayout.isDrawerOpen(mLeftDrawerMenu)) {
				mLeftDrawerLayout.closeDrawer(mLeftDrawerMenu);
				return;
			}
		}
		else {

			// First, close the drawer

			if (mLeftDrawerLayout.isDrawerOpen(mLeftDrawerMenu)) {
				mLeftDrawerLayout.closeDrawer(mLeftDrawerMenu);
				return;
			}

			// Then, try to pop backstack

			if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
				getSupportFragmentManager().popBackStack();
				return;
			}
		}

		super.onBackPressed();
	}

	// </editor-fold desc="LifeCycle">

	// <editor-fold desc="ActionBar">

	@Override public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.dossiers_menu, menu);

		// Alignment in TopBar isn't working on XML, but works programmatically

		Toolbar.LayoutParams spinnerContainerLayoutParams = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT,
																					 Toolbar.LayoutParams.WRAP_CONTENT,
																					 Gravity.TOP | Gravity.END
		);
		spinnerContainerLayoutParams.rightMargin = Math.round(DeviceUtils.dipsToPixels(this, 20));
		mFiltersSpinner.setLayoutParams(spinnerContainerLayoutParams);

		return super.onCreateOptionsMenu(menu);
	}

	@Override public boolean onPrepareOptionsMenu(Menu menu) {

		// Show or hide specific menu actions depending on displayed fragment

		Fragment dossierFragment = getSupportFragmentManager().findFragmentByTag(DossierListFragment.FRAGMENT_TAG);
		mFiltersSpinner.setVisibility((dossierFragment == null) ? View.GONE : View.VISIBLE);

		// Show or hide specific menu actions depending on Drawer state.

		if ((mLeftDrawerLayout != null) && (mLeftDrawerMenu != null)) {
			boolean actionsVisibility = !mLeftDrawerLayout.isDrawerVisible(mLeftDrawerMenu) && (MyAccounts.INSTANCE.getSelectedAccount() != null);
			menu.setGroupVisible(R.id.dossiers_menu_actions, actionsVisibility);
			return super.onPrepareOptionsMenu(menu);
		}

		return false;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {

		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event

		if (mLeftDrawerToggle.onOptionsItemSelected(item))
			return true;

		// TODO : handle dossier(s) actions
		// Handle presses on the action bar items

		switch (item.getItemId()) {

			case R.id.action_settings:
				startActivityForResult(new Intent(this, PreferencesActivity.class), PreferencesActivity.PREFERENCES_ACTIVITY_REQUEST_CODE);
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

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

	@Override public void onNothingSelected(AdapterView<?> parent) { }

	// </editor-fold desc="ActionBar">

	// <editor-fold desc="ActionMode">

	@Override public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
		actionMode.setTitleOptionalHint(true);
		MenuInflater inflater = actionMode.getMenuInflater();
		inflater.inflate(R.menu.dossiers_list_menu_actions, menu);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(ContextCompat.getColor(this, R.color.contextual_700));
		}

		return true;
	}

	@Override public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {

		DossierListFragment fragment = (DossierListFragment) getSupportFragmentManager().findFragmentByTag(DossierListFragment.FRAGMENT_TAG);

		// Default cases

		if (fragment == null)
			return false;

		HashSet<Dossier> checkedDossiers = fragment.getCheckedDossiers();

		if ((checkedDossiers == null) || (checkedDossiers.isEmpty())) {
			actionMode.finish();
			return false;
		}

		// Compute visibility

		actionMode.setTitle(getString(R.string.action_mode_nb_dossiers).replace("-number-", String.valueOf(checkedDossiers.size())));
		// Get the intersection of all possible actions on checked dossiers and update the menu
		menu.setGroupVisible(R.id.dossiers_menu_main_actions, false);
		menu.setGroupVisible(R.id.dossiers_menu_other_actions, false);

		HashSet<Action> actions = new HashSet<>(Arrays.asList(Action.values()));

		boolean sign = false;
		for (Dossier dossier : checkedDossiers) {
			actions.retainAll(dossier.getActions());
			sign = sign || (dossier.getActions().contains(Action.SIGNATURE) && !dossier.isSignPapier());
		}

		for (Action action : actions) {

			MenuItem item;
			int menuItemId;
			String menuTitle;

			// Si c'est le visa, et qu'on a aussi de la signature dans le lot, on prend
			// la signature (possible en API v3).
			if (action.equals(Action.VISA) && (sign)) {
				menuItemId = Action.SIGNATURE.getMenuItemId();
				menuTitle = getString(Action.VISA.getTitle()) + "/" + getString(Action.SIGNATURE.getTitle());
			}
			else {
				menuItemId = action.getMenuItemId();
				menuTitle = getString(action.getTitle());
			}

			// If we only have signPapier type, we only have a VISA, actually
			boolean isVisible = !(action.equals(Action.SIGNATURE) && !sign);

			// Set current state

			item = menu.findItem(menuItemId);
			if (item != null) {
				item.setTitle(menuTitle);
				item.setVisible(isVisible);
			}
		}

		return true;
	}

	@Override public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

		DossierListFragment dossierListFragment = (DossierListFragment) getSupportFragmentManager().findFragmentByTag(DossierListFragment.FRAGMENT_TAG);
		return (dossierListFragment != null) && onActionItemChecked(menuItem, dossierListFragment.getCheckedDossiers());
	}

	private boolean onActionItemChecked(MenuItem menuItem, HashSet<Dossier> dossiers) {
		DossierListFragment dossierListFragment = (DossierListFragment) getSupportFragmentManager().findFragmentByTag(DossierListFragment.FRAGMENT_TAG);

		if (dossierListFragment != null) {
			String bureauId = dossierListFragment.getBureauId();

			DialogFragment actionDialog;
			switch (menuItem.getItemId()) {
				case R.id.action_visa:
					actionDialog = VisaDialogFragment.newInstance(new ArrayList<>(dossierListFragment.getCheckedDossiers()), bureauId);
					actionDialog.setTargetFragment(dossierListFragment, VisaDialogFragment.REQUEST_CODE_VISA);
					actionDialog.show(getSupportFragmentManager(), VisaDialogFragment.FRAGMENT_TAG);
					return true;
				case R.id.action_signature:
					actionDialog = SignatureDialogFragment.newInstance(new ArrayList<>(dossierListFragment.getCheckedDossiers()), bureauId);
					actionDialog.setTargetFragment(dossierListFragment, SignatureDialogFragment.REQUEST_CODE_SIGNATURE);
					actionDialog.show(getSupportFragmentManager(), SignatureDialogFragment.FRAGMENT_TAG);
					return true;
				case R.id.action_mailsec:
					actionDialog = MailSecDialogFragment.newInstance(new ArrayList<>(dossierListFragment.getCheckedDossiers()), bureauId);
					actionDialog.show(getSupportFragmentManager(), "MailSecDialogFragment");
					return true;
				case R.id.action_tdt_actes:
				case R.id.action_tdt_helios:
					actionDialog = TdtHeliosDialogFragment.newInstance(new ArrayList<>(dossierListFragment.getCheckedDossiers()), bureauId);
					actionDialog.show(getSupportFragmentManager(), "TdtHeliosDialogFragment");
					return true;
				case R.id.action_archivage:
					actionDialog = ArchivageDialogFragment.newInstance(new ArrayList<>(dossierListFragment.getCheckedDossiers()), bureauId);
					actionDialog.show(getSupportFragmentManager(), "ArchivageDialogFragment");
					return true;
				case R.id.action_rejet:
					actionDialog = RejectDialogFragment.newInstance(new ArrayList<>(dossierListFragment.getCheckedDossiers()), bureauId);
					actionDialog.setTargetFragment(dossierListFragment, RejectDialogFragment.REQUEST_CODE_REJECT);
					actionDialog.show(getSupportFragmentManager(), RejectDialogFragment.FRAGMENT_TAG);
					return true;
				default:
					return false;
			}
		}

		return false;
	}

	@Override public void onDestroyActionMode(ActionMode actionMode) {
		DossierListFragment fragment = (DossierListFragment) getSupportFragmentManager().findFragmentByTag(DossierListFragment.FRAGMENT_TAG);

		if (fragment != null)
			fragment.clearSelection();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
		}

		mActionMode = null;
	}

	// </editor-fold desc="ActionMode">

	private void replaceLeftFragment(@NonNull Fragment fragment, @NonNull String tag, boolean animated) {

		// Bypass and send to the Drawer, if there isn't any left panel

		if (findViewById(R.id.left_fragment) == null) {
			replaceDrawerFragment(fragment, tag, animated);
			return;
		}

		// Replace whatever is in the fragment_container view with this fragment.

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		int enter = animated ? R.anim.push_right_to_center : 0;
		int exit = animated ? R.anim.push_center_to_left : 0;
		transaction.setCustomAnimations(enter, exit, R.anim.push_center_to_right, R.anim.push_left_to_center);
		transaction.replace(R.id.left_fragment, fragment, tag);

		if (animated)
			transaction.addToBackStack(null);

		transaction.commit();
	}

	private void replaceDrawerFragment(@NonNull Fragment fragment, @NonNull String tag, boolean animated) {

		// Replace whatever is in the fragment_container view with this fragment.

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		int enter = animated ? R.anim.push_right_to_center : 0;
		int exit = animated ? R.anim.push_center_to_left : 0;
		transaction.setCustomAnimations(enter, exit, R.anim.push_center_to_right, R.anim.push_left_to_center);
		transaction.replace(R.id.drawer_panel, fragment, tag);

		if (animated)
			transaction.addToBackStack(null);

		transaction.commit();
	}

	private void importCertificate(@NonNull final String url, @Nullable final String password) {

		String certificateFileName = url.substring(url.lastIndexOf('/') + 1);
		final String certificateLocalPath = new File(IParapheurApplication.getContext().getExternalCacheDir(), certificateFileName).getAbsolutePath();

		// Download and import

		new AsyncTask<Void, Void, Void>() {

			private int mErrorMessageResource = -1;

			@Override protected Void doInBackground(Void... params) {

				try {
					boolean downloadSuccessful = RESTClient.INSTANCE.downloadCertificate(url, certificateLocalPath);

					if (!downloadSuccessful)
						mErrorMessageResource = R.string.import_error_message_cant_download_certificate;
				}
				catch (IParapheurException e) {
					Crashlytics.logException(e);
					e.printStackTrace();
				}

				return null;
			}

			@Override protected void onPostExecute(Void aVoid) {

				if (mErrorMessageResource == -1) {

					if (password != null) {
						FileUtils.importCertificate(MainActivity.this, new File(certificateLocalPath), password);
					}
					else {
						DialogFragment actionDialog = ImportCertificatesDialogFragment.newInstance(new File(certificateLocalPath));
						actionDialog.show(getSupportFragmentManager(), ImportCertificatesDialogFragment.FRAGMENT_TAG);
					}
				}
				else {
					Toast.makeText(MainActivity.this, mErrorMessageResource, Toast.LENGTH_SHORT).show();
				}

				super.onPostExecute(aVoid);
			}

		}.execute();
	}

	private void launchActionPopup(@NonNull Dossier dossier, @NonNull String bureauId, @NonNull Action action) {

		DossierListFragment dossierListFragment = (DossierListFragment) getSupportFragmentManager().findFragmentByTag(DossierListFragment.FRAGMENT_TAG);
		ArrayList<Dossier> singleList = new ArrayList<>();
		singleList.add(dossier);
		DialogFragment actionDialog;

		if (action == Action.REJET) {
			actionDialog = RejectDialogFragment.newInstance(singleList, bureauId);
			actionDialog.setTargetFragment(dossierListFragment, RejectDialogFragment.REQUEST_CODE_REJECT);
			actionDialog.show(getSupportFragmentManager(), RejectDialogFragment.FRAGMENT_TAG);
		}
		else if (action == Action.VISA) {
			actionDialog = VisaDialogFragment.newInstance(singleList, bureauId);
			actionDialog.setTargetFragment(dossierListFragment, VisaDialogFragment.REQUEST_CODE_VISA);
			actionDialog.show(getSupportFragmentManager(), VisaDialogFragment.FRAGMENT_TAG);
		}
		else if (action == Action.SIGNATURE) {
			actionDialog = SignatureDialogFragment.newInstance(singleList, bureauId);
			actionDialog.setTargetFragment(dossierListFragment, SignatureDialogFragment.REQUEST_CODE_SIGNATURE);
			actionDialog.show(getSupportFragmentManager(), SignatureDialogFragment.FRAGMENT_TAG);
		}
	}

	// <editor-fold desc="AccountFragmentListener">

	@Override public void onAccountSelected(@NonNull Account account) {

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			BureauxListFragment bureauxFragment = new BureauxListFragment();
			replaceDrawerFragment(bureauxFragment, BureauxListFragment.FRAGMENT_TAG, true);
		}
		else {
			mLeftDrawerLayout.closeDrawer(mLeftDrawerMenu);
		}

		MyAccounts.INSTANCE.selectAccount(account.getId());

		// If we selected a new account, and we are on the DossierFragment displayed
		// We'll want to pop the BackStack to get on the BureauFragment

		DossierListFragment dossierListFragment = (DossierListFragment) getSupportFragmentManager().findFragmentByTag(DossierListFragment.FRAGMENT_TAG);
		if (dossierListFragment != null)
			if (getSupportFragmentManager().getBackStackEntryCount() > 0)
				getSupportFragmentManager().popBackStack();

		// Then , we just update the BureauFragment to the accurate Account

		BureauxListFragment bureauxFragment = (BureauxListFragment) getSupportFragmentManager().findFragmentByTag(BureauxListFragment.FRAGMENT_TAG);
		if (bureauxFragment != null)
			bureauxFragment.updateBureaux(true);
	}

	@Override public void onCreateAccountInvoked() {
		Intent preferencesIntent = new Intent(this, PreferencesActivity.class);
		preferencesIntent.putExtra(PreferencesActivity.ARGUMENT_GO_TO_FRAGMENT, PreferencesAccountFragment.class.getSimpleName());
		startActivityForResult(preferencesIntent, PreferencesActivity.PREFERENCES_ACTIVITY_REQUEST_CODE);
	}

	// </editor-fold desc="AccountFragmentListener">

	// <editor-fold desc="BureauListFragmentListener">

	@Override public void onBureauListFragmentSelected(@Nullable String id) {
		if (id != null) {
			DossierListFragment fragment = DossierListFragment.newInstance(id);
			replaceLeftFragment(fragment, DossierListFragment.FRAGMENT_TAG, true);
		}
	}

	// </editor-fold desc="BureauListFragmentListener">

	// <editor-fold desc="FilterDialogListener">

	public void onFilterSave(@NonNull Filter filter) {
		MyFilters.INSTANCE.selectFilter(filter);
		MyFilters.INSTANCE.save(filter);

		mFiltersSpinner.setSelection(mFilterAdapter.getPosition(filter), true);

		mFilterAdapter.notifyDataSetChanged();
		onDataChanged();
	}

	public void onFilterChange(@NonNull Filter filter) {

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
	@Override public void onDataChanged() {
		DossierListFragment listFragment = (DossierListFragment) getSupportFragmentManager().findFragmentByTag(DossierListFragment.FRAGMENT_TAG);
		if (listFragment != null) {
			// this method will reload dossiers fragments
			listFragment.reload();
		}

		onDossierSelected(null, null);
		invalidateOptionsMenu();
	}

	// </editor-fold desc="FilterDialogListener">

	// <editor-fold desc="DossierListFragmentListener">

	@Override public void onDossierSelected(@Nullable Dossier dossier, @Nullable String bureauId) {

		if (dossier != null)
			if (mLeftDrawerLayout.isDrawerOpen(mLeftDrawerMenu))
				mLeftDrawerLayout.closeDrawer(mLeftDrawerMenu);

		DossierDetailFragment fragment = (DossierDetailFragment) getSupportFragmentManager().findFragmentByTag(DossierDetailFragment.FRAGMENT_TAG);
		if ((fragment != null) && (dossier != null) && (bureauId != null)) {
			fragment.showProgressLayout();
			fragment.update(dossier, bureauId);
		}
	}

	@Override public void onDossierCheckedChanged(boolean forceClose) {
		if (mActionMode != null)
			mActionMode.invalidate();
		else if (!forceClose)
			mActionMode = startSupportActionMode(this);
	}

	@Override public void onDossiersLoaded(int size) {
		onDossierSelected(null, null);

		if (mFilterAdapter == null)
			mFilterAdapter = new FilterAdapter(this);

		mFiltersSpinner.setAdapter(mFilterAdapter);
		mFiltersSpinner.setVisibility(View.VISIBLE);
	}

	@Override public void onDossiersNotLoaded() {
		mFiltersSpinner.setVisibility(View.INVISIBLE);
	}

	// </editor-fold desc="DossierListFragmentListener">

	// <editor-fold desc="DossierDetailsFragmentListener">

	@Override public void toggleInfoDrawer() {

		if (mRightDrawerLayout.isDrawerOpen(GravityCompat.END))
			mRightDrawerLayout.closeDrawer(GravityCompat.END);
		else
			mRightDrawerLayout.openDrawer(GravityCompat.END);
	}

	@Override public void lockInfoDrawer(boolean lock) {

		if (lock) {
			mRightDrawerLayout.closeDrawer(GravityCompat.END);
			mRightDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}
		else {
			mRightDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		}
	}

	@Override public void onValidateButtonClicked(@NonNull Dossier dossier, @NonNull String bureauId) {
		if (dossier.getActions().contains(Action.SIGNATURE))
			launchActionPopup(dossier, bureauId, Action.SIGNATURE);
		else
			launchActionPopup(dossier, bureauId, Action.VISA);
	}

	@Override public void onCancelButtonClicked(@NonNull Dossier dossier, @NonNull String bureauId) {
		launchActionPopup(dossier, bureauId, Action.REJET);
	}

	// </editor-fold desc="DossierDetailsFragmentListener">

	/**
	 * Listener used on the Drawer Layout used to control the Action Bar content depending
	 * on the Drawer state.
	 */
	private class DossiersActionBarDrawerToggle extends ActionBarDrawerToggle {

		public DossiersActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout) {
			super(activity, drawerLayout, (Toolbar) activity.findViewById(R.id.home_toolbar), R.string.drawer_open, R.string.drawer_close);
		}

		@Override public void onDrawerClosed(View view) {

			if (getSupportActionBar() != null) {
				Account selectedAccount = MyAccounts.INSTANCE.getSelectedAccount();
				if (selectedAccount != null)
					getSupportActionBar().setTitle(selectedAccount.getTitle());
			}

			// calls onPrepareOptionMenu to show context specific actions
			invalidateOptionsMenu();
		}

		@Override public void onDrawerOpened(View drawerView) {
			if (getSupportActionBar() != null)
				getSupportActionBar().setTitle(R.string.app_name);

			// calls onPrepareOptionMenu to hide context specific actions
			invalidateOptionsMenu();
		}

		@Override public void onDrawerStateChanged(int newState) {

			if (newState == DrawerLayout.STATE_SETTLING)
				if (mActionMode != null)
					mActionMode.finish();

			invalidateOptionsMenu();
		}
	}

}
