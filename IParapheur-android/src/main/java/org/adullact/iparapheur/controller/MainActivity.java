/*
 * <p>iParapheur Android<br/>
 * Copyright (C) 2016 Adullact-Projet.</p>
 *
 * <p>This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.</p>
 *
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.</p>
 *
 * <p>You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.</p>
 */
package org.adullact.iparapheur.controller;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.crashlytics.android.Crashlytics;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.AccountListFragment;
import org.adullact.iparapheur.controller.dossier.DossierDetailFragment;
import org.adullact.iparapheur.controller.dossier.action.ArchivageDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.MailSecDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.RejectDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.SignatureDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.TdtActesDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.VisaDialogFragment;
import org.adullact.iparapheur.controller.preferences.ImportCertificatesDialogFragment;
import org.adullact.iparapheur.controller.preferences.PreferencesAccountFragment;
import org.adullact.iparapheur.controller.preferences.PreferencesActivity;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.utils.AccountUtils;
import org.adullact.iparapheur.utils.CollectionUtils;
import org.adullact.iparapheur.utils.FileUtils;
import org.adullact.iparapheur.utils.IParapheurException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * An activity representing a list of Dossiers.
 * The activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link MenuFragment} and the item details
 * is a {@link DossierDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link MenuFragment.MenuFragmentListener} interface
 * to listen for item selections.
 */
public class MainActivity extends AppCompatActivity implements MenuFragment.MenuFragmentListener, AccountListFragment.AccountListFragmentListener,
		ActionMode.Callback, DossierDetailFragment.DossierDetailsFragmentListener {

	private static final String SHARED_PREFERENCES_MAIN = ":iparapheur:shared_preferences_main";
	private static final String SHARED_PREFERENCES_IS_DRAWER_KNOWN = "is_drawer_known";
	private static final String SAVED_STATE_SHOULD_SHOW_ACCOUNT_AFTER_ROTATION = "should_show_account_after_rotation";

	private static final String SCHEME_URI = "iparapheur";
	private static final String SCHEME_URI_IMPORTCERTIFICATE = "importCertificate";
	private static final String SCHEME_URI_IMPORTCERTIFICATE_URL = "AndroidUrl";
	private static final String SCHEME_URI_IMPORTCERTIFICATE_PASSWORD = "AndroidPwd";

	private DrawerLayout mLeftDrawerLayout;
	private DrawerLayout mRightDrawerLayout;
	private FrameLayout mLeftDrawerMenu;
	private ActionBarDrawerToggle mLeftDrawerToggle;
	private ViewSwitcher mNavigationDrawerAccountViewSwitcher;

	private boolean mSouldShowAccountAfterRotation = false;
	private ActionMode mActionMode;                            // The actionMode used when dossiers are checked

	// <editor-fold desc="LifeCycle">

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// To have a transparent StatusBar, and a background color behind

		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

		// Loading indicator

		setContentView(R.layout.main_activity);

		mLeftDrawerLayout = (DrawerLayout) findViewById(R.id.activity_dossiers_drawer_layout);
		mRightDrawerLayout = (DrawerLayout) findViewById(R.id.activity_dossiers_right_drawer_layout);
		mLeftDrawerMenu = (FrameLayout) findViewById(R.id.activity_dossiers_left_drawer);

		Toolbar toolbar = (Toolbar) findViewById(R.id.menu_toolbar);
		setSupportActionBar(toolbar);

		if (getSupportActionBar() != null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Drawers

		mLeftDrawerToggle = new DossiersActionBarDrawerToggle(this, mLeftDrawerLayout);
		mLeftDrawerLayout.addDrawerListener(mLeftDrawerToggle);
		mRightDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

		mNavigationDrawerAccountViewSwitcher = (ViewSwitcher) findViewById(R.id.navigation_drawer_viewswitcher);

		ImageButton drawerAccountImageButton = (ImageButton) findViewById(R.id.navigation_drawer_menu_header_account_button);
		if (drawerAccountImageButton != null) {
			drawerAccountImageButton.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {

					if (mNavigationDrawerAccountViewSwitcher == null)
						return;

					boolean switchToAccountView = (mNavigationDrawerAccountViewSwitcher.getDisplayedChild() == 0);
					if (switchToAccountView)
						mNavigationDrawerAccountViewSwitcher.setDisplayedChild(1);
					else
						mNavigationDrawerAccountViewSwitcher.setDisplayedChild(0);

					View filterButton = findViewById(R.id.navigation_drawer_filters_menu_header_filters_imagebutton);
					View downloadButton = findViewById(R.id.navigation_drawer_filters_menu_header_download_imagebutton);

					filterButton.setVisibility(switchToAccountView ? View.GONE : View.VISIBLE);
					downloadButton.setVisibility(switchToAccountView ? View.GONE : View.VISIBLE);
				}
			});
		}

		// ContentView Fragment restore

		Fragment contentFragment = getFragmentManager().findFragmentByTag(DossierDetailFragment.FRAGMENT_TAG);
		if (contentFragment == null)
			contentFragment = new DossierDetailFragment();
		contentFragment.setRetainInstance(true);

		FragmentTransaction contentTransaction = getFragmentManager().beginTransaction();
		contentTransaction.replace(R.id.dossier_detail_layout, contentFragment, DossierDetailFragment.FRAGMENT_TAG);
		contentTransaction.commit();
	}

	@Override protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Sync the toggle state after onRestoreInstanceState has occurred.

		mLeftDrawerToggle.syncState();
		refreshNavigationDrawerHeader();

		// Default FAB visibility

		View fabSwitcher = findViewById(R.id.mupdf_main_fab_viewswitcher);
		FloatingActionButton mainFab = (FloatingActionButton) findViewById(R.id.mupdf_main_menu_fabbutton);

		if (fabSwitcher != null)
			fabSwitcher.setVisibility(View.GONE);

		if (mainFab != null)
			mainFab.hide();

		//

		if (savedInstanceState != null)
			mSouldShowAccountAfterRotation = savedInstanceState.getBoolean(SAVED_STATE_SHOULD_SHOW_ACCOUNT_AFTER_ROTATION);
	}

	@Override protected void onStart() {
		super.onStart();

		// Starting checks

		List<File> certificatesFoundList = FileUtils.getBksFromDownloadFolder();
		if (!certificatesFoundList.isEmpty()) {
			File certificateFound = certificatesFoundList.get(0);
			DialogFragment actionDialog = ImportCertificatesDialogFragment.newInstance(certificateFound);
			actionDialog.show(getFragmentManager(), ImportCertificatesDialogFragment.FRAGMENT_TAG);
		}
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

		if (!isDrawerKnown) {
			mLeftDrawerLayout.openDrawer(mLeftDrawerMenu);

			// Registering the fact that the user knows the drawer
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(SHARED_PREFERENCES_IS_DRAWER_KNOWN, true);
			editor.apply();
		}
		else if (mSouldShowAccountAfterRotation) {

			mSouldShowAccountAfterRotation = false;
			mLeftDrawerLayout.openDrawer(mLeftDrawerMenu);

			if (isDeviceInPortrait)
				if (mNavigationDrawerAccountViewSwitcher != null)
					mNavigationDrawerAccountViewSwitcher.setDisplayedChild(1);
		}
		else {
			mLeftDrawerLayout.closeDrawer(mLeftDrawerMenu);
		}

		// Restoring proper Drawer state on selected dossiers

		MenuFragment menuFragment = (MenuFragment) getFragmentManager().findFragmentByTag(MenuFragment.FRAGMENT_TAG);
		if ((menuFragment != null) && !menuFragment.getCheckedDossiers().isEmpty()) {
			mActionMode = startSupportActionMode(this);

			if (isDeviceInPortrait)
				mLeftDrawerLayout.openDrawer(mLeftDrawerMenu);
			else
				mLeftDrawerLayout.closeDrawer(mLeftDrawerMenu);
		}
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == PreferencesActivity.PREFERENCES_ACTIVITY_REQUEST_CODE) {

			// Notify BureauxFragments to update accounts list (the bureau will update back this Activity if needed)
			AccountListFragment accountListFragment = (AccountListFragment) getFragmentManager().findFragmentByTag(AccountListFragment.FRAGMENT_TAG);

			if (accountListFragment != null) {
				accountListFragment.accountsChanged();
			}
		}
	}

	@Override public void onBackPressed() {

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

			// Drawer account back

			if (mNavigationDrawerAccountViewSwitcher.getDisplayedChild() == 1) {
				mNavigationDrawerAccountViewSwitcher.setDisplayedChild(0);
				return;
			}

			if (mLeftDrawerLayout.isDrawerOpen(mLeftDrawerMenu)) {

				// Menu back

				MenuFragment bureauxFragment = (MenuFragment) getFragmentManager().findFragmentByTag(MenuFragment.FRAGMENT_TAG);
				if (bureauxFragment != null)
					if (bureauxFragment.onBackPressed())
						return;

				// Close the drawer

				if (mLeftDrawerLayout.isDrawerOpen(mLeftDrawerMenu)) {
					mLeftDrawerLayout.closeDrawer(mLeftDrawerMenu);
					return;
				}

			}
			else {

				// Collapse the FAB

				DossierDetailFragment dossierDetailFragment = (DossierDetailFragment) getFragmentManager().findFragmentByTag(DossierDetailFragment.FRAGMENT_TAG);
				if (dossierDetailFragment != null)
					if (dossierDetailFragment.onBackPressed())
						return;

				// Menu back

				MenuFragment bureauxFragment = (MenuFragment) getFragmentManager().findFragmentByTag(MenuFragment.FRAGMENT_TAG);
				if (bureauxFragment != null) {
					if (bureauxFragment.onBackPressed()) {
						mLeftDrawerLayout.openDrawer(mLeftDrawerMenu);
						return;
					}
				}

			}
		}
		else {

			// First, close the drawer

			if (mLeftDrawerLayout.isDrawerOpen(mLeftDrawerMenu)) {
				mLeftDrawerLayout.closeDrawer(mLeftDrawerMenu);
				return;
			}

			// Collapse the FAB

			DossierDetailFragment dossierDetailFragment = (DossierDetailFragment) getFragmentManager().findFragmentByTag(DossierDetailFragment.FRAGMENT_TAG);
			if (dossierDetailFragment != null)
				if (dossierDetailFragment.onBackPressed())
					return;

			// Then, try to pop backstack

			MenuFragment bureauxFragment = (MenuFragment) getFragmentManager().findFragmentByTag(MenuFragment.FRAGMENT_TAG);
			if (bureauxFragment != null)
				if (bureauxFragment.onBackPressed())
					return;
		}

		super.onBackPressed();
	}

	@Override protected void onSaveInstanceState(Bundle outState) {

		boolean isDrawerOpened = mLeftDrawerLayout.isDrawerOpen(mLeftDrawerMenu);
		boolean isInLandscape = mNavigationDrawerAccountViewSwitcher == null;
		boolean isAccountViewSelected = (mNavigationDrawerAccountViewSwitcher != null) && (mNavigationDrawerAccountViewSwitcher.getDisplayedChild() == 1);
		boolean isAccountShown = isDrawerOpened && (isInLandscape || isAccountViewSelected);

		outState.putBoolean(SAVED_STATE_SHOULD_SHOW_ACCOUNT_AFTER_ROTATION, isAccountShown);

		super.onSaveInstanceState(outState);
	}

	// </editor-fold desc="LifeCycle">

	// <editor-fold desc="ActionBar">

	@Override public boolean onCreateOptionsMenu(Menu menu) {

		Toolbar actionsToolbar = (Toolbar) findViewById(R.id.actions_toolbar);
		if (actionsToolbar != null) {

			actionsToolbar.getMenu().clear();
			actionsToolbar.inflateMenu(R.menu.main_activity);
			actionsToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
				@Override public boolean onMenuItemClick(MenuItem item) {
					return onOptionsItemSelected(item);
				}
			});
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override public boolean onPrepareOptionsMenu(Menu menu) {
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

	// </editor-fold desc="ActionBar">

	// <editor-fold desc="ActionMode">

	@Override public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
		actionMode.setTitleOptionalHint(true);
		MenuInflater inflater = actionMode.getMenuInflater();
		inflater.inflate(R.menu.dossier_details_fragment_actions, menu);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(ContextCompat.getColor(this, R.color.contextual_700));
		}

		return true;
	}

	@Override public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {

		MenuFragment fragment = (MenuFragment) getFragmentManager().findFragmentByTag(MenuFragment.FRAGMENT_TAG);

		// Default cases

		if (fragment == null)
			return false;

		HashSet<Dossier> checkedDossiers = fragment.getCheckedDossiers();

		if ((checkedDossiers == null) || (checkedDossiers.isEmpty())) {
			actionMode.finish();
			return false;
		}

		// Get available actions from Dossiers

		HashSet<Action> actions = new HashSet<>(Arrays.asList(Action.values()));
		boolean sign = false;
		for (Dossier dossier : checkedDossiers) {
			actions.retainAll(dossier.getActions());
			sign = sign || (dossier.getActionDemandee() == Action.SIGNATURE) && !dossier.isSignPapier();
		}

		// Compute visibility

		actionMode.setTitle(String.format(getString(R.string.action_mode_nb_dossiers), checkedDossiers.size()));
		menu.setGroupVisible(R.id.dossiers_menu_main_actions, false);
		menu.setGroupVisible(R.id.dossiers_menu_other_actions, false);

		for (Action action : actions) {

			MenuItem item;
			int menuItemId;
			String menuTitle;

			// If we have a mixed set of VISA and SIGNATURE, then we have a general SIGNATURE
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

		MenuFragment menuFragment = (MenuFragment) getFragmentManager().findFragmentByTag(MenuFragment.FRAGMENT_TAG);
		if (menuFragment == null)
			return false;

		Bureau bureau = menuFragment.getSelectedBureau();
		Action invokedAction = Action.fromId(menuItem.getItemId());

		if ((invokedAction != null) && (bureau != null))
			launchActionPopup(menuFragment.getCheckedDossiers(), bureau.getId(), invokedAction);

		return true;
	}

	@Override public void onDestroyActionMode(ActionMode actionMode) {

		MenuFragment menuFragment = (MenuFragment) getFragmentManager().findFragmentByTag(MenuFragment.FRAGMENT_TAG);
		if (menuFragment != null)
			menuFragment.clearCheckSelection();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
		}

		mActionMode = null;
	}

	// </editor-fold desc="ActionMode">

	private void refreshNavigationDrawerHeader() {

		Account account = AccountUtils.SELECTED_ACCOUNT;

		TextView navigationDrawerAccountTitle = (TextView) findViewById(R.id.navigation_drawer_menu_header_title);
		TextView navigationDrawerAccountSubTitle = (TextView) findViewById(R.id.navigation_drawer_menu_header_subtitle);

		if (navigationDrawerAccountTitle != null)
			navigationDrawerAccountTitle.setText(account.getTitle());

		if (navigationDrawerAccountSubTitle != null)
			navigationDrawerAccountSubTitle.setText(account.getLogin());

	}

	private void importCertificate(@NonNull final String url, @Nullable final String password) {

		String certificateFileName = url.substring(url.lastIndexOf('/') + 1);
		final String certificateLocalPath = new File(getExternalCacheDir(), certificateFileName).getAbsolutePath();

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
						actionDialog.show(getFragmentManager(), ImportCertificatesDialogFragment.FRAGMENT_TAG);
					}
				}
				else {
					Toast.makeText(MainActivity.this, mErrorMessageResource, Toast.LENGTH_SHORT).show();
				}

				super.onPostExecute(aVoid);
			}

		}.execute();
	}

	private void launchActionPopup(@NonNull Set<Dossier> dossierSet, @NonNull String bureauId, @NonNull Action action) {

		MenuFragment menuFragment = (MenuFragment) getFragmentManager().findFragmentByTag(MenuFragment.FRAGMENT_TAG);
		DialogFragment actionDialog;
		ArrayList<Dossier> dossierList = new ArrayList<>(dossierSet);

		if (action == Action.REJET) {
			actionDialog = RejectDialogFragment.newInstance(dossierList, bureauId);
			actionDialog.setTargetFragment(menuFragment, RejectDialogFragment.REQUEST_CODE_REJECT);
			actionDialog.show(getFragmentManager(), RejectDialogFragment.FRAGMENT_TAG);
		}
		else if (action == Action.VISA) {
			actionDialog = VisaDialogFragment.newInstance(dossierList, bureauId);
			actionDialog.setTargetFragment(menuFragment, VisaDialogFragment.REQUEST_CODE_VISA);
			actionDialog.show(getFragmentManager(), VisaDialogFragment.FRAGMENT_TAG);
		}
		else if (action == Action.SIGNATURE) {
			actionDialog = SignatureDialogFragment.newInstance(dossierList, bureauId);
			actionDialog.setTargetFragment(menuFragment, SignatureDialogFragment.REQUEST_CODE_SIGNATURE);
			actionDialog.show(getFragmentManager(), SignatureDialogFragment.FRAGMENT_TAG);
		}
		else if (action == Action.MAILSEC) {
			actionDialog = MailSecDialogFragment.newInstance(dossierList, bureauId);
			actionDialog.show(getFragmentManager(), "MailSecDialogFragment");
		}
		else if ((action == Action.TDT) || (action == Action.TDT_HELIOS) || (action == Action.TDT_ACTES)) {
			actionDialog = TdtActesDialogFragment.newInstance(dossierList, bureauId);
			actionDialog.setTargetFragment(menuFragment, TdtActesDialogFragment.REQUEST_CODE_ACTES);
			actionDialog.show(getFragmentManager(), TdtActesDialogFragment.FRAGMENT_TAG);
		}
		else if (action == Action.ARCHIVAGE) {
			actionDialog = ArchivageDialogFragment.newInstance(dossierList, bureauId);
			actionDialog.show(getFragmentManager(), "ArchivageDialogFragment");
		}
		else {
			Log.e("Adrien", "UNKNOWN ACTION : " + action);
		}
	}

	// <editor-fold desc="AccountFragmentListener">

	@Override public void onAccountSelected(@NonNull Account account) {

		AccountUtils.SELECTED_ACCOUNT = account;
		refreshNavigationDrawerHeader();

		// Close the drawer

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
			mLeftDrawerLayout.closeDrawer(mLeftDrawerMenu);
		else if (mNavigationDrawerAccountViewSwitcher != null)
			mNavigationDrawerAccountViewSwitcher.setDisplayedChild(0);

		// If we selected a new account, and we have a Dossier list displayed
		// We'll want to pop the BackStack to get on the Bureau list
		// Then , we just update the Bureau with the accurate Account

		MenuFragment menuFragment = (MenuFragment) getFragmentManager().findFragmentByTag(MenuFragment.FRAGMENT_TAG);
		if (menuFragment != null) {
			menuFragment.onBackPressed();
			menuFragment.updateBureaux(true);
		}
	}

	@Override public void onCreateAccountInvoked() {
		Intent preferencesIntent = new Intent(this, PreferencesActivity.class);
		preferencesIntent.putExtra(PreferencesActivity.ARGUMENT_GO_TO_FRAGMENT, PreferencesAccountFragment.class.getSimpleName());
		startActivityForResult(preferencesIntent, PreferencesActivity.PREFERENCES_ACTIVITY_REQUEST_CODE);
	}

	// </editor-fold desc="AccountFragmentListener">

	// <editor-fold desc="MenuFragment">

	@Override public void onDossierListFragmentSelected(@NonNull Dossier dossier, @NonNull String bureauId) {

		if (mLeftDrawerLayout.isDrawerOpen(mLeftDrawerMenu))
			mLeftDrawerLayout.closeDrawer(mLeftDrawerMenu);

		DossierDetailFragment fragment = (DossierDetailFragment) getFragmentManager().findFragmentByTag(DossierDetailFragment.FRAGMENT_TAG);
		if ((fragment != null)) {
			fragment.showProgressLayout();
			fragment.update(dossier, bureauId, null);
		}
	}

	@Override public void onDossierCheckedChanged(boolean forceClose) {

		if (mActionMode != null)
			mActionMode.invalidate();
		else if (!forceClose)
			mActionMode = startSupportActionMode(this);
	}

	// </editor-fold desc="MenuFragment">

	// <editor-fold desc="DossierDetailsFragmentListener">

	@Override public boolean isAnyDrawerOpened() {
		return (mRightDrawerLayout.isDrawerVisible(GravityCompat.END) || mLeftDrawerLayout.isDrawerVisible(GravityCompat.START));
	}

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

	@Override public void onActionButtonClicked(@NonNull Dossier dossier, @NonNull String bureauId, @NonNull Action action) {
		launchActionPopup(CollectionUtils.asSet(dossier), bureauId, action);
	}

	// </editor-fold desc="DossierDetailsFragmentListener">

	/**
	 * Listener used on the Drawer Layout used to control the Action Bar content depending
	 * on the Drawer state.
	 */
	private class DossiersActionBarDrawerToggle extends ActionBarDrawerToggle {

		private DossiersActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout) {
			super(activity, drawerLayout, (Toolbar) activity.findViewById(R.id.menu_toolbar), R.string.drawer_open, R.string.drawer_close);
		}

		@Override public void onDrawerClosed(View view) {

			if (getSupportActionBar() != null) {
				Account selectedAccount = AccountUtils.SELECTED_ACCOUNT;
				if (selectedAccount != null)
					getSupportActionBar().setTitle(selectedAccount.getTitle());
			}

			if (mNavigationDrawerAccountViewSwitcher != null)
				mNavigationDrawerAccountViewSwitcher.setDisplayedChild(0);

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
