package org.adullact.iparapheur.controller.dossier;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.connectivity.RESTClient;
import org.adullact.iparapheur.controller.utils.LoadingTask;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;

import java.util.ArrayList;

/**
 * Created by jmaire on 02/12/2013.
 */
public class ActionDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private LoadingTask.DataChangeListener listener;
    private ArrayList<Dossier> dossiers;
    private Action action;
    private String bureauId;

    private TextView annotationPublique;
    private TextView annotationPrivee;

    public ActionDialogFragment(ArrayList<Dossier> dossiers, Action action, String bureauId) {
        this.dossiers = dossiers;
        this.action = action;
        this.bureauId = bureauId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Pass null as the parent view because its going in the dialog layout
        View layout = inflater.inflate(R.layout.action_dialog, null);
        annotationPublique = (TextView) layout.findViewById(R.id.action_dialog_annotation_publique);
        annotationPrivee = (TextView) layout.findViewById(R.id.action_dialog_annotation_privee);

        ListView dossiersView = (ListView) layout.findViewById(R.id.action_dialog_dossiers);
        dossiersView.setAdapter(new ArrayAdapter<Dossier>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, this.dossiers));
        dossiersView.setItemsCanFocus(false);

        builder.setView(layout)
                // Add action buttons
                .setPositiveButton(action.getTitle(), this)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ActionDialogFragment.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof LoadingTask.DataChangeListener))
        {
            throw new IllegalStateException("Activity must implement DataChangeListener.");
        }
        listener = (LoadingTask.DataChangeListener) activity;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        new ValidateActionTask(getActivity(), listener).execute();
    }

    private class ValidateActionTask extends LoadingTask {
        public ValidateActionTask(Activity context, DataChangeListener dataChangeListener) {
            super(context, dataChangeListener);
        }

        @Override
        protected Void doInBackground(String... params) {
            if (isCancelled()) {return null;}
            String annotPub = annotationPublique.getText().toString();
            String annotPriv = annotationPrivee.getText().toString();

            switch (action) {
                case VISA:
                    Log.d("debug", "VISA on " + dossiers);
                    RESTClient.INSTANCE.viser(dossiers,
                            action.name(),
                            annotPub,
                            annotPriv,
                            bureauId);
                    break;

                case SIGNATURE:
                    break;
                case MAILSEC:
                    break;
                case TDT:
                    break;
                case AVIS_COMPLEMENTAIRE:
                    break;
                case TRANSFERT_SIGNATURE:
                    break;
                case ARCHIVAGE:
                    break;
                case REJETER:
                    break;
            }

            return null;
        }
    }
}