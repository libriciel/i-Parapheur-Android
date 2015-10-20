package org.adullact.iparapheur.controller.rest.api;

import android.os.Environment;

import com.crashlytics.android.Crashlytics;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.controller.rest.RESTUtils;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.RequestResponse;
import org.adullact.iparapheur.utils.IParapheurException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Date;


public abstract class RESTClientAPI implements IParapheurAPI {

	protected static final String ACTION_LOGIN = "/parapheur/api/login";
	private static final long SESSION_TIMEOUT = 30 * 60 * 1000l;

	@Override public int test(Account account) throws IParapheurException {
		int messageRes = R.string.test_unreachable;
		String request = "{'username': '" + account.getLogin() + "', 'password': '" + account.getPassword() + "'}";
		RequestResponse response = RESTUtils.post(BASE_PATH + account.getUrl() + ACTION_LOGIN, request);
		if (response != null) {
			if (response.getCode() == HttpURLConnection.HTTP_OK) {
				messageRes = R.string.test_ok;
			}
			else {
				switch (response.getCode()) {
					case HttpURLConnection.HTTP_FORBIDDEN:
						messageRes = R.string.test_forbidden;
						break;
					case HttpURLConnection.HTTP_NOT_FOUND:
						messageRes = R.string.test_not_found;
						break;
					case HttpURLConnection.HTTP_INTERNAL_ERROR:
						if (response.getError().contains("Tenant does not exist")) {
							messageRes = R.string.test_tenant_not_exist;
						}
						break;
				}
			}
		}
		return messageRes;
	}

	@Override public String getTicket(Account account) throws IParapheurException {

		String ticket = account.getTicket();
		Long time = new Date().getTime();

		if ((ticket == null) || ((time - account.getLastRequest()) > SESSION_TIMEOUT)) {
			try {
				String request = "{'username': '" + account.getLogin() + "', 'password': '" + account.getPassword() + "'}";
				RequestResponse response = RESTUtils.post(BASE_PATH + account.getUrl() + ACTION_LOGIN, request);
				if (response != null) {
					JSONObject json = response.getResponse();
					if (json != null) {
						json = json.getJSONObject("data");
						if (json != null) {
							ticket = json.getString("ticket");
							account.setTicket(ticket);
						}
					}
				}
			}
			catch (JSONException ex) {
				throw new IParapheurException(R.string.error_parse, null);
			}
		}

		return ticket;
	}

	public String buildUrl(String action) throws IParapheurException {
		return buildUrl(action, null);
	}

	public String buildUrl(String action, String params) throws IParapheurException {
		Account account = MyAccounts.INSTANCE.getSelectedAccount();
		String ticket = getTicket(account);
		String tenant = account.getTenant();

		if (ticket == null)
			throw new IParapheurException(R.string.error_no_ticket, null);

		account.setLastRequest(new Date().getTime());

		StringBuilder stringBuilder = new StringBuilder(BASE_PATH);
		stringBuilder.append((tenant != null) ? tenant + "." : "");
		stringBuilder.append(MyAccounts.INSTANCE.getSelectedAccount().getUrl());
		stringBuilder.append(action);
		stringBuilder.append("?alf_ticket=").append(ticket);
		stringBuilder.append(((params != null) ? "&" + params : ""));

		return stringBuilder.toString();
	}

	@Override public boolean downloadFile(String url, String path) throws IParapheurException {

		String state = Environment.getExternalStorageState();

		if (!Environment.MEDIA_MOUNTED.equals(state))
			throw new IParapheurException(R.string.error_no_storage, null);

		File file = new File(path);
		String fullUrl = buildUrl(url);

		FileOutputStream fileOutput = null;

		try {
			InputStream response = RESTUtils.downloadFile(fullUrl);
			fileOutput = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int bufferLength;
			while ((bufferLength = response.read(buffer)) > 0) {
				fileOutput.write(buffer, 0, bufferLength);
			}
			//close the output stream when done
			fileOutput.close();
		}
		catch (FileNotFoundException e) {
			Crashlytics.logException(e);
			throw new IParapheurException(R.string.error_file_not_found, null);
		}
		catch (IOException e) {
			Crashlytics.logException(e);
			throw new IParapheurException(R.string.error_parse, null);
		}
		finally {
			if (fileOutput != null) {
				try {
					fileOutput.close();
				}
				catch (IOException logOrIgnore) {
					logOrIgnore.printStackTrace();
				}
			}
		}

		return file.exists();
	}

}
