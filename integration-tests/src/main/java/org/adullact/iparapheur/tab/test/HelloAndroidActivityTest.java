package org.adullact.iparapheur.tab.test;

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

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
        FolderActivity activity = startActivitySync( FolderActivity.class );
        Thread.sleep( 15000 );
        Screenshots.poseForScreenshotNamed( "iParapheurTab-Folder" );
        assertNotNull( activity );
    }

    private <T extends Activity> T startActivitySync( Class<T> clazz )
    {
        Intent intent = new Intent( getInstrumentation().getTargetContext(), clazz );
        intent.setFlags( intent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK );
        return ( T ) getInstrumentation().startActivitySync( intent );
    }

}
