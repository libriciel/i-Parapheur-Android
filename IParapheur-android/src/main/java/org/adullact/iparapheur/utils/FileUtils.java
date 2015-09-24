package org.adullact.iparapheur.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.adullact.iparapheur.controller.IParapheurApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class FileUtils {

	public static final String SHARED_PREFERENCES_CERTIFICATES_PASSWORDS = ":iparapheur:shared_preferences_certificates_passwords";

	public static @Nullable File getDirectoryForDossier(@NonNull String dossierId) {
		File directory = new File(IParapheurApplication.getContext().getExternalCacheDir(), dossierId);
		boolean success = directory.mkdirs();

		return success ? directory : null;
	}

	public static @Nullable File getFileForDocument(@NonNull Context context, @NonNull String dossierId, @NonNull String documentId) {

		if (!DeviceUtils.isDebugOffline())
			return new File(FileUtils.getDirectoryForDossier(dossierId), documentId);
		else
			return createFileFromAsset(context, "offline_test_file.pdf");
	}

	public static @Nullable String getInternalCertificateStoragePath(@NonNull Context context) {

		String path = null;
		boolean accessible = false;
		File rootFile = context.getExternalFilesDir(null);

		if (rootFile != null) {
			path = rootFile.getAbsolutePath() + File.separator + "certificates" + File.separator;
			accessible = new File(path).mkdirs();
		}

		return accessible ? path : null;
	}

	public static @Nullable File getBksFromCertificateFolder(@NonNull Context context) {
		String folder = getInternalCertificateStoragePath(context);
		return (folder != null ? getBksFromFolder(new File(folder)) : null);
	}

	public static @Nullable File getBksFromDownloadFolder() {
		return getBksFromFolder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
	}

	public static @Nullable File getBksFromFolder(@NonNull File folder) {

		File jks = null;

		if (folder.listFiles() != null)
			for (File file : folder.listFiles())
				if (file.getName().endsWith("bks"))
					jks = file;

		return jks;
	}

	/**
	 * Creates a file form an Asset, through {@link #createFileFromInputStream}.
	 */
	private static @Nullable File createFileFromAsset(@NonNull Context context, @NonNull String assetFileName) {
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
	private static @Nullable File createFileFromInputStream(Context context, InputStream inputStream, String fileName) {
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
