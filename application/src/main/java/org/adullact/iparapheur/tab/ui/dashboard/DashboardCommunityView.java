package org.adullact.iparapheur.tab.ui.dashboard;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.adullact.iparapheur.tab.R;

public class DashboardCommunityView
        extends LinearLayout
{

    private TextView nameTextView;

    private GridView officesGridView;

    public DashboardCommunityView( Context context )
    {
        super( context );
        setup();
    }

    public DashboardCommunityView( Context context, AttributeSet attrs )
    {
        super( context, attrs );
        setup();
    }

    public DashboardCommunityView( Context context, AttributeSet attrs, int defStyle )
    {
        super( context, attrs, defStyle );
        setup();
    }

    private void setup()
    {
        setOrientation( VERTICAL );
        inflate( getContext(), R.layout.dashboard_community, this );
        nameTextView = ( TextView ) findViewById( R.id.dashboard_community_name );
        officesGridView = ( GridView ) findViewById( R.id.dashboard_community_gridview );
    }

    public TextView getNameTextView()
    {
        return nameTextView;
    }

    public GridView getOfficesGridView()
    {
        return officesGridView;
    }

}
