package org.adullact.iparapheur.controller.preferences;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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

	@Override public void onResume() {
		super.onResume();

		if (getActivity() instanceof AppCompatActivity) {
			AppCompatActivity parentActivity = (AppCompatActivity) getActivity();
			if (parentActivity.getSupportActionBar() != null)
				parentActivity.getSupportActionBar().setTitle(R.string.preferences_header_about);
		}
	}
}
