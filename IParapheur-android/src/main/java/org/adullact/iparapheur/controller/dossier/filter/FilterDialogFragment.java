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
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.model.Filter;

import java.util.ArrayList;


public class FilterDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

	public static final String FRAGMENT_TAG = "filter_dialog";
	public static final int REQUEST_CODE_FILTER = 6091220;       // Because F-I-L-T-E-R = 06-09-12-20

	private static final String PARCELABLE_FIELD_FILTER = "filter";

	private Filter mFilter;
	private Filter mOriginalFilter;
	private EditText mTitleText;
	private Spinner mStateSpinner;
	private ExpandableListView mTypologieList;
	private TypologieListAdapter mTypologieListAdapter;

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

	@Override public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {

		mOriginalFilter = getArguments().getParcelable(PARCELABLE_FIELD_FILTER);
		mFilter = new Filter(mOriginalFilter);

		// Retrieving views

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View content = inflater.inflate(R.layout.filter_dialog_fragment, null);

		mTitleText = (EditText) content.findViewById(R.id.filter_dialog_titre);
		mStateSpinner = (Spinner) content.findViewById(R.id.filter_dialog_state_spinner);
		mTypologieList = (ExpandableListView) content.findViewById(R.id.filter_dialog_typology);
		View label = content.findViewById(R.id.filter_dialog_titre_label);

		// Setting values

		label.requestFocus(); // Prevents keyboard popping
		mTitleText.setText(mOriginalFilter.getTitle());

		FilterStateSpinnerAdapter spinnerAdapterState = new FilterStateSpinnerAdapter(getActivity());
		mStateSpinner.setAdapter(spinnerAdapterState);
		mStateSpinner.setSelection(Filter.states.indexOf(mOriginalFilter.getState()), false);

		// Building dialog

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(mFilter.getName());
		builder.setView(content);
		builder.setPositiveButton(R.string.action_filtrer, this);
		builder.setNeutralButton(R.string.enregistrer_filtre, this);
		builder.setNegativeButton(android.R.string.cancel, this);

		return builder.create();
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mTypologieListAdapter = new TypologieListAdapter(getActivity(), mOriginalFilter);
		mTypologieList.setAdapter(mTypologieListAdapter);
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
