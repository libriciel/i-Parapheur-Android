package org.adullact.iparapheur.controller.rest.api;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.controller.rest.RESTUtils;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.RequestResponse;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.JsonExplorer;
import org.json.JSONException;
import org.json.JSONStringer;

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

		RequestResponse response = RESTUtils.post(BASE_PATH + account.getServerBaseUrl() + ACTION_LOGIN, request);
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

		// Default case

		String ticket = account.getTicket();
		Long time = new Date().getTime();

		if ((!TextUtils.isEmpty(ticket)) && ((time - account.getLastRequest()) < SESSION_TIMEOUT))
			return ticket;

		// Building request

		String request;
		try {
			JSONStringer requestStringer = new JSONStringer();
			requestStringer.object();
			requestStringer.key("username").value(account.getLogin());
			requestStringer.key("password").value(account.getPassword());
			requestStringer.endObject();

			request = requestStringer.toString();
		}
		catch (JSONException ex) { throw new IParapheurException(R.string.error_parse, null); }

		// Parsing response

		RequestResponse response = RESTUtils.post(BASE_PATH + account.getServerBaseUrl() + ACTION_LOGIN, request);
		if (response != null) {

			String responseTicket = new JsonExplorer(response.getResponse()).findObject("data").optString("ticket");
			if (TextUtils.isEmpty(responseTicket))
				throw new IParapheurException(R.string.error_parse, null);

			account.setTicket(responseTicket);
		}

		return ticket;
	}

	public @NonNull String buildUrl(@NonNull String action) throws IParapheurException {
		return buildUrl(action, null);
	}

	public @NonNull String buildUrl(@NonNull String action, @Nullable String params) throws IParapheurException {
		return buildUrl(MyAccounts.INSTANCE.getSelectedAccount(), action, params, true);
	}

	public @NonNull String buildUrl(Account account, @NonNull String action, @Nullable String params, boolean withTicket) throws IParapheurException {

		// Default checks

		if (account == null)
			throw new IParapheurException(R.string.error_no_account, null);

		String ticket = getTicket(account);
		if (withTicket && TextUtils.isEmpty(ticket))
			throw new IParapheurException(R.string.error_no_ticket, null);

		account.setLastRequest(new Date().getTime());

		// Build URL

		StringBuilder stringBuilder = new StringBuilder(BASE_PATH);
		if (!TextUtils.isEmpty(account.getTenant()))
			stringBuilder.append(account.getTenant()).append(".");

		stringBuilder.append(account.getServerBaseUrl());
		stringBuilder.append(action);

		if (withTicket)
			stringBuilder.append("?alf_ticket=").append(ticket);

		if (!TextUtils.isEmpty(params))
			stringBuilder.append("&").append(params);

		//

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

			while ((bufferLength = response.read(buffer)) > 0)
				fileOutput.write(buffer, 0, bufferLength);

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
				try { fileOutput.close(); }
				catch (IOException logOrIgnore) { logOrIgnore.printStackTrace(); }
			}
		}

		return file.exists();
	}

}
