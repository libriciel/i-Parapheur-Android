package org.adullact.iparapheur.tab.ui.dashboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.InjectView;

import com.google.inject.Inject;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.Office;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.ui.office.OfficeActivity;

public class DashboardActivity
        extends RoboFragmentActivity
{

    /* package */ static final String TAG = DashboardActivity.class.getSimpleName();

    @Inject
    /* package */ IParapheurHttpClient client;

    @InjectView( R.id.dashboard_layout )
    private LinearLayout dashboardLayout;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        Log.i( TAG, "onCreate" );
        setContentView( R.layout.dashboard );
        new DashboardLoadingTask( this )
        {

            // This method is called on the UI thread
            // Populates UI views
            @Override
            protected void beforeDialogDismiss( Map<String, List<Office>> officesByCommunity )
            {
                Log.d( TAG, "Got result: " + officesByCommunity );

                for ( Map.Entry<String, List<Office>> eachEntry : officesByCommunity.entrySet() ) {

                    String community = eachEntry.getKey();
                    final List<Office> offices = eachEntry.getValue();

                    DashboardCommunityView eachCommunityView = new DashboardCommunityView( context );
                    eachCommunityView.getNameTextView().setText( community );
                    eachCommunityView.getOfficesGridView().setAdapter( new OfficeListAdapter( context, offices ) );
                    eachCommunityView.getOfficesGridView().setOnItemClickListener( new OnItemClickListener()
                    {

                        public void onItemClick( AdapterView<?> parent, View view, int position, long id )
                        {
                            Office office = offices.get( position );
                            Log.d( TAG, "Selected Office: " + office );
                            startActivity( new Intent( getApplication(), OfficeActivity.class ).putExtra( "office:identity", office.getIdentity() ) );
                        }

                    } );

                    dashboardLayout.addView( eachCommunityView );

                }
            }

        }.execute( new Void[]{} );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        return super.onCreateOptionsMenu( menu );
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

            ItemDataViewsHolder dataViews;
            if ( convertView == null ) {
                convertView = inflater.inflate( R.layout.dashboard_officesgrid_item, null );
                dataViews = new ItemDataViewsHolder();
                dataViews.icon = ( ImageView ) convertView.findViewById( R.id.dashboard_officesgrid_item_icon );
                dataViews.title = ( TextView ) convertView.findViewById( R.id.dashboard_officesgrid_item_title );
                convertView.setTag( dataViews );
            } else {
                dataViews = ( ItemDataViewsHolder ) convertView.getTag();
            }

            final Office office = offices.get( position );

            dataViews.title.setText( office.getTitle() );
            dataViews.icon.setImageResource( R.drawable.ic_list_document );

            return convertView;
        }

        private static class ItemDataViewsHolder
        {

            private ImageView icon;

            private TextView title;

        }

    }

}
