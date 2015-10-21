package org.adullact.iparapheur.controller.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.utils.FileUtils;
import org.adullact.iparapheur.utils.PKCS7Signer;
import org.adullact.iparapheur.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Date;
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
	private static final String LIST_FIELD_IS_EXPIRED = "list_field_is_expired";
	private static final String LIST_FIELD_EXPIRATION_DATE_STRING = "list_field_expiration_date_string";

	private ListView mCertificatesList;
	private List<Map<String, Object>> mCertificatesData;

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

		String[] orderedFieldNames = new String[]{LIST_FIELD_TITLE, LIST_FIELD_EXPIRATION_DATE_STRING};
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

		// Delete certificate file

		String currentFileName = mCertificatesData.get(position).get(LIST_FIELD_TITLE).toString();
		List<File> certificateList = FileUtils.getBksFromCertificateFolder(getActivity());

		boolean success = false;
		for (File certificate : certificateList)
			if (TextUtils.equals(certificate.getName(), currentFileName))
				success = success || certificate.delete();

		// Refresh UI

		if (success) {
			Log.i(LOG_TAG, "Delete certificate " + currentFileName);
			mCertificatesData.remove(position);
			((SimpleAdapter) mCertificatesList.getAdapter()).notifyDataSetChanged();
			Toast.makeText(getActivity(), R.string.pref_certificates_message_delete_success, Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(getActivity(), R.string.pref_certificates_message_delete_failed, Toast.LENGTH_SHORT).show();
		}
	}

	public void buildCertificatesDataMap() {

		mCertificatesData.clear();

		List<File> certificatesList = FileUtils.getBksFromCertificateFolder(getActivity());
		for (File certificate : certificatesList) {

			// Retrieving Certificate expiration date.
			// And computing every other operation, better here than at runtime in the Adapter.

			SharedPreferences settings = getActivity().getSharedPreferences(FileUtils.SHARED_PREFERENCES_CERTIFICATES_PASSWORDS, 0);
			String certificatePassword = settings.getString(certificate.getName(), "");
			PKCS7Signer signer = new PKCS7Signer(certificate.getAbsolutePath(), certificatePassword, "", "");

			Date certificateExpirationDate = null;
			try {
				signer.loadKeyStore();
				certificateExpirationDate = signer.getCertificateExpirationDate();
			}
			catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException e) {
				Crashlytics.logException(e);
				e.printStackTrace();
			}

			String expirationDateString = getString(R.string.pref_certificates_expiration_date);
			expirationDateString = expirationDateString.replaceAll("-date-", StringUtils.getLocalizedSmallDate(certificateExpirationDate));
			Boolean isExpired = (certificateExpirationDate == null) || (certificateExpirationDate.before(new Date()));

			// Mapping results

			Map<String, Object> certificateData = new HashMap<>();
			certificateData.put(LIST_FIELD_TITLE, certificate.getName());
			certificateData.put(LIST_FIELD_EXPIRATION_DATE_STRING, expirationDateString);
			certificateData.put(LIST_FIELD_IS_EXPIRED, isExpired);
			mCertificatesData.add(certificateData);
		}
	}

	private class CertificateSimpleAdapter extends SimpleAdapter {

		private int mRegularColor;
		private int mErrorColor;

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
			mErrorColor = ContextCompat.getColor(context, R.color.red_500);
			mRegularColor = ContextCompat.getColor(context, R.color.text_black_secondary);
		}

		@Override public View getView(final int position, View convertView, ViewGroup parent) {

			// We reset the Tag before recycling the view, with super, then reassign it
			// because we don't want to trigger the EditText TextChangedListeners
			// when the system recycles the views.

			final View v = super.getView(position, convertView, parent);

			final ImageButton deleteButton = (ImageButton) v.findViewById(R.id.preferences_certificates_fragment_cell_delete_imagebutton);
			final TextView expirationTextView = (TextView) v.findViewById(R.id.preferences_certificates_fragment_cell_expiration_textview);

			// Cell buttons listener

			deleteButton.setOnClickListener(
					new View.OnClickListener() {
						@Override public void onClick(View arg0) {
							onDeleteButtonClicked(position);
						}
					}
			);

			// Warns expiration date

			Boolean isExpired = (Boolean) mCertificatesData.get(position).get(LIST_FIELD_IS_EXPIRED);
			expirationTextView.setTextColor(isExpired ? mErrorColor : mRegularColor);

			//

			return v;
		}
	}
}
