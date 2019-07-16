/*
 * iParapheur Android
 * Copyright (C) 2016-2019 Libriciel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.adullact.iparapheur.controller.preferences;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import org.adullact.iparapheur.R;

import java.util.HashMap;
import java.util.Map;


public class PreferencesActivity extends AppCompatActivity implements PreferencesMenuFragment.PreferenceMenuFragmentListener {

    public static final int PREFERENCES_ACTIVITY_REQUEST_CODE = 1001;
    public static final String ARGUMENT_GO_TO_FRAGMENT = "go_to_fragment";

    private static Map<Class<? extends Fragment>, String> fragmentsTagsMap;

    static {
        fragmentsTagsMap = new HashMap<>();
        fragmentsTagsMap.put(PreferencesAccountFragment.class, PreferencesAccountFragment.FRAGMENT_TAG);
        fragmentsTagsMap.put(PreferencesCertificatesFragment.class, PreferencesCertificatesFragment.FRAGMENT_TAG);
        fragmentsTagsMap.put(PreferencesAboutFragment.class, PreferencesAboutFragment.FRAGMENT_TAG);
        fragmentsTagsMap.put(PreferencesLicencesFragment.class, PreferencesLicencesFragment.FRAGMENT_TAG);
    }

    // <editor-fold desc="LifeCycle">

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // To have a transparent StatusBar, and a background color behind

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.preferences_activity);

        // ActionBar

        Toolbar toolbar = findViewById(R.id.preferences_activity_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Existing Fragment restoration handling, the Google way
        // http://developer.android.com/guide/topics/resources/runtime-changes.html

        Fragment retainedFragment = getRetainedFragmentInstance();
        if (retainedFragment != null) {

            // Clear BackStack before restoring existing fragment (everything over the Menu),
            // because a wrong BackStack can stay after rotation.
            String retainedFragmentTag = fragmentsTagsMap.get(retainedFragment.getClass());
            getFragmentManager().popBackStackImmediate(retainedFragmentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            // Rebuild stack
            replaceMainFragment(PreferencesMenuFragment.newInstance(), PreferencesMenuFragment.FRAGMENT_TAG, false);
            replaceMainFragment(retainedFragment, retainedFragmentTag, true);
        } else {
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

        FragmentManager fragmentManager = getFragmentManager();
        for (Map.Entry<Class<? extends Fragment>, String> fragmentTagEntry : fragmentsTagsMap.entrySet())
            if ((fragmentManager.findFragmentByTag(fragmentTagEntry.getValue())) != null)
                return fragmentManager.findFragmentByTag(fragmentTagEntry.getValue());

        return null;
    }

    private void replaceMainFragment(@NonNull Fragment fragment, @NonNull String tag, boolean addToBackStack) {

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.preferences_activity_main_layout, fragment, tag);

        if (addToBackStack)
            transaction.addToBackStack(tag);

        transaction.commit();
    }

    // <editor-fold desc="PreferenceMenuFragmentListener">

    @Override public void onMenuElementClicked(@NonNull Fragment fragment) {
        String fragmentTag = fragmentsTagsMap.get(fragment.getClass());
        replaceMainFragment(fragment, fragmentTag, true);
    }

    // </editor-fold desc="PreferenceMenuFragmentListener">
}
