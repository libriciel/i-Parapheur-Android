package org.adullact.iparapheur.controller.preferences;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import org.adullact.iparapheur.R;


public class AboutPreferenceFragment extends PreferenceFragment {

	public static final String FRAGMENT_TAG = "AboutPreferenceFragment";

	@Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.preferences_licences_fragment, container, false);

		// Retrieve current version name

		String currentVersion = "1.3";
		try { currentVersion = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName; }
		catch (PackageManager.NameNotFoundException e) {
			Crashlytics.logException(e);
			e.printStackTrace();
		}

		// Set current version in the TextView

		TextView versionTextView = (TextView) v.findViewById(R.id.about_version_number_textview);
		String versionText = String.valueOf(versionTextView.getText());
		versionText = versionText.replaceAll("-version-", currentVersion);
		versionTextView.setText(versionText);

		return v;
	}

}
