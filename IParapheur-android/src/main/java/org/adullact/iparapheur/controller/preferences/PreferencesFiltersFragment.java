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
package org.adullact.iparapheur.controller.preferences;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.dossier.filter.MyFilters;
import org.adullact.iparapheur.model.Filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PreferencesFiltersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreferencesFiltersFragment extends Fragment {

	public static final String FRAGMENT_TAG = "preferences_filters_fragment";
	public static final String LOG_TAG = "PrefsFiltersFrag";

	private static final String LIST_FIELD_NAME = "list_field_name";
	private static final String LIST_FIELD_ID = "list_field_id";

	private ListView mFiltersListView;
	private List<Map<String, Object>> mFiltersData;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment PreferencesMenuFragment.
	 */
	public static PreferencesFiltersFragment newInstance() {
		return new PreferencesFiltersFragment();
	}

	public PreferencesFiltersFragment() {
		// Required empty public constructor
	}

	// <editor-fold desc="LifeCycle">

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		mFiltersData = new ArrayList<>();
		buildFiltersDataMap();
	}

	@Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.preferences_certificates_fragment, container, false);

		mFiltersListView = (ListView) v.findViewById(R.id.preferences_certificates_fragment_main_list);

		// Building ListAdapter

		String[] orderedFieldNames = new String[]{LIST_FIELD_NAME};
		int[] orderedFieldIds = new int[]{R.id.preferences_filters_fragment_cell_title_textview};

		SimpleAdapter filtersAdapter = new FiltersSimpleAdapter(getActivity(),
																mFiltersData,
																R.layout.preferences_filters_fragment_cell,
																orderedFieldNames,
																orderedFieldIds
		);
		mFiltersListView.setAdapter(filtersAdapter);

		//

		return v;
	}

	@Override public void onResume() {
		super.onResume();

		if (getActivity() instanceof AppCompatActivity) {
			AppCompatActivity parentActivity = (AppCompatActivity) getActivity();
			if (parentActivity.getSupportActionBar() != null)
				parentActivity.getSupportActionBar().setTitle(R.string.pref_header_filters);
		}
	}

	// </editor-fold desc="LifeCycle">

	private void onDeleteButtonClicked(int position) {

		// Delete saved Filter

		String currentFilterId = mFiltersData.get(position).get(LIST_FIELD_ID).toString();
		Filter currentFilter = MyFilters.INSTANCE.getFilter(currentFilterId);
		MyFilters.INSTANCE.delete(currentFilter);
		Log.i(LOG_TAG, "Delete filer " + currentFilter);

		// Refresh UI

		mFiltersData.remove(position);
		((SimpleAdapter) mFiltersListView.getAdapter()).notifyDataSetChanged();
		Toast.makeText(getActivity(), R.string.pref_filters_message_delete_success, Toast.LENGTH_SHORT).show();
	}

	public void buildFiltersDataMap() {

		mFiltersData.clear();

		List<Filter> filterList = MyFilters.INSTANCE.getFilters();
		for (Filter filter : filterList) {

			// Mapping results

			Map<String, Object> certificateData = new HashMap<>();
			certificateData.put(LIST_FIELD_NAME, filter.getName());
			certificateData.put(LIST_FIELD_ID, filter.getId());
			mFiltersData.add(certificateData);
		}
	}

	private class FiltersSimpleAdapter extends SimpleAdapter {

		/**
		 * Constructor
		 *
		 * @param context  The context where the View associated with this SimpleAdapter is running
		 * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
		 *                 Maps contain the data for each row, and should include all the entries specified in
		 *                 "from"
		 * @param resource Resource identifier of a view layout that defines the views for this list
		 *                 item. The layout file should include at least those named views defined in "to"
		 * @param from     A list of column names that will be added to the Map associated with each
		 *                 item.
		 * @param to       The views that should display column in the "from" parameter. These should all be
		 *                 TextViews. The first N views in this list are given the values of the first N columns
		 */
		public FiltersSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
		}

		@Override public View getView(final int position, View convertView, ViewGroup parent) {

			// We reset the Tag before recycling the view, with super, then reassign it
			// because we don't want to trigger the EditText TextChangedListeners
			// when the system recycles the views.

			final View v = super.getView(position, convertView, parent);

			final ImageButton deleteButton = (ImageButton) v.findViewById(R.id.preferences_filters_fragment_cell_delete_imagebutton);
			deleteButton.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View arg0) {
					onDeleteButtonClicked(position);
				}
			});

			return v;
		}
	}
}
