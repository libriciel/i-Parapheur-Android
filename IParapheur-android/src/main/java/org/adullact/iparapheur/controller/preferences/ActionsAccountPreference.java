package org.adullact.iparapheur.controller.preferences;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.controller.utils.LoadingTask;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.controller.utils.IParapheurException;

/**
 * Created by jmaire on 29/10/13.
 */
public class ActionsAccountPreference extends Preference
{
    public interface ActionsAccountPreferenceListener {
        public void onAccountDeleted(Account deleted);
        public void onAccountTested(Account toTest);
    }

    private ActionsAccountPreferenceListener listener;
    private Account account;

    public ActionsAccountPreference(Context context, ActionsAccountPreferenceListener listener, Account account)
    {
        super(context);
        this.listener = listener;
        this.account = account;
    }

    @Override
    public View getView(View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            LinearLayout footerLayout = (LinearLayout) inflater.inflate(R.layout.settings_account_actions, null);
            Button delete = (Button) footerLayout.findViewById(R.id.setting_account_actions_delete);
            Button test = (Button) footerLayout.findViewById(R.id.setting_account_actions_test);

            delete.setOnClickListener(new View.OnClickListener() {
                public void onClick( View view ) {
                    listener.onAccountDeleted(account);
                }
            } );
            test.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onAccountTested(account);
                }
            });
            convertView = footerLayout;
        }
        return convertView;
    }

}