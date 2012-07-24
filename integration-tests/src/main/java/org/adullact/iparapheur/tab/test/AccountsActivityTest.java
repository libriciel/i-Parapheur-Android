package org.adullact.iparapheur.tab.test;

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;

import com.github.rtyley.android.screenshot.celebrity.Screenshots;

import org.adullact.iparapheur.tab.ui.folder.FolderActivity;
import org.adullact.iparapheur.tab.ui.settings.AccountsActivity;
import org.adullact.iparapheur.tab.ui.splashscreen.SplashScreenActivity;

public class AccountsActivityTest
        extends ActivityInstrumentationTestCase2<FolderActivity>
{

    public AccountsActivityTest()
    {
        super( FolderActivity.class );
    }

    public void testSplashScreen()
            throws InterruptedException
    {
        SplashScreenActivity splashActivity = startActivitySync( SplashScreenActivity.class );
        assertNotNull( splashActivity );

        Thread.sleep( 500 );
        Screenshots.poseForScreenshotNamed( "iParapheurTab-0-SplashScreen" );

        Thread.sleep( 500 );
        Screenshots.poseForScreenshotNamed( "iParapheurTab-1-Dashboard" );

        AccountsActivity accountActivity = startActivitySync( AccountsActivity.class );
        Thread.sleep( 500 );
        Screenshots.poseForScreenshotNamed( "iParapheurTab-2-Accounts" );

        TouchUtils.clickView( this, accountActivity.findViewById( org.adullact.iparapheur.tab.R.id.accounts_add_button ) );
        Thread.sleep( 500 );
        Screenshots.poseForScreenshotNamed( "iParapheurTab-3-Accounts-New" );
    }

    private <T extends Activity> T startActivitySync( Class<T> clazz )
    {
        Intent intent = new Intent( getInstrumentation().getTargetContext(), clazz );
        intent.setFlags( intent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK );
        return ( T ) getInstrumentation().startActivitySync( intent );
    }

}
