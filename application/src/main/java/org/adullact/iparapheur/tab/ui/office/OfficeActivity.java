package org.adullact.iparapheur.tab.ui.office;

import java.util.List;

import android.os.Bundle;
import android.widget.TextView;

import roboguice.inject.InjectView;

import com.google.inject.Inject;

import de.akquinet.android.androlog.Log;

import org.codeartisans.android.toolbox.activity.RoboFragmentActivity;
import org.codeartisans.android.toolbox.logging.AndrologInitOnCreateObserver;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.Folder;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.ui.actionbar.ActionBarActivityObserver;

public class OfficeActivity
        extends RoboFragmentActivity
{

    public static final String EXTRA_ACCOUNT_IDENTITY = "account:identity";

    public static final String EXTRA_OFFICE_IDENTITY = "office:identity";

    public static final String EXTRA_OFFICE_TITLE = "office:title";

    @Inject
    private AndrologInitOnCreateObserver andrologInitOnCreateObserver;

    @Inject
    private ActionBarActivityObserver actionBarObserver;

    @Inject
    private AccountsRepository accountsRepository;

    @Inject
    private IParapheurHttpClient iParapheurClient;

    @InjectView( R.id.office_title )
    private TextView officeTitleView;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        String accountIdentity = getIntent().getExtras().getString( EXTRA_ACCOUNT_IDENTITY );
        String officeIdentity = getIntent().getExtras().getString( EXTRA_OFFICE_IDENTITY );
        String officeTitle = getIntent().getExtras().getString( EXTRA_OFFICE_TITLE );
        Log.i( "onCreate for office: " + accountIdentity + " / " + officeIdentity );
        super.onCreate( savedInstanceState );
        setContentView( R.layout.office );
        officeTitleView.setText( officeTitle );
        new OfficeLoadingTask( this, accountsRepository, iParapheurClient )
        {

            // This method is called on the UI thread
            // Populates UI views
            @Override
            protected void beforeDialogDismiss( List<Folder> folders )
            {
                Log.d( OfficeActivity.this, "Got result: " + folders );
                if ( folders != null && !folders.isEmpty() ) {
                    // TODO Populates UI views
                }
            }

        }.execute( new OfficeLoadingTask.Params( accountIdentity, officeIdentity, 1, 10 ) );
    }

}
