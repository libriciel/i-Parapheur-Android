package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.utils.LoadingWithProgressTask;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.utils.IParapheurException;

import java.util.ArrayList;

/**
 * Created by jmaire on 02/12/2013.
 */
public class VisaDialogFragment extends ActionDialogFragment implements DialogInterface.OnClickListener {

    protected TextView annotationPublique;
    protected TextView annotationPrivee;

    public VisaDialogFragment() {}

    public static VisaDialogFragment newInstance(ArrayList<Dossier> dossiers, String bureauId) {
        VisaDialogFragment f = new VisaDialogFragment();

        // Supply parameters as an arguments.
        Bundle args = new Bundle();
        args.putParcelableArrayList("dossiers", dossiers);
        args.putString("bureauId", bureauId);
        f.setArguments(args);

        return f;
    }

    @Override
    protected View createView() {
        View layout = super.createView();

        annotationPublique = (TextView) layout.findViewById(R.id.action_import_password);
        annotationPrivee = (TextView) layout.findViewById(R.id.action_dialog_private_annotation);

        return layout;
    }

    @Override
    protected int getTitle() {
        return Action.VISA.getTitle();
    }

    @Override
    protected int getViewId() {
        return R.layout.action_dialog;
    }

    @Override
    protected void executeTask() {
        new VisaTask(getActivity()).execute();
    }


    private class VisaTask extends LoadingWithProgressTask {

        public VisaTask(Activity context) {
            super(context, listener);

        }

        @Override
        protected void load(String... params) throws IParapheurException {
            if (isCancelled()) {return;}
            String annotPub = annotationPublique.getText().toString();
            String annotPriv = annotationPrivee.getText().toString();
            int i = 0;
            int total = dossiers.size();
            publishProgress(i);

            for (Dossier dossier : dossiers) {
                if (isCancelled()) {return;}
                //Log.d("debug", "VISA on " + dossier);
                RESTClient.INSTANCE.viser(dossier,
                        annotPub,
                        annotPriv,
                        bureauId);
                i++;
                publishProgress(i * 100 / total);
            }
        }
    }
}