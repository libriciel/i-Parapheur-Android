package org.adullact.iparapheur.controller.dossier.filter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.model.Filter;

import java.util.ArrayList;

public class FilterDialog extends DialogFragment implements DialogInterface.OnClickListener {

    public interface FilterDialogListener {

        void onFilterSave(Filter filter);

        void onFilterChange(Filter filter);

        void onFilterCancel();
    }

    public static final String TAG = "filter_dialog";

    private FilterDialogListener listener;
    private Filter filter;
    private Filter originalFilter;

    private EditText titleText;
    private Spinner etatSpinner;
    private FilterEtatSpinnerAdapter etatSpinnerAdapter;
    private ExpandableListView typologieList;
    private TypologieListAdapter typologieListAdapter;

    public FilterDialog() {}

    public static FilterDialog newInstance(Filter filter) {
        FilterDialog f = new FilterDialog();

        // Supply parameters as an arguments.
        Bundle args = new Bundle();
        args.putParcelable("filter", filter);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof FilterDialogListener))
        {
            throw new IllegalStateException("Activity must implement FilterDialogListener.");
        }
        listener = (FilterDialogListener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.originalFilter = getArguments().getParcelable("filter");
        this.filter = new Filter(originalFilter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // TITLE
        builder.setTitle(filter.getNom());

        // CONTENT
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View content = inflater.inflate(R.layout.filter_dialog, null);

        // Titre du dossier
        this.titleText = (EditText) content.findViewById(R.id.filter_dialog_titre);
        this.titleText.setText(this.originalFilter.getTitre());

        // Etat du dossier
        this.etatSpinner = (Spinner) content.findViewById(R.id.filter_dialog_etat_spinner);
        etatSpinnerAdapter = new FilterEtatSpinnerAdapter(getActivity());
        this.etatSpinner.setAdapter(etatSpinnerAdapter);
        this.etatSpinner.setSelection(Filter.etats.indexOf(this.originalFilter.getEtat()), false);

        // Typologie
        this.typologieList = (ExpandableListView) content.findViewById(R.id.filter_dialog_typology_list);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(content);

        // Filrer
        builder.setPositiveButton(R.string.action_filtrer, this);
        // Enregistrer
        builder.setNeutralButton(R.string.enregistrer_filtre, this);
        // Annuler
        builder.setNegativeButton(R.string.cancel, this);

        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.typologieListAdapter = new TypologieListAdapter(getActivity(), originalFilter);
        this.typologieList.setAdapter(this.typologieListAdapter);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch(which) {
            case DialogInterface.BUTTON_NEGATIVE :
                listener.onFilterCancel();
                dismiss();
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                updateFilter();
                createTitleDialog();
                break;
            case DialogInterface.BUTTON_POSITIVE:
                updateFilter();
                if (originalFilter.getId().equals(Filter.DEFAULT_ID)) {
                    filter.setId(Filter.DEFAULT_ID);
                }
                listener.onFilterChange(filter);
                break;
        }
    }

    private void createTitleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // TITLE
        builder.setTitle(R.string.filtre_nom);

        // CONTENT
        final EditText content = new EditText(getActivity());
        content.setHint(R.string.filtre_nom_hint);
        if (!originalFilter.getId().equals(Filter.DEFAULT_ID)) {
            content.setText(filter.getNom());
        }
        builder.setView(content);

        // Enregistrer
        builder.setNeutralButton(R.string.enregistrer_filtre, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (content.getText().toString().trim().isEmpty()) {
                    filter.setNom(content.getHint().toString());
                } else {
                    filter.setNom(content.getText().toString().trim());
                }
                listener.onFilterSave(filter);
            }
        });

        builder.create().show();
    }

    private void updateFilter() {
        this.filter.setTitre(this.titleText.getText().toString());
        this.filter.setEtat(Filter.etats.get(this.etatSpinner.getSelectedItemPosition()));

        this.filter.setTypes(this.typologieListAdapter.getSelectedTypes());
        this.filter.setSousTypes(this.typologieListAdapter.getSelectedSousTypes());
    }

    private class FilterEtatSpinnerAdapter extends ArrayAdapter<String> {

        public FilterEtatSpinnerAdapter(Context context) {
            super(context, R.layout.filter_etat_spinner, R.id.filter_etat_spinner_text);
        }

        @Override
        public int getCount() {
            return Filter.etats.size();
        }

        @Override
        public String getItem(int position) {
            return Filter.etatsTitres.get(Filter.etats.get(position));
        }

        @Override
        public int getPosition(String item) {
            ArrayList<String> values = new ArrayList<String>(Filter.etatsTitres.values());
            return values.indexOf(item);
        }
    }
}
