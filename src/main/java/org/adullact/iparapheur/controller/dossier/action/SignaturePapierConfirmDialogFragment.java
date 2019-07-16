/*
 * iParapheur Android
 * Copyright (C) 2016-2019 Libriciel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.View;

import org.adullact.iparapheur.R;


public class SignaturePapierConfirmDialogFragment extends DialogFragment {

	public static final String FRAGMENT_TAG = "sign_papier_dialog_fragment";
	public static final int REQUEST_CODE_SIGN_PAPIER = 1601160518;    // Because P-A-P-E-R = 16-01-16-05-18

	public static SignaturePapierConfirmDialogFragment newInstance() {
		return new SignaturePapierConfirmDialogFragment();
	}

	@Override public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {

		// Create view

		View view = View.inflate(getActivity(), R.layout.action_dialog_signature_papier_confirmation, null);

		// Build Dialog

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Main_Dialog);
		builder.setView(view);
		builder.setPositiveButton(R.string.signature_papier_transform, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
			}
		});

		return builder.create();
	}

}
