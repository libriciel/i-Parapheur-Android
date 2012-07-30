package org.adullact.iparapheur.tab.logging;

import android.content.Context;
import de.akquinet.android.androlog.reporter.Report;
import de.akquinet.android.androlog.reporter.Reporter;
import java.util.Properties;

/**
 * This is a base Reporter provided as an example.
 */
public class IParapheurReporter
        implements Reporter
{

    public void configure( Properties configuration )
    {
    }

    public boolean send( Context context, String message, Throwable error )
    {
        System.out.println( "IPARAPHEUR-TAB REPORTER IS SPEAKING" );
        System.out.println( "Context is: " + context );
        System.out.println( "Message is: " + message );
        System.out.println( "Error is: " + error );
        String report = new Report( context, message, error ).getReportAsJSON().toString();
        System.out.println( "----------- REPORT BEGIN ------------" );
        System.out.println( report );
        System.out.println( "----------- REPORT END ------------" );
        return true;
    }

}
