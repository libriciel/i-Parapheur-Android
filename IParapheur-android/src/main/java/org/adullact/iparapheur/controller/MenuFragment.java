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
package org.adullact.iparapheur.controller;

import android.animation.Animator;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.j256.ormlite.dao.Dao;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.controller.dossier.action.RejectDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.SignatureDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.VisaDialogFragment;
import org.adullact.iparapheur.controller.dossier.filter.FilterDialogFragment;
import org.adullact.iparapheur.controller.dossier.filter.MyFilters;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.database.DatabaseHelper;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.Filter;
import org.adullact.iparapheur.model.ParapheurType;
import org.adullact.iparapheur.utils.CollectionUtils;
import org.adullact.iparapheur.utils.DeviceUtils;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.ViewUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;


/**
 * This fragment manages {@link Bureau} and {@link Dossier} lists on the left panel.
 *
 * Both lists are pretty much the same, but some actions on the {@link Dossier} needs to be helded by the parent list.
 * That's a mess with two separate {@link Fragment}s, that's why we have an easiest {@link ViewSwitcher}.
 *
 * The ActionBar editor-fold manages pretty much everything about {@link Filter}s.
 */
public class MenuFragment extends Fragment {

	public static final String FRAGMENT_TAG = "menu_fragment";

	// Views
	private ViewSwitcher mViewSwitcher;
	private ListView mBureauListView;
	private ListView mDossierListView;
	private SwipeRefreshLayout mBureauSwipeRefreshLayout;
	private SwipeRefreshLayout mDossierSwipeRefreshLayout;
	private View mBureauEmptyView;
	private View mDossierEmptyView;
	private View mDossierEmptyFiltersAlertView;

	// Data
	private List<Bureau> mBureauList = new ArrayList<>();
	private List<Dossier> mDossierList = new ArrayList<>();
	private List<ParapheurType> mTypology = new ArrayList<>();
	private HashSet<Dossier> mCheckedDossiers = new HashSet<>();
	private HashMap<MenuItem, Filter> mDisplayedFilters = new HashMap<>();
	private Bureau mSelectedBureau = null;                          // Which Bureau is displayed in the submenu
	private Dossier mDisplayedDossier = null;                       // Which Dossier is displayed in the Pdf viewer fragment
	private Bureau mDisplayedBureau = null;                         // Which Bureau is displayed in the Pdf viewer fragment
	private AsyncTask<Void, ?, ?> mPendingAsyncTask = null;

	// <editor-fold desc="LifeCycle">

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.menu_fragment, container, false);
		setRetainInstance(true);

		// Retrieve Views

		mViewSwitcher = (ViewSwitcher) view.findViewById(R.id.menu_fragment_viewswitcher);
		mBureauSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.menu_fragment_bureaux_swiperefreshlayout);
		mDossierSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.menu_fragment_dossiers_swiperefreshlayout);
		mBureauListView = (ListView) view.findViewById(R.id.menu_fragment_bureaux_listview);
		mDossierListView = (ListView) view.findViewById(R.id.menu_fragment_dossier_listview);
		mBureauEmptyView = view.findViewById(R.id.menu_fragment_bureaux_empty);
		mDossierEmptyView = view.findViewById(R.id.menu_fragment_dossier_empty);
		mDossierEmptyFiltersAlertView = view.findViewById(R.id.menu_fragment_dossier_empty_filter_alert_textview);

		// Setting up listeners, etc

		mBureauSwipeRefreshLayout.setColorSchemeResources(R.color.secondary_500, R.color.secondary_300, R.color.secondary_700);
		mBureauSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override public void onRefresh() {
				executeAsyncTask(new BureauxLoadingTask());
			}
		});

		mBureauListView.setEmptyView(mBureauEmptyView);
		mBureauListView.setAdapter(new BureauListAdapter(getActivity()));
		mBureauListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onBureauClicked(position);
			}
		});
		mBureauListView.setOnScrollListener(new AbsListView.OnScrollListener() {

			@Override public void onScrollStateChanged(AbsListView view, int scrollState) { }

			@Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				int topRowVerticalPosition = (mBureauListView.getChildCount() == 0) ? 0 : mBureauListView.getChildAt(0).getTop();
				boolean onTop = (firstVisibleItem == 0) && (topRowVerticalPosition >= 0);
				boolean isDisabled = mBureauEmptyView.getVisibility() == View.VISIBLE;
				mBureauSwipeRefreshLayout.setEnabled(isDisabled || onTop);
			}
		});

		mDossierSwipeRefreshLayout.setColorSchemeResources(R.color.secondary_500, R.color.secondary_300, R.color.secondary_700);
		mDossierSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override public void onRefresh() {
				if (mSelectedBureau != null)
					executeAsyncTask(new DossiersLoadingTask());
			}
		});

		mDossierListView.setEmptyView(mDossierEmptyView);
		mDossierListView.setAdapter(new DossierListAdapter(getActivity()));
		mDossierListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onDossierClicked(position);
			}
		});
		mDossierListView.setOnScrollListener(new AbsListView.OnScrollListener() {

			@Override public void onScrollStateChanged(AbsListView view, int scrollState) { }

			@Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				int topRowVerticalPosition = (mDossierListView.getChildCount() == 0) ? 0 : mDossierListView.getChildAt(0).getTop();
				boolean onTop = (firstVisibleItem == 0) && (topRowVerticalPosition >= 0);
				boolean isDisabled = mDossierEmptyView.getVisibility() == View.VISIBLE;
				mDossierSwipeRefreshLayout.setEnabled(isDisabled || onTop);
			}
		});

		// Restore previous state, in case of rotation

		if (mBureauList.isEmpty()) {
			mBureauListView.setVisibility(View.INVISIBLE);
			mBureauEmptyView.setVisibility(View.VISIBLE);
		}
		else {
			mBureauListView.setVisibility(View.VISIBLE);
			mBureauEmptyView.setVisibility(View.INVISIBLE);

			if (mSelectedBureau != null)
				mViewSwitcher.setDisplayedChild(1);
		}

		//

		return view;
	}

	@Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);

		// This button is not in this Fragment directly,
		// it's in the navigation drawer. But we need to inflate it anyway.

		final ImageButton filterListPortraitButton = (ImageButton) getActivity().findViewById(R.id.navigation_drawer_filters_menu_header_filters_imagebutton);
		if (filterListPortraitButton != null) {
			filterListPortraitButton.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {

					PopupMenu popup = new PopupMenu(getActivity(), filterListPortraitButton);
					inflateFilterSubMenu(popup.getMenu());
					ViewUtils.setForceShowIcon(popup);

					popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						@Override public boolean onMenuItemClick(MenuItem item) {
							return onFilterItemSelected(item);
						}
					});
					popup.show();
				}
			});
		}
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {

		// In case of signature/visa/etc, let's give a few seconds to the server
		// and refresh the content.

		switch (requestCode) {

			case FilterDialogFragment.REQUEST_CODE_FILTER:

				if (resultCode == Activity.RESULT_OK) {
					executeAsyncTask(new DossiersLoadingTask());
					getActivity().invalidateOptionsMenu();
				}

				break;

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

							((MenuFragmentListener) getActivity()).onDossierCheckedChanged(true);
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

	@Override public void onResume() {
		getActivity().invalidateOptionsMenu();
		super.onResume();

		DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
		try {
			final Dao<Dossier, Integer> dossierDao = dbHelper.getDossierDao();
			Log.i("Adrien", ">>> " + dossierDao.queryForAll());
			final Dao<Bureau, Integer> bureauDao = dbHelper.getBureauDao();
			Log.w("Adrien", ">>> " + bureauDao.queryForAll());
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called manually from parent Activity.
	 *
	 * @return true if the event was consumed.
	 */
	public boolean onBackPressed() {

		getActivity().invalidateOptionsMenu();

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

	// <editor-fold desc="ActionBar">

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		Toolbar menuToolbar = (Toolbar) getActivity().findViewById(R.id.menu_toolbar);
		if (menuToolbar != null)
			menuToolbar.inflateMenu(R.menu.menu_fragment);
	}

	@Override public void onPrepareOptionsMenu(Menu menu) {

		// Compute main filters icon visibility

		boolean isDossierList = (mViewSwitcher.getDisplayedChild() == 1);
		boolean isInLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
		boolean isListFiltered = (MyFilters.INSTANCE.getSelectedFilter() != null);

		// Refreshing navigation drawer filter button (visible in portrait)

		final ImageButton filterListPortraitButton = (ImageButton) getActivity().findViewById(R.id.navigation_drawer_filters_menu_header_filters_imagebutton);
		filterListPortraitButton.setImageResource(isListFiltered ? R.drawable.ic_filter_outline_white_24dp : R.drawable.ic_filter_remove_outline_white_24dp);
		filterListPortraitButton.setVisibility((isDossierList && !isInLandscape) ? View.VISIBLE : View.GONE);

		// Refreshing toolbar filter button (visible in landscape)

		Toolbar menuToolbar = (Toolbar) getActivity().findViewById(R.id.menu_toolbar);
		MenuItem filterItem = menuToolbar.getMenu().findItem(R.id.menu_fragment_filter_selection_item);
		filterItem.setIcon(isListFiltered ? R.drawable.ic_filter_outline_white_24dp : R.drawable.ic_filter_remove_outline_white_24dp);
		filterItem.setVisible(isDossierList && isInLandscape);

		inflateFilterSubMenu(filterItem.getSubMenu());

		//

		mDossierEmptyFiltersAlertView.setVisibility(isListFiltered ? View.VISIBLE : View.INVISIBLE);
		super.onPrepareOptionsMenu(menu);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		return onFilterItemSelected(item) || getActivity().onOptionsItemSelected(item);
	}

	// </editor-fold desc="ActionBar">

	private void inflateFilterSubMenu(@NonNull Menu menu) {

		// No filter button (if any filter is available)

		List<Filter> filterList = MyFilters.INSTANCE.getFilters(getActivity());
		menu.clear();

		if (!filterList.isEmpty()) {
			MenuItem item = menu.add(Menu.NONE, R.id.action_no_filter, 1, R.string.No_filter);
			item.setIcon(R.drawable.ic_filter_remove_outline_black_24dp);
		}

		// Inflate Filters

		mDisplayedFilters.clear();

		for (Filter filter : filterList) {

			MenuItem item = menu.add(Menu.NONE, R.id.action_filter, 2, filter.getName());
			item.setIcon(R.drawable.ic_filter_outline_black_24dp);

			mDisplayedFilters.put(item, filter);
		}

		// Add a Filter button (greyed)

		MenuItem addMenuItem = menu.add(Menu.NONE, R.id.action_add_filter, 3, R.string.Add_filter);
		SpannableString addMenuItemString = new SpannableString(addMenuItem.getTitle());
		addMenuItemString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.grey_600)), 0, addMenuItemString.length(), 0);
		addMenuItem.setTitle(addMenuItemString);
		addMenuItem.setIcon(R.drawable.ic_add_circle_grey600_24dp);
	}

	private boolean onFilterItemSelected(@NonNull MenuItem item) {

		switch (item.getItemId()) {

			case R.id.action_no_filter:

				MyFilters.INSTANCE.selectFilter(null);
				getActivity().invalidateOptionsMenu();
				executeAsyncTask(new DossiersLoadingTask());

				return true;

			case R.id.action_add_filter:

				Filter filter = MyFilters.INSTANCE.getSelectedFilter();
				if (filter == null)
					filter = new Filter();

				FilterDialogFragment filterDialog = FilterDialogFragment.newInstance(filter, mTypology);
				filterDialog.setTargetFragment(this, FilterDialogFragment.REQUEST_CODE_FILTER);
				filterDialog.show(getActivity().getFragmentManager(), FilterDialogFragment.FRAGMENT_TAG);

				return true;

			case R.id.action_filter:

				Filter currentFilter = mDisplayedFilters.get(item);
				if (currentFilter != null) {
					MyFilters.INSTANCE.selectFilter(currentFilter);
					getActivity().invalidateOptionsMenu();
					executeAsyncTask(new DossiersLoadingTask());
				}

				return true;
		}

		return false;
	}

	public Bureau getSelectedBureau() {
		return mSelectedBureau;
	}

	public HashSet<Dossier> getCheckedDossiers() {
		return mCheckedDossiers;
	}

	public void clearCheckSelection() {
		mCheckedDossiers.clear();
		((DossierListAdapter) mDossierListView.getAdapter()).notifyDataSetChanged();
	}

	public void updateBureaux(boolean forceReload) {

		if (forceReload)
			mBureauList.clear();

		if ((mBureauList.isEmpty()) && (MyAccounts.INSTANCE.getSelectedAccount() != null)) {
			mBureauListView.setVisibility(View.INVISIBLE);
			mBureauEmptyView.setVisibility(View.VISIBLE);
			executeAsyncTask(new BureauxLoadingTask());
		}
	}

	private void onBureauClicked(int position) {

		getActivity().invalidateOptionsMenu();

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

	private void onDossierClicked(int position) {

		// Reselect filter

		if (mDossierList.get(position) == mDisplayedDossier)
			return;

		// Refreshing Bureau list, to have a selected state
		// only on the selected Dossier's Bureau.

		mDisplayedBureau = mSelectedBureau;
		int displayedBureauPosition = mBureauList.indexOf(mDisplayedBureau);
		mBureauListView.setItemChecked(displayedBureauPosition, true);

		// Saving it in case of back and forth in menuing

		mDisplayedDossier = mDossierList.get(position);

		// Callback

		Dossier selectedDossier = ((DossierListAdapter) mDossierListView.getAdapter()).getItem(position);
		if (selectedDossier != null)
			((MenuFragmentListener) getActivity()).onDossierListFragmentSelected(selectedDossier, mSelectedBureau.getId());
	}

	private void executeAsyncTask(@NonNull AsyncTask<Void, ?, ?> task) {

		if (mPendingAsyncTask != null)
			mPendingAsyncTask.cancel(false);

		mPendingAsyncTask = task;
		mPendingAsyncTask.execute();
	}

	// <editor-fold desc="Interface">

	public interface MenuFragmentListener {

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

				// Download data

				try { mBureauList.addAll(RESTClient.INSTANCE.getBureaux()); }
				catch (final IParapheurException exception) { return exception; }

				// Save in Database

				DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
				try {
					final Dao<Bureau, Integer> bureauDao = dbHelper.getBureauDao();

					// This callable allow us to insert/update in loops
					// and calling db only once...
					bureauDao.callBatchTasks(new Callable<Void>() {
						@Override public Void call() throws Exception {

							for (Bureau bureau : mBureauList) {
								bureau.setSyncDate(new Date());
								bureauDao.createOrUpdate(bureau);
							}

							return null;
						}
					});
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				mBureauList.add(new Bureau(UUID.randomUUID().toString(), "bureau defaut", 0, 0));
			}

			return null;
		}

		@Override protected void onPostExecute(IParapheurException exception) {
			super.onPostExecute(exception);

			// Adrien start test

			List<String> bureauIds = new ArrayList<>();
			for (Bureau bureau : mBureauList) {
				bureauIds.add(bureau.getId());
			}

			new DossiersDownloadTask().execute(bureauIds.toArray(new String[bureauIds.size()]));

			// Adrien end test

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
				ViewUtils.crossfade(mBureauListView, mBureauEmptyView);

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
			mTypology.clear();

			if (!DeviceUtils.isDebugOffline()) {

				try { mDossierList.addAll(RESTClient.INSTANCE.getDossiers(mSelectedBureau.getId())); }
				catch (IParapheurException exception) { return exception; }

				try { mTypology.addAll(RESTClient.INSTANCE.getTypologie()); }
				catch (IParapheurException exception) { return new IParapheurException(R.string.Error_on_typology_update, exception.getLocalizedMessage()); }
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
				ViewUtils.crossfade(mDossierListView, mDossierEmptyView);

			// Error management

			if (exception != null) {
				String message = getString(exception.getResId());
				Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
			}
		}
	}

	private class DossiersDownloadTask extends AsyncTask<String, Void, IParapheurException> {

		@Override protected IParapheurException doInBackground(String... bureauIds) {

			try {

				// Downloading

				final ArrayList<Dossier> dossierList = new ArrayList<>();
				for (String bureauId : bureauIds) {

					List<Dossier> incompleteDossierList = RESTClient.INSTANCE.getDossiers(bureauId);
					for (Dossier incompleteDossier : incompleteDossierList) {

						Dossier fullDossier = RESTClient.INSTANCE.getDossier(bureauId, incompleteDossier.getId());
						fullDossier.setCircuit(RESTClient.INSTANCE.getCircuit(incompleteDossier.getId()));
						fullDossier.setParent(Bureau.findInList(mBureauList, bureauId));
						dossierList.add(fullDossier);

						for (Document document : fullDossier.getDocumentList())
							document.setParent(fullDossier);
					}
				}

				// Saving in database

				DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
				final Dao<Dossier, Integer> dossierDao = dbHelper.getDossierDao();
				final Dao<Document, Integer> documentDao = dbHelper.getDocumentDao();

				// This callable allow us to insert/update in loops
				// and calling db only once...
				dossierDao.callBatchTasks(new Callable<Void>() {
					@Override public Void call() throws Exception {

						for (Dossier dossier : dossierList) {
							dossier.setSyncDate(new Date());
							dossierDao.createOrUpdate(dossier);

							for (Document document : dossier.getDocumentList()) {
								document.setSyncDate(new Date());
								documentDao.createOrUpdate(document);
							}
						}

						return null;
					}
				});
			}
			catch (IParapheurException e) {
				return e;
			}
			catch (Exception e) {
				return new IParapheurException(-1, "DB error");
			}

			return null;
		}

		@Override protected void onPostExecute(IParapheurException e) {
			super.onPostExecute(e);
		}
	}

	private class BureauListAdapter extends ArrayAdapter<Bureau> {

		private BureauListAdapter(Context context) {
			super(context, R.layout.bureaux_list_cell, R.id.bureau_list_cell_title);
		}

		@Override public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {

			View cell = super.getView(position, convertView, parent);

			TextView lateBadgeTextView = (TextView) cell.findViewById(R.id.bureau_list_cell_late);
			TextView todoCountTextView = (TextView) cell.findViewById(R.id.bureau_list_cell_todo_count);
			TextView bureauTitleTextView = (TextView) cell.findViewById(R.id.bureau_list_cell_title);

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

				bureauTitleTextView.setText(currentBureau.getTitle());
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

		private DossierListAdapter(Context context) {
			super(context, R.layout.dossiers_list_cell, R.id.dossiers_list_item_title);
		}

		@Override public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {

			final View cellView = super.getView(position, convertView, parent);
			Dossier dossier = mDossierList.get(position);
			boolean isChecked = mCheckedDossiers.contains(dossier);

			// Text

			// FIXME : changement d'api avec toutes les actions.
			((TextView) cellView.findViewById(R.id.dossiers_list_item_extras)).setText(String.format("%s / %s", dossier.getType(), dossier.getSousType()));
			((TextView) cellView.findViewById(R.id.dossiers_list_item_title)).setText(dossier.getName());

			// CheckBox

			View checkableLayout = cellView.findViewById(R.id.dossiers_list_item_checkable_layout);

			if (Dossier.haveActions(mDossierList.get(position))) {
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
			return mDossierList.get(position); // FIXME = Adrien = OOB
		}

		@Override public int getPosition(Dossier item) {
			return mDossierList.indexOf(item);
		}

		@Override public boolean isEmpty() {
			return mDossierList.isEmpty();
		}

		private void toggleSelection(View view) {

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

					@Override public void onAnimationEnd(
							Animator animator) { ((MenuFragmentListener) getActivity()).onDossierCheckedChanged(mCheckedDossiers.isEmpty()); }

					@Override public void onAnimationCancel(Animator animator) { }

					@Override public void onAnimationRepeat(Animator animator) { }
				});
			}
			else {
				mCheckedDossiers.add(dossier);
				ViewUtils.flip(getActivity(), mainView, selectorView, null);
				((MenuFragmentListener) getActivity()).onDossierCheckedChanged(mCheckedDossiers.isEmpty());
			}
		}
	}
}