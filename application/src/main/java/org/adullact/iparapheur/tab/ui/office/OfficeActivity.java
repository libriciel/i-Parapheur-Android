package org.adullact.iparapheur.tab.ui.office;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.adullact.iparapheur.tab.R;

public class OfficeActivity
        extends Activity
{

    private static final String TAG = OfficeActivity.class.getSimpleName();

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        Log.i( TAG, "onCreate" );
        setContentView( R.layout.office );
    }

}
