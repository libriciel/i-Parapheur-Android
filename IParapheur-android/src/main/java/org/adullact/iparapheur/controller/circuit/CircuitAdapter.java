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
package org.adullact.iparapheur.controller.circuit;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.model.EtapeCircuit;

import java.text.SimpleDateFormat;
import java.util.List;


public class CircuitAdapter extends ArrayAdapter<EtapeCircuit> {

	public CircuitAdapter(Context context, List<EtapeCircuit> circuit) {
		super(context, R.layout.fragment_dossier_info_list_cell, R.id.etape_circuit_bureau, circuit);
	}

	@Override public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);

		EtapeCircuit etape = getItem(position);
		((ImageView) view.findViewById(R.id.etape_circuit_icon)).setImageResource(etape.getAction().getIcon(etape.isApproved()));
		((TextView) view.findViewById(R.id.etape_circuit_bureau)).setText(etape.getBureauName());

		if (etape.isApproved()) {
			SimpleDateFormat df = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
			String signataire = etape.getSignataire();
			String validation = getContext().getResources().getString(R.string.le) + " " + df.format(etape.getDateValidation());
			if ((!TextUtils.isEmpty(signataire)) && !signataire.equalsIgnoreCase("null")) {
				validation += " " + getContext().getResources().getString(R.string.par) + " " + signataire;
			}
			((TextView) view.findViewById(R.id.etape_circuit_validation)).setText(validation);
		}

		return view;
	}
}
