/*
 * iParapheur Android
 * Copyright (C) 2016-2019 Libriciel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.adullact.iparapheur.controller.rest.api;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.RESTUtils;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.RequestResponse;
import org.adullact.iparapheur.utils.AccountUtils;
import org.adullact.iparapheur.utils.HttpException;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.JsonExplorer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;


public abstract class RestClientApi implements IParapheurAPI {

    private static final String ACTION_LOGIN = "/parapheur/api/login";
    private static final String LOG_TAG = "RestClientApi";

    public static final long SESSION_TIMEOUT = 30 * 60 * 1000L;


    @Override public int test(Account account) throws IParapheurException {

        // Build request

        String requestContent = RESTUtils.getAuthenticationJsonData(account);
        String requestUrl = buildUrl(account, ACTION_LOGIN, null, false);

        RequestResponse response = RESTUtils.post(requestUrl, requestContent);

        // Parse response

        int messageRes = R.string.http_error_undefined;

        if (response == null)
            messageRes = R.string.http_error_undefined;
        else if (response.getCode() == HttpURLConnection.HTTP_OK)
            messageRes = R.string.test_ok;
        else if (response.getCode() == HttpURLConnection.HTTP_FORBIDDEN)
            messageRes = R.string.test_forbidden;
        else if (response.getCode() == HttpURLConnection.HTTP_NOT_FOUND)
            messageRes = R.string.test_not_found;
        else if ((response.getCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) && response.getError().contains("Tenant does not exist"))
            messageRes = R.string.test_tenant_not_exist;

        return messageRes;
    }

    @Override public String getTicket(Account account) throws IParapheurException {

        // Default case

        if (RESTUtils.hasValidTicket(account))
            return account.getTicket();

        // Building request

        String requestContent = RESTUtils.getAuthenticationJsonData(account);
        String requestUrl = buildUrl(account, ACTION_LOGIN, null, false);

        RequestResponse response = RESTUtils.post(requestUrl, requestContent);

        // Parsing response

        if (response != null) {

            String responseTicket = new JsonExplorer(response.getResponse()).findObject("data").optString("ticket");
            if (TextUtils.isEmpty(responseTicket))
                throw new IParapheurException(R.string.error_parse, null);

            account.setTicket(responseTicket);
        }

        return account.getTicket();
    }

    @Deprecated public @NonNull String buildUrl(@NonNull String action) throws IParapheurException {
        return buildUrl(action, null);
    }

    @Deprecated public @NonNull String buildUrl(@NonNull String action, @Nullable String params) throws IParapheurException {
        return buildUrl(AccountUtils.SELECTED_ACCOUNT, action, params, true);
    }

    public @NonNull String buildUrl(@NonNull Account account, @NonNull String action) throws IParapheurException {
        return buildUrl(account, action, null, true);
    }

    public @NonNull String buildUrl(Account account, @NonNull String action, @Nullable String params, boolean withTicket) throws IParapheurException {
        return buildUrl(account, action, params, withTicket, true);
    }

    public @NonNull String buildUrl(Account account, @NonNull String action, @Nullable String params, boolean withTicket,
                                    boolean withTenant) throws IParapheurException {

        // Default checks

        if (account == null)
            throw new IParapheurException(R.string.error_no_account, null);

        String ticket = null;
        if (withTicket)
            ticket = getTicket(account);

        if (withTicket && TextUtils.isEmpty(ticket))
            throw new IParapheurException(R.string.error_no_ticket, null);

        account.setLastRequest(new Date());

        // Build URL

        StringBuilder stringBuilder = new StringBuilder(BASE_PATH);

        if (withTenant && (!TextUtils.isEmpty(account.getTenant())))
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

    @Override public boolean downloadFile(@NonNull Account currentAccount, @NonNull String url, @NonNull String path) throws IParapheurException {

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

            // Close the output stream when done
            fileOutput.close();
        } catch (FileNotFoundException e) {
            Crashlytics.logException(e);
            throw new IParapheurException(R.string.error_file_not_found, null);
        } catch (IOException e) {
            Crashlytics.logException(e);
            throw new IParapheurException(R.string.error_parse, null);
        } finally {
            if (fileOutput != null) {
                try { fileOutput.close(); } catch (IOException logOrIgnore) { Log.e(LOG_TAG, logOrIgnore.getLocalizedMessage()); }
            }
        }

        return file.exists();
    }

    @Override public boolean downloadCertificate(@NonNull Account currentAccount, @NonNull String urlString,
                                                 @NonNull String certificateLocalPath) throws IParapheurException {

        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                throw new HttpException(connection.getResponseCode());

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(certificateLocalPath);

            byte data[] = new byte[4096];
            int count;

            while ((count = input.read(data)) != -1)
                output.write(data, 0, count);
        } catch (HttpException | IOException e) {
            Crashlytics.logException(e);
            Log.e(LOG_TAG, e.getLocalizedMessage());
            throw new IParapheurException(R.string.import_error_message_cant_download_certificate, e.getLocalizedMessage());
        } finally {

            try {
                if (output != null)
                    output.close();

                if (input != null)
                    input.close();
            } catch (IOException ignored) { }

            if (connection != null)
                connection.disconnect();
        }

        return new File(certificateLocalPath).exists();
    }

}
