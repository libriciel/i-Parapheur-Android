package org.adullact.iparapheur.controller.preferences;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import org.adullact.iparapheur.R;


public class PreferencesActivity extends AppCompatActivity implements PreferencesMenuFragment.PreferenceMenuFragmentListener {

	// <editor-fold desc="LifeCycle">

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// To have a transparent StatusBar, and a background color behind

		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

		setContentView(R.layout.preferences_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.preferences_activity_toolbar);
		setSupportActionBar(toolbar);

		if (getSupportActionBar() != null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	// </editor-fold desc="LifeCycle">

	// <editor-fold desc="ActionBar">

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		onBackPressed();
		return true;
	}

	// </editor-fold desc="ActionBar">

	private void replaceMainFragment(@NonNull Fragment fragment, @NonNull String tag) {

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.preferences_activity_content, fragment, tag);
		transaction.addToBackStack(tag);
		transaction.commit();
	}

	// <editor-fold desc="PreferenceMenuFragmentListener">

	@Override public void onMenuElementClicked(@NonNull Fragment fragment, @NonNull String fragmentTag) {
		replaceMainFragment(fragment, fragmentTag);
	}

	// </editor-fold desc="PreferenceMenuFragmentListener">

}
