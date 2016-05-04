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
package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.utils.LoadingTask;

import java.util.ArrayList;


public abstract class ActionDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

	protected LoadingTask.DataChangeListener listener;
	protected ArrayList<Dossier> dossiers;
	protected String bureauId;

	public ActionDialogFragment() {}

	// <editor-fold desc="LifeCycle">

	@Override public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
		if (getArguments() != null) {
			this.dossiers = getArguments().getParcelableArrayList("dossiers");
			this.bureauId = getArguments().getString("bureauId");
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(createView())
				// Set action button
				.setPositiveButton(getTitle(), this).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				ActionDialogFragment.this.getDialog().cancel();
			}
		});

		return builder.create();
	}

	@Override public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof LoadingTask.DataChangeListener))
			throw new IllegalStateException("Activity must implement DataChangeListener.");

		listener = (LoadingTask.DataChangeListener) activity;
	}

	@Override public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface.
		listener = null;
	}

	// </editor-fold desc="LifeCycle">

	@Override public void onClick(DialogInterface dialog, int which) {
		executeTask();
	}

	protected View createView() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		// Pass null as the parent view because its going in the dialog layout
		View layout = inflater.inflate(getViewId(), null);

		ListView dossiersView = (ListView) layout.findViewById(R.id.action_dialog_dossiers);
		dossiersView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, this.dossiers));
		dossiersView.setItemsCanFocus(false);
		return layout;
	}

	/**
	 * return the title of the action of the dialog. Used to edit positive button text.
	 *
	 * @return the title of the action done in this dialog.
	 */
	protected abstract int getTitle();

	/**
	 * return the id of the view inflated in this dialog.
	 *
	 * @return the id of the view used in this dialog.
	 */
	protected abstract int getViewId();

	/**
	 * Called when the positive button is pressed.
	 */
	protected abstract void executeTask();
}