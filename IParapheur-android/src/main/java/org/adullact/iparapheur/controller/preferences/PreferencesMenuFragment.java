package org.adullact.iparapheur.controller.preferences;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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
public class PreferencesMenuFragment extends Fragment {

	private PreferenceMenuFragmentListener mListener;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment PreferencesMenuFragment.
	 */
	public static PreferencesMenuFragment newInstance() {
		PreferencesMenuFragment fragment = new PreferencesMenuFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public PreferencesMenuFragment() {
		// Required empty public constructor
	}

	// <editor-fold desc="LifeCycle">

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_preferences_menu, container, false);
		return view;
	}

	@Override public void onAttach(Context context) {
		super.onAttach(context);
		try {
			mListener = (PreferenceMenuFragmentListener) context;
		}
		catch (ClassCastException e) {
			throw new ClassCastException(
					context.toString() + " must implement OnFragmentInteractionListener"
			);
		}
	}

	@Override public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	// </editor-fold desc="LifeCycle">

	public void onMenuElementClicked(Uri uri) {

		if (mListener != null)
			mListener.onMenuElementClicked("" + uri);
	}

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
		void onMenuElementClicked(String element);
	}

}
