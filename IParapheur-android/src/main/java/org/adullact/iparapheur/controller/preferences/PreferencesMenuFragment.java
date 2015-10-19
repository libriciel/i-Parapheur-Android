package org.adullact.iparapheur.controller.preferences;

import android.content.Context;
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
			throw new ClassCastException(
					context.toString() + " must implement OnFragmentInteractionListener"
			);
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
		String clickedFragmentTag = null;

		switch (v.getId()) {
			case R.id.preferences_account:
				clickedFragment = PreferencesAccountFragment.newInstance();
				clickedFragmentTag = PreferencesAccountFragment.FRAGMENT_TAG;
				break;
			case R.id.preferences_certificates:
				clickedFragment = PreferencesCertificatesFragment.newInstance();
				clickedFragmentTag = PreferencesCertificatesFragment.FRAGMENT_TAG;
				break;
			case R.id.preferences_about:
				clickedFragment = PreferencesAboutFragment.newInstance();
				clickedFragmentTag = PreferencesAboutFragment.FRAGMENT_TAG;
				break;
			case R.id.preferences_licenses:
				clickedFragment = PreferencesLicencesFragment.newInstance();
				clickedFragmentTag = PreferencesLicencesFragment.FRAGMENT_TAG;
				break;
		}

		// throw exception to parent activity

		if (clickedFragment != null)
			mListener.onMenuElementClicked(clickedFragment, clickedFragmentTag);
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

		// TODO: Update argument type and name
		void onMenuElementClicked(@NonNull Fragment fragment, @NonNull String tag);
	}

}
