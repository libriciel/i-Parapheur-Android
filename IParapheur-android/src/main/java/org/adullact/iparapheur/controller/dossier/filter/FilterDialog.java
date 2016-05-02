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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.model.Filter;

import java.util.ArrayList;


public class FilterDialog extends DialogFragment implements DialogInterface.OnClickListener {

	public static final String FRAGMENT_TAG = "filter_dialog";
	public static final int REQUEST_CODE_FILTER = 6091220;       // Because F-I-L-T-E-R = 06-09-12-20

	private FilterDialogListener listener;
	private Filter filter;
	private Filter originalFilter;
	private EditText titleText;
	private Spinner spinnerState;
	private ExpandableListView typologieList;
	private TypologieListAdapter typologieListAdapter;

	public FilterDialog() {}

	public static FilterDialog newInstance(Filter filter) {
		FilterDialog f = new FilterDialog();

		// Supply parameters as an arguments.
		Bundle args = new Bundle();
		args.putParcelable("filter", filter);
		f.setArguments(args);

		return f;
	}

	// <editor-fold desc="LifeCycle">

	@Override public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof FilterDialogListener))
			throw new IllegalStateException("Activity must implement FilterDialogListener.");

		listener = (FilterDialogListener) activity;
	}

	@Override public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
		this.originalFilter = getArguments().getParcelable("filter");
		this.filter = new Filter(originalFilter);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(filter.getName());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View content = inflater.inflate(R.layout.filter_dialog, null);

		// Folder title
		this.titleText = (EditText) content.findViewById(R.id.filter_dialog_titre);
		this.titleText.setText(this.originalFilter.getTitle());

		// FolderState
		this.spinnerState = (Spinner) content.findViewById(R.id.filter_dialog_state_spinner);
		FilterStateSpinnerAdapter spinnerAdapterState = new FilterStateSpinnerAdapter(getActivity());
		this.spinnerState.setAdapter(spinnerAdapterState);
		this.spinnerState.setSelection(Filter.states.indexOf(this.originalFilter.getState()), false);

		// Typologie
		this.typologieList = (ExpandableListView) content.findViewById(R.id.filter_dialog_typology_list);

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(content);

		builder.setPositiveButton(R.string.action_filtrer, this);
		builder.setNeutralButton(R.string.enregistrer_filtre, this);
		builder.setNegativeButton(android.R.string.cancel, this);

		return builder.create();
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.typologieListAdapter = new TypologieListAdapter(getActivity(), originalFilter);
		this.typologieList.setAdapter(this.typologieListAdapter);
	}

	@Override public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface.
		listener = null;
	}

	// </editor-fold desc="LifeCycle">

	@Override public void onClick(DialogInterface dialog, int which) {
		switch (which) {
			case DialogInterface.BUTTON_NEGATIVE:
				listener.onFilterCancel();
				dismiss();
				break;
			case DialogInterface.BUTTON_NEUTRAL:
				updateFilter();
				createTitleDialog();
				break;
			case DialogInterface.BUTTON_POSITIVE:
				updateFilter();
				if (originalFilter.getId().equals(Filter.DEFAULT_ID)) {
					filter.setId(Filter.DEFAULT_ID);
				}
				listener.onFilterChange(filter);
				break;
		}
	}

	private void createTitleDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// TITLE
		builder.setTitle(R.string.filtre_nom);

		// CONTENT
		final EditText content = new EditText(getActivity());
		content.setHint(R.string.Add_filter);
		if (!originalFilter.getId().equals(Filter.DEFAULT_ID)) {
			content.setText(filter.getName());
		}
		builder.setView(content);

		// Enregistrer
		builder.setNeutralButton(R.string.enregistrer_filtre, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				if (content.getText().toString().trim().isEmpty()) {
					filter.setName(content.getHint().toString());
				}
				else {
					filter.setName(content.getText().toString().trim());
				}
				listener.onFilterSave(filter);
			}
		});

		builder.create().show();
	}

	private void updateFilter() {
		this.filter.setTitle(this.titleText.getText().toString());
		this.filter.setState(Filter.states.get(this.spinnerState.getSelectedItemPosition()));

		this.filter.setTypes(this.typologieListAdapter.getSelectedTypes());
		this.filter.setSubTypes(this.typologieListAdapter.getSelectedSousTypes());
	}

	public interface FilterDialogListener {

		void onFilterSave(@NonNull Filter filter);

		void onFilterChange(@NonNull Filter filter);

		void onFilterCancel();
	}

	private class FilterStateSpinnerAdapter extends ArrayAdapter<String> {

		public FilterStateSpinnerAdapter(Context context) {
			super(context, R.layout.filter_state_spinner, R.id.filter_state_spinner_text);
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
}
