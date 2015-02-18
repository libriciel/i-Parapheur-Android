package org.adullact.iparapheur.controller.bureau;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.IParapheur;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.controller.utils.IParapheurException;
import org.adullact.iparapheur.controller.utils.LoadingTask;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Bureau;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jmaire on 19/11/2013.
 */
public class BureauxFragment extends Fragment implements LoadingTask.DataChangeListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemSelectedListener {


    public static final String TAG = "Bureaux_list";

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
     * list of accounts displayed in the spinner
     */
    private List<Account> accounts;
    /**
     * list of bureaux currently displayed in this Fragment
     */
    private List<Bureau> bureaux;
    /**
     * the currently selected dossier
     */
    private int selectedBureau = ListView.INVALID_POSITION;
    /**
     * listView used to show the bureaux of the currently selected account
     */
    private ListView listView;

    /**
     * Spinner containing user's accounts.
     */
    private Spinner accountsSpinner;

    /**
     * Swipe refresh layout on top of the list view
     */
    private SwipeRefreshLayout swipeRefreshLayout;


    // Called only once as retainInstance is set to true.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        accounts = new ArrayList<Account>();
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
        super.onViewCreated(view, savedInstanceState);
        listView = (ListView) view.findViewById(R.id.bureaux_list);
        listView.setOnItemClickListener(this);
        listView.setEmptyView(view.findViewById(android.R.id.empty));
        accountsSpinner = (Spinner) getView().findViewById(R.id.bureaux_accounts_spinner);
        accountsSpinner.setOnItemSelectedListener(this);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.bureaux_refresh_layout);
        swipeRefreshLayout.setColorScheme(android.R.color.holo_green_light,
                android.R.color.holo_red_light,
                android.R.color.holo_blue_light,
                android.R.color.holo_orange_light);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setAdapter(new BureauListAdapter(getActivity()));
        accountsSpinner.setAdapter(new AccountSpinnerAdapter(getActivity()));
        swipeRefreshLayout.setOnRefreshListener(this);
        updateAccounts();
        updateBureaux(false);
    }

    private void updateAccounts() {
        accounts.clear();
        int i = -1;
        for (Account account : MyAccounts.INSTANCE.getAccounts()) {
            if (account.isValid()) {
                accounts.add(account);
                i ++;
                if ((MyAccounts.INSTANCE.getSelectedAccount() != null) && account.equals(MyAccounts.INSTANCE.getSelectedAccount())) {
                    accountsSpinner.setSelection(i);
                }
            }
        }
        ((AccountSpinnerAdapter) accountsSpinner.getAdapter()).notifyDataSetChanged();
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

    // onItemSelected implementation (used on accounts spinner)
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Account newlylySelectedAccount = accounts.get(i);
        Account selectedAccount = MyAccounts.INSTANCE.getSelectedAccount();
        if ((selectedAccount == null) || !newlylySelectedAccount.equals(selectedAccount))
        {
            MyAccounts.INSTANCE.selectAccount(newlylySelectedAccount.getId());
            updateBureaux(true);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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

    // SwipeRefreshLayout listener
    @Override
    public void onRefresh() {
        new BureauxLoadingTask(getActivity(), this).execute();
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
        protected void load(String... params) throws IParapheurException {
            // Check if this task is cancelled as often as possible.
            if (isCancelled()) {return;}
            if (!IParapheur.OFFLINE) {
                bureaux = RESTClient.INSTANCE.getBureaux();
            }
            else {
                bureaux = new ArrayList<Bureau>();
                bureaux.add(new Bureau(UUID.randomUUID().toString(), "bureau defaut"));
            }
        }

        @Override
        protected void showProgress() {
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected void hideProgress() {
            swipeRefreshLayout.setRefreshing(false);
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

    private class AccountSpinnerAdapter extends ArrayAdapter<Account>
    {

        public AccountSpinnerAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_activated_1, android.R.id.text1);
        }

        @Override
        public int getCount() {
            return (accounts == null)? 0 : accounts.size();
        }

        @Override
        public Account getItem(int position) {
            return accounts.get(position);
        }

        @Override
        public int getPosition(Account item) {
            return accounts.indexOf(item);
        }

        @Override
        public boolean isEmpty() {
            return (accounts == null) || accounts.isEmpty();
        }
    }
}