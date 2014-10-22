package org.adullact.iparapheur.controller.dossier;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import org.adullact.iparapheur.controller.IParapheur;
import org.adullact.iparapheur.controller.dossier.action.TdtHeliosDialogFragment;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.controller.dossier.action.ArchivageDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.MailSecDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.RejetDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.SignatureDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.VisaDialogFragment;
import org.adullact.iparapheur.controller.utils.LoadingTask;
import org.adullact.iparapheur.controller.utils.SwipeRefreshListFragment;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.controller.utils.IParapheurException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * A list fragment representing a list of Dossiers. This fragment
 * supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link DossierDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link org.adullact.iparapheur.controller.dossier.DossierListFragment.DossierListFragmentListener}
 * interface.
 *
 * This fragment is also used to retain all the dossiers informations.
 * The detail fragment uses the dossiers of this fragment, so the already downloaded
 * details of a dossier are retained in this fragment. Also the pdf is saved on external storage
 * and its url is stored in the dossier information.
 */
public class DossierListFragment extends SwipeRefreshListFragment implements LoadingTask.DataChangeListener, SwipeRefreshLayout.OnRefreshListener {

    public static String TAG = "Dossiers_list";

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    //private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private DossierListFragmentListener listener;

    /**
     * Bureau id where the dossiers belongs
     */
    private String bureauId;
    /**
     * List of dossiers displayed in this fragment
     */
    private List<Dossier> dossiers;

    /**
     * the currently selected dossier
     */
    private int selectedDossier = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections and datachanges.
     */
    public interface DossierListFragmentListener {
        /**
         * Callback used when a dossier has been selected.
         */
        void onDossierSelected(String dossierId, String bureauId);
        /**
         * Callback used when a dossiers has been loaded.
         * @param size
         */
        void onDossiersLoaded(int size);

        void onDossiersNotLoaded();
        /**
         * Callback used when a dossier has been checked.
         */
        void onDossierCheckedChanged();
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
        if (!(activity instanceof DossierListFragmentListener))
        {
            throw new IllegalStateException("Activity must implement DossierListFragmentListener.");
        }
        listener = (DossierListFragmentListener) activity;
    }

    // Called only once as retainInstance is set to true.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View getInitialView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dossiers_list, container, true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().setDivider(new ColorDrawable(android.R.color.holo_blue_light));
        getListView().setDividerHeight(1);
        getListView().setBackgroundColor(getResources().getColor(android.R.color.background_light));
        setListAdapter(new DossierListAdapter(getActivity(), listener));
        setOnRefreshListener(this);
        setHasOptionsMenu(false);
        setColorScheme(android.R.color.holo_green_light,
                android.R.color.holo_red_light,
                android.R.color.holo_blue_light,
                android.R.color.holo_orange_light);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Reset the active callbacks interface .
        listener = null;
    }

    public void setBureauId(String bureauId) {
        if (this.bureauId != bureauId) {
            this.bureauId = bureauId;
            if (bureauId == null) {
                this.dossiers = null;
            }
            getDossiers(true);
        }
    }

    public String getBureauId() {
        return bureauId;
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
            getListView().clearChoices();
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
        }
        ((DossierListAdapter) getListView().getAdapter()).clearSelection();
        getDossiers(true);
    }

    @Override
    public void onDataChanged() {
        ((DossierListAdapter) getListView().getAdapter()).clearSelection();
        if (bureauId != null) {
            listener.onDossiersLoaded(this.dossiers.size());
        }
        else {
            listener.onDossiersNotLoaded();
        }

        if (selectedDossier != ListView.INVALID_POSITION) {
            selectedDossier = ListView.INVALID_POSITION;
            setActivatedPosition(ListView.INVALID_POSITION);
            /* if a dossier was previously selected, we have to notify the parent
             * activity that the data has changed, so the activity remove the previously selected
             * dossier details
             */
            listener.onDossierSelected(null, null);
        }
    }

    @Override
    public void onRefresh() {
        if (this.bureauId != null) {
            new DossiersLoadingTask(getActivity(), this).execute(bureauId);
        }
        else {
            setRefreshing(false);
        }
    }

    public HashSet<Dossier> getCheckedDossiers() {
        return ((DossierListAdapter) getListAdapter()).getCheckedDossiers();
    }

    public void clearSelection() {
        ((DossierListAdapter) getListAdapter()).clearSelection();
    }

    private class DossierListAdapter extends ArrayAdapter<Dossier> implements View.OnClickListener, View.OnTouchListener {

        private final DossierListFragmentListener listener;
        private HashSet<Dossier> checkedDossiers;
        private final GestureDetector gestureDetector = new GestureDetector(getContext(), new OnSwipeGestureListener());

        public DossierListAdapter(Context context, DossierListFragmentListener listener) {
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
            Action actionDemandee = dossier.getActionDemandee();
            if (actionDemandee != null) {
                ((ImageView) v.findViewById(R.id.dossiers_list_item_image)).setImageResource(actionDemandee.getIcon(false));
            }
            CheckBox c = (CheckBox) v.findViewById(R.id.dossiers_list_item_checkBox);
            if (dossiers.get(position).hasActions()) {
                c.setVisibility(View.VISIBLE);
                c.setTag(position);
                // don't use setOnCheckedChangeListener, it doesn't work well with the ActionMode in
                // DossiersActivity
                c.setOnClickListener(this);
                c.setChecked(checkedDossiers.contains(dossier));
            }
            else {
                c.setVisibility(View.GONE);
            }
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
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.dossiers_list_item_checkBox :
                    if (!isRefreshing()) {
                        Dossier dossier = dossiers.get((Integer) v.getTag());
                        if (((CheckBox) v).isChecked()) {
                            checkedDossiers.add(dossier);
                        }
                        else {
                            checkedDossiers.remove(dossier);
                        }
                        // will update ActionMode, so the actions will be updated
                        listener.onDossierCheckedChanged();
                    }
                    break;
                case R.id.dossiers_list_item_selectable_layout:
                    Integer position = (Integer) v.getTag();
                    if (position != selectedDossier && !isRefreshing()) {
                        listener.onDossierSelected(dossiers.get(position).getId(), bureauId);
                        setActivatedPosition(position);
                    }
                    break;
            }
        }

        public HashSet<Dossier> getCheckedDossiers() {
            return checkedDossiers;
        }

        public void clearSelection() {
            checkedDossiers.clear();
            notifyDataSetChanged();
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class OnSwipeGestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                        }
                    }
                } catch (Exception exception) {
                    //exception.printStackTrace();
                }
                return result;
            }
        }

        public void onSwipeRight() {
            //Log.d("swipe", "SWIPE RIGHT");
        }

        public void onSwipeLeft() {
            //Log.d("swipe", "SWIPE LEFT");
        }

    }

    private class DossiersLoadingTask extends LoadingTask {

        public DossiersLoadingTask(Activity context, DataChangeListener listener) {
            super(context, listener);
        }

        @Override
        protected void load(String... params) throws IParapheurException {
            // Check if this task is cancelled as often as possible.
            if (isCancelled()) {return;}
            if (!IParapheur.OFFLINE) {
                dossiers = RESTClient.INSTANCE.getDossiers(params[0]);
            }
            else {
                dossiers = new ArrayList<Dossier>();
                Dossier dossier1 = new Dossier(1);
                Dossier dossier2 = new Dossier(2);
                dossiers.add(dossier1);
                dossiers.add(dossier2);
            }
        }

        @Override
        protected void showProgress() {
            setRefreshing(true);
        }

        @Override
        protected void hideProgress() {
            setRefreshing(false);
        }
    }
}
