package org.adullact.iparapheur.controller.dossier.action;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
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
		builder.setPositiveButton(R.string.signature_papier_transform, null);
		builder.setNegativeButton(android.R.string.cancel, null);

		return builder.create();
	}

}
