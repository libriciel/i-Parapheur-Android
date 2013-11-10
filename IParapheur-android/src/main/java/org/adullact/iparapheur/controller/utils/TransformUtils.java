package org.adullact.iparapheur.controller.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jmaire on 04/11/2013.
 */
public class TransformUtils {

    public static String inputStreamToString(InputStream is) throws IOException {
        String line = "";
        StringBuilder total = new StringBuilder();

        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        // Read response until the end
        while ((line = rd.readLine()) != null) {
            total.append(line);
        }
        return total.toString();
    }

    public static Date parseISO8601Date( String iso8601Date )
    {
        if ((iso8601Date == null) || iso8601Date.isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(iso8601Date);
        } catch ( ParseException ex ) {
            return null;
        }
    }
}
