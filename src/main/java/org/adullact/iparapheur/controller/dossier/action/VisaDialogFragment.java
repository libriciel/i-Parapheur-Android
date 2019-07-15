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
package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.utils.AccountUtils;
import org.adullact.iparapheur.utils.CollectionUtils;
import org.adullact.iparapheur.utils.IParapheurException;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class VisaDialogFragment extends DialogFragment {

    public static final String FRAGMENT_TAG = "visa_dialog_fragment";
    public static final int REQUEST_CODE_VISA = 22091901;    // Because V-I-S-A = 22-09-19-01

    private static final String LOG_TAG = "VisaDialogFragment";
    private static final String ARGUMENTS_DOSSIERS = "dossiers";
    private static final String ARGUMENTS_BUREAU_ID = "bureau_id";

    protected EditText mPublicAnnotationEditText;
    protected EditText mPrivateAnnotationEditText;
    protected TextView mPublicAnnotationLabel;
    protected TextView mPrivateAnnotationLabel;

    private String mBureauId;
    private ArrayList<Dossier> mDossierList;

    public static @NonNull VisaDialogFragment newInstance(@NonNull ArrayList<Dossier> dossiers, @NonNull String bureauId) {

        VisaDialogFragment fragment = new VisaDialogFragment();

        Bundle args = new Bundle();
        Gson gson = CollectionUtils.buildGsonWithDateParser();
        args.putString(ARGUMENTS_DOSSIERS, gson.toJson(dossiers));
        args.putString(ARGUMENTS_BUREAU_ID, bureauId);

        fragment.setArguments(args);

        return fragment;
    }

    // <editor-fold desc="LifeCycle">

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Gson gson = CollectionUtils.buildGsonWithDateParser();
            Type typologyType = new TypeToken<ArrayList<Dossier>>() {}.getType();

            try { mDossierList = gson.fromJson(getArguments().getString(ARGUMENTS_DOSSIERS), typologyType); } catch (JsonSyntaxException e) {
                mDossierList = new ArrayList<>();
            }

            mBureauId = getArguments().getString(ARGUMENTS_BUREAU_ID);
        }
    }

    @Override public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create view

        View view = View.inflate(getActivity(), R.layout.action_dialog_visa, null);

        mPublicAnnotationEditText = (EditText) view.findViewById(R.id.action_visa_public_annotation);
        mPrivateAnnotationEditText = (EditText) view.findViewById(R.id.action_visa_private_annotation);
        mPublicAnnotationLabel = (TextView) view.findViewById(R.id.action_visa_public_annotation_label);
        mPrivateAnnotationLabel = (TextView) view.findViewById(R.id.action_visa_private_annotation_label);

        // Set listeners

        mPublicAnnotationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View v, boolean hasFocus) {
                mPublicAnnotationLabel.setActivated(hasFocus);
            }
        });

        mPrivateAnnotationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View v, boolean hasFocus) {
                mPrivateAnnotationLabel.setActivated(hasFocus);
            }
        });

        // Build Dialog

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Main_Dialog);
        builder.setView(view);
        builder.setPositiveButton(R.string.action_viser, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                // Do nothing here because we override this button in the onStart() to change the close behaviour.
                // However, we still need this because on older versions of Android :
                // unless we pass a handler the button doesn't get instantiated
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                onCancelButtonClicked();
            }
        });

        return builder.create();
    }

    @Override public void onStart() {
        super.onStart();

        // Overriding the AlertDialog.Builder#setPositiveButton
        // To be able to manage a click without dismissing the popup.

        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    onVisaButtonClicked();
                }
            });
        }
    }

    // </editor-fold desc="LifeCycle">

    private void onVisaButtonClicked() {

        new VisaTask().execute();
        // See VisaTask#onPostExecute for popup dismiss.
    }

    private void onCancelButtonClicked() {
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
    }

    private class VisaTask extends AsyncTask<Void, Void, Boolean> {

        private String mAnnotPub;
        private String mAnnotPriv;
        private int mErrorMessage;

        @Override protected void onPreExecute() {
            super.onPreExecute();

            mAnnotPub = mPublicAnnotationEditText.getText().toString();
            mAnnotPriv = mPrivateAnnotationEditText.getText().toString();
            mErrorMessage = -1;
        }

        @Override protected Boolean doInBackground(Void... args) {

            if (isCancelled())
                return false;

            for (Dossier dossier : mDossierList) {

                try {
                    RESTClient.INSTANCE.viser(AccountUtils.SELECTED_ACCOUNT, dossier, mAnnotPub, mAnnotPriv, mBureauId);
                    Log.d(LOG_TAG, "VISA on : " + dossier.getName());
                } catch (IParapheurException e) {
                    Log.e(LOG_TAG, e.getLocalizedMessage());
                    Crashlytics.logException(e);
                    mErrorMessage = (e.getResId() > 0) ? e.getResId() : R.string.visa_error_message_not_sent_to_server;
                }

                if (isCancelled())
                    return false;
            }

            // If no error message, then the signature is successful.
            return (mErrorMessage == -1);
        }

        @Override protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success) {
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
                dismissAllowingStateLoss();
            } else if (getActivity() != null) {
                Toast.makeText(getActivity(), ((mErrorMessage != -1) ? mErrorMessage : R.string.visa_error_message_unknown_error), Toast.LENGTH_SHORT).show();
            }
        }

    }

}
