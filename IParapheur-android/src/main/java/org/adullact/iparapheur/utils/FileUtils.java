/*
 * <p>iParapheur Android<br/>
 * Copyright (C) 2016 Adullact-Projet.</p>
 *
 * <p>This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.</p>
 *
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.</p>
 *
 * <p>You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.</p>
 */
package org.adullact.iparapheur.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.model.Dossier;

import java.io.File;
import java.io.FileInputStream;
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

	private static final String LOG = "FileUtils";
	public static final String SHARED_PREFERENCES_CERTIFICATES_PASSWORDS = ":iparapheur:shared_preferences_certificates_passwords";

	private static final String ASSET_DEMO_PDF_FILE_NAME = "offline_test_file.pdf";
	private static final String ASSET_CERIFICATES_IMPORT_TUTO = "i-Parapheur_mobile_import_certificats_v1.pdf";

	private static final String DOSSIER_DATA_FOLDER_NAME = "dossiers";

	private static void copy(@NonNull File src, @NonNull File dst) {

		try {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dst);

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static @Nullable File getDirectoryForDossier(@NonNull Context context, @NonNull Dossier dossier) {

		File folder = new File(context.getExternalFilesDir(null) + File.separator + DOSSIER_DATA_FOLDER_NAME);
		if (!folder.exists())
			//noinspection ResultOfMethodCallIgnored
			folder.mkdirs();

		File directory = new File(folder.getAbsolutePath(), dossier.getId());

		if (!directory.mkdirs())
			if (!directory.exists())
				Log.e(LOG, "getDirectoryForDossier failed");

		return directory;
	}

	public static void launchCertificateTutoPdfIntent(@NonNull Context context) {

		File pdfFile = createFileFromAsset(context, ASSET_CERIFICATES_IMPORT_TUTO);
		File fileFolder = context.getExternalFilesDir(null);

		// Default case
		// Should never happen, but hides IDE warnings

		if ((pdfFile == null) || (fileFolder == null))
			return;

		// The Asset file is in the internal folder, and cannot be shared directly.
		// We have to declare a FileProvider, and manage exported files or not...
		// Here, we simply move this asset into the external data folder.
		// That's easier, and takes way less code to manage.

		File exportablePdfFile = new File(fileFolder.getAbsolutePath() + File.separator + pdfFile.getName());
		if (!exportablePdfFile.exists())
			copy(pdfFile, exportablePdfFile);

		// View Intent, calling any PDF viewer

		Intent intentShareFile = new Intent(Intent.ACTION_VIEW);
		intentShareFile.setDataAndType(Uri.fromFile(exportablePdfFile), "application/pdf");
		context.startActivity(Intent.createChooser(intentShareFile, context.getString(R.string.Choose_an_app)));
	}

	private static @Nullable File getInternalCertificateStoragePath(@NonNull Context context) {

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

	private static @NonNull List<File> getBksFromFolder(@NonNull File folder) {

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

	public static long getFreeSpace(@NonNull Context context) {

		File folder = new File(context.getExternalFilesDir(null) + File.separator + DOSSIER_DATA_FOLDER_NAME);

		if (!folder.exists())
			//noinspection ResultOfMethodCallIgnored
			folder.mkdirs();

		StatFs statFs = new StatFs(folder.getAbsolutePath());

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2)
			//noinspection deprecation
			return (statFs.getAvailableBlocks() * statFs.getBlockSize());
		else
			return (statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong());
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
			//noinspection ResultOfMethodCallIgnored
			file.delete(); // removing previous file, if exists.

			OutputStream outputStream = new FileOutputStream(file);
			byte buffer[] = new byte[1024];
			int length;

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
