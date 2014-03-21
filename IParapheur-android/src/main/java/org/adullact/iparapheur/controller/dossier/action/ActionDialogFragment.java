package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.utils.LoadingTask;
import org.adullact.iparapheur.model.Dossier;

import java.util.ArrayList;

/**
 * Created by jmaire on 03/12/2013.
 */
public abstract class ActionDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    protected LoadingTask.DataChangeListener listener;
    protected ArrayList<Dossier> dossiers;
    protected String bureauId;

    public ActionDialogFragment() {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            this.dossiers = getArguments().getParcelableArrayList("dossiers");
            this.bureauId = getArguments().getString("bureauId");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(createView())
                // Set action button
                .setPositiveButton(getTitle(), this)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ActionDialogFragment.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

    protected View createView() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Pass null as the parent view because its going in the dialog layout
        View layout = inflater.inflate(getViewId(), null);

        ListView dossiersView = (ListView) layout.findViewById(R.id.action_dialog_dossiers);
        dossiersView.setAdapter(new ArrayAdapter<Dossier>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, this.dossiers));
        dossiersView.setItemsCanFocus(false);
        return layout;
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
        executeTask();
    }

    /**
     * return the title of the action of the dialog. Used to edit positive button text.
     * @return the title of the action done in this dialog.
     */
    protected abstract int getTitle();

    /**
     * return the id of the view inflated in this dialog.
     * @return the id of the view used in this dialog.
     */
    protected abstract int getViewId();

    /**
     * Called when the positive button is pressed.
     */
    protected abstract void executeTask();
}