package org.adullact.iparapheur.controller.dossier;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.dossier.action.RejectDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.SignatureDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.VisaDialogFragment;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.utils.DeviceUtils;
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

	public static String FRAGMENT_TAG = "dossiers_list_fragment";
	public static String ARG_BUREAU_ID = "bureau_id";

	private DossierListFragmentListener mListener;
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

	@Override public void onAttach(Context context) {
		super.onAttach(context);

		// Activities containing this fragment must implement its callbacks.
		if (!(context instanceof DossierListFragmentListener))
			throw new IllegalStateException("Activity must implement DossierListFragmentListener.");

		mListener = (DossierListFragmentListener) context;
	}

	@Override public View getInitialView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dossiers_list, container, false);
		mContentView = view.findViewById(android.R.id.content);
		mSpinnerProgress = view.findViewById(android.R.id.progress);
		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		getListView().setDivider(new ColorDrawable(ContextCompat.getColor(getActivity(), android.R.color.background_light)));
		getListView().setDividerHeight(1);
		getListView().setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.background_light));
		setListAdapter(new DossierListAdapter(getActivity(), mListener));
		setOnRefreshListener(this);
		setHasOptionsMenu(false);
	}

	@Override public void onStart() {
		super.onStart();

		setBureauId(getArguments().getString(ARG_BUREAU_ID, null));
	}

	@Override public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface.
		mListener = null;
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {

		// In case of signature/visa/etc, let's give a few seconds to the server
		// and refresh the content.

		switch (requestCode) {

			case SignatureDialogFragment.REQUEST_CODE_SIGNATURE:
			case VisaDialogFragment.REQUEST_CODE_VISA:
			case RejectDialogFragment.REQUEST_CODE_REJECT:

				if (resultCode == Activity.RESULT_OK) {

					new Handler(Looper.getMainLooper()).postDelayed(
							new Runnable() {
								public void run() {
									reload();

									if (mListener != null)
										mListener.onDossierCheckedChanged();
								}
							}, 1500l
					);
				}

				break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	// </editor-fold desc="LifeCycle">

	public String getBureauId() {
		return mBureauId;
	}

	public void setBureauId(String bureauId) {
		if ((mBureauId == null) || !(mBureauId.contentEquals(bureauId))) {
			mBureauId = bureauId;

			if (bureauId == null)
				mDossiersList = null;

			mSpinnerProgress.setVisibility(View.VISIBLE);
			mContentView.setVisibility(View.INVISIBLE);
			getDossiers(true);
		}
	}

	private void getDossiers(boolean forceReload) {

		if (mBureauId == null)
			onDataChanged();
		else if ((mDossiersList == null) || forceReload)
			new DossiersLoadingTask(getActivity(), this).execute(mBureauId);
	}

	private void setActivatedPosition(int position) {

		if (position == ListView.INVALID_POSITION)
			getListView().clearChoices();
		else
			getListView().setItemChecked(position, true);

		selectedDossier = position;
	}

	public HashSet<Dossier> getCheckedDossiers() {
		return ((DossierListAdapter) getListAdapter()).getCheckedDossiers();
	}

	public void clearSelection() {
		((DossierListAdapter) getListAdapter()).clearSelection();
	}

	/**
	 * called by the parent Activity to reload the list
	 */
	public void reload() {

		// If a dossier was previously selected, we have to notify the parent
		// activity that the data has changed, so the activity remove the previously selected
		// dossier details

		if (selectedDossier != ListView.INVALID_POSITION) {
			selectedDossier = ListView.INVALID_POSITION;
			setActivatedPosition(ListView.INVALID_POSITION);
		}

		((DossierListAdapter) getListView().getAdapter()).clearSelection();
		getDossiers(true);
	}

	// <editor-fold desc="DataChangeListener">

	@Override public void onDataChanged() {
		if (isAdded()) {

			((DossierListAdapter) getListView().getAdapter()).clearSelection();

			if (mListener != null) {

				if (mBureauId != null)
					mListener.onDossiersLoaded(mDossiersList.size());
				else
					mListener.onDossiersNotLoaded();
			}

			if (selectedDossier != ListView.INVALID_POSITION) {
				selectedDossier = ListView.INVALID_POSITION;
				setActivatedPosition(ListView.INVALID_POSITION);
				/* if a dossier was previously selected, we have to notify the parent
				 * activity that the data has changed, so the activity remove the previously selected
				 * dossier details
				 */
				if (mListener != null)
					mListener.onDossierSelected(null, null);
			}
		}
	}

	// </editor-fold desc="DataChangeListener">

	// <editor-fold desc="OnRefreshListener">

	@Override public void onRefresh() {
		if (mBureauId != null) {
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

		@Override public View getView(int position, View convertView, ViewGroup parent) {
			final View cellView = super.getView(position, convertView, parent);
			Dossier dossier = mDossiersList.get(position);
			boolean isChecked = checkedDossiers.contains(dossier);

			// Text
			// FIXME : changement d'api avec toutes les actions..

			((TextView) cellView.findViewById(R.id.dossiers_list_item_extras)).setText(dossier.getType() + " / " + dossier.getSousType());

			// CheckBox

			View checkableLayout = cellView.findViewById(R.id.dossiers_list_item_checkable_layout);

			if (mDossiersList.get(position).hasActions()) {
				checkableLayout.setVisibility(View.VISIBLE);
				checkableLayout.setTag(position);
				checkableLayout.setOnClickListener(
						new View.OnClickListener() {
							@Override public void onClick(View view) {
								toggleSelection(view);
							}
						}
				);
			}
			else {
				checkableLayout.setVisibility(View.GONE);
			}

			// Main icon

			Action actionDemandee = dossier.getActionDemandee();

			if (actionDemandee != null) {
				ImageView iconImageView = ((ImageView) cellView.findViewById(R.id.dossiers_list_item_image_main));

				if (!TextUtils.isEmpty(getString(actionDemandee.getTitle()))) {
					String actionName = getString(actionDemandee.getTitle());

					if (actionName.contentEquals(getString(R.string.action_signer)))
						iconImageView.setImageResource(R.drawable.ic_sign_24dp);
					else if (actionName.contentEquals(getString(R.string.action_archiver)))
						iconImageView.setImageResource(R.drawable.ic_archivage_24dp);
					else if (actionName.contentEquals(getString(R.string.action_viser)))
						iconImageView.setImageResource(R.drawable.ic_visa_24dp);
					else if (actionName.contentEquals(getString(R.string.action_mailsec)))
						iconImageView.setImageResource(R.drawable.ic_mailsec_24dp);
					else if (actionName.startsWith(getString(R.string.action_tdt))) // using startsWith, to catch helios and actes
						iconImageView.setImageResource(R.drawable.ic_tdt_24dp);

					View iconImageViewContainer = cellView.findViewById(R.id.dossiers_list_item_image_main_container);
					View selectorImageviewContainer = cellView.findViewById(R.id.dossiers_list_item_image_selector_container);
					iconImageViewContainer.setAlpha(isChecked ? 0f : 1f);
					selectorImageviewContainer.setAlpha(isChecked ? 1f : 0f);
				}
			}

			// Click events

			View selectableLayout = cellView.findViewById(R.id.dossiers_list_item_selectable_layout);
			selectableLayout.setTag(position);
			selectableLayout.setOnClickListener(this);

			return cellView;
		}

		@Override public int getCount() {
			return (mDossiersList == null) ? 0 : mDossiersList.size();
		}

		@Override public Dossier getItem(int position) {
			return mDossiersList.get(position);
		}

		@Override public int getPosition(Dossier item) {
			return mDossiersList.indexOf(item);
		}

		@Override public boolean isEmpty() {
			return (mDossiersList == null) || mDossiersList.isEmpty();
		}

		public void toggleSelection(View view) {
			if (!isRefreshing()) {

				// Toggle checked state, and animate

				Dossier dossier = mDossiersList.get((Integer) view.getTag());
				View mainView = view.findViewById(R.id.dossiers_list_item_image_main_container);
				View selectorView = view.findViewById(R.id.dossiers_list_item_image_selector_container);

				if (checkedDossiers.contains(dossier)) {
					checkedDossiers.remove(dossier);

					// We call the checkedListener with a delay,
					// because the ActionMode cancelling calls an invalidate that breaks the animations
					ViewUtils.flip(
							getActivity(), selectorView, mainView, new Animator.AnimatorListener() {

								@Override public void onAnimationStart(Animator animator) { }

								@Override public void onAnimationEnd(Animator animator) { listener.onDossierCheckedChanged(); }

								@Override public void onAnimationCancel(Animator animator) { }

								@Override public void onAnimationRepeat(Animator animator) { }
							}
					);
				}
				else {
					checkedDossiers.add(dossier);
					ViewUtils.flip(getActivity(), mainView, selectorView, null);
					listener.onDossierCheckedChanged();
				}
			}
		}

		@Override public void onClick(View v) {

			switch (v.getId()) {
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

		@Override public boolean onTouch(View v, MotionEvent event) {
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

			@Override public boolean onDown(MotionEvent e) {
				return true;
			}

			@Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

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

				return false;
			}
		}

	}

	private class DossiersLoadingTask extends LoadingTask {

		public DossiersLoadingTask(Activity activity, DataChangeListener listener) {
			super(activity, listener);
		}

		@Override protected void load(String... params) throws IParapheurException {
			// Check if this task is cancelled as often as possible.
			if (isCancelled()) {
				return;
			}
			if (!DeviceUtils.isDebugOffline()) {
				mDossiersList = RESTClient.INSTANCE.getDossiers(params[0]);
			}
			else {
				mDossiersList = new ArrayList<>();
				Dossier dossier1 = new Dossier(1);
				Dossier dossier2 = new Dossier(2);
				mDossiersList.add(dossier1);
				mDossiersList.add(dossier2);
			}
		}

		@Override protected void showProgress() {
			if (isAdded())
				if (mSpinnerProgress.getVisibility() != View.VISIBLE)
					setRefreshing(true);
		}

		@Override protected void hideProgress() {
			if (isAdded()) {

				if (mSpinnerProgress.getVisibility() == View.VISIBLE)
					ViewUtils.crossfade(getActivity(), mContentView, mSpinnerProgress);

				if (isRefreshing())
					setRefreshing(false);
			}
		}
	}
}
