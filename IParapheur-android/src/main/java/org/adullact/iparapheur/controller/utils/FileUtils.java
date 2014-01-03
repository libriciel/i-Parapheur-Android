package org.adullact.iparapheur.controller.utils;

import android.os.Environment;

import org.adullact.iparapheur.controller.IParapheur;

import java.io.File;

/**
 * Created by jmaire on 06/11/2013.
 */
public class FileUtils {

    public static File getDirectoryForDossier(String dossierId) {
        File directory = new File (IParapheur.getContext().getExternalCacheDir(), dossierId);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    public static File getFileForDocument(String dossierId, String documentId) {
        return new File(FileUtils.getDirectoryForDossier(dossierId), documentId);
        // OFFLINE
        //return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(), "defautDoc.pdf");
    }

    public static boolean isStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
