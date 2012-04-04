package org.adullact.iparapheur.tab.test;

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.view.KeyEvent;
import android.view.View;

import org.adullact.iparapheur.tab.ui.folder.FolderActivity;

import com.github.rtyley.android.screenshot.celebrity.Screenshots;

public class HelloAndroidActivityTest
        extends ActivityInstrumentationTestCase2<FolderActivity>
{

    public HelloAndroidActivityTest()
    {
        super( FolderActivity.class );
    }

    public void testActivity()
            throws InterruptedException
    {
        FolderActivity folderActivity = startActivitySync( FolderActivity.class );
        assertNotNull( folderActivity );

        Thread.sleep( 2000 );
        Screenshots.poseForScreenshotNamed( "iParapheurTab-Folder" );

        TouchUtils.clickView( this, folderActivity.findViewById( org.adullact.iparapheur.tab.R.id.folder_button_sign ) );
        Thread.sleep( 2000 );
        Screenshots.poseForScreenshotNamed( "iParapheurTab-Folder-Sign-Keyboard" );

        sendKeys( KeyEvent.KEYCODE_BACK );;
        Thread.sleep( 2000 );
        Screenshots.poseForScreenshotNamed( "iParapheurTab-Folder-Sign" );

        sendKeys( KeyEvent.KEYCODE_BACK );;
        TouchUtils.clickView( this, folderActivity.findViewById( org.adullact.iparapheur.tab.R.id.folder_button_reject ) );
        Thread.sleep( 2000 );
        Screenshots.poseForScreenshotNamed( "iParapheurTab-Folder-Reject-Keyboard" );

        sendKeys( KeyEvent.KEYCODE_BACK );;
        Thread.sleep( 2000 );
        Screenshots.poseForScreenshotNamed( "iParapheurTab-Folder-Reject" );

    }

    private <T extends Activity> T startActivitySync( Class<T> clazz )
    {
        Intent intent = new Intent( getInstrumentation().getTargetContext(), clazz );
        intent.setFlags( intent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK );
        return ( T ) getInstrumentation().startActivitySync( intent );
    }

}
