package org.adullact.iparapheur.controller.preferences;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.adullact.iparapheur.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PreferencesCertificatesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreferencesCertificatesFragment extends Fragment {

	public static final String FRAGMENT_TAG = "preferences_certificates_fragment";
	public static final String LOG_TAG = "PrefsCertificatesFrag";

	private static final String LIST_FIELD_TITLE = "list_field_title";
	private static final String LIST_FIELD_EXPIRATION_DATE = "list_field_expiration_date";

	private ListView mCertificatesList;
	private List<Map<String, String>> mCertificatesData;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment PreferencesMenuFragment.
	 */
	public static PreferencesCertificatesFragment newInstance() {
		return new PreferencesCertificatesFragment();
	}

	public PreferencesCertificatesFragment() {
		// Required empty public constructor
	}

	// <editor-fold desc="LifeCycle">

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		mCertificatesData = new ArrayList<>();
		buildCertificatesDataMap();
	}

	@Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.preferences_certificates_fragment, container, false);

		mCertificatesList = (ListView) v.findViewById(R.id.preferences_certificates_fragment_main_list);

		// Building ListAdapter

		String[] orderedFieldNames = new String[]{LIST_FIELD_TITLE, LIST_FIELD_EXPIRATION_DATE};
		int[] orderedFieldIds = new int[]{
				R.id.preferences_certificates_fragment_cell_title_textview, R.id.preferences_certificates_fragment_cell_expiration_textview
		};

		SimpleAdapter certificatesAdapter = new CertificateSimpleAdapter(
				getActivity(), mCertificatesData, R.layout.preferences_certificates_fragment_cell, orderedFieldNames, orderedFieldIds
		);
		mCertificatesList.setAdapter(certificatesAdapter);

		//

		return v;
	}

	@Override public void onResume() {
		super.onResume();

		if (getActivity() instanceof AppCompatActivity) {
			AppCompatActivity parentActivity = (AppCompatActivity) getActivity();
			if (parentActivity.getSupportActionBar() != null)
				parentActivity.getSupportActionBar().setTitle(R.string.pref_header_certificates);
		}
	}

	// </editor-fold desc="LifeCycle">

	private void onDeleteButtonClicked(int position) {

		String currentFile = mCertificatesData.get(position).get(LIST_FIELD_TITLE);
		// TODO Delete and check
		Log.i(LOG_TAG, "Delete certificate " + currentFile);
		mCertificatesData.remove(position);

		((SimpleAdapter) mCertificatesList.getAdapter()).notifyDataSetChanged();
		Toast.makeText(getActivity(), R.string.pref_certificates_message_delete_success, Toast.LENGTH_SHORT).show();
	}

	public void buildCertificatesDataMap() {

		mCertificatesData.clear();

//		List<File> certificatesList = //TODO : list files;
//		for (File certificate : certificatesList) {
		Map<String, String> certificateData = new HashMap<>();
		certificateData.put(LIST_FIELD_TITLE, "Test.bks");
		certificateData.put(LIST_FIELD_EXPIRATION_DATE, "exp. 12/05/60");
		mCertificatesData.add(certificateData);
//		}
	}

	private class CertificateSimpleAdapter extends SimpleAdapter {

		/**
		 * Constructor
		 *
		 * @param context  The context where the View associated with this SimpleAdapter is running
		 * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
		 *                 Maps contain the data for each row, and should include all the entries specified in
		 *                 "from"
		 * @param resource Resource identifier of a view layout that defines the views for this list
		 *                 item. The layout file should include at least those named views defined in "to"
		 * @param from     A list of column names that will be added to the Map associated with each
		 *                 item.
		 * @param to       The views that should display column in the "from" parameter. These should all be
		 *                 TextViews. The first N views in this list are given the values of the first N columns
		 */
		public CertificateSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
		}

		@Override public View getView(final int position, View convertView, ViewGroup parent) {

			// We reset the Tag before recycling the view, with super, then reassign it
			// because we don't want to trigger the EditText TextChangedListeners
			// when the system recycles the views.

			final View v = super.getView(position, convertView, parent);

			// Cell buttons listener

			final ImageButton deleteButton = (ImageButton) v.findViewById(R.id.preferences_certificates_fragment_cell_delete_button);
			deleteButton.setOnClickListener(
					new View.OnClickListener() {
						@Override public void onClick(View arg0) {
							onDeleteButtonClicked(position);
						}
					}
			);

			// Warns expiration date

			// TODO : set expirationDate color to red

			//

			return v;
		}
	}
}
