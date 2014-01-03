package org.adullact.iparapheur.controller.bureau;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.controller.connectivity.RESTClient;
import org.adullact.iparapheur.controller.utils.LoadingTask;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Bureau;

import java.util.ArrayList;

/**
 * Created by jmaire on 19/11/2013.
 */
public class BureauxFragment extends Fragment implements View.OnClickListener, LoadingTask.DataChangeListener, AdapterView.OnItemClickListener {


    /**
     * The parent activity must implement this interface.
     * Used to notify the activity on bureaux changes
     */
    public interface BureauSelectedListener {
        /**
         * Called when the bureau identified by the id passed in parameter has been
         * selected by the user or when data changes (id will be null)
         * @param id the bureau id or null if none is selected (data changed)
         */
        void onBureauSelected(String id);
    }


    private BureauSelectedListener listener;
    /**
     * list of bureaux currently displayed in this Fragment
     */
    private ArrayList<Bureau> bureaux;
    /**
     * the currently selected dossier
     */
    private int selectedBureau = ListView.INVALID_POSITION;
    /**
     * listView used to show the bureaux of the currently selected account
     */
    private ListView listView;


    // Called only once as retainInstance is set to true.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof BureauSelectedListener))
        {
            throw new IllegalStateException("Activity must implement BureauSelectedListener.");
        }
        listener = (BureauSelectedListener) activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bureaux, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        listView = (ListView) view.findViewById(R.id.bureaux_list);
        listView.setAdapter(new BureauListAdapter(getActivity()));
        listView.setOnItemClickListener(this);
        updateAccounts();
        updateBureaux(false);
    }

    private void updateAccounts() {
        RadioGroup accountContainer = (RadioGroup) getView().findViewById(R.id.bureaux_account_container);
        accountContainer.removeAllViews();
        int i = 0;
        for (Account account : MyAccounts.INSTANCE.getAccounts()) {
            RadioButton radio = new RadioButton(getActivity());
            //radio.setId(View.generateViewId()); // Mandatory to check buttons
            // FIXME
            radio.setId(i);
            i++;
            radio.setText(account.getTitle());
            radio.setTag(account.getId());
            radio.setOnClickListener(this);
            if ((MyAccounts.INSTANCE.getSelectedAccount() != null) && account.equals(MyAccounts.INSTANCE.getSelectedAccount())) {
                radio.setChecked(true);
            }
            accountContainer.addView(radio);
        }
    }

    private void updateBureaux(boolean forceReload) {
        if (forceReload) {
            this.bureaux = null;
        }
        if ((bureaux == null) && (MyAccounts.INSTANCE.getSelectedAccount() != null)) {
            new BureauxLoadingTask(getActivity(), this).execute();
        }
        onDataChanged();
    }

    // OnClickListener implementation (used on radio buttons for accounts)
    @Override
    public void onClick(View v) {
        String newlylySelectedAccount = (String) v.getTag();
        if ((MyAccounts.INSTANCE.getSelectedAccount() == null) ||
                !newlylySelectedAccount.equals(MyAccounts.INSTANCE.getSelectedAccount().getId()))
        {
            MyAccounts.INSTANCE.selectAccount(newlylySelectedAccount);
            updateBureaux(true);
        }
    }

    // OnItemClickListener implementation (used on bureaux list)
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position != selectedBureau) {
            listener.onBureauSelected(bureaux.get(position).getId());
        }
    }

    // DataChangeListener
    @Override
    public void onDataChanged() {
        ((BureauListAdapter) listView.getAdapter()).notifyDataSetChanged();
        /* if there's only 1 bureau, select it automatically
         *
         */
        if ((bureaux != null) && (bureaux.size() == 1)) {
            listener.onBureauSelected(bureaux.get(0).getId());
        }
        else {
            /* if a bureau was previously selected, we have to notify the parent
             * activity that the data has changed, so the activity remove the previously selected
             * dossiers list and details
             */
            listener.onBureauSelected(null);
        }
    }

    public void accountsChanged() {
        updateAccounts();
        updateBureaux(true);
        listener.onBureauSelected(null);
    }

    private class BureauxLoadingTask extends LoadingTask {
        public BureauxLoadingTask(Activity context, DataChangeListener listener) {
            super(context, listener);
        }

        @Override
        protected void load(String... params) {
            // Check if this task is cancelled as often as possible.
            if (isCancelled()) {return;}
            bureaux = RESTClient.INSTANCE.getBureaux();
            // OFFLINE
            //bureaux = new ArrayList<Bureau>();
            //bureaux.add(new Bureau(UUID.randomUUID().toString(), "bureau defaut"));
        }
    }

    private class BureauListAdapter extends ArrayAdapter<Bureau>
    {

        public BureauListAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_activated_1, android.R.id.text1);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            // TODO : update more infos here
            return v;
        }

        @Override
        public int getCount() {
            return (bureaux == null)? 0 : bureaux.size();
        }

        @Override
        public Bureau getItem(int position) {
            return bureaux.get(position);
        }

        @Override
        public int getPosition(Bureau item) {
            return bureaux.indexOf(item);
        }

        @Override
        public boolean isEmpty() {
            return (bureaux == null) || bureaux.isEmpty();
        }
    }
}