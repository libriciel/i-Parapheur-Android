package org.adullact.iparapheur.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.annotation.Nullable;

import org.adullact.iparapheur.controller.IParapheurApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

	public static File getDirectoryForDossier(String dossierId) {
		File directory = new File(IParapheurApplication.getContext().getExternalCacheDir(), dossierId);
		directory.mkdirs();

		return directory;
	}

	public static File getFileForDocument(Context context, String dossierId, String documentId) {
		if (!DeviceUtils.isDebugOffline()) {
			return new File(FileUtils.getDirectoryForDossier(dossierId), documentId);
		}
		else {
			return createFileFromAsset(context, "offline_test_file.pdf");
		}
	}

	public static boolean isStorageAvailable() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	public static @Nullable File getBksFromDownloadFolder() {

		File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		File jks = null;

		if (folder.listFiles() != null)
			for (File file : folder.listFiles())
				if (file.getName().endsWith("bks"))
					jks = file;

		return jks;
	}

	/**
	 * Creates a file form an Asset, through {@link #createFileFromInputStream}.
	 *
	 * @param context
	 * @param assetFileName
	 */
	private static File createFileFromAsset(Context context, String assetFileName) {
		AssetManager am = context.getAssets();

		try {
			InputStream inputStream = am.open(assetFileName);
			return createFileFromInputStream(context, inputStream, assetFileName);
		}
		catch (IOException ioException) {
			ioException.printStackTrace();
		}

		return null;
	}

	/**
	 * Creates a file from a steam in the intern cacheDir/temp_files/ directory.
	 *
	 * @param context
	 * @param inputStream
	 * @param fileName
	 */
	private static File createFileFromInputStream(Context context, InputStream inputStream, String fileName) {
		File fileFolder = new File(context.getCacheDir().getAbsolutePath() + File.separator + "temp_files");
		fileFolder.mkdirs();

		try {
			File file = new File(fileFolder.getAbsolutePath() + File.separator + fileName);
			file.delete(); // removing previous file, if exists.

			OutputStream outputStream = new FileOutputStream(file);
			byte buffer[] = new byte[1024];
			int length = 0;

			while ((length = inputStream.read(buffer)) > 0)
				outputStream.write(buffer, 0, length);

			outputStream.close();
			inputStream.close();

			return file;
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}
