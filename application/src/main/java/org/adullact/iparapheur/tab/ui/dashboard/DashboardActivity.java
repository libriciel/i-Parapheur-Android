package org.adullact.iparapheur.tab.ui.dashboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import roboguice.inject.InjectView;
import roboguice.util.Strings;

import com.google.inject.Inject;

import de.akquinet.android.androlog.Log;

import org.codeartisans.android.toolbox.activity.RoboActivity;
import org.codeartisans.android.toolbox.app.UserErrorDialogFactory;
import org.codeartisans.android.toolbox.logging.AndrologInitOnCreateObserver;
import org.codeartisans.android.toolbox.os.AsyncTaskResult;

import org.adullact.iparapheur.tab.IParapheurTabException;
import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.Community;
import org.adullact.iparapheur.tab.model.Office;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.ui.Refreshable;
import org.adullact.iparapheur.tab.ui.actionbar.ActionBarActivityObserver;
import org.adullact.iparapheur.tab.ui.dashboard.FluidGridView.OnItemClickListener;
import org.adullact.iparapheur.tab.ui.office.OfficeActivity;
import org.adullact.iparapheur.tab.ui.settings.AccountsActivity;

public class DashboardActivity
        extends RoboActivity
        implements Refreshable
{

    @Inject
    private AndrologInitOnCreateObserver andrologInitOnCreateObserver;

    @Inject
    private ActionBarActivityObserver actionBarObserver;

    @Inject
    private AccountsRepository accountsRepository;

    @Inject
    private IParapheurHttpClient client;

    @InjectView( R.id.dashboard_layout )
    private LinearLayout dashboardLayout;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        Log.d( this, "onCreate" );
        super.onCreate( savedInstanceState );
        setContentView( R.layout.dashboard );
        refresh();
    }

    public void refresh()
    {
        dashboardLayout.removeAllViews();
        if ( accountsRepository.all().isEmpty() ) {

            // No account
            TextView textView = new TextView( this );
            textView.setText( "Aucun compte iParapheur configuré." );
            dashboardLayout.addView( textView );

        } else {

            new DashboardLoadingTask( this, accountsRepository, client )
            {

                // This method is called on the UI thread
                // Populates UI views
                @Override
                protected void beforeDialogDismiss( AsyncTaskResult<Map<Community, List<Office>>, IParapheurTabException> result )
                {
                    Map<Community, List<Office>> officesByCommunity = result.getResult();

                    if ( officesByCommunity == null || officesByCommunity.isEmpty() ) {

                        // No offices
                        TextView textView = new TextView( context );
                        textView.setText( "Aucun bureau." );
                        dashboardLayout.addView( textView );

                    } else {

                        for ( Map.Entry<Community, List<Office>> eachEntry : officesByCommunity.entrySet() ) {

                            Community community = eachEntry.getKey();
                            final List<Office> offices = eachEntry.getValue();

                            DashboardCommunityView eachCommunityView = new DashboardCommunityView( context );
                            eachCommunityView.getNameTextView().setText( community.getName() );
                            eachCommunityView.getOfficesGridView().setAdapter( new OfficeListAdapter( context, offices ) );
                            eachCommunityView.getOfficesGridView().setOnItemClickListener( new OnItemClickListener()
                            {

                                public void onItemClick( FluidGridView fluidGridView, View itemView, int position, long id )
                                {
                                    Office office = offices.get( position );
                                    Intent intent = new Intent( getApplication(), OfficeActivity.class );
                                    intent.putExtra( OfficeActivity.EXTRA_ACCOUNT_IDENTITY, office.getAccountIdentity() );
                                    intent.putExtra( OfficeActivity.EXTRA_OFFICE_IDENTITY, office.getIdentity() );
                                    intent.putExtra( OfficeActivity.EXTRA_OFFICE_TITLE, office.getTitle() );
                                    startActivity( intent );
                                }

                            } );
                            dashboardLayout.addView( eachCommunityView );
                        }

                    }
                }

                private DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener()
                {

                    public void onClick( DialogInterface dialog, int id )
                    {
                        dialog.cancel();
                    }

                };

                private DialogInterface.OnClickListener accounts = new DialogInterface.OnClickListener()
                {

                    public void onClick( DialogInterface dialog, int id )
                    {
                        startActivity( new Intent( context, AccountsActivity.class ) );
                    }

                };

                @Override
                protected void afterDialogDismiss( AsyncTaskResult<Map<Community, List<Office>>, IParapheurTabException> result )
                {
                    // Error handling to user
                    if ( result.hasError() ) {
                        UserErrorDialogFactory.show( context, "Le chargement de certains bureaux à échoué", result.getErrors(),
                                                     "Continuer", cancel, "Comptes", accounts );
                    }
                }

            }.execute( new Void[]{} );
        }
    }

    public static final class OfficeListAdapter
            extends BaseAdapter
    {

        private final List<Office> offices;

        private LayoutInflater inflater;

        public OfficeListAdapter( Context context, Collection<Office> offices )
        {
            this.inflater = LayoutInflater.from( context );
            this.offices = new ArrayList<Office>( offices );
        }

        @Override
        public int getCount()
        {
            return offices.size();
        }

        @Override
        public Object getItem( int position )
        {
            return offices.get( position );
        }

        @Override
        public long getItemId( int position )
        {
            return position;
        }

        /**
         * The system calls this when it's time for the adapter to draw a list
         * item user interface for the first time.
         * 
         * @return The root of the item's layout.
         */
        @Override
        public View getView( int position, View convertView, ViewGroup parent )
        {

            final ItemDataViewsHolder dataViews;
            if ( convertView == null ) {
                convertView = inflater.inflate( R.layout.dashboard_officesgrid_item, null );
                dataViews = new ItemDataViewsHolder();
                dataViews.icon = ( ImageView ) convertView.findViewById( R.id.dashboard_officesgrid_item_icon );
                dataViews.title = ( TextView ) convertView.findViewById( R.id.dashboard_officesgrid_item_title );
                dataViews.todo = ( TextView ) convertView.findViewById( R.id.dashboard_officesgrid_item_todo );
                dataViews.late = ( TextView ) convertView.findViewById( R.id.dashboard_officesgrid_item_late );
                convertView.setTag( dataViews );
            } else {
                dataViews = ( ItemDataViewsHolder ) convertView.getTag();
            }

            final Office office = offices.get( position );
            dataViews.icon.setImageResource( R.drawable.ic_office );
            if ( !Strings.isEmpty( office.getLogoUrl() ) ) {
                new OfficeLogoLoadingTask()
                {

                    @Override
                    protected void onPostExecute( Bitmap result )
                    {
                        if ( result != null ) {
                            dataViews.icon.setImageBitmap( result );
                        }
                    }

                }.execute( office.getLogoUrl() );
            }
            dataViews.title.setText( office.getTitle() );
            if ( office.getTodoFolderCount() > 0 ) {
                dataViews.todo.setText( "" + office.getTodoFolderCount() );
                dataViews.todo.setVisibility( View.VISIBLE );
            } else {
                dataViews.todo.setText( "" );
                dataViews.todo.setVisibility( View.INVISIBLE );
            }
            if ( office.getLateFolderCount() > 0 ) {
                dataViews.late.setText( "" + office.getLateFolderCount() );
                dataViews.late.setVisibility( View.VISIBLE );
            } else {
                dataViews.late.setText( "" );
                dataViews.late.setVisibility( View.INVISIBLE );
            }

            return convertView;
        }

        private static class ItemDataViewsHolder
        {

            private ImageView icon;

            private TextView title;

            private TextView todo;

            private TextView late;

        }

    }

}
