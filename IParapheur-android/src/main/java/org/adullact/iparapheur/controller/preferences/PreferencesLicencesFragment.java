package org.adullact.iparapheur.controller.preferences;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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

	public static final String FRAGMENT_TAG = "PreferencesLicencesFragment";

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

	@Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.preferences_licences_fragment, container, false);
	}

	// </editor-fold desc="LifeCycle">

}
