package org.adullact.iparapheur.controller.dossier;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.adullact.iparapheur.R;

import java.util.ArrayList;

/**
 * Created by jmaire on 16/11/2013.
 */
public class DossierBatchFragment extends Fragment {

    public static final String DOSSIER = "dossier";
    private ArrayList<String> dossiers;
    private ListView listView;

    public DossierBatchFragment() {
        this.dossiers = new ArrayList<String>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dossiers_batch_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (getArguments().containsKey(DOSSIER)) {
            this.dossiers.add(getArguments().getString(DOSSIER));
        }
        listView = (ListView) view.findViewById(R.id.dossiers_list);
        listView.setItemsCanFocus(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, this.dossiers));
    }

    public void addDossier(String dossier) {
        dossiers.add(dossier);
        updateView();
    }

    public void removeDossier(String dossier) {
        dossiers.remove(dossier);
        updateView();
    }

    private void updateView() {
        ((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
    }
}
