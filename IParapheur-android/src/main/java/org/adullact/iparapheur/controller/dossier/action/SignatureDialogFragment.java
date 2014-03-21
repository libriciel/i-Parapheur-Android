package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.os.Bundle;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.connectivity.RESTClient;
import org.adullact.iparapheur.controller.utils.LoadingWithProgressTask;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;

import java.util.ArrayList;

/**
 * Created by jmaire on 03/12/2013.
 */
public class SignatureDialogFragment extends ActionDialogFragment implements View.OnClickListener {

    protected TextView annotationPublique;
    protected TextView annotationPrivee;
    private TextView certInfo;
    private String selectedCertAlias;

    public SignatureDialogFragment() {}

    public static SignatureDialogFragment newInstance(ArrayList<Dossier> dossiers, String bureauId) {
        SignatureDialogFragment f = new SignatureDialogFragment();

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

        annotationPublique = (TextView) layout.findViewById(R.id.action_dialog_annotation_publique);
        annotationPrivee = (TextView) layout.findViewById(R.id.action_dialog_annotation_privee);

        layout.findViewById(R.id.action_dialog_signature_certificate_button).setOnClickListener(this);
        certInfo = (TextView) layout.findViewById(R.id.action_dialog_signature_cert_info);

        return layout;
    }

    @Override
    protected int getTitle() {
        return Action.SIGNATURE.getTitle();
    }

    @Override
    protected int getViewId() {
        return R.layout.action_dialog_signature;
    }

    @Override
    protected void executeTask() {
        new SignTask(getActivity()).execute();
    }

    // Cert chooser button click
    @Override
    public void onClick(View v) {
        KeyChain.choosePrivateKeyAlias(getActivity(),
                new KeyChainAliasCallback() {
                    public void alias(final String alias) {
                        selectedCertAlias = alias;
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                certInfo.setText(alias);
                            }
                        });
                    }
                },
                // FIXME :  only RSA?
                new String[]{"RSA"}, // List of acceptable key types. null for any
                null,                // issuer, null for any
                null,                // host name of server requesting the cert, null if unavailable
                -1,                  // port of server requesting the cert, -1 if unavailable
                null);               // alias to preselect, null if unavailable
    }


    private class SignTask extends LoadingWithProgressTask {

        public SignTask(Activity activity) {
            super(activity, listener);
        }

        @Override
        protected void load(String... params) {
            if (isCancelled()) {return;}
            String annotPub = annotationPublique.getText().toString();
            String annotPriv = annotationPrivee.getText().toString();
            int i = 0;
            int total = dossiers.size();
            publishProgress(i);
            for (Dossier dossier : dossiers) {
                if (isCancelled()) {return;}
                String signValue = "";
                // TODO sign and update signValue
                //Log.d("debug", "Signature sur " + dossier.getName() + "avec le certificat " + selectedCertAlias);
                RESTClient.INSTANCE.signer(dossier.getId(),
                        signValue,
                        annotPub,
                        annotPriv,
                        bureauId);
                i++;
                publishProgress(i * 100 / total);
            }
        }
    }
}
