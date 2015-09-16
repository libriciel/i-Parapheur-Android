package org.adullact.iparapheur.controller.preferences;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

	// <editor-fold desc="PreferenceMenuFragmentListener">

	@Override public void onMenuElementClicked(String element) {
		Log.i("Adrien", "Click");
	}

	// </editor-fold desc="PreferenceMenuFragmentListener">

}
