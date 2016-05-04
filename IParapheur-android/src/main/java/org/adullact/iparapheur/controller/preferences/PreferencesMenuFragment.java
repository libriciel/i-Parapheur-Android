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
package org.adullact.iparapheur.controller.preferences;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.adullact.iparapheur.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PreferenceMenuFragmentListener} interface
 * to handle interaction events.
 * Use the {@link PreferencesMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreferencesMenuFragment extends Fragment implements View.OnClickListener {

	public static final String FRAGMENT_TAG = "preferences_menu_fragment";
	private PreferenceMenuFragmentListener mListener;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment PreferencesMenuFragment.
	 */
	public static PreferencesMenuFragment newInstance() {
		return new PreferencesMenuFragment();
	}

	public PreferencesMenuFragment() {
		// Required empty public constructor
	}

	// <editor-fold desc="LifeCycle">

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.preferences_menu_fragment, container, false);

		v.findViewById(R.id.preferences_account).setOnClickListener(this);
		v.findViewById(R.id.preferences_certificates).setOnClickListener(this);
		v.findViewById(R.id.preferences_about).setOnClickListener(this);
		v.findViewById(R.id.preferences_licenses).setOnClickListener(this);

		return v;
	}

	@Override public void onAttach(Context context) {
		super.onAttach(context);

		try { mListener = (PreferenceMenuFragmentListener) context; }
		catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement OnFragmentInteractionListener");
		}
	}

	@Override public void onResume() {
		super.onResume();

		if (getActivity() instanceof AppCompatActivity) {
			AppCompatActivity parentActivity = (AppCompatActivity) getActivity();
			if (parentActivity.getSupportActionBar() != null)
				parentActivity.getSupportActionBar().setTitle(R.string.settings);
		}
	}

	@Override public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	// </editor-fold desc="LifeCycle">

	// <editor-fold desc="OnClickListener">

	@Override public void onClick(View v) {

		if (mListener == null)
			return;

		// Determine which Fragment was clicked

		Fragment clickedFragment = null;

		switch (v.getId()) {
			case R.id.preferences_account:
				clickedFragment = PreferencesAccountFragment.newInstance();
				break;
			case R.id.preferences_certificates:
				clickedFragment = PreferencesCertificatesFragment.newInstance();
				break;
			case R.id.preferences_about:
				clickedFragment = PreferencesAboutFragment.newInstance();
				break;
			case R.id.preferences_licenses:
				clickedFragment = PreferencesLicencesFragment.newInstance();
				break;
		}

		// throw exception to parent activity

		if (clickedFragment != null)
			mListener.onMenuElementClicked(clickedFragment);
	}

	// </editor-fold desc="OnClickListener">

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface PreferenceMenuFragmentListener {

		void onMenuElementClicked(@NonNull Fragment fragment);
	}

}
