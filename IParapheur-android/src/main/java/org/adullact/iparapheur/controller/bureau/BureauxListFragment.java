package org.adullact.iparapheur.controller.bureau;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.utils.DeviceUtils;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.LoadingTask;
import org.adullact.iparapheur.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BureauxListFragment extends Fragment implements LoadingTask.DataChangeListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

	public static final String TAG = "bureaux_list_fragment";
	private BureauListFragmentListener listener;

	private List<Bureau> mBureaux;                                // List of mBureaux currently displayed in this Fragment
	private int selectedBureau = ListView.INVALID_POSITION;       // The currently selected dossier
	private ListView listView;                                    // ListView used to show the mBureaux of the currently selected account
	private SwipeRefreshLayout swipeRefreshLayout;                // Swipe refresh layout on top of the list view
	private View mSpinnerProgressView;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof BureauListFragmentListener))
			throw new IllegalStateException("Activity must implement BureauSelectedListener.");

		listener = (BureauListFragmentListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.bureaux_list_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		listView = (ListView) view.findViewById(R.id.bureaux_list);
		listView.setOnItemClickListener(this);
		listView.setEmptyView(view.findViewById(android.R.id.empty));

		swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.bureaux_refresh_layout);
		swipeRefreshLayout.setColorSchemeResources(R.color.secondary_500, R.color.secondary_300, R.color.secondary_700);

		mSpinnerProgressView = view.findViewById(android.R.id.progress);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listView.setAdapter(new BureauListAdapter(getActivity()));
		swipeRefreshLayout.setOnRefreshListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();

		swipeRefreshLayout.setVisibility(View.INVISIBLE);
		mSpinnerProgressView.setVisibility(View.VISIBLE);
		updateBureaux(true);
	}

	public void updateBureaux(boolean forceReload) {
		if (forceReload)
			this.mBureaux = null;

		if ((mBureaux == null) && (MyAccounts.INSTANCE.getSelectedAccount() != null)) {
			swipeRefreshLayout.setVisibility(View.INVISIBLE);
			mSpinnerProgressView.setVisibility(View.VISIBLE);
			new BureauxLoadingTask(getActivity(), this).execute();
		}

		onDataChanged();
	}

	// <editor-fold desc="OnItemClickListener">

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position != selectedBureau)
			listener.onBureauListFragmentSelected(mBureaux.get(position).getId());
	}

	// </editor-fold desc="OnItemClickListener">

	// <editor-fold desc="DataChangeListener">
	@Override
	public void onDataChanged() {
		((BureauListAdapter) listView.getAdapter()).notifyDataSetChanged();

		// if a bureau was previously selected, we have to notify the parent
		// activity that the data has changed, so the activity remove the previously selected
		// dossiers list and details
		listener.onBureauListFragmentSelected(null);
	}

	// </editor-fold desc="DataChangeListener">

	// <editor-fold desc="SwipeRefreshLayout">
	@Override
	public void onRefresh() {
		new BureauxLoadingTask(getActivity(), this).execute();
	}

	// </editor-fold desc="SwipeRefreshLayout">

	/**
	 * The parent activity must implement this interface.
	 * Used to notify the activity on bureaux changes
	 */
	public interface BureauListFragmentListener {

		/**
		 * Called when the bureau identified by the id passed in parameter has been
		 * selected by the user or when data changes (id will be null)
		 *
		 * @param id the bureau id or null if none is selected (data changed)
		 */
		void onBureauListFragmentSelected(@Nullable String id);
	}

	private class BureauxLoadingTask extends LoadingTask {

		public BureauxLoadingTask(Activity activity, DataChangeListener listener) {
			super(activity, listener);
		}

		@Override
		protected void load(String... params) throws IParapheurException {
			// Check if this task is cancelled as often as possible.
			if (isCancelled())
				return;

			if (!DeviceUtils.isDebugOffline()) {
				try {
					mBureaux = RESTClient.INSTANCE.getBureaux();
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
				mBureaux = new ArrayList<>();
				mBureaux.add(new Bureau(UUID.randomUUID().toString(), "bureau defaut"));
			}
		}

		@Override
		protected void showProgress() {
			if (isAdded())
				if (mSpinnerProgressView.getVisibility() != View.VISIBLE)
					swipeRefreshLayout.setRefreshing(true);
		}

		@Override
		protected void hideProgress() {
			if (isAdded()) {

				if (mSpinnerProgressView.getVisibility() == View.VISIBLE)
					ViewUtils.crossfade(getActivity(), swipeRefreshLayout, mSpinnerProgressView);

				if (swipeRefreshLayout.isRefreshing())
					swipeRefreshLayout.setRefreshing(false);
			}
		}
	}

	private class BureauListAdapter extends ArrayAdapter<Bureau> {

		public BureauListAdapter(Context context) {
			super(context, R.layout.bureaux_list_cell, R.id.bureau_list_cell_title);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

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

		@Override
		public int getCount() {
			return (mBureaux == null) ? 0 : mBureaux.size();
		}

		@Override
		public Bureau getItem(int position) {
			return mBureaux.get(position);
		}

		@Override
		public int getPosition(Bureau item) {
			return mBureaux.indexOf(item);
		}

		@Override
		public boolean isEmpty() {
			return (mBureaux == null) || mBureaux.isEmpty();
		}
	}

}