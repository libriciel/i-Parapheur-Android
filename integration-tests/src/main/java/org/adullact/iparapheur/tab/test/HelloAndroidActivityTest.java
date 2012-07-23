package org.adullact.iparapheur.tab.test;

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.view.KeyEvent;

import com.github.rtyley.android.screenshot.celebrity.Screenshots;

import org.adullact.iparapheur.tab.ui.folder.FolderActivity;
import org.adullact.iparapheur.tab.ui.splashscreen.SplashScreenActivity;

public class HelloAndroidActivityTest
        extends ActivityInstrumentationTestCase2<FolderActivity>
{

    public HelloAndroidActivityTest()
    {
        super( FolderActivity.class );
    }

    public void testSplashScreen()
            throws InterruptedException
    {
        SplashScreenActivity splashActivity = startActivitySync( SplashScreenActivity.class );
        assertNotNull( splashActivity );

        Thread.sleep( 500 );
        Screenshots.poseForScreenshotNamed( "iParapheurTab-SplashScreen" );

        if (false) {
            TouchUtils.clickView( this, splashActivity.findViewById( org.adullact.iparapheur.tab.R.id.folder_button_positive ) );
            Thread.sleep( 1000 );
            Screenshots.poseForScreenshotNamed( "iParapheurTab-Folder-Sign-Keyboard" );

            sendKeys( KeyEvent.KEYCODE_BACK );
            Thread.sleep( 1000 );
            Screenshots.poseForScreenshotNamed( "iParapheurTab-Folder-Sign" );

            sendKeys( KeyEvent.KEYCODE_BACK );
            Thread.sleep( 1000 );
            TouchUtils.clickView( this, splashActivity.findViewById( org.adullact.iparapheur.tab.R.id.folder_button_negative ) );
            Thread.sleep( 1000 );
            Screenshots.poseForScreenshotNamed( "iParapheurTab-Folder-Reject-Keyboard" );

            sendKeys( KeyEvent.KEYCODE_BACK );
            Thread.sleep( 1000 );
            Screenshots.poseForScreenshotNamed( "iParapheurTab-Folder-Reject" );
        }

    }

    private <T extends Activity> T startActivitySync( Class<T> clazz )
    {
        Intent intent = new Intent( getInstrumentation().getTargetContext(), clazz );
        intent.setFlags( intent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK );
        return ( T ) getInstrumentation().startActivitySync( intent );
    }

}
