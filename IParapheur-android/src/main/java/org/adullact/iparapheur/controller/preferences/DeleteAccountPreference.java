package org.adullact.iparapheur.controller.preferences;

import android.content.Context;
import android.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.model.Account;

/**
 * Created by jmaire on 29/10/13.
 */
public class DeleteAccountPreference extends Preference
{
    public interface OnAccountDeletedListener {
        public void onAccountDeleted(Account deleted);
    }

    private OnAccountDeletedListener listener;
    private Account account;

    public DeleteAccountPreference(Context context, OnAccountDeletedListener listener, Account account)
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
            LinearLayout footerLayout = (LinearLayout) inflater.inflate(R.layout.settings_account_delete, null);
            Button delete = (Button) footerLayout.findViewById(R.id.setting_account_delete_button);

            delete.setOnClickListener(new View.OnClickListener() {
                public void onClick( View view ) {
                    listener.onAccountDeleted(account);
                }
            } );
            convertView = footerLayout;
        }
        return convertView;
    }

}