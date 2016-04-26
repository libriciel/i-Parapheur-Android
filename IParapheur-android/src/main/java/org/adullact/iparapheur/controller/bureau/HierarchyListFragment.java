/*
 * <p>iParapheur Android<br/>
 * Copyright (C) 2016 Adullact-Projet.</p>
 *
 * <p>This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.</p>
 *
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.</p>
 *
 * <p>You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.</p>
 */
package org.adullact.iparapheur.controller.bureau;

import android.animation.Animator;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.utils.DeviceUtils;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.LoadingTask;
import org.adullact.iparapheur.utils.ViewUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;


public class HierarchyListFragment extends Fragment {

	public static final String FRAGMENT_TAG = "bureaux_list_fragment";

	private HierarchyListFragmentListener mListener;
	private ViewSwitcher mViewSwitcher;

	private ListView mBureauListView;                             // ListView used to show the Bureau of the currently selected account
	private ListView mDossierListView;                            // ListView used to show the Bureau of the currently selected account
	private SwipeRefreshLayout mBureauSwipeRefreshLayout;         // Swipe refresh layout on top of the list view
	private SwipeRefreshLayout mDossierSwipeRefreshLayout;        // Swipe refresh layout on top of the list view
	private View mBureauEmptyView;
	private View mDossierEmptyView;

	private List<Bureau> mBureauxList;                            // List of Bureau currently displayed in this Fragment
	private List<Dossier> mDossiersList = new ArrayList<>();      // List of Bureau currently displayed in this Fragment
	private String mSelectedBureauId = null;                      // The currently selected dossier
	private int mSelectedDossier = ListView.INVALID_POSITION;     // The currently selected dossier

	// <editor-fold desc="LifeCycle">

	@Override public void onAttach(Context context) {
		super.onAttach(context);

		// Activities containing this fragment must implement its callbacks.
		if (!(context instanceof HierarchyListFragmentListener))
			throw new IllegalStateException("Activity must implement BureauSelectedListener.");

		mListener = (HierarchyListFragmentListener) context;
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.hierarchy_list_fragment, container, false);

		// Retrieve Views

		mViewSwitcher = (ViewSwitcher) view.findViewById(R.id.hierarchy_viewswitcher);
		mBureauSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.hierarchy_bureaux_swiperefreshlayout);
		mDossierSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.hierarchy_dossier_swiperefreshlayout);
		mBureauListView = (ListView) view.findViewById(R.id.hierarchy_bureaux_listview);
		mDossierListView = (ListView) view.findViewById(R.id.hierarchy_dossier_listview);
		mBureauEmptyView = view.findViewById(R.id.hierarchy_bureaux_empty);
		mDossierEmptyView = view.findViewById(R.id.hierarchy_dossier_empty);

		// Setting up listeners, etc

		mBureauSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override public void onRefresh() {
				new BureauxLoadingTask().execute();
			}
		});
		mBureauSwipeRefreshLayout.setColorSchemeResources(R.color.secondary_500, R.color.secondary_300, R.color.secondary_700);
		mBureauListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Bureau bureauClicked = ((BureauListAdapter) mBureauListView.getAdapter()).getItem(position);

				if (bureauClicked != null) {
					mSelectedBureauId = bureauClicked.getId();
					onBureauClicked(bureauClicked);
				}
				else {
					mSelectedBureauId = null;
				}
			}
		});
		mBureauListView.setEmptyView(mBureauEmptyView);
		mBureauListView.setAdapter(new BureauListAdapter(getActivity()));

		mDossierSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override public void onRefresh() {
				new DossiersLoadingTask().execute();
			}
		});
		mDossierSwipeRefreshLayout.setColorSchemeResources(R.color.secondary_500, R.color.secondary_300, R.color.secondary_700);
		mDossierListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Dossier selectedDossier = ((DossierListAdapter) mDossierListView.getAdapter()).getItem(position);
				mListener.onDossierListFragmentSelected(selectedDossier, mSelectedBureauId);
			}
		});
		mDossierListView.setEmptyView(mDossierEmptyView);
		mDossierListView.setAdapter(new DossierListAdapter(getActivity()));

		//

		return view;
	}

	@Override public void onStart() {
		super.onStart();

		mBureauListView.setVisibility(View.INVISIBLE);
		mBureauEmptyView.setVisibility(View.VISIBLE);
		updateBureaux(true);
	}

	@Override public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface.
		mListener = null;
	}

	// </editor-fold desc="LifeCycle">

	public void updateBureaux(boolean forceReload) {
		if (forceReload)
			this.mBureauxList = null;

		if ((mBureauxList == null) && (MyAccounts.INSTANCE.getSelectedAccount() != null)) {
			mBureauListView.setVisibility(View.INVISIBLE);
			mBureauEmptyView.setVisibility(View.VISIBLE);
			new BureauxLoadingTask().execute();
		}

		onBureauDataChanged();
	}

	private void onBureauClicked(@NonNull Bureau bureau) {

		mSelectedBureauId = bureau.getId();
		new DossiersLoadingTask().execute();

		mViewSwitcher.setInAnimation(getActivity(), R.anim.slide_in_right);
		mViewSwitcher.setOutAnimation(getActivity(), R.anim.slide_out_left);
		mViewSwitcher.setDisplayedChild(1);
	}

	public void onBureauDataChanged() {
		((BureauListAdapter) mBureauListView.getAdapter()).notifyDataSetChanged();

		// if a bureau was previously selected, we have to notify the parent
		// activity that the data has changed, so the activity remove the previously selected
		// dossiers list and details
		if (mListener != null)
			mListener.onBureauListFragmentSelected(null);
	}

	public void onDossierDataChanged() {
		((DossierListAdapter) mDossierListView.getAdapter()).notifyDataSetChanged();

		// if a bureau was previously selected, we have to notify the parent
		// activity that the data has changed, so the activity remove the previously selected
		// dossiers list and details
//		if (mListener != null)
//			mListener.onDossierListFragmentSelected(null);
	}

	public boolean popBackStack() {

		if (mViewSwitcher.getDisplayedChild() == 1) {
			mViewSwitcher.setInAnimation(getActivity(), android.R.anim.slide_in_left);
			mViewSwitcher.setOutAnimation(getActivity(), android.R.anim.slide_out_right);
			mViewSwitcher.setDisplayedChild(0);

			// Fore some reason, the bureau list view is empty on a ViewSwitcher flip
			// Calling the adapter refresh fixes it...
			((BureauListAdapter) mBureauListView.getAdapter()).notifyDataSetChanged();

			return true;
		}
		else {
			return false;
		}
	}

	// <editor-fold desc="SwipeRefreshLayout">
//	@Override public void onRefresh() {
//		new BureauxLoadingTask(getActivity(), this).execute();
//	}

	// </editor-fold desc="SwipeRefreshLayout">

	/**
	 * The parent activity must implement this interface.
	 * Used to notify the activity on bureaux changes
	 */
	public interface HierarchyListFragmentListener {

		/**
		 * Called when the bureau identified by the id passed in parameter has been
		 * selected by the user or when data changes (id will be null)
		 *
		 * @param id the bureau id or null if none is selected (data changed)
		 */
		void onBureauListFragmentSelected(@Nullable String id);

		void onDossierListFragmentSelected(@NonNull Dossier dossier, @NonNull String bureauId);

		void onDossierCheckedChanged(boolean checked);
	}

	private class BureauxLoadingTask extends LoadingTask {

		public BureauxLoadingTask() {

			super(getActivity(), new DataChangeListener() {
				@Override public void onDataChanged() {
					onBureauDataChanged();
				}
			});
		}

		@Override protected void load(String... params) throws IParapheurException {
			// Check if this task is cancelled as often as possible.
			if (isCancelled())
				return;

			if (!DeviceUtils.isDebugOffline()) {
				try {
					mBureauxList = RESTClient.INSTANCE.getBureaux();
				}
				catch (final IParapheurException exception) {
					activity.runOnUiThread(new Runnable() {
						public void run() {
							String message = activity.getString(exception.getResId());
							Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
						}
					});
				}
			}
			else {
				mBureauxList = new ArrayList<>();
				mBureauxList.add(new Bureau(UUID.randomUUID().toString(), "bureau defaut"));
			}
		}

		@Override protected void showProgress() {
			if (isAdded())
				mBureauSwipeRefreshLayout.setRefreshing(true);
		}

		@Override protected void hideProgress() {
			if (isAdded()) {

				if (mBureauListView.getVisibility() == View.VISIBLE)
					ViewUtils.crossfade(getActivity(), mBureauEmptyView, mBureauSwipeRefreshLayout);

				mBureauSwipeRefreshLayout.setRefreshing(false);
			}
		}
	}

	private class DossiersLoadingTask extends AsyncTask<Void, Void, Void> {

		@Override protected void onPreExecute() {
			super.onPreExecute();
//			ViewUtils.crossfade(getActivity(), mBureauEmptyView, mBureauListView);
			mDossierSwipeRefreshLayout.setRefreshing(true);
		}

		@Override protected Void doInBackground(Void... params) {
			mDossiersList.clear();

			if (!DeviceUtils.isDebugOffline()) {
				try { mDossiersList.addAll(RESTClient.INSTANCE.getDossiers(mSelectedBureauId)); }
				catch (IParapheurException e) { e.printStackTrace(); }
			}
			else {
				Dossier dossier1 = new Dossier(1);
				Dossier dossier2 = new Dossier(2);
				mDossiersList.add(dossier1);
				mDossiersList.add(dossier2);
			}

			return null;
		}

		@Override protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			ViewUtils.crossfade(getActivity(), mBureauEmptyView, mBureauListView);
			mDossierSwipeRefreshLayout.setRefreshing(false);
			mDossierListView.setItemChecked(ListView.INVALID_POSITION, true);
			((DossierListAdapter) mDossierListView.getAdapter()).notifyDataSetChanged();
		}
	}

	private class BureauListAdapter extends ArrayAdapter<Bureau> {

		public BureauListAdapter(Context context) {
			super(context, R.layout.bureaux_list_cell, R.id.bureau_list_cell_title);
		}

		@Override public View getView(int position, View convertView, ViewGroup parent) {

			View cell = super.getView(position, convertView, parent);

			TextView lateBadgeTextView = (TextView) cell.findViewById(R.id.bureau_list_cell_late);
			TextView todoCountTextView = (TextView) cell.findViewById(R.id.bureau_list_cell_todo_count);

			Bureau currentBureau = getItem(position);
			if (currentBureau != null) {

				// Determines subtitle content

				String subtitle;

				if (currentBureau.getTodoCount() == 0)
					subtitle = getString(R.string.no_dossier);
				else if (currentBureau.getTodoCount() == 1)
					subtitle = getString(R.string.one_dossier);
				else
					subtitle = getString(R.string.nb_dossiers).replace("-number-", String.valueOf(currentBureau.getTodoCount()));

				// Applies values

				todoCountTextView.setText(subtitle);
				lateBadgeTextView.setText(String.valueOf(currentBureau.getLateCount()));
				lateBadgeTextView.setVisibility((currentBureau.getLateCount() != 0 ? View.VISIBLE : View.INVISIBLE));
			}

			return cell;
		}

		@Override public int getCount() {
			return (mBureauxList == null) ? 0 : mBureauxList.size();
		}

		@Override public Bureau getItem(int position) {
			return mBureauxList.get(position);
		}

		@Override public int getPosition(Bureau item) {
			return mBureauxList.indexOf(item);
		}

		@Override public boolean isEmpty() {
			return (mBureauxList == null) || mBureauxList.isEmpty();
		}
	}

	private class DossierListAdapter extends ArrayAdapter<Dossier> {

		//		private final DossierListFragmentListener listener;
		//		private final GestureDetector gestureDetector = new GestureDetector(getContext(), new OnSwipeGestureListener());
		private HashSet<Dossier> checkedDossiers;

		//		public DossierListAdapter(Context context, DossierListFragmentListener listener) {
		public DossierListAdapter(Context context) {
			super(context, R.layout.dossiers_list_cell, R.id.dossiers_list_item_title);
//			this.listener = listener;
			this.checkedDossiers = new HashSet<>();
		}

		@Override public View getView(int position, View convertView, ViewGroup parent) {
			final View cellView = super.getView(position, convertView, parent);
			Dossier dossier = mDossiersList.get(position);
			boolean isChecked = checkedDossiers.contains(dossier);

			// Text
			// FIXME : changement d'api avec toutes les actions..

			((TextView) cellView.findViewById(R.id.dossiers_list_item_extras)).setText(String.format("%s / %s", dossier.getType(), dossier.getSousType()));

			// CheckBox

			View checkableLayout = cellView.findViewById(R.id.dossiers_list_item_checkable_layout);

			if (mDossiersList.get(position).hasActions()) {
				checkableLayout.setVisibility(View.VISIBLE);
				checkableLayout.setTag(position);
				checkableLayout.setOnClickListener(new View.OnClickListener() {
					@Override public void onClick(View view) {
						toggleSelection(view);
					}
				});
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

					if (actionName.contentEquals(getString(R.string.action_signer)) && !dossier.isSignPapier())
						iconImageView.setImageResource(R.drawable.ic_sign_24dp);
					else if (actionName.contentEquals(getString(R.string.action_signer)) && dossier.isSignPapier())
						iconImageView.setImageResource(R.drawable.ic_visa_24dp);
					else if (actionName.contentEquals(getString(R.string.action_viser)))
						iconImageView.setImageResource(R.drawable.ic_visa_24dp);
					else if (actionName.contentEquals(getString(R.string.action_archiver)))
						iconImageView.setImageResource(R.drawable.ic_archivage_24dp);
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
//			selectableLayout.setOnClickListener(this);

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
			if (!mDossierSwipeRefreshLayout.isRefreshing()) {

				// Toggle checked state, and animate

				Dossier dossier = mDossiersList.get((Integer) view.getTag());
				View mainView = view.findViewById(R.id.dossiers_list_item_image_main_container);
				View selectorView = view.findViewById(R.id.dossiers_list_item_image_selector_container);

				if (checkedDossiers.contains(dossier)) {
					checkedDossiers.remove(dossier);

					// We call the checkedListener with a delay,
					// because the ActionMode cancelling calls an invalidate that breaks the animations
					ViewUtils.flip(getActivity(), selectorView, mainView, new Animator.AnimatorListener() {

						@Override public void onAnimationStart(Animator animator) { }

						@Override public void onAnimationEnd(Animator animator) { mListener.onDossierCheckedChanged(false); }

						@Override public void onAnimationCancel(Animator animator) { }

						@Override public void onAnimationRepeat(Animator animator) { }
					});
				}
				else {
					checkedDossiers.add(dossier);
					ViewUtils.flip(getActivity(), mainView, selectorView, null);
					mListener.onDossierCheckedChanged(false);
				}
			}
		}

//		@Override public void onClick(View v) {
//
//			switch (v.getId()) {
//				default:
//					Integer position = (Integer) v.getTag();
//					if (position != mSelectedDossier && !isRefreshing()) {
//						listener.onDossierSelected(mDossiersList.get(position), mBureauId);
//						setActivatedPosition(position);
//					}
//					break;
//			}
//		}

		public HashSet<Dossier> getCheckedDossiers() {
			return checkedDossiers;
		}

		public void clearSelection() {
			checkedDossiers.clear();
			notifyDataSetChanged();
		}

//		@Override public boolean onTouch(View v, MotionEvent event) {
//			return gestureDetector.onTouchEvent(event);
//		}

		public void onSwipeRight() {
			//Log.d("swipe", "SWIPE RIGHT");
		}

		public void onSwipeLeft() {
			//Log.d("swipe", "SWIPE LEFT");
		}

//		private final class OnSwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
//
//			private static final int SWIPE_THRESHOLD = 100;
//			private static final int SWIPE_VELOCITY_THRESHOLD = 100;
//
//			@Override public boolean onDown(MotionEvent e) {
//				return true;
//			}
//
//			@Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//
//				try {
//					float diffY = e2.getY() - e1.getY();
//					float diffX = e2.getX() - e1.getX();
//					if (Math.abs(diffX) > Math.abs(diffY)) {
//						if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
//							if (diffX > 0) {
//								onSwipeRight();
//							}
//							else {
//								onSwipeLeft();
//							}
//						}
//					}
//				}
//				catch (Exception exception) {
//					//exception.printStackTrace();
//				}
//
//				return false;
//			}
//		}

	}

}