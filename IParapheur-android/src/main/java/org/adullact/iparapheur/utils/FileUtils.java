package org.adullact.iparapheur.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.IParapheurApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;


public class FileUtils {

	public static final String LOG = "FileUtils";
	public static final String SHARED_PREFERENCES_CERTIFICATES_PASSWORDS = ":iparapheur:shared_preferences_certificates_passwords";

	private static final String ASSET_DEMO_PDF_FILE_NAME = "offline_test_file.pdf";

	public static @Nullable File getDirectoryForDossier(@NonNull String dossierId) {
		File directory = new File(IParapheurApplication.getContext().getExternalCacheDir(), dossierId);

		if (!directory.mkdirs())
			if (!directory.exists())
				Log.e(LOG, "getDirectoryForDossier failed");

		Log.e(LOG, "directory : " + directory.getAbsolutePath() + " " + directory.exists());

		return directory;
	}

	@SuppressWarnings("ConstantConditions") public static @NonNull File getFileForDocument(@NonNull Context context, @NonNull String dossierId,
																						   @NonNull String documentId) {

		if (!DeviceUtils.isDebugOffline())
			return new File(FileUtils.getDirectoryForDossier(dossierId), documentId);
		else
			return createFileFromAsset(context, ASSET_DEMO_PDF_FILE_NAME);
	}

	public static @Nullable File getInternalCertificateStoragePath(@NonNull Context context) {

		boolean accessible = false;
		File rootFolder = context.getExternalFilesDir(null);
		File certificateFolder = null;

		if (rootFolder != null) {
			String certificatePath = rootFolder.getAbsolutePath() + File.separator + "certificates" + File.separator;
			certificateFolder = new File(certificatePath);
			accessible = certificateFolder.exists() || certificateFolder.mkdirs();
		}

		return accessible ? certificateFolder : null;
	}

	public static @NonNull List<File> getBksFromCertificateFolder(@NonNull Context context) {
		File folder = getInternalCertificateStoragePath(context);
		return (folder != null ? getBksFromFolder(folder) : new ArrayList<File>());
	}

	public static @NonNull List<File> getBksFromDownloadFolder() {
		return getBksFromFolder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
	}

	public static @NonNull List<File> getBksFromFolder(@NonNull File folder) {

		List<File> jks = new ArrayList<>();

		if (folder.listFiles() != null)
			for (File file : folder.listFiles())
				if (file.getName().endsWith("bks"))
					jks.add(file);

		return jks;
	}

	public static boolean importCertificate(@NonNull Activity activity, @NonNull File certificateFile, @NonNull String password) {

		// Test password

		boolean bksOpeningSuccess = false;
		try {
			PKCS7Signer pkcs7signer = new PKCS7Signer(certificateFile.getAbsolutePath(), password, "", "");
			pkcs7signer.loadKeyStore();
			bksOpeningSuccess = true;
		}
		catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(activity, R.string.import_error_message_opening_bks_file, Toast.LENGTH_SHORT).show();
		}
		catch (NoSuchAlgorithmException | KeyStoreException | CertificateException e) {
			e.printStackTrace();
			Toast.makeText(activity, R.string.import_error_message_incompatible_device, Toast.LENGTH_SHORT).show();
		}

		// Stop on error

		if (!bksOpeningSuccess)
			return false;

		// Import to intern memory

		boolean movedSuccessfully;

		File to = new File(FileUtils.getInternalCertificateStoragePath(activity), certificateFile.getName());
		movedSuccessfully = certificateFile.renameTo(to);

		if (movedSuccessfully) {

			SharedPreferences settings = activity.getSharedPreferences(FileUtils.SHARED_PREFERENCES_CERTIFICATES_PASSWORDS, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(certificateFile.getName(), password);
			editor.apply();

			Toast.makeText(activity, R.string.import_successful, Toast.LENGTH_SHORT).show();
			return true;
		}
		else {
			Toast.makeText(activity, R.string.import_error_message_cant_copy_certificate, Toast.LENGTH_SHORT).show();
			return false;
		}
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
	 */
	private static @Nullable File createFileFromInputStream(Context context, InputStream inputStream, String fileName) {

		File fileFolder = new File(context.getCacheDir().getAbsolutePath() + File.separator + "temp_files");

		// Default case

		boolean accessible = fileFolder.exists() || fileFolder.mkdirs();
		if (!accessible)
			return null;

		//

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
