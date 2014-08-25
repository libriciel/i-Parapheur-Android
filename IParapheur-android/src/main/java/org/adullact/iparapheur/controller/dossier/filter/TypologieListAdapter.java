package org.adullact.iparapheur.controller.dossier.filter;

import android.app.Activity;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.controller.utils.IParapheurException;
import org.adullact.iparapheur.controller.utils.LoadingTask;
import org.adullact.iparapheur.model.Filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
* Created by jmaire on 10/02/2014.
*/
class TypologieListAdapter extends BaseExpandableListAdapter implements LoadingTask.DataChangeListener {

    private Context context;
    private final Filter filter;
    private Map<String, ArrayList<String>> typologie;
    private HashMap<String, ArrayList<String>> selection;

    public TypologieListAdapter(Context context, Filter filter) {
        this.context = context;
        this.filter = filter;
        this.selection = new HashMap<String, ArrayList<String>>();
        this.typologie = new LinkedHashMap<String, ArrayList<String>>();
        new TypologieLoadingTask((Activity) context, this).execute();
    }

    @Override
    public int getGroupCount() {
        return typologie.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String type = (String) typologie.keySet().toArray()[groupPosition];
        return typologie.get(type).size();
    }

    @Override
    public String getGroup(int groupPosition) {
        return (String) typologie.keySet().toArray()[groupPosition];
    }

    @Override
    public String getChild(int groupPosition, int childPosition) {
        int i = 0;
        String type = (String) typologie.keySet().toArray()[groupPosition];
        return typologie.get(type).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.typologie_list_type, null);
        }
        ((ExpandableListView) parent).expandGroup(groupPosition, false);
        String type = getGroup(groupPosition);
        TextView textView = (TextView) convertView.findViewById(R.id.typologie_list_type_text);
        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.typologie_list_type_checkbox);

        (textView).setText(type);
        textView.setTag(checkBox);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox1 = (CheckBox) v.getTag();
                checkBox1.performClick();
            }
        });

        checkBox.setChecked(selection.containsKey(type));
        checkBox.setTag(type);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                if (cb.isChecked()) {
                    selectGroup((String) cb.getTag());
                }
                else {
                    unselectGroup((String) cb.getTag());
                }
                TypologieListAdapter.this.notifyDataSetChanged();
            }
        });
        //Log.d("debug", "GET TYPE");
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.typologie_list_soustype, null);
        }

        String sousType = getChild(groupPosition, childPosition);
        TextView textView = (TextView) convertView.findViewById(R.id.typologie_list_soustype_text);
        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.typologie_list_soustype_checkbox);

        textView.setText(sousType);
        textView.setTag(checkBox);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox1 = (CheckBox) v.getTag();
                checkBox1.performClick();
            }
        });

        String type = getGroup(groupPosition);

        (checkBox).setChecked((selection.containsKey(type)) && (selection.get(type).contains(sousType))) ;
        checkBox.setTag(new Pair<String, String>(type, sousType));
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                Pair<String, String> pair = (Pair<String, String>) cb.getTag();
                if (cb.isChecked()) {
                    selectChild(pair.first, pair.second);
                } else {
                    unselectChild(pair.first, pair.second);
                }
                TypologieListAdapter.this.notifyDataSetChanged();
            }
        });
        //Log.d("debug", "GET SOUTYPE");
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private void selectGroup(String type) {
        ArrayList<String> sousTypes = new ArrayList<String>(typologie.get(type));
        this.selection.put(type, sousTypes);
    }

    private void unselectGroup(String type) {
        this.selection.remove(type);
    }

    public void selectChild(String type, String sousType) {
        if (!selection.containsKey(type)) {
            selection.put(type, new ArrayList<String>());
        }
        if (!selection.get(type).contains(sousType)) {
            selection.get(type).add(sousType);
        }
    }

    public void unselectChild(String type, String sousType) {
        if (selection.containsKey(type)) {
            selection.get(type).remove(sousType);
            if (selection.get(type).isEmpty()) {
                selection.remove(type);
            }
        }
    }

    public List<String> getSelectedTypes() {
        return new ArrayList<String>(selection.keySet());
    }

    public List<String> getSelectedSousTypes() {
        List<String> sousTypes = new ArrayList<String>();
        for (String type : selection.keySet()) {
            sousTypes.addAll(selection.get(type));
        }
        return sousTypes;
    }

    @Override
    public void onDataChanged() {
        initSelection();
        notifyDataSetChanged();
    }

    private void initSelection() {
        this.selection.clear();
        for (String type : filter.getTypes()) {
            if (typologie.containsKey(type)) {
                ArrayList<String> selectedSousTypes = new ArrayList<String>(typologie.get(type));
                ArrayList<String> currentSousTypes = new ArrayList<String>(typologie.get(type));
                if (currentSousTypes.removeAll(filter.getSousTypes())) {
                    if (!currentSousTypes.isEmpty()) {
                        selectedSousTypes.retainAll(filter.getSousTypes());
                    }
                }
                this.selection.put(type, selectedSousTypes);
            }

        }
    }

    private class TypologieLoadingTask extends LoadingTask {

        public TypologieLoadingTask(Activity activity, DataChangeListener listener) {
            super(activity, listener);
        }

        @Override
        protected void load(String... params) throws IParapheurException {
            typologie = RESTClient.INSTANCE.getTypologie();
        }
    }
}
