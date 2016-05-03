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
package org.adullact.iparapheur.controller.dossier.filter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Filter;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FilterDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

	public static final String FRAGMENT_TAG = "filter_dialog";
	public static final int REQUEST_CODE_FILTER = 6091220;       // Because F-I-L-T-E-R = 06-09-12-20

	private static final String PARCELABLE_FIELD_FILTER = "filter";
	private static final String EXPANDABLE_LIST_ADAPTER_NAME = "name";
	private static final String EXPANDABLE_LIST_ADAPTER_IS_CHECKED = "checked";

	// Views
	private Filter mFilter;
	private Filter mOriginalFilter;
	private EditText mTitleText;
	private Spinner mStateSpinner;
	private ExpandableListView mTypologyListView;

	// Data
	List<Map<String, String>> mTypologyListGroupData = new ArrayList<>();
	List<List<Map<String, String>>> mTypologyListChildData = new ArrayList<>();

	public FilterDialogFragment() {}

	public static FilterDialogFragment newInstance(Filter filter) {
		FilterDialogFragment f = new FilterDialogFragment();

		// Supply parameters as an arguments.
		Bundle args = new Bundle();
		args.putParcelable(PARCELABLE_FIELD_FILTER, filter);
		f.setArguments(args);

		return f;
	}

	// <editor-fold desc="LifeCycle">

	@Override public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mOriginalFilter = getArguments().getParcelable(PARCELABLE_FIELD_FILTER);
		mFilter = new Filter(mOriginalFilter);
	}

	@Override public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {

		// Creating views

		View content = View.inflate(getActivity(), R.layout.filter_dialog_fragment, null);

		mTitleText = (EditText) content.findViewById(R.id.filter_dialog_titre);
		mStateSpinner = (Spinner) content.findViewById(R.id.filter_dialog_state_spinner);
		mTypologyListView = (ExpandableListView) content.findViewById(R.id.filter_dialog_typology);
		View label = content.findViewById(R.id.filter_dialog_titre_label);

		// Inflate values

		label.requestFocus(); // Prevents keyboard popping
		mTitleText.setText(mOriginalFilter.getTitle());

		FilterStateSpinnerAdapter spinnerStateAdapter = new FilterStateSpinnerAdapter(getActivity());
		mStateSpinner.setAdapter(spinnerStateAdapter);
		mStateSpinner.setSelection(Filter.states.indexOf(mOriginalFilter.getState()), false);

		mTypologyListView.setAdapter(new TypologyGroupAdapter());

		// Build dialog

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(mFilter.getName());
		builder.setView(content);
		builder.setPositiveButton(R.string.action_filtrer, this);
		builder.setNeutralButton(R.string.enregistrer_filtre, this);
		builder.setNegativeButton(android.R.string.cancel, this);

		return builder.create();
	}

	@Override public void onStart() {
		super.onStart();

		if (mTypologyListGroupData.isEmpty() && mTypologyListChildData.isEmpty())
			new TypologyLoadingTask().execute();
	}

	// </editor-fold desc="LifeCycle">

	@Override public void onClick(DialogInterface dialog, int which) {
		switch (which) {

			case DialogInterface.BUTTON_NEGATIVE:
				getTargetFragment().onActivityResult(REQUEST_CODE_FILTER, Activity.RESULT_CANCELED, null);
				dismiss();
				break;

			case DialogInterface.BUTTON_NEUTRAL:
				saveFilter();
				createTitleDialog();
				break;

			case DialogInterface.BUTTON_POSITIVE:
				saveFilter();

				if (mOriginalFilter.getId().equals(Filter.DEFAULT_ID))
					mFilter.setId(Filter.DEFAULT_ID);

				getTargetFragment().onActivityResult(REQUEST_CODE_FILTER, Activity.RESULT_OK, null);
				break;
		}
	}

	private void createTitleDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.filtre_nom);

		// CONTENT
		final EditText content = new EditText(getActivity());
		content.setHint(R.string.Add_filter);
		content.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_black));
		if (!mOriginalFilter.getId().equals(Filter.DEFAULT_ID)) {
			content.setText(mFilter.getName());
		}
		builder.setView(content);

		// Enregistrer
		builder.setPositiveButton(R.string.enregistrer_filtre, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {

				if (TextUtils.isEmpty(String.valueOf(content.getText())))
					mFilter.setName(content.getHint().toString());
				else
					mFilter.setName(String.valueOf(content.getText()).trim());

				saveFilter();
				FilterDialogFragment.this.getTargetFragment().onActivityResult(REQUEST_CODE_FILTER, Activity.RESULT_OK, null);
			}
		});

		builder.create().show();
	}

	private void saveFilter() {

		mFilter.setTitle(mTitleText.getText().toString());
		mFilter.setState(Filter.states.get(mStateSpinner.getSelectedItemPosition()));

		mFilter.setTypes(mTypologieListAdapter.getSelectedTypes());
		mFilter.setSubTypes(mTypologieListAdapter.getSelectedSousTypes());

		MyFilters.INSTANCE.save(mFilter);
	}

	private class TypologyLoadingTask extends AsyncTask<Void, Void, IParapheurException> {

		@Override protected IParapheurException doInBackground(Void... params) {

			mTypologyListGroupData.clear();
			mTypologyListChildData.clear();

			// Retrieve data

			HashMap<String, ArrayList<String>> retrievedTypology = new HashMap<>();
			try { retrievedTypology.putAll(RESTClient.INSTANCE.getTypologie()); }
			catch (IParapheurException e) { return e; }

			// Populate values

			for (Map.Entry<String, ArrayList<String>> type : retrievedTypology.entrySet()) {

				// SubType parse

				List<Map<String, String>> subTypeMapList = new ArrayList<>();
				for (String subTypeName : type.getValue()) {
					HashMap<String, String> subTypeMap = new HashMap<>();
					subTypeMap.put(EXPANDABLE_LIST_ADAPTER_NAME, subTypeName);
					subTypeMap.put(EXPANDABLE_LIST_ADAPTER_IS_CHECKED, Boolean.FALSE.toString());
					subTypeMapList.add(subTypeMap);
				}
				mTypologyListChildData.add(subTypeMapList);

				// Type parse

				HashMap<String, String> typeMap = new HashMap<>();
				typeMap.put(EXPANDABLE_LIST_ADAPTER_NAME, type.getKey());
				typeMap.put(EXPANDABLE_LIST_ADAPTER_IS_CHECKED, Boolean.FALSE.toString());
				mTypologyListGroupData.add(typeMap);
			}

			return null;
		}

		@Override protected void onPostExecute(IParapheurException e) {
			super.onPostExecute(e);

			((SimpleExpandableListAdapter) mTypologyListView.getExpandableListAdapter()).notifyDataSetChanged();

			if (e != null)
				Toast.makeText(getActivity(), R.string.Error_on_typology_update, Toast.LENGTH_LONG).show();
		}
	}

	private class FilterStateSpinnerAdapter extends ArrayAdapter<String> {

		public FilterStateSpinnerAdapter(Context context) {
			super(context, R.layout.filter_dialog_fragment_spinner, R.id.filter_state_spinner_text);
		}

		@Override public int getCount() {
			return Filter.states.size();
		}

		@Override public String getItem(int position) {
			return Filter.statesTitles.get(Filter.states.get(position));
		}

		@Override public int getPosition(String item) {
			ArrayList<String> values = new ArrayList<>(Filter.statesTitles.values());
			return values.indexOf(item);
		}
	}

	private class TypologyGroupAdapter extends SimpleExpandableListAdapter {

		public TypologyGroupAdapter() {
			super(
					getActivity(),
					mTypologyListGroupData,
					R.layout.filter_dialog_fragment_expandablelistview_type,
					new String[]{EXPANDABLE_LIST_ADAPTER_NAME},
					new int[]{R.id.filter_dialog_fragment_expandablelistview_type_title},
					mTypologyListChildData,
					R.layout.filter_dialog_fragment_expandablelistview_subtype,
					new String[]{EXPANDABLE_LIST_ADAPTER_NAME},
					new int[]{R.id.filter_dialog_fragment_expandablelistview_subtype_title}
			);
		}

		@Override public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, final ViewGroup parent) {
			View content = super.getGroupView(groupPosition, isExpanded, convertView, parent);

			View checkboxIndeterminate = content.findViewById(R.id.filter_dialog_fragment_expandablelistview_type_checkbox_indeterminate);
			CheckBox checkbox = (CheckBox) content.findViewById(R.id.filter_dialog_fragment_expandablelistview_type_checkbox);
			Boolean isChecked = StringUtils.nullableBooleanValueOf(mTypologyListGroupData.get(groupPosition), EXPANDABLE_LIST_ADAPTER_IS_CHECKED);

			checkbox.setOnCheckedChangeListener(null);
			checkbox.setChecked((isChecked != null) && isChecked);
			checkboxIndeterminate.setVisibility((isChecked == null) ? View.VISIBLE : View.GONE);
			checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

					mTypologyListGroupData.get(groupPosition).put(EXPANDABLE_LIST_ADAPTER_IS_CHECKED, String.valueOf(isChecked));

					// Expand positive checked state

					for (Map<String, String> childData : mTypologyListChildData.get(groupPosition))
						childData.put(EXPANDABLE_LIST_ADAPTER_IS_CHECKED, String.valueOf(isChecked));

					((TypologyGroupAdapter) mTypologyListView.getExpandableListAdapter()).notifyDataSetChanged();
				}
			});

			content.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					if (mTypologyListView.isGroupExpanded(groupPosition))
						mTypologyListView.collapseGroup(groupPosition);
					else
						mTypologyListView.expandGroup(groupPosition);
				}
			});

			return content;
		}

		@Override public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			View content = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);

			CheckBox checkbox = (CheckBox) content.findViewById(R.id.filter_dialog_fragment_expandablelistview_subtype_checkbox);
			boolean isChecked = Boolean.valueOf(mTypologyListChildData.get(groupPosition).get(childPosition).get(EXPANDABLE_LIST_ADAPTER_IS_CHECKED));

			checkbox.setOnCheckedChangeListener(null);
			checkbox.setChecked(isChecked);
			checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mTypologyListChildData.get(groupPosition).get(childPosition).put(EXPANDABLE_LIST_ADAPTER_IS_CHECKED, String.valueOf(isChecked));

					// Expand checked state to parent

					boolean isEntireGroupChecked = true;
					boolean isEntireGroupNotChecked = true;

					for (Map<String, String> groupData : mTypologyListChildData.get(groupPosition)) {
						isEntireGroupChecked = isEntireGroupChecked && Boolean.valueOf(groupData.get(EXPANDABLE_LIST_ADAPTER_IS_CHECKED));
						isEntireGroupNotChecked = isEntireGroupNotChecked && !Boolean.valueOf(groupData.get(EXPANDABLE_LIST_ADAPTER_IS_CHECKED));
					}

					if (isEntireGroupChecked || isEntireGroupNotChecked)
						mTypologyListGroupData.get(groupPosition).put(EXPANDABLE_LIST_ADAPTER_IS_CHECKED, String.valueOf(isEntireGroupChecked));
					else
						mTypologyListGroupData.get(groupPosition).remove(EXPANDABLE_LIST_ADAPTER_IS_CHECKED);

					((TypologyGroupAdapter) mTypologyListView.getExpandableListAdapter()).notifyDataSetChanged();
				}
			});

			return content;
		}
	}

}
