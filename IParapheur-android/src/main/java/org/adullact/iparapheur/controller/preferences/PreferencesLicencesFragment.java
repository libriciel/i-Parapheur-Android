package org.adullact.iparapheur.controller.preferences;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.adullact.iparapheur.R;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PreferencesLicencesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreferencesLicencesFragment extends Fragment {

	public static final String FRAGMENT_TAG = "preferences_licences_fragment";

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment PreferencesMenuFragment.
	 */
	public static PreferencesLicencesFragment newInstance() {
		return new PreferencesLicencesFragment();
	}

	public PreferencesLicencesFragment() {
		// Required empty public constructor
	}

	// <editor-fold desc="LifeCycle">

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.preferences_licences_fragment, container, false);
	}

	@Override public void onResume() {
		super.onResume();

		if (getActivity() instanceof AppCompatActivity) {
			AppCompatActivity parentActivity = (AppCompatActivity) getActivity();
			if (parentActivity.getSupportActionBar() != null)
				parentActivity.getSupportActionBar().setTitle(R.string.pref_header_licenses);
		}
	}

	// </editor-fold desc="LifeCycle">

}