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
package org.adullact.iparapheur.controller.rest.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.RESTUtils;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Annotation;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Circuit;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.Filter;
import org.adullact.iparapheur.model.PageAnnotations;
import org.adullact.iparapheur.model.ParapheurType;
import org.adullact.iparapheur.model.RequestResponse;
import org.adullact.iparapheur.model.SignInfo;
import org.adullact.iparapheur.utils.AccountUtils;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.JsonExplorer;
import org.adullact.iparapheur.utils.SerializableSparseArray;

import java.util.List;


public enum RESTClient implements IParapheurAPI {

	INSTANCE;
	private static final String RESOURCE_API_VERSION = "/parapheur/api/getApiLevel";
	private static final int API_VERSION_MAX = 4;

	private final RestClientApi1 restClientAPI1 = new RestClientApi1();
	private final RestClientApi3 restClientAPI3 = new RestClientApi3();
	private final RestClientApi3 restClientAPI4 = new RestClientApi4();

	private int getAPIVersion(@NonNull Account account) throws IParapheurException {
		return getAPIVersion(account, true, false);
	}

	/**
	 * Renvoie la version d'API du serveur i-Parapheur associé à ce compte.
	 * Cette méthode peut faire une requête au serveur, il faut donc l'appeler dans
	 * un thread (ex. AsyncTask).
	 *
	 * @param account le compte pour lequel on veur récupérer la version de l'API
	 * @return in entier représentant la version de l'API.
	 */
	private int getAPIVersion(@NonNull Account account, boolean withTenant, boolean withAuthentication) throws IParapheurException {

		// Default check

		Integer apiVersion = account.getApiVersion();
		if ((apiVersion != null))
			return apiVersion;

		// Request

		Log.v("Adrien", ">>>>> " + account);
		Log.v("Adrien", ">>>>> " + account.getServerBaseUrl());
		String url = restClientAPI4.buildUrl(account, RESOURCE_API_VERSION, null, withAuthentication, withTenant);

		try {
			RequestResponse response = RESTUtils.get(url);
			apiVersion = new JsonExplorer(response.getResponse()).optInt("level", -1);
		}
		catch (IParapheurException e) {

			// 404 errors may be Tenant unavailability
			// So we check for non-tenant reachability with a recursive call
			if ((e.getResId() == R.string.http_error_404) && withTenant) {

				boolean isReachableWithoutTenant = true;
				try { getAPIVersion(account, false, withAuthentication); }
				catch (IParapheurException subEx) { isReachableWithoutTenant = (subEx.getResId() != R.string.http_error_404); }

				if (isReachableWithoutTenant)
					throw new IParapheurException(R.string.test_tenant_not_exist, null);
				else
					throw new IParapheurException(R.string.test_unreachable, null);
			}

			// Certificate errors may be Tenant wrong parameter
			// So we check for non-tenant reachability with a recursive call
			if ((e.getResId() == R.string.error_server_not_configured) && withTenant) {

				boolean isReachableWithoutTenant = true;
				try { getAPIVersion(account, false, withAuthentication); }
				catch (IParapheurException subEx) { isReachableWithoutTenant = (subEx.getResId() != R.string.error_server_not_configured); }

				if (isReachableWithoutTenant)
					throw new IParapheurException(R.string.error_server_not_configured_for_tenant, null);

				throw e;
			}

			// Authentication is mandatory on (API < 3)
			// So we have to retrieve a ticket, and recursive call the method
			else if ((e.getResId() == R.string.http_error_401) && !withAuthentication) {

				restClientAPI1.getTicket(account);
				apiVersion = getAPIVersion(account, withTenant, true);
			}

			else {
				throw e;
			}
		}

		if (apiVersion == -1)
			throw new IParapheurException(R.string.error_mismatch_versions, account.getTitle());

		account.setApiVersion(apiVersion);
		return apiVersion;
	}

	@Override public int test(Account account) throws IParapheurException {
		return getRESTClient(account).test(account);
	}

	@Override public String getTicket(Account account) throws IParapheurException {
		return null;
	}

	@Override public boolean updateAccountInformations(@NonNull Account account) throws IParapheurException {
		return getRESTClient(account).updateAccountInformations(account);
	}

	@Override public List<Bureau> getBureaux(@NonNull Account account) throws IParapheurException {
		return getRESTClient().getBureaux(account);
	}

	private IParapheurAPI getRESTClient() throws IParapheurException {
		return getRESTClient(AccountUtils.SELECTED_ACCOUNT);
	}

	private IParapheurAPI getRESTClient(Account account) throws IParapheurException {
		Integer apiVersion = getAPIVersion(account);
		IParapheurAPI apiClient = null;

		if (apiVersion > API_VERSION_MAX)
			throw new IParapheurException(R.string.Error_forward_parapheur_version, AccountUtils.SELECTED_ACCOUNT.getTitle());

		switch (apiVersion) {
			case 1:
				apiClient = restClientAPI1;
				break;
			case 3:
				apiClient = restClientAPI3;
				break;
			case 4:
				apiClient = restClientAPI4;
				break;
		}

		if (apiClient == null)
			throw new IParapheurException(-1, "Unsupported API");

		return apiClient;
	}

	@Override public Dossier getDossier(String bureauId, String dossierId) throws IParapheurException {
		return getRESTClient(AccountUtils.SELECTED_ACCOUNT).getDossier(bureauId, dossierId);
	}

	@Override public List<Dossier> getDossiers(@NonNull Account account, @NonNull String bureauId, @Nullable Filter filter) throws IParapheurException {
		return getRESTClient(AccountUtils.SELECTED_ACCOUNT).getDossiers(account, bureauId, filter);
	}

	@Override public List<ParapheurType> getTypologie() throws IParapheurException {
		return getRESTClient().getTypologie();
	}

	@Override public Circuit getCircuit(String dossierId) throws IParapheurException {
		return getRESTClient().getCircuit(dossierId);
	}

	public SerializableSparseArray<PageAnnotations> getAnnotations(@NonNull String dossierId, @NonNull String documentId) throws IParapheurException {
		return getRESTClient().getAnnotations(dossierId, documentId);
	}

	@Override public String createAnnotation(@NonNull Account account, @NonNull String dossierId, @NonNull String documentId, @NonNull Annotation annotation,
											 int page) throws IParapheurException {
		return getRESTClient().createAnnotation(account, dossierId, documentId, annotation, page);
	}

	@Override public void updateAnnotation(@NonNull String dossierId, @NonNull String documentId, @NonNull Annotation annotation,
										   int page) throws IParapheurException {
		getRESTClient().updateAnnotation(dossierId, documentId, annotation, page);
	}

	@Override public void deleteAnnotation(@NonNull String dossierId, @NonNull String documentId, @NonNull String annotationId,
										   int page) throws IParapheurException {
		getRESTClient().deleteAnnotation(dossierId, documentId, annotationId, page);
	}

	@Override public boolean downloadFile(@NonNull String url, @NonNull String path) throws IParapheurException {
		return getRESTClient().downloadFile(url, path);
	}

	@Override public boolean downloadCertificate(@NonNull String urlString, @NonNull String certificateLocalPath) throws IParapheurException {
		return getRESTClient().downloadCertificate(urlString, certificateLocalPath);
	}

	@Override public boolean viser(Dossier dossier, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
		return getRESTClient().viser(dossier, annotPub, annotPriv, bureauId);
	}

	@Override public boolean signer(String dossierId, String signValue, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
		return getRESTClient().signer(dossierId, signValue, annotPub, annotPriv, bureauId);
	}

	@Override public boolean signPapier(String dossierId, String bureauId) throws IParapheurException {
		return getRESTClient().signPapier(dossierId, bureauId);
	}

	@Override public boolean archiver(String dossierId, String archiveTitle, boolean withAnnexes, String bureauId) throws IParapheurException {
		return getRESTClient().archiver(dossierId, archiveTitle, withAnnexes, bureauId);
	}

	@Override public boolean envoiTdtHelios(String dossierId, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
		return getRESTClient().envoiTdtHelios(dossierId, annotPub, annotPriv, bureauId);
	}

	@Override public boolean envoiTdtActes(String dossierId, String nature, String classification, String numero, long dateActes, String objet, String annotPub,
										   String annotPriv, String bureauId) throws IParapheurException {
		return getRESTClient().envoiTdtActes(dossierId, nature, classification, numero, dateActes, objet, annotPub, annotPriv, bureauId);
	}

	@Override public boolean envoiMailSec(String dossierId, List<String> destinataires, List<String> destinatairesCC, List<String> destinatairesCCI,
										  String sujet, String message, String password, boolean showPassword, boolean annexesIncluded,
										  String bureauId) throws IParapheurException {
		// TODO : manage annexes
		return getRESTClient().envoiMailSec(dossierId,
											destinataires,
											destinatairesCC,
											destinatairesCCI,
											sujet,
											message,
											password,
											showPassword,
											annexesIncluded,
											bureauId
		);
	}

	@Override public boolean rejeter(String dossierId, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
		return getRESTClient().rejeter(dossierId, annotPub, annotPriv, bureauId);
	}

	@Override public SignInfo getSignInfo(String dossierId, String bureauId) throws IParapheurException {
		return getRESTClient().getSignInfo(dossierId, bureauId);
	}

}
