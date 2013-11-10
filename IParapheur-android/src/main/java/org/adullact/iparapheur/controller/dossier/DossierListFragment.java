package org.adullact.iparapheur.controller.dossier;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.adullact.iparapheur.controller.DataChangeListener;
import org.adullact.iparapheur.controller.connectivity.RESTClient;
import org.adullact.iparapheur.controller.utils.LoadingTask;
import org.adullact.iparapheur.model.Dossier;

import java.util.ArrayList;
import java.util.UUID;

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
public class DossierListFragment extends ListFragment implements DataChangeListener {

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
    private String bureauId = "433149e9-a552-4472-90a6-2f08eb046eca";
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
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DossierListFragment() {}

    // Called only once as retainInstance is set to true.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new DossierListAdapter(getActivity()));
        setRetainInstance(true);
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
        getDossiers(false);
    }

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

    @Override
    public void onDetach() {
        super.onDetach();
        // Reset the active callbacks interface .
        listener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        // Notify the parent activity that a dossier has been selected, only if not already selected.
        if (position != selectedDossier) {
            listener.onDossierSelected(dossiers.get(position).getId());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedDossier != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, selectedDossier);
        }
    }

    private void getDossiers(boolean forceReload) {
        if ((dossiers == null) || forceReload) {
            new DossiersLoadingTask(getActivity(), this).execute("bureauId");
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

    @Override
    public void onDataChanged() {
        ((DossierListAdapter) getListAdapter()).notifyDataSetChanged();
        /* if a dossier was previously selected, we have to notify the parent
         * activity that the data has changed, so the activity remove the previously selected
         * dossier details
         */
        if (selectedDossier != ListView.INVALID_POSITION) {
            selectedDossier = ListView.INVALID_POSITION;
            listener.onDossierSelected(null);
        }
    }

    private class DossierListAdapter extends ArrayAdapter<Dossier>
    {

        public DossierListAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_activated_2, android.R.id.text1);
        }

        @Override
        public long getItemId(int position) {
            // FIXME : is it ok?
            return UUID.fromString(dossiers.get(position).getId()).getMostSignificantBits();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            Dossier dossier = dossiers.get(position);
            ((TextView) v.findViewById(android.R.id.text2)).setText(dossier.getType() + " / " + dossier.getSousType());
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
    }

    private class DossiersLoadingTask extends LoadingTask {

        public DossiersLoadingTask(Activity context, DataChangeListener listener) {
            super(context, listener);
        }

        @Override
        protected Void doInBackground(String... params) {
            // Check if this task is cancelled as often as possible.
            if (isCancelled()) {return null;}
            dossiers = RESTClient.INSTANCE.getDossiers(bureauId);
            return null;
        }
    }

}
