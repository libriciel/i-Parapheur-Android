package org.adullact.iparapheur.controller.circuit;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.model.EtapeCircuit;

import java.text.SimpleDateFormat;
import java.util.List;

/**
* Created by jmaire on 13/01/2014.
*/
public class CircuitAdapter extends ArrayAdapter<EtapeCircuit>
{

    public CircuitAdapter(Context context, List<EtapeCircuit> circuit) {
        super(context, R.layout.etape_circuit, R.id.etape_circuit_bureau, circuit);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        EtapeCircuit etape = getItem(position);
        ((ImageView) view.findViewById(R.id.etape_circuit_icon)).setImageResource(etape.getAction().getIcon(etape.isApproved()));
        if (etape.isApproved()) {
            SimpleDateFormat df = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
            String validation = getContext().getResources().getString(R.string.par) + " " +
                    etape.getSignataire() +
                    getContext().getResources().getString(R.string.the) + df.format(etape.getDateValidation());
            ((TextView) view.findViewById(R.id.etape_circuit_validation)).setText(validation);
        }
        return view;
    }
}
