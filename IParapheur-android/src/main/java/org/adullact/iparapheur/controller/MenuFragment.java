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
import android.app.DialogFragment;
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
import com.j256.ormlite.dao.ForeignCollection;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.dossier.DownloadDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.RejectDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.SignatureDialogFragment;
import org.adullact.iparapheur.controller.dossier.action.VisaDialogFragment;
import org.adullact.iparapheur.controller.dossier.filter.FilterDialogFragment;
import org.adullact.iparapheur.controller.dossier.filter.MyFilters;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.database.DatabaseHelper;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.Filter;
import org.adullact.iparapheur.model.ParapheurType;
import org.adullact.iparapheur.utils.AccountUtils;
import org.adullact.iparapheur.utils.BureauUtils;
import org.adullact.iparapheur.utils.DeviceUtils;
import org.adullact.iparapheur.utils.DocumentUtils;
import org.adullact.iparapheur.utils.DossierUtils;
import org.adullact.iparapheur.utils.FileUtils;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.StringUtils;
import org.adullact.iparapheur.utils.ViewUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

	private static final String LOG_TAG = "MenuFragment";
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
	private AsyncTask<Account, ?, ?> mPendingAsyncTask = null;

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

		// These buttons aren't directly in this Fragment,
		// they are's in the navigation drawer. But we need to inflate them anyway.

		final ImageButton filterListButton = (ImageButton) getActivity().findViewById(R.id.navigation_drawer_filters_menu_header_filters_imagebutton);
		if (filterListButton != null) {
			filterListButton.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {

					PopupMenu popup = new PopupMenu(getActivity(), filterListButton);
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

		final ImageButton downloadButton = (ImageButton) getActivity().findViewById(R.id.navigation_drawer_filters_menu_header_download_imagebutton);
		if (downloadButton != null) {
			downloadButton.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					onDownloadItemSelected();
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

		Log.i("Adrien", "account :: " + AccountUtils.SELECTED_ACCOUNT);

		if (mBureauList.isEmpty())
			updateBureaux(true);
	}

	@Override public void onResume() {
		getActivity().invalidateOptionsMenu();
		super.onResume();
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

		Toolbar menuToolbar = (Toolbar) getActivity().findViewById(R.id.menu_toolbar);

		// Compute main  icon visibility

		boolean isDossierList = (mViewSwitcher.getDisplayedChild() == 1);
		boolean isBureauList = (mViewSwitcher.getDisplayedChild() == 0);
		boolean isInLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
		boolean isListFiltered = (MyFilters.INSTANCE.getSelectedFilter() != null);
		boolean hasBureaux = (!mBureauList.isEmpty()) && (!mBureauSwipeRefreshLayout.isRefreshing());
		boolean hasDossiers = (!mDossierList.isEmpty()) && (!mDossierSwipeRefreshLayout.isRefreshing());

		// Download visibility (visible in Landscape)

		MenuItem downloadItem = menuToolbar.getMenu().findItem(R.id.menu_fragment_download_item);
		downloadItem.setVisible(isBureauList && isInLandscape && hasBureaux);

		// Download visibility (visible in Portrait)

		final ImageButton downloadPortraitButton = (ImageButton) getActivity().findViewById(R.id.navigation_drawer_filters_menu_header_download_imagebutton);
		downloadPortraitButton.setVisibility((isBureauList && !isInLandscape && hasBureaux) ? View.VISIBLE : View.GONE);

		// Refreshing navigation drawer filter button (visible in portrait)

		final ImageButton filterListPortraitButton = (ImageButton) getActivity().findViewById(R.id.navigation_drawer_filters_menu_header_filters_imagebutton);
		filterListPortraitButton.setImageResource(isListFiltered ? R.drawable.ic_filter_outline_white_24dp : R.drawable.ic_filter_remove_outline_white_24dp);
		filterListPortraitButton.setVisibility((isDossierList && (!isInLandscape) && hasDossiers) ? View.VISIBLE : View.GONE);

		// Refreshing toolbar filter button (visible in landscape)

		MenuItem filterItem = menuToolbar.getMenu().findItem(R.id.menu_fragment_filter_selection_item);
		filterItem.setIcon(isListFiltered ? R.drawable.ic_filter_outline_white_24dp : R.drawable.ic_filter_remove_outline_white_24dp);
		filterItem.setVisible(isDossierList && isInLandscape && hasDossiers);

		inflateFilterSubMenu(filterItem.getSubMenu());

		//

		mDossierEmptyFiltersAlertView.setVisibility(isListFiltered ? View.VISIBLE : View.INVISIBLE);
		super.onPrepareOptionsMenu(menu);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {

		if (Arrays.asList(R.id.action_no_filter, R.id.action_add_filter, R.id.action_filter).contains(item.getItemId())) {
			if (!DeviceUtils.isConnected(getActivity())) {
				Toast.makeText(getActivity(), R.string.Action_unavailable_offline, Toast.LENGTH_LONG).show();
				return true;
			}
		}

		if (item.getItemId() == R.id.menu_fragment_filter_selection_item)
			return onFilterItemSelected(item);

		if (item.getItemId() == R.id.menu_fragment_download_item)
			return onDownloadItemSelected();

		return getActivity().onOptionsItemSelected(item);
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

		if (Arrays.asList(R.id.action_no_filter, R.id.action_add_filter, R.id.action_filter).contains(item.getItemId())) {
			if (!DeviceUtils.isConnected(getActivity())) {
				Toast.makeText(getActivity(), R.string.Action_unavailable_offline, Toast.LENGTH_LONG).show();
				return true;
			}
		}

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

	private boolean onDownloadItemSelected() {

		if (!DeviceUtils.isConnected(getActivity())) {
			Toast.makeText(getActivity(), R.string.Action_unavailable_offline, Toast.LENGTH_LONG).show();
			return true;
		}
		else if (getFragmentManager().findFragmentByTag(DownloadDialogFragment.FRAGMENT_TAG) == null) {
			DialogFragment actionDialog = DownloadDialogFragment.newInstance(AccountUtils.SELECTED_ACCOUNT);
			actionDialog.show(getFragmentManager(), DownloadDialogFragment.FRAGMENT_TAG);
		}

		return true;
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

		if ((mBureauList.isEmpty()) && (AccountUtils.SELECTED_ACCOUNT != null)) {
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

			// Cleanup previous views data

			mDossierList.clear();
			((DossierListAdapter) mDossierListView.getAdapter()).notifyDataSetChanged();

			// Update bureau

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

	private void executeAsyncTask(@NonNull AsyncTask<Account, ?, ?> task) {

		if (mPendingAsyncTask != null)
			mPendingAsyncTask.cancel(false);

		mPendingAsyncTask = task;
		mPendingAsyncTask.execute(AccountUtils.SELECTED_ACCOUNT);
	}

	// <editor-fold desc="Interface">

	public interface MenuFragmentListener {

		void onDossierListFragmentSelected(@NonNull Dossier dossier, @NonNull String bureauId);

		void onDossierCheckedChanged(boolean forceClose);
	}

	// </editor-fold desc="Interface">

	private class BureauxLoadingTask extends AsyncTask<Account, Void, IParapheurException> {

		private Account mCurrentAccount;

		@Override protected void onPreExecute() {
			super.onPreExecute();
			mBureauSwipeRefreshLayout.setRefreshing(true);
		}

		@Override protected IParapheurException doInBackground(Account... params) {

			mCurrentAccount = params[0];
			if (mCurrentAccount == null)
				return new IParapheurException(-1, "No account selected");

			// Update Account from DB

			final DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
			Dao<Account, Integer> accountDao = null;
			try {
				accountDao = dbHelper.getAccountDao();
				String selectedId = mCurrentAccount.getId();
				List<Account> fetchedAccountList = accountDao.queryBuilder().where().eq(Account.DB_FIELD_ID, selectedId).query();

				if (fetchedAccountList.size() > 0)
					mCurrentAccount = fetchedAccountList.get(0);
			}
			catch (SQLException e) {
				e.printStackTrace();
			}

			// Default case

			if ((mCurrentAccount == null) || (accountDao == null))
				return null;

			//

			if (DeviceUtils.isConnected(getActivity())) {

				// Check Api version

				Integer currentApi = mCurrentAccount.getApiVersion();
				if ((currentApi == null) || (currentApi < RESTClient.API_VERSION_MAX)) {
					Log.d(LOG_TAG, "current API : " + currentApi + ", checking for update...");

					int newApi = 0;
					try { newApi = RESTClient.INSTANCE.getApiVersion(mCurrentAccount); }
					catch (IParapheurException e) { e.printStackTrace(); }

					if (newApi > 0) {
						mCurrentAccount.setApiVersion(newApi);
						try { accountDao.createOrUpdate(mCurrentAccount); }
						catch (SQLException e) { e.printStackTrace(); }
					}
				}

				if (mCurrentAccount.getApiVersion() == null)
					return null;

				// Download data

				final List<Bureau> bureauList = new ArrayList<>();

				try { bureauList.addAll(RESTClient.INSTANCE.getBureaux(mCurrentAccount)); }
				catch (final IParapheurException exception) { return exception; }

				mBureauList.clear();
				mBureauList.addAll(bureauList);

				// Cleanup and save in Database

				try {

					dbHelper.getAccountDao().update(mCurrentAccount);

					final List<Bureau> bureauxToDelete = BureauUtils.getDeletableBureauList(mCurrentAccount, bureauList);
					final List<Dossier> dossierToDeleteList = DossierUtils.getAllChildrenFrom(bureauxToDelete);
					final List<Document> documentToDeleteList = DocumentUtils.getAllChildrenFrom(dossierToDeleteList);

					Log.d(LOG_TAG, "delete Bureaux   : " + bureauxToDelete);
					Log.d(LOG_TAG, "delete Dossiers  : " + dossierToDeleteList);
					Log.d(LOG_TAG, "delete Documents : " + documentToDeleteList);

					dbHelper.getBureauDao().callBatchTasks(new Callable<Void>() {
						@Override public Void call() throws Exception {

							dbHelper.getDocumentDao().delete(documentToDeleteList);
							dbHelper.getDossierDao().delete(dossierToDeleteList);
							dbHelper.getBureauDao().delete(bureauxToDelete);

							for (Bureau newBureau : bureauList) {
								newBureau.setSyncDate(new Date());
								newBureau.setParent(mCurrentAccount);
								dbHelper.getBureauDao().createOrUpdate(newBureau);
							}

							return null;
						}
					});

					// Cleanup files

					for (Document documentToDelete : documentToDeleteList)
						//noinspection ResultOfMethodCallIgnored
						DocumentUtils.getFile(getActivity(), documentToDelete.getParent(), documentToDelete).delete();

					for (Dossier dossierToDelete : dossierToDeleteList)
						//noinspection ResultOfMethodCallIgnored
						FileUtils.getDirectoryForDossier(getActivity(), dossierToDelete).delete();
				}
				catch (Exception e) { e.printStackTrace(); }
			}
			else {

				// Offline backup

				mBureauList.clear();
				ForeignCollection<Bureau> bureauForeignList = mCurrentAccount.getChildrenBureaux();
				mBureauList.addAll(bureauForeignList);
			}

			return null;
		}

		@Override protected void onPostExecute(IParapheurException exception) {
			super.onPostExecute(exception);

			mPendingAsyncTask = null;

			if (isCancelled())
				return;

			Collections.sort(mBureauList, BureauUtils.buildAlphabeticalComparator());
			((BureauListAdapter) mBureauListView.getAdapter()).notifyDataSetChanged();

			// Retrieving previous state

			int displayedBureauPosition = mBureauList.indexOf(mDisplayedBureau);
			mBureauListView.setItemChecked(displayedBureauPosition, true);

			// Refreshing views state

			getActivity().invalidateOptionsMenu();
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

	private class DossiersLoadingTask extends AsyncTask<Account, Void, IParapheurException> {

		private Account mCurrentAccount;

		@Override protected void onPreExecute() {
			super.onPreExecute();
			mDossierSwipeRefreshLayout.setRefreshing(true);
		}

		@Override protected IParapheurException doInBackground(Account... params) {

			mTypology.clear();
			mCurrentAccount = AccountUtils.SELECTED_ACCOUNT;
			final DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
			Filter currentFilter = MyFilters.INSTANCE.getSelectedFilter();

			if (DeviceUtils.isConnected(getActivity())) {

				List<Dossier> fetchedDossierList = new ArrayList<>();
				try { fetchedDossierList.addAll(RESTClient.INSTANCE.getDossiers(mCurrentAccount, mSelectedBureau.getId(), currentFilter)); }
				catch (IParapheurException exception) { return exception; }

				mDossierList.clear();
				mDossierList.addAll(fetchedDossierList);

				try { mTypology.addAll(RESTClient.INSTANCE.getTypologie(mCurrentAccount)); }
				catch (IParapheurException exception) { return new IParapheurException(R.string.Error_on_typology_update, exception.getLocalizedMessage()); }

				// Cleanup data

				if (currentFilter == null) {

					final List<Dossier> dossierToDeleteList = DossierUtils.getDeletableDossierList(Collections.singletonList(mSelectedBureau),
																								   fetchedDossierList
					);
					final List<Document> documentToDeleteList = DocumentUtils.getAllChildrenFrom(dossierToDeleteList);

					Log.d("BureauxLoadingTask", "delete Dossiers  : " + dossierToDeleteList);
					Log.d("BureauxLoadingTask", "delete Documents : " + documentToDeleteList);

					// Cleanup DB

					try {
						dbHelper.getBureauDao().callBatchTasks(new Callable<Void>() {
							@Override public Void call() throws Exception {
								dbHelper.getDocumentDao().delete(documentToDeleteList);
								dbHelper.getDossierDao().delete(dossierToDeleteList);
								return null;
							}
						});
					}
					catch (Exception exception) { return new IParapheurException(R.string.Error_on_typology_update, exception.getLocalizedMessage()); }

					// Cleanup files

					for (Document documentToDelete : documentToDeleteList)
						//noinspection ResultOfMethodCallIgnored
						DocumentUtils.getFile(getActivity(), documentToDelete.getParent(), documentToDelete).delete();

					for (Dossier dossierToDelete : dossierToDeleteList)
						//noinspection ResultOfMethodCallIgnored
						FileUtils.getDirectoryForDossier(getActivity(), dossierToDelete).delete();
				}
			}
			else {  // Offline backup

				try {

					// Update bureau from DB

					Dao<Bureau, Integer> bureauDao = dbHelper.getBureauDao();
					String selectedId = mSelectedBureau.getId();
					List<Bureau> fetchedBureauList = bureauDao.queryBuilder().where().eq(Bureau.DB_FIELD_ID, selectedId).query();

					if (fetchedBureauList.size() > 0)
						mSelectedBureau = fetchedBureauList.get(0);
					else
						return null;

					//Update List

					mDossierList.clear();
					mDossierList.addAll(mSelectedBureau.getChildrenDossiers());
				}
				catch (SQLException e) { e.printStackTrace(); }
			}

			return null;
		}

		@Override protected void onPostExecute(IParapheurException exception) {
			super.onPostExecute(exception);

			mPendingAsyncTask = null;
			if (isCancelled())
				return;

			Collections.sort(mDossierList, DossierUtils.buildCreationDateComparator());
			((DossierListAdapter) mDossierListView.getAdapter()).notifyDataSetChanged();

			// Retrieving previous state

			int displayedDossierPosition = mDossierList.indexOf(mDisplayedDossier);
			mDossierListView.setItemChecked(displayedDossierPosition, true);

			// Refreshing views state

			getActivity().invalidateOptionsMenu();
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

	private class BureauListAdapter extends ArrayAdapter<Bureau> {

		private BureauListAdapter(Context context) {
			super(context, R.layout.bureaux_list_cell, R.id.bureau_list_cell_title);
		}

		@Override public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {

			View cell = super.getView(position, convertView, parent);

			TextView bureauTitleTextView = (TextView) cell.findViewById(R.id.bureau_list_cell_title);
			TextView todoCountTextView = (TextView) cell.findViewById(R.id.bureau_list_cell_todo);
			TextView detailsTextView = (TextView) cell.findViewById(R.id.bureau_list_cell_details);

			Bureau currentBureau = getItem(position);
			if (currentBureau != null) {

				bureauTitleTextView.setText(currentBureau.getTitle());

				// Details text

				if (!DeviceUtils.isConnected(getActivity())) {

					// Color

					if (currentBureau.getLateCount() > 0)
						todoCountTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_500));
					else
						todoCountTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_black_secondary));

					// To do text

					if (currentBureau.getTodoCount() == 0)
						todoCountTextView.setText(R.string.no_dossier);
					else if ((currentBureau.getTodoCount() == 1) && (currentBureau.getLateCount() == 0))
						todoCountTextView.setText(R.string.one_dossier);
					else if ((currentBureau.getTodoCount() == 1) && (currentBureau.getLateCount() > 0))
						todoCountTextView.setText(R.string.one_late_dossier);
					else if (currentBureau.getLateCount() == currentBureau.getTodoCount())
						todoCountTextView.setText(getString(R.string.nb_late_dossiers, currentBureau.getTodoCount()));
					else if (currentBureau.getLateCount() > 0)
						todoCountTextView.setText(getString(R.string.nb_dossiers_nb_late, currentBureau.getTodoCount(), currentBureau.getLateCount()));
					else
						todoCountTextView.setText(getString(R.string.nb_dossiers, currentBureau.getTodoCount()));

					// Sync date

					detailsTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_black_secondary));
					detailsTextView.setText(getString(R.string.Sync_date,
													  StringUtils.getVerySmallDate(currentBureau.getSyncDate()),
													  StringUtils.getSmallTime(currentBureau.getSyncDate())
					));
				}
				else {

					// Color

					todoCountTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_black_secondary));

					if (currentBureau.getLateCount() > 0)
						detailsTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_500));
					else
						detailsTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_black_secondary));

					// To do text

					if (currentBureau.getTodoCount() == 0)
						todoCountTextView.setText(R.string.no_dossier);
					else if (currentBureau.getTodoCount() == 1)
						todoCountTextView.setText(R.string.one_dossier);
					else
						todoCountTextView.setText(getString(R.string.nb_dossiers, currentBureau.getTodoCount()));

					// Late text

					if (currentBureau.getLateCount() == 0)
						detailsTextView.setText(R.string.no_late_dossier);
					else if (currentBureau.getLateCount() == 1)
						detailsTextView.setText(R.string.one_late_dossier);
					else
						detailsTextView.setText(getString(R.string.nb_late_dossiers, currentBureau.getLateCount()));
				}
			}

			return cell;
		}

		@Override public int getCount() {
			return mBureauList.size();
		}

		@Override public Bureau getItem(int position) {
			return mBureauList.get(position); // FIXME : OOB
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

			TextView nameTextView = (TextView) cellView.findViewById(R.id.dossiers_list_item_title);
			TextView typeTextView = (TextView) cellView.findViewById(R.id.dossiers_list_item_typology);
			TextView dateTextView = (TextView) cellView.findViewById(R.id.dossiers_list_item_date);

			String typologyText = String.format("%s / %s", dossier.getType(), dossier.getSousType());

			typeTextView.setText(typologyText);
			nameTextView.setText(dossier.getName());

			// Date text

			dateTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_black_secondary));

			if ((dossier.getDateLimite() != null) && (new Date().after(dossier.getDateLimite()))) {
				String lateText = getString(R.string.Late_since, StringUtils.getLocalizedSmallDate(dossier.getDateLimite()));
				dateTextView.setText(lateText);
				dateTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_500));
			}
			else if (dossier.getSyncDate() != null) {
				String syncText = getString(R.string.Sync_date,
											StringUtils.getVerySmallDate(dossier.getSyncDate()),
											StringUtils.getSmallTime(dossier.getSyncDate())
				);
				dateTextView.setText(syncText);
			}
			else if (dossier.getDateCreation() != null) {
				String emitSinceText = getString(R.string.Emit_since, StringUtils.getLocalizedSmallDate(dossier.getDateCreation()));
				dateTextView.setText(emitSinceText);
			}
			else {
				typeTextView.setText(dossier.getType());
				dateTextView.setText(dossier.getSousType());
			}

			// CheckBox

			View checkableLayout = cellView.findViewById(R.id.dossiers_list_item_checkable_layout);

			if (DossierUtils.haveActions(mDossierList.get(position))) {
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
					else if (actionName.contentEquals(getString(R.string.action_seal)))
						iconImageView.setImageResource(R.drawable.ic_cachet_color_24dp);
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

					@Override public void onAnimationEnd(Animator animator) {
						((MenuFragmentListener) getActivity()).onDossierCheckedChanged(mCheckedDossiers.isEmpty());
					}

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