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
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
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
import org.adullact.iparapheur.controller.dossier.action.RejectDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.SignatureDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.VisaDialogFragment;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.utils.CollectionUtils;
import org.adullact.iparapheur.utils.DeviceUtils;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;


/**
 * This fragment manages {@link Bureau} and {@link Dossier} lists on the left panel.
 * Both lists are pretty much the same, but some actions on the {@link Dossier} needs to be helded by the parent list.
 * That's a mess with two separate {@link Fragment}s, that's why we have an easiest {@link ViewSwitcher}.
 */
public class HierarchyListFragment extends Fragment {

	public static final String FRAGMENT_TAG = "hierarchy_list_fragment";

	// Views
	private ViewSwitcher mViewSwitcher;
	private ListView mBureauListView;
	private ListView mDossierListView;
	private SwipeRefreshLayout mBureauSwipeRefreshLayout;
	private SwipeRefreshLayout mDossierSwipeRefreshLayout;
	private View mBureauEmptyView;
	private View mDossierEmptyView;

	// Data
	private HierarchyListFragmentListener mListener;
	private List<Bureau> mBureauList = new ArrayList<>();
	private List<Dossier> mDossierList = new ArrayList<>();
	private HashSet<Dossier> mCheckedDossiers = new HashSet<>();
	private Bureau mSelectedBureau = null;                          // Which Bureau is displayed in the submenu
	private Dossier mDisplayedDossier = null;                       // Which Dossier is displayed in the Pdf viewer fragment
	private Bureau mDisplayedBureau = null;                         // Which Bureau is displayed in the Pdf viewer fragment
	private AsyncTask<Void, ?, ?> mPendingAsyncTask = null;

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
				executeAsyncTask(new BureauxLoadingTask());
			}
		});
		mBureauSwipeRefreshLayout.setColorSchemeResources(R.color.secondary_500, R.color.secondary_300, R.color.secondary_700);
		mBureauListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onBureauClicked(position);
			}
		});
		mBureauListView.setEmptyView(mBureauEmptyView);
		mBureauListView.setAdapter(new BureauListAdapter(getActivity()));

		mDossierSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override public void onRefresh() {
				if (mSelectedBureau != null) {
					executeAsyncTask(new DossiersLoadingTask());
				}
			}
		});
		mDossierSwipeRefreshLayout.setColorSchemeResources(R.color.secondary_500, R.color.secondary_300, R.color.secondary_700);
		mDossierListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onDossierClicked(position);
			}
		});
		mDossierListView.setEmptyView(mDossierEmptyView);
		mDossierListView.setAdapter(new DossierListAdapter(getActivity()));

		// Restore previous state, in case of rotation

		if (mBureauList.isEmpty()) {
			mBureauListView.setVisibility(View.INVISIBLE);
			mBureauEmptyView.setVisibility(View.VISIBLE);
		}
		else {
			mBureauListView.setVisibility(View.VISIBLE);
			mBureauEmptyView.setVisibility(View.INVISIBLE);

			if (mSelectedBureau != null) {
				mViewSwitcher.setDisplayedChild(1);
			}
		}

		//

		return view;
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {

		// In case of signature/visa/etc, let's give a few seconds to the server
		// and refresh the content.

		switch (requestCode) {

			case VisaDialogFragment.REQUEST_CODE_VISA:
			case RejectDialogFragment.REQUEST_CODE_REJECT:
			case SignatureDialogFragment.REQUEST_CODE_SIGNATURE:
			default:

				if ((resultCode == Activity.RESULT_OK) || (resultCode == SignatureDialogFragment.RESULT_CODE_SIGN_PAPIER)) {

					new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
						public void run() {

							mCheckedDossiers.clear();
							((DossierListAdapter) mDossierListView.getAdapter()).notifyDataSetChanged();

							executeAsyncTask(new DossiersLoadingTask());

							if (mListener != null)
								mListener.onDossierCheckedChanged(true);
						}
					}, 1500L);
				}

				break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override public void onStart() {
		super.onStart();

		if (mBureauList.isEmpty())
			updateBureaux(true);
	}

	@Override public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * Called manually from parent Activity.
	 *
	 * @return true if the event was consumed.
	 */
	public boolean onBackPressed() {

		if (mViewSwitcher.getDisplayedChild() == 1) {

			mViewSwitcher.setInAnimation(getActivity(), android.R.anim.slide_in_left);
			mViewSwitcher.setOutAnimation(getActivity(), android.R.anim.slide_out_right);
			mViewSwitcher.setDisplayedChild(0);

			// Fore some reason, the bureau list view is empty on a ViewSwitcher flip
			// Calling the adapter refresh fixes it...
			((BureauListAdapter) mBureauListView.getAdapter()).notifyDataSetChanged();

			mSelectedBureau = null;

			return true;
		}
		else {
			return false;
		}
	}

	// </editor-fold desc="LifeCycle">

	public Bureau getSelectedBureau() {
		return mSelectedBureau;
	}

	public HashSet<Dossier> getCheckedDossiers() {
		return mCheckedDossiers;
	}

	public void updateBureaux(boolean forceReload) {
		if (forceReload)
			this.mBureauList.clear();

		if ((mBureauList.isEmpty()) && (MyAccounts.INSTANCE.getSelectedAccount() != null)) {
			mBureauListView.setVisibility(View.INVISIBLE);
			mBureauEmptyView.setVisibility(View.VISIBLE);
			executeAsyncTask(new BureauxLoadingTask());
		}
	}

	private void onBureauClicked(int position) {

		// Faking the Bureau list selection, by selecting the previous one (or -1 if any).
		// We want to have a selected state only on the selected Dossier's Bureau.
		// The bureau will be selected in #onDossierClicked

		int displayedBureauPosition = mBureauList.indexOf(mDisplayedBureau);
		mBureauListView.setItemChecked(displayedBureauPosition, true);

		// Switching to Dossiers list

		Bureau bureauClicked = ((BureauListAdapter) mBureauListView.getAdapter()).getItem(position);

		if (bureauClicked != null) {
			mSelectedBureau = bureauClicked;
			executeAsyncTask(new DossiersLoadingTask());

			mViewSwitcher.setInAnimation(getActivity(), R.anim.slide_in_right);
			mViewSwitcher.setOutAnimation(getActivity(), R.anim.slide_out_left);
			mViewSwitcher.setDisplayedChild(1);
		}
		else {
			mSelectedBureau = null;
		}
	}

	public void onDossierClicked(int position) {

		// Refreshing Bureau list, to have a selected state
		// only on the selected Dossier's Bureau.

		mDisplayedBureau = mSelectedBureau;
		int displayedBureauPosition = mBureauList.indexOf(mDisplayedBureau);
		mBureauListView.setItemChecked(displayedBureauPosition, true);

		// Saving it in case of back and forth in menuing

		mDisplayedDossier = mDossierList.get(position);

		// Callback

		Dossier selectedDossier = ((DossierListAdapter) mDossierListView.getAdapter()).getItem(position);
		mListener.onDossierListFragmentSelected(selectedDossier, mSelectedBureau.getId());
	}

	private void executeAsyncTask(@NonNull AsyncTask<Void, ?, ?> task) {

		if (mPendingAsyncTask != null)
			mPendingAsyncTask.cancel(false);

		mPendingAsyncTask = task;
		mPendingAsyncTask.execute();
	}

	// <editor-fold desc="Interface">

	public interface HierarchyListFragmentListener {

		void onDossierListFragmentSelected(@NonNull Dossier dossier, @NonNull String bureauId);

		void onDossierCheckedChanged(boolean forceClose);
	}

	// </editor-fold desc="Interface">

	private class BureauxLoadingTask extends AsyncTask<Void, Void, IParapheurException> {

		@Override protected void onPreExecute() {
			super.onPreExecute();
			mBureauSwipeRefreshLayout.setRefreshing(true);
		}

		@Override protected IParapheurException doInBackground(Void... params) {
			mBureauList.clear();

			if (!DeviceUtils.isDebugOffline()) {
				try { mBureauList.addAll(RESTClient.INSTANCE.getBureaux()); }
				catch (final IParapheurException exception) { return exception; }
			}
			else {
				mBureauList.add(new Bureau(UUID.randomUUID().toString(), "bureau defaut"));
			}

			return null;
		}

		@Override protected void onPostExecute(IParapheurException exception) {
			super.onPostExecute(exception);

			mPendingAsyncTask = null;

			if (isCancelled())
				return;

			((BureauListAdapter) mBureauListView.getAdapter()).notifyDataSetChanged();

			// Retrieving previous state

			int displayedBureauPosition = mBureauList.indexOf(mDisplayedBureau);
			mBureauListView.setItemChecked(displayedBureauPosition, true);

			// Refreshing views state

			mBureauSwipeRefreshLayout.setRefreshing(false);

			if ((mBureauEmptyView.getVisibility() == View.VISIBLE) && !mBureauList.isEmpty())
				ViewUtils.crossfade(getActivity(), mBureauListView, mBureauEmptyView);

			// Error management

			if (exception != null) {
				String message = getString(exception.getResId());
				Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
			}
		}
	}

	private class DossiersLoadingTask extends AsyncTask<Void, Void, IParapheurException> {

		@Override protected void onPreExecute() {
			super.onPreExecute();
			mDossierSwipeRefreshLayout.setRefreshing(true);
		}

		@Override protected IParapheurException doInBackground(Void... params) {
			mDossierList.clear();

			if (!DeviceUtils.isDebugOffline()) {
				try { mDossierList.addAll(RESTClient.INSTANCE.getDossiers(mSelectedBureau.getId())); }
				catch (IParapheurException exception) { return exception; }
			}
			else {
				Dossier dossier1 = new Dossier("1", "Test 01", Action.VISA, CollectionUtils.asSet(Action.VISA), "Type", "Sous-Type", new Date(), null, false);
				Dossier dossier2 = new Dossier("2", "Test 02", Action.VISA, CollectionUtils.asSet(Action.VISA), "Type", "Sous-Type", new Date(), null, false);
				mDossierList.add(dossier1);
				mDossierList.add(dossier2);
			}

			return null;
		}

		@Override protected void onPostExecute(IParapheurException exception) {
			super.onPostExecute(exception);

			mPendingAsyncTask = null;
			if (isCancelled())
				return;

			((DossierListAdapter) mDossierListView.getAdapter()).notifyDataSetChanged();

			// Retrieving previous state

			int displayedDossierPosition = mDossierList.indexOf(mDisplayedDossier);
			mDossierListView.setItemChecked(displayedDossierPosition, true);

			// Refreshing views state

			mDossierSwipeRefreshLayout.setRefreshing(false);

			if ((mDossierEmptyView.getVisibility() == View.VISIBLE) && !mDossierList.isEmpty())
				ViewUtils.crossfade(getActivity(), mDossierListView, mDossierEmptyView);

			// Error management

			if (exception != null) {
				String message = getString(exception.getResId());
				Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
			}
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
					subtitle = String.format(getString(R.string.nb_dossiers), currentBureau.getTodoCount());

				// Applies values

				todoCountTextView.setText(subtitle);
				lateBadgeTextView.setText(String.valueOf(currentBureau.getLateCount()));
				lateBadgeTextView.setVisibility((currentBureau.getLateCount() != 0 ? View.VISIBLE : View.INVISIBLE));
			}

			return cell;
		}

		@Override public int getCount() {
			return mBureauList.size();
		}

		@Override public Bureau getItem(int position) {
			return mBureauList.get(position);
		}

		@Override public int getPosition(Bureau item) {
			return mBureauList.indexOf(item);
		}

		@Override public boolean isEmpty() {
			return mBureauList.isEmpty();
		}
	}

	private class DossierListAdapter extends ArrayAdapter<Dossier> {

		public DossierListAdapter(Context context) {
			super(context, R.layout.dossiers_list_cell, R.id.dossiers_list_item_title);
		}

		@Override public View getView(int position, View convertView, ViewGroup parent) {

			final View cellView = super.getView(position, convertView, parent);
			Dossier dossier = mDossierList.get(position);
			boolean isChecked = mCheckedDossiers.contains(dossier);

			// Text

			// FIXME : changement d'api avec toutes les actions.
			((TextView) cellView.findViewById(R.id.dossiers_list_item_extras)).setText(String.format("%s / %s", dossier.getType(), dossier.getSousType()));

			// CheckBox

			View checkableLayout = cellView.findViewById(R.id.dossiers_list_item_checkable_layout);

			if (mDossierList.get(position).hasActions()) {
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
			return mDossierList.size();
		}

		@Override public Dossier getItem(int position) {
			return mDossierList.get(position);
		}

		@Override public int getPosition(Dossier item) {
			return mDossierList.indexOf(item);
		}

		@Override public boolean isEmpty() {
			return mDossierList.isEmpty();
		}

		public void toggleSelection(View view) {

			if (mDossierSwipeRefreshLayout.isRefreshing())
				return;

			// Toggle checked state, and animate

			Dossier dossier = mDossierList.get((Integer) view.getTag());
			View mainView = view.findViewById(R.id.dossiers_list_item_image_main_container);
			View selectorView = view.findViewById(R.id.dossiers_list_item_image_selector_container);

			if (mCheckedDossiers.contains(dossier)) {
				mCheckedDossiers.remove(dossier);

				// We call the checkedListener with a delay,
				// because the ActionMode cancelling calls an invalidate that breaks the animations
				ViewUtils.flip(getActivity(), selectorView, mainView, new Animator.AnimatorListener() {

					@Override public void onAnimationStart(Animator animator) { }

					@Override public void onAnimationEnd(Animator animator) { mListener.onDossierCheckedChanged(mCheckedDossiers.isEmpty()); }

					@Override public void onAnimationCancel(Animator animator) { }

					@Override public void onAnimationRepeat(Animator animator) { }
				});
			}
			else {
				mCheckedDossiers.add(dossier);
				ViewUtils.flip(getActivity(), mainView, selectorView, null);
				mListener.onDossierCheckedChanged(mCheckedDossiers.isEmpty());
			}
		}
	}
}