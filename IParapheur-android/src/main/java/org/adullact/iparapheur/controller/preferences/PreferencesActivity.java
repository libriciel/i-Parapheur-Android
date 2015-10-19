package org.adullact.iparapheur.controller.preferences;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.model.Account;


public class PreferencesActivity extends AppCompatActivity implements PreferencesMenuFragment.PreferenceMenuFragmentListener, PreferencesAccountFragment.PreferencesAccountFragmentListener {

	public static final int PREFERENCES_ACTIVITY_REQUEST_CODE = 1001;
	public static final String ARGUMENT_GO_TO_FRAGMENT = "go_to_fragment";

	public final String[] availableSubFragmentsTagList = {
			PreferencesAccountFragment.FRAGMENT_TAG,
			PreferencesCertificatesFragment.FRAGMENT_TAG,
			PreferencesAboutFragment.FRAGMENT_TAG,
			PreferencesLicencesFragment.FRAGMENT_TAG
	};

	// <editor-fold desc="LifeCycle">

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// To have a transparent StatusBar, and a background color behind

		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		setContentView(R.layout.preferences_activity);

		// ActionBar

		Toolbar toolbar = (Toolbar) findViewById(R.id.preferences_activity_toolbar);
		setSupportActionBar(toolbar);

		if (getSupportActionBar() != null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Existing Fragment restoration handling, the Google way
		// http://developer.android.com/guide/topics/resources/runtime-changes.html

		Fragment retainedFragment = getRetainedFragmentInstance();
		if (retainedFragment != null) {

			// Clear BackStack before restoring existing fragment (everything over the Menu),
			// because a wrong BackStack can stay after rotation.
			getSupportFragmentManager().popBackStackImmediate(retainedFragment.getTag(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

			// Rebuild stack
			replaceMainFragment(PreferencesMenuFragment.newInstance(), PreferencesMenuFragment.FRAGMENT_TAG, false);
			replaceMainFragment(retainedFragment, retainedFragment.getTag(), true);
		}
		else {
			replaceMainFragment(PreferencesMenuFragment.newInstance(), PreferencesMenuFragment.FRAGMENT_TAG, false);
		}
	}

	@Override protected void onStart() {
		super.onStart();

		// Quick access

		if (getIntent().getStringExtra(ARGUMENT_GO_TO_FRAGMENT) != null) {
			String fragmentName = getIntent().getStringExtra(ARGUMENT_GO_TO_FRAGMENT);

			if (TextUtils.equals(fragmentName, PreferencesAccountFragment.class.getSimpleName()))
				replaceMainFragment(PreferencesAccountFragment.newInstance(), PreferencesAccountFragment.FRAGMENT_TAG, true);

			getIntent().removeExtra(ARGUMENT_GO_TO_FRAGMENT);
		}
	}

	// </editor-fold desc="LifeCycle">

	// <editor-fold desc="ActionBar">

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		onBackPressed();
		return true;
	}

	// </editor-fold desc="ActionBar">

	private @Nullable Fragment getRetainedFragmentInstance() {

		FragmentManager fragmentManager = getSupportFragmentManager();
		for (String fragmentTag : availableSubFragmentsTagList)
			if ((fragmentManager.findFragmentByTag(fragmentTag)) != null)
				return fragmentManager.findFragmentByTag(fragmentTag);

		return null;
	}

	private void replaceMainFragment(@NonNull Fragment fragment, @NonNull String tag, boolean addToBackStack) {

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.preferences_activity_main_layout, fragment, tag);

		if (addToBackStack)
			transaction.addToBackStack(tag);

		transaction.commit();
	}

	// <editor-fold desc="PreferenceMenuFragmentListener">

	@Override public void onMenuElementClicked(@NonNull Fragment fragment, @NonNull String fragmentTag) {
		replaceMainFragment(fragment, fragmentTag, true);
	}

	// </editor-fold desc="PreferenceMenuFragmentListener">

	// <editor-fold desc="PreferencesAccountFragmentListener">

	@Override public void onAccountModified(@NonNull Account account) {
		setResult(Activity.RESULT_OK);
	}

	// </editor-fold desc="PreferencesAccountFragmentListener">
}
