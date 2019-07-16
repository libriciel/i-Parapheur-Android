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
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.utils.FileUtils;
import org.adullact.iparapheur.utils.PKCS7Signer;
import org.adullact.iparapheur.utils.StringsUtils;

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
    private View mEmptyView;

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

        mCertificatesList = v.findViewById(R.id.preferences_certificates_fragment_main_list);
        mEmptyView = v.findViewById(R.id.preferences_certificates_fragment_empty_view_linearlayout);
        Button certificateTutoButton = v.findViewById(R.id.preferences_certificates_fragment_certificate_tuto_button);

        // Buttons listeners

        certificateTutoButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                FileUtils.launchCertificateTutoPdfIntent(getActivity());
            }
        });

        // Building ListAdapter

        String[] orderedFieldNames = new String[]{LIST_FIELD_TITLE, LIST_FIELD_EXPIRATION_DATE_STRING};
        int[] orderedFieldIds = new int[]{
                R.id.preferences_certificates_fragment_cell_title_textview, R.id.preferences_certificates_fragment_cell_expiration_textview
        };

        SimpleAdapter certificatesAdapter = new CertificateSimpleAdapter(getActivity(),
                mCertificatesData,
                R.layout.preferences_certificates_fragment_cell,
                orderedFieldNames,
                orderedFieldIds
        );

        mCertificatesList.setAdapter(certificatesAdapter);

        //

        checkEmptyViewVisibility();

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
            checkEmptyViewVisibility();

            ((SimpleAdapter) mCertificatesList.getAdapter()).notifyDataSetChanged();
            Toast.makeText(getActivity(), R.string.pref_certificates_message_delete_success, Toast.LENGTH_SHORT).show();
        } else {
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

            Date certifExpDate = null;
            try {
                signer.loadKeyStore();
                certifExpDate = signer.getCertificateExpirationDate();
            } catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException e) {
                Crashlytics.logException(e);
                Log.e(LOG_TAG, e.getLocalizedMessage());
            }

            String expString = String.format(getString(R.string.pref_certificates_expiration_date), StringsUtils.getLocalizedSmallDate(certifExpDate));
            Boolean isExpired = (certifExpDate == null) || (certifExpDate.before(new Date()));

            // Mapping results

            Map<String, Object> certificateData = new HashMap<>();
            certificateData.put(LIST_FIELD_TITLE, certificate.getName());
            certificateData.put(LIST_FIELD_EXPIRATION_DATE_STRING, expString);
            certificateData.put(LIST_FIELD_IS_EXPIRED, isExpired);
            mCertificatesData.add(certificateData);
        }
    }

    private void checkEmptyViewVisibility() {
        mCertificatesList.setVisibility(mCertificatesData.isEmpty() ? View.GONE : View.VISIBLE);
        mEmptyView.setVisibility(mCertificatesData.isEmpty() ? View.VISIBLE : View.GONE);
    }

    //

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
        CertificateSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            mErrorColor = ContextCompat.getColor(context, R.color.red_500);
            mRegularColor = ContextCompat.getColor(context, R.color.text_black_secondary);
        }

        @Override public View getView(final int position, View convertView, ViewGroup parent) {

            // We reset the Tag before recycling the view, with super, then reassign it
            // because we don't want to trigger the EditText TextChangedListeners
            // when the system recycles the views.

            final View v = super.getView(position, convertView, parent);

            final ImageButton deleteButton = v.findViewById(R.id.preferences_certificates_fragment_cell_delete_imagebutton);
            final TextView expirationTextView = v.findViewById(R.id.preferences_certificates_fragment_cell_expiration_textview);

            // Cell buttons listener

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View arg0) {
                    onDeleteButtonClicked(position);
                }
            });

            // Warns expiration date

            Boolean isExpired = (Boolean) mCertificatesData.get(position).get(LIST_FIELD_IS_EXPIRED);
            expirationTextView.setTextColor(isExpired ? mErrorColor : mRegularColor);

            //

            return v;
        }

    }

}
