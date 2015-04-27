package org.adullact.iparapheur.controller.dossier;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.IParapheurApplication;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.LoadingTask;
import org.adullact.iparapheur.utils.SwipeRefreshListFragment;
import org.adullact.iparapheur.utils.ViewUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A list fragment representing a list of Dossiers. This fragment
 * supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link DossierDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link org.adullact.iparapheur.controller.dossier.DossierListFragment.DossierListFragmentListener}
 * interface.
 * <p/>
 * This fragment is also used to retain all the dossiers informations.
 * The detail fragment uses the dossiers of this fragment, so the already downloaded
 * details of a dossier are retained in this fragment. Also the pdf is saved on external storage
 * and its url is stored in the dossier information.
 */
public class DossierListFragment extends SwipeRefreshListFragment implements LoadingTask.DataChangeListener, SwipeRefreshLayout.OnRefreshListener {

	public static String TAG = "dossiers_list_fragment";
	public static String ARG_BUREAU_ID = "bureau_id";

	private DossierListFragmentListener listener;
	private String mBureauId;                                   // Bureau id where the dossiers belongs
	private List<Dossier> mDossiersList;                        // List of dossiers displayed in this fragment
	private int selectedDossier = ListView.INVALID_POSITION;    // The currently selected dossier
	private View mSpinnerProgress;
	private View mContentView;

	public DossierListFragment() { }

	/**
	 * Instantiate a new Fragment, and give it an argument specifying the Bureau it should show
	 *
	 * @param bureauId Targeted bureau id
	 * @return a Fragment
	 */
	public static @NonNull DossierListFragment newInstance(@NonNull String bureauId) {
		DossierListFragment dossierFragment = new DossierListFragment();

		Bundle args = new Bundle();
		args.putString(ARG_BUREAU_ID, bureauId);
		dossierFragment.setArguments(args);

		dossierFragment.setRetainInstance(true);
		return dossierFragment;
	}

	// <editor-fold desc="LifeCycle">

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof DossierListFragmentListener))
			throw new IllegalStateException("Activity must implement DossierListFragmentListener.");

		listener = (DossierListFragmentListener) activity;
	}

	@Override
	public View getInitialView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dossiers_list, container, false);
		mContentView = view.findViewById(android.R.id.content);
		mSpinnerProgress = view.findViewById(android.R.id.progress);
		return view;
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
	}

	@Override
	public void onStart() {
		super.onStart();

		setBureauId(getArguments().getString(ARG_BUREAU_ID, null));
	}

	@Override
	public void onDetach() {
		super.onDetach();
		// Reset the active callbacks interface .
		listener = null;
	}

	// </editor-fold desc="LifeCycle">

	public String getBureauId() {
		return mBureauId;
	}

	public void setBureauId(String bureauId) {
		if ((mBureauId == null) || !(mBureauId.contentEquals(bureauId))) {
			mBureauId = bureauId;

			if (bureauId == null)
				this.mDossiersList = null;

			mSpinnerProgress.setVisibility(View.VISIBLE);
			mContentView.setVisibility(View.INVISIBLE);
			getDossiers(true);
		}
	}

	private void getDossiers(boolean forceReload) {
		if (mBureauId == null) {
			onDataChanged();
		}
		else if ((mDossiersList == null) || forceReload) {
			new DossiersLoadingTask(getActivity(), this).execute(mBureauId);
		}
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().clearChoices();
		}
		else {
			getListView().setItemChecked(position, true);
		}
		selectedDossier = position;
	}

	public HashSet<Dossier> getCheckedDossiers() {
		return ((DossierListAdapter) getListAdapter()).getCheckedDossiers();
	}

	public void clearSelection() {
		((DossierListAdapter) getListAdapter()).clearSelection();
	}

	/**
	 * Used by the containing activity to pass a dossier to the detail fragment.
	 * We get the dossier from here because this fragment has its instance state retained
	 * (the fragment isn't destroyed, so all the dossiers information are kept in memory).
	 *
	 * @param id the id of the dossier.
	 * @return the dossier with the id equal to the id passed in parameter.
	 */
	public Dossier getDossier(String id) {
		int position = mDossiersList.indexOf(new Dossier(id));
		return mDossiersList.get(position);
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

	// <editor-fold desc="DataChangeListener">

	@Override
	public void onDataChanged() {
		if (isAdded()) {

			((DossierListAdapter) getListView().getAdapter()).clearSelection();

			if (mBureauId != null)
				listener.onDossiersLoaded(this.mDossiersList.size());
			else
				listener.onDossiersNotLoaded();

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
	}

	// </editor-fold desc="DataChangeListener">

	// <editor-fold desc="OnRefreshListener">

	@Override
	public void onRefresh() {
		if (this.mBureauId != null) {
			new DossiersLoadingTask(getActivity(), this).execute(mBureauId);
		}
		else {
			setRefreshing(false);
		}
	}

	// </editor-fold desc="OnRefreshListener">

	// <editor-fold desc="Listener">

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections and datachanges.
	 */
	public interface DossierListFragmentListener {

		void onDossierSelected(@Nullable Dossier dossier, @Nullable String bureauId);

		void onDossiersLoaded(int size);

		void onDossiersNotLoaded();

		void onDossierCheckedChanged();
	}

	// </editor-fold desc="Listener">

	private class DossierListAdapter extends ArrayAdapter<Dossier> implements View.OnClickListener, View.OnTouchListener {

		private final DossierListFragmentListener listener;
		private final GestureDetector gestureDetector = new GestureDetector(getContext(), new OnSwipeGestureListener());
		private HashSet<Dossier> checkedDossiers;

		public DossierListAdapter(Context context, DossierListFragmentListener listener) {
			super(context, R.layout.dossiers_list_cell, R.id.dossiers_list_item_title);
			this.listener = listener;
			this.checkedDossiers = new HashSet<>();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View cellView = super.getView(position, convertView, parent);
			Dossier dossier = mDossiersList.get(position);

			((TextView) cellView.findViewById(R.id.dossiers_list_item_extras)).setText(dossier.getType() + " / " + dossier.getSousType());
			// FIXME : changement d'api avec toutes les actions..

			Action actionDemandee = dossier.getActionDemandee();
			if (actionDemandee != null)
				((ImageView) cellView.findViewById(R.id.dossiers_list_item_image)).setImageResource(actionDemandee.getIcon(false));

			CheckBox c = (CheckBox) cellView.findViewById(R.id.dossiers_list_item_checkBox);
			if (mDossiersList.get(position).hasActions()) {
				c.setVisibility(View.VISIBLE);
				c.setTag(position);
				// don't use setOnCheckedChangeListener, it doesn't work well with the ActionMode in DossiersActivity
				c.setOnClickListener(this);
				c.setChecked(checkedDossiers.contains(dossier));
			}
			else {
				c.setVisibility(View.GONE);
			}

			LinearLayout l = (LinearLayout) cellView.findViewById(R.id.dossiers_list_item_selectable_layout);
			l.setTag(position);
			l.setOnClickListener(this);

			return cellView;
		}

		@Override
		public int getCount() {
			return (mDossiersList == null) ? 0 : mDossiersList.size();
		}

		@Override
		public Dossier getItem(int position) {
			return mDossiersList.get(position);
		}

		@Override
		public int getPosition(Dossier item) {
			return mDossiersList.indexOf(item);
		}

		@Override
		public boolean isEmpty() {
			return (mDossiersList == null) || mDossiersList.isEmpty();
		}

		@Override
		public void onClick(View v) {

			switch (v.getId()) {
				case R.id.dossiers_list_item_checkBox:
					if (!isRefreshing()) {
						Dossier dossier = mDossiersList.get((Integer) v.getTag());

						if (((CheckBox) v).isChecked())
							checkedDossiers.add(dossier);
						else
							checkedDossiers.remove(dossier);

						// will update ActionMode, so the actions will be updated
						listener.onDossierCheckedChanged();
					}
					break;
				default:
					Integer position = (Integer) v.getTag();
					if (position != selectedDossier && !isRefreshing()) {
						listener.onDossierSelected(mDossiersList.get(position), mBureauId);
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

		public void onSwipeRight() {
			//Log.d("swipe", "SWIPE RIGHT");
		}

		public void onSwipeLeft() {
			//Log.d("swipe", "SWIPE LEFT");
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
							}
							else {
								onSwipeLeft();
							}
						}
					}
				}
				catch (Exception exception) {
					//exception.printStackTrace();
				}
				return result;
			}
		}

	}

	private class DossiersLoadingTask extends LoadingTask {

		public DossiersLoadingTask(Activity context, DataChangeListener listener) {
			super(context, listener);
		}

		@Override
		protected void load(String... params) throws IParapheurException {
			// Check if this task is cancelled as often as possible.
			if (isCancelled()) {
				return;
			}
			if (!IParapheurApplication.OFFLINE) {
				mDossiersList = RESTClient.INSTANCE.getDossiers(params[0]);
			}
			else {
				mDossiersList = new ArrayList<Dossier>();
				Dossier dossier1 = new Dossier(1);
				Dossier dossier2 = new Dossier(2);
				mDossiersList.add(dossier1);
				mDossiersList.add(dossier2);
			}
		}

		@Override
		protected void showProgress() {
			if (isAdded())
				if (mSpinnerProgress.getVisibility() != View.VISIBLE)
					setRefreshing(true);
		}

		@Override
		protected void hideProgress() {
			if (isAdded()) {

				if (mSpinnerProgress.getVisibility() == View.VISIBLE)
					ViewUtils.crossfade(getActivity(), mContentView, mSpinnerProgress);

				if (isRefreshing())
					setRefreshing(false);
			}
		}
	}
}
