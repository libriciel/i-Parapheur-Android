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

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;

import org.adullact.iparapheur.R;


public class ChooseFilterNameDialogFragment extends DialogFragment {

	public static final String FRAGMENT_TAG = "choose_filter_name_dialog_fragment";
	public static final int REQUEST_CODE_FILTER_NAME = 614011305;       // Because F-N-A-M-E = 06-14-01-13-05
	public static final String RESULT_BUNDLE_TITLE = "title";

	private EditText mEditText;

	public static ChooseFilterNameDialogFragment newInstance() {
		return new ChooseFilterNameDialogFragment();
	}

	@Override public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {

		View content = View.inflate(getActivity(), R.layout.filter_dialog_fragment_filter_name_popup, null);
		mEditText = (EditText) content.findViewById(R.id.filter_dialog_fragment_filter_name_edittext);

		// Build Popup

		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.filtre_nom);
		builder.setView(content);
		builder.setPositiveButton(R.string.Save, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				onConfirmButtonClicked();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				onCancelButtonClicked();
			}
		});
		return builder.create();
	}

	private void onConfirmButtonClicked() {

		Intent resultIntent = new Intent();
		resultIntent.putExtra(RESULT_BUNDLE_TITLE, String.valueOf(mEditText.getText()));

		getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, resultIntent);
		dismiss();
	}

	private void onCancelButtonClicked() {
		getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
		dismiss();
	}
}
