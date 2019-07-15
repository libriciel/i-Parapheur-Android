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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import org.adullact.iparapheur.R;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PreferencesAboutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreferencesAboutFragment extends Fragment {

    private static final String LOG_TAG = "PreferencesAboutFragment";
    public static final String FRAGMENT_TAG = "preferences_about_fragment";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PreferencesMenuFragment.
     */
    public static PreferencesAboutFragment newInstance() {
        return new PreferencesAboutFragment();
    }

    public PreferencesAboutFragment() {
        // Required empty public constructor
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.preferences_about_fragment, container, false);

        // Retrieve current version name

        String currentVersion = "1.4";
        try {
            currentVersion = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Crashlytics.logException(e);
            Log.e(LOG_TAG, e.getLocalizedMessage());
        }

        // Set current version in the TextView

        TextView versionTextView = v.findViewById(R.id.about_version_number_textview);
        String versionText = getString(R.string.about_version_number, currentVersion);
        versionTextView.setText(versionText);

        return v;
    }

    @Override public void onResume() {
        super.onResume();

        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity parentActivity = (AppCompatActivity) getActivity();
            if (parentActivity.getSupportActionBar() != null)
                parentActivity.getSupportActionBar().setTitle(R.string.preferences_header_about);
        }
    }

}
