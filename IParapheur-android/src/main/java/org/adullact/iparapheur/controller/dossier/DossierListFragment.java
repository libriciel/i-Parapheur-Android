package org.adullact.iparapheur.controller.dossier;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.connectivity.RESTClient;
import org.adullact.iparapheur.controller.dossier.action.ArchivageDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.MailSecDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.RejetDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.SignatureDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.TdtDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.VisaDialogFragment;
import org.adullact.iparapheur.controller.utils.LoadingTask;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * A list fragment representing a list of Dossiers. This fragment
 * supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link DossierDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link org.adullact.iparapheur.controller.dossier.DossierListFragment.DossierSelectedListener}
 * interface.
 *
 * This fragment is also used to retain all the dossiers informations.
 * The detail fragment uses the dossiers of this fragment, so the already downloaded
 * details of a dossier are retained in this fragment. Also the pdf is saved on external storage
 * and its url is stored in the dossier information.
 */
public class DossierListFragment extends ListFragment implements LoadingTask.DataChangeListener {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private DossierSelectedListener listener;

    /**
     * Bureau id where the dossiers belongs
     */
    private String bureauId;
    /**
     * List of dossiers displayed in this fragment
     */
    private ArrayList<Dossier> dossiers;

    /**
     * the currently selected dossier
     */
    private int selectedDossier = ListView.INVALID_POSITION;


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface DossierSelectedListener {
        /**
         * Callback for when a dossier has been selected.
         */
        void onDossierSelected(String id);
        void onDossierChecked(String id);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DossierListFragment() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof DossierSelectedListener))
        {
            throw new IllegalStateException("Activity must implement DossierSelectedListener.");
        }
        listener = (DossierSelectedListener) activity;
    }

    // Called only once as retainInstance is set to true.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new DossierListAdapter(getActivity(), listener));
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getView().setBackgroundColor(getResources().getColor(android.R.color.background_light));
        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Reset the active callbacks interface .
        listener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedDossier != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, selectedDossier);
        }
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.dossiers_list_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu (Menu menu) {
        menu.setGroupVisible(R.id.dossiers_menu_main_actions, false);
        menu.setGroupVisible(R.id.dossiers_menu_other_actions, false);

        HashSet<Action> actions = new HashSet<Action>();
        if (((DossierListAdapter) getListAdapter()).getCheckedDossiers().size() > 0) {
            actions.addAll(Arrays.asList(Action.values()));
        }
        for (Dossier dossier : ((DossierListAdapter) getListAdapter()).getCheckedDossiers()) {
            actions.retainAll(dossier.getActions());
        }

        for (Action action : actions) {
            MenuItem item = menu.findItem(action.getMenuItemId());
            item.setTitle(action.getTitle());
            item.setVisible(true);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DialogFragment actionDialog;
        HashSet<Dossier> checkedDossiers = ((DossierListAdapter) getListAdapter()).getCheckedDossiers();
        switch (item.getItemId()) {
            case R.id.action_visa:
                actionDialog = new VisaDialogFragment(new ArrayList<Dossier>(checkedDossiers), bureauId);
                actionDialog.show(getFragmentManager(), "VisaDialogFragment");
                return true;
            case R.id.action_signature:
                actionDialog = new SignatureDialogFragment(new ArrayList<Dossier>(checkedDossiers), bureauId);
                actionDialog.show(getFragmentManager(), "SignatureDialogFragment");
                return true;
            case R.id.action_mailsec:
                actionDialog = new MailSecDialogFragment(new ArrayList<Dossier>(checkedDossiers), bureauId);
                actionDialog.show(getFragmentManager(), "MailSecDialogFragment");
                return true;
            case R.id.action_tdt:
                actionDialog = new TdtDialogFragment(new ArrayList<Dossier>(checkedDossiers), bureauId);
                actionDialog.show(getFragmentManager(), "TdtDialogFragment");
                return true;
            case R.id.action_archivage:
                actionDialog = new ArchivageDialogFragment(new ArrayList<Dossier>(checkedDossiers), bureauId);
                actionDialog.show(getFragmentManager(), "ArchivageDialogFragment");
                return true;
            case R.id.action_rejet:
                actionDialog = new RejetDialogFragment(new ArrayList<Dossier>(checkedDossiers), bureauId);
                actionDialog.show(getFragmentManager(), "RejetDialogFragment");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setBureauId(String bureauId) {
        this.bureauId = bureauId;
        if (bureauId == null) {
            this.dossiers = null;
        }
        getDossiers(true);
    }

    private void getDossiers(boolean forceReload) {
        if (bureauId == null) {
            onDataChanged();
        }
        else if ((dossiers == null) || forceReload) {
            new DossiersLoadingTask(getActivity(), this).execute(bureauId);
        }
    }

    /**
     * Used by the containing activity to pass a dossier to the detail fragment.
     * We get the dossier from here because this fragment has its instance state retained
     * (the fragment isn't destroyed, so all the dossiers information are kept in memory).
     * @param id the id of the dossier.
     * @return the dossier with the id equal to the id passed in parameter.
     */
    public Dossier getDossier(String id) {
        int position = dossiers.indexOf(new Dossier(id));
        return dossiers.get(position);
    }


    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(selectedDossier, false);
        } else {
            getListView().setItemChecked(position, true);
        }
        selectedDossier = position;
    }

    /**
     * called by the parent Activity to reload the list
     */
    public void reload() {
        /* if a dossier was previously selected, we have to notify the parent
         * activity that the data has changed, so the activity remove the previously selected
         * dossier details
         */
        if (selectedDossier != ListView.INVALID_POSITION) {
            selectedDossier = ListView.INVALID_POSITION;
            setActivatedPosition(ListView.INVALID_POSITION);
            listener.onDossierSelected(null);
        }
        ((DossierListAdapter) getListAdapter()).clearSelection();

        getDossiers(true);
    }

    @Override
    public void onDataChanged() {
        ((DossierListAdapter) getListAdapter()).clearSelection();
        if (selectedDossier != ListView.INVALID_POSITION) {
            selectedDossier = ListView.INVALID_POSITION;
            setActivatedPosition(ListView.INVALID_POSITION);
            /* if a dossier was previously selected, we have to notify the parent
             * activity that the data has changed, so the activity remove the previously selected
             * dossier details
             */
            listener.onDossierSelected(null);
        }
    }

    private class DossierListAdapter extends ArrayAdapter<Dossier> implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

        private final DossierSelectedListener listener;
        private HashSet<Dossier> checkedDossiers;

        public DossierListAdapter(Context context, DossierSelectedListener listener) {
            super(context, R.layout.dossiers_list_item, R.id.dossiers_list_item_title);
            this.listener = listener;
            this.checkedDossiers = new HashSet<Dossier>();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            Dossier dossier = dossiers.get(position);
            ((TextView) v.findViewById(R.id.dossiers_list_item_extras)).setText(dossier.getType() + " / " + dossier.getSousType());
            // FIXME : changement d'api avec toutes les actions..
            Action actionDemandee = dossier.getActions().get(0);
            if (actionDemandee != null) {
                ((ImageView) v.findViewById(R.id.dossiers_list_item_image)).setImageResource(actionDemandee.getIcon(false));
            }
            CheckBox c = (CheckBox) v.findViewById(R.id.dossiers_list_item_checkBox);
            c.setTag(position);
            c.setOnCheckedChangeListener(this);
            c.setChecked(checkedDossiers.contains(dossier));
            LinearLayout l = (LinearLayout) v.findViewById(R.id.dossiers_list_item_selectable_layout);
            l.setTag(position);
            l.setOnClickListener(this);
            return v;
        }

        @Override
        public int getCount() {
            return (dossiers == null)? 0 : dossiers.size();
        }

        @Override
        public Dossier getItem(int position) {
            return dossiers.get(position);
        }

        @Override
        public int getPosition(Dossier item) {
            return dossiers.indexOf(item);
        }

        @Override
        public boolean isEmpty() {
            return (dossiers == null) || dossiers.isEmpty();
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Dossier dossier = dossiers.get((Integer) buttonView.getTag());
            boolean updated = (isChecked)?
                    checkedDossiers.add(dossier) :
                    checkedDossiers.remove(dossier);

            if (updated) {
                // will call invalidateOptionMenu, so the actions will be updated
                listener.onDossierChecked(dossier.getId());
            }
        }

        @Override
        public void onClick(View v) {
            Integer position = (Integer) v.getTag();
            if (position != selectedDossier) {
                listener.onDossierSelected(dossiers.get(position).getId());
                setActivatedPosition(position);
            }
        }

        public HashSet<Dossier> getCheckedDossiers() {
            return checkedDossiers;
        }

        public void clearSelection() {
            checkedDossiers.clear();
            notifyDataSetChanged();
        }
    }

    private class DossiersLoadingTask extends LoadingTask {

        public DossiersLoadingTask(Activity context, DataChangeListener listener) {
            super(context, listener);
        }

        @Override
        protected Void doInBackground(String... params) {
            // Check if this task is cancelled as often as possible.
            if (isCancelled()) {return null;}
            dossiers = RESTClient.INSTANCE.getDossiers(params[0]);
            return null;
        }
    }

}
