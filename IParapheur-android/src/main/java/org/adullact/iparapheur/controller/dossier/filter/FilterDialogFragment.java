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
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.preferences.ChooseFilterNameDialogFragment;
import org.adullact.iparapheur.model.Filter;
import org.adullact.iparapheur.model.ParapheurType;
import org.adullact.iparapheur.model.State;
import org.adullact.iparapheur.utils.StringUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FilterDialogFragment extends DialogFragment {

	public static final String FRAGMENT_TAG = "filter_dialog";
	public static final int REQUEST_CODE_FILTER = 6091220;       // Because F-I-L-T-E-R = 06-09-12-20

	private static final String PARCELABLE_FIELD_FILTER = "filter";
	private static final String BUNDLE_INSTANCE_STATE_GROUP_DATA = "group_data";
	private static final String BUNDLE_INSTANCE_STATE_CHILD_DATA = "child_data";
	private static final String EXPANDABLE_LIST_ADAPTER_NAME = "name";
	private static final String EXPANDABLE_LIST_ADAPTER_IS_CHECKED = "checked";

	// Views
	private Filter mFilter;
	private EditText mTitleText;
	private Spinner mStateSpinner;
	private ExpandableListView mTypologyListView;

	// Data
	List<Map<String, String>> mTypologyListGroupData = new ArrayList<>();
	List<List<Map<String, String>>> mTypologyListChildData = new ArrayList<>();

	public FilterDialogFragment() {}

	public static FilterDialogFragment newInstance(Filter filter, List<ParapheurType> typology) {

		FilterDialogFragment fragment = new FilterDialogFragment();
		List<Map<String, String>> typologyListGroupData = new ArrayList<>();
		List<List<Map<String, String>>> typologyListChildData = new ArrayList<>();

		// Parse Typology

		for (ParapheurType type : typology) {

			// SubType parse

			List<Map<String, String>> subTypeMapList = new ArrayList<>();
			for (String subTypeName : type.getSubTypes()) {
				HashMap<String, String> subTypeMap = new HashMap<>();
				subTypeMap.put(EXPANDABLE_LIST_ADAPTER_NAME, subTypeName);
				subTypeMap.put(EXPANDABLE_LIST_ADAPTER_IS_CHECKED, Boolean.FALSE.toString());
				subTypeMapList.add(subTypeMap);
			}
			typologyListChildData.add(subTypeMapList);

			// Type parse

			HashMap<String, String> typeMap = new HashMap<>();
			typeMap.put(EXPANDABLE_LIST_ADAPTER_NAME, type.getName());
			typeMap.put(EXPANDABLE_LIST_ADAPTER_IS_CHECKED, Boolean.FALSE.toString());
			typologyListGroupData.add(typeMap);
		}

		// Supply parameters as an arguments.

		Gson gson = new Gson();
		Bundle args = new Bundle();
		args.putParcelable(PARCELABLE_FIELD_FILTER, filter);
		args.putString(BUNDLE_INSTANCE_STATE_GROUP_DATA, gson.toJson(typologyListGroupData));
		args.putString(BUNDLE_INSTANCE_STATE_CHILD_DATA, gson.toJson(typologyListChildData));
		fragment.setArguments(args);

		return fragment;
	}

	// <editor-fold desc="LifeCycle">

	@Override public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mFilter = new Filter();

		// Retrieve Typology

		String groupDataJson;
		String childpDataJson;

		if (savedInstanceState != null) {
			groupDataJson = savedInstanceState.getString(BUNDLE_INSTANCE_STATE_GROUP_DATA);
			childpDataJson = savedInstanceState.getString(BUNDLE_INSTANCE_STATE_CHILD_DATA);
		}
		else {
			groupDataJson = getArguments().getString(BUNDLE_INSTANCE_STATE_GROUP_DATA);
			childpDataJson = getArguments().getString(BUNDLE_INSTANCE_STATE_CHILD_DATA);
		}

		Gson gson = new Gson();
		Type groupDataType = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		Type childDataType = new TypeToken<ArrayList<List<Map<String, String>>>>() {}.getType();
		mTypologyListGroupData = gson.fromJson(groupDataJson, groupDataType);
		mTypologyListChildData = gson.fromJson(childpDataJson, childDataType);
	}

	@Override public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {

		// Creating views

		View content = View.inflate(getActivity(), R.layout.filter_dialog_fragment, null);

		mTitleText = (EditText) content.findViewById(R.id.filter_dialog_titre);
		mStateSpinner = (Spinner) content.findViewById(R.id.filter_dialog_state_spinner);
		mTypologyListView = (ExpandableListView) content.findViewById(R.id.filter_dialog_typology);
		View label = content.findViewById(R.id.filter_dialog_titre_label);

		// Inflate values

		FilterStateSpinnerAdapter spinnerStateAdapter = new FilterStateSpinnerAdapter(getActivity());
		mStateSpinner.setAdapter(spinnerStateAdapter);
		mStateSpinner.setSelection(State.A_TRAITER.ordinal(), false);

		mTypologyListView.setAdapter(new TypologySimpleExpandableListAdapter());

		// Build dialog

		label.requestFocus(); // Prevents keyboard popping

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.New_filter);
		builder.setView(content);
		builder.setPositiveButton(R.string.action_filtrer, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				onFilterButtonClicked();
			}
		});
		builder.setNeutralButton(R.string.enregistrer_filtre, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				// Do nothing here because we override this button in the onStart() to change the close behaviour.
				// However, we still need this because on older versions of Android :
				// unless we pass a handler the button doesn't get instantiated
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				onNegativeButtonClicked();
			}
		});

		return builder.create();
	}

	@Override public void onStart() {
		super.onStart();

		// Overriding the AlertDialog.Builder#setPositiveButton
		// To be able to manage a click without dismissing the popup.

		android.support.v7.app.AlertDialog dialog = (android.support.v7.app.AlertDialog) getDialog();
		if (dialog == null)
			return;

		Button signButton = dialog.getButton(Dialog.BUTTON_NEUTRAL);
		signButton.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				onSaveButtonClicked();
			}
		});
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if ((requestCode == ChooseFilterNameDialogFragment.REQUEST_CODE_FILTER_NAME) && (resultCode == Activity.RESULT_OK)) {
			String name = data.getStringExtra(ChooseFilterNameDialogFragment.RESULT_BUNDLE_TITLE);
			saveFilterAndDimsiss(name);
			return;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		Gson gson = new Gson();
		outState.putString(BUNDLE_INSTANCE_STATE_GROUP_DATA, gson.toJson(mTypologyListGroupData));
		outState.putString(BUNDLE_INSTANCE_STATE_CHILD_DATA, gson.toJson(mTypologyListChildData));
	}

	// </editor-fold desc="LifeCycle">

	private void onNegativeButtonClicked() {
		getTargetFragment().onActivityResult(REQUEST_CODE_FILTER, Activity.RESULT_CANCELED, null);
		dismiss();
	}

	private void onSaveButtonClicked() {

//		if (TextUtils.isEmpty(mFilter.getName()))
		showChooseNameDialog();
//		else
//			saveFilter(mFilter.getName());
	}

	private void onFilterButtonClicked() {

		refreshCurrentFilter(null);
		MyFilters.INSTANCE.selectFilter(mFilter);

		getTargetFragment().onActivityResult(REQUEST_CODE_FILTER, Activity.RESULT_OK, null);
	}

	private void showChooseNameDialog() {

		ChooseFilterNameDialogFragment chooseNameDialogFragment = ChooseFilterNameDialogFragment.newInstance();
		chooseNameDialogFragment.setTargetFragment(this, ChooseFilterNameDialogFragment.REQUEST_CODE_FILTER_NAME);
		chooseNameDialogFragment.show(getActivity().getFragmentManager(), ChooseFilterNameDialogFragment.FRAGMENT_TAG);
	}

	private void refreshCurrentFilter(@Nullable String name) {

		if (name != null)
			mFilter.setName(name);

		mFilter.setTitle(mTitleText.getText().toString());
		mFilter.setState(State.values()[mStateSpinner.getSelectedItemPosition()]);

		ArrayList<String> selectedTypes = new ArrayList<>();
		for (Map<String, String> typeData : mTypologyListGroupData)
			if (Boolean.valueOf(typeData.get(EXPANDABLE_LIST_ADAPTER_IS_CHECKED)))
				selectedTypes.add(typeData.get(EXPANDABLE_LIST_ADAPTER_NAME));

		ArrayList<String> selectedSubTypes = new ArrayList<>();
		for (List<Map<String, String>> subtypeDataList : mTypologyListChildData)
			for (Map<String, String> subtypeData : subtypeDataList)
				if (Boolean.valueOf(subtypeData.get(EXPANDABLE_LIST_ADAPTER_IS_CHECKED)))
					selectedSubTypes.add(subtypeData.get(EXPANDABLE_LIST_ADAPTER_NAME));

		mFilter.setTypeList(selectedTypes);
		mFilter.setSubTypeList(selectedSubTypes);
	}

	private void saveFilterAndDimsiss(@NonNull String name) {

		refreshCurrentFilter(name);
		MyFilters.INSTANCE.save(mFilter);
		MyFilters.INSTANCE.selectFilter(mFilter);

		getTargetFragment().onActivityResult(REQUEST_CODE_FILTER, Activity.RESULT_OK, null);
		dismiss();
	}

	private class FilterStateSpinnerAdapter extends ArrayAdapter<String> {

		public FilterStateSpinnerAdapter(Context context) {
			super(context, R.layout.filter_dialog_fragment_spinner, R.id.filter_state_spinner_text);
		}

		@Override public int getCount() {
			return State.values().length;
		}

		@Override public String getItem(int position) {
			return getString(State.values()[position].getNameRes());
		}

		@Override public int getPosition(String item) {
			State state = State.fromName(getActivity(), item);
			return (state != null) ? state.ordinal() : Spinner.INVALID_POSITION;
		}
	}

	private class TypologySimpleExpandableListAdapter extends SimpleExpandableListAdapter {

		public TypologySimpleExpandableListAdapter() {
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

					((TypologySimpleExpandableListAdapter) mTypologyListView.getExpandableListAdapter()).notifyDataSetChanged();
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

					((TypologySimpleExpandableListAdapter) mTypologyListView.getExpandableListAdapter()).notifyDataSetChanged();
				}
			});

			return content;
		}
	}

}
