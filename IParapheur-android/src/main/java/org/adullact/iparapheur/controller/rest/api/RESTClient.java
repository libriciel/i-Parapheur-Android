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

	public static final int API_VERSION_MAX = 4;

	private final RestClientApi1 restClientAPI1 = new RestClientApi1();
	private final RestClientApi3 restClientAPI3 = new RestClientApi3();
	private final RestClientApi3 restClientAPI4 = new RestClientApi4();

	public int getApiVersion(@NonNull Account account) throws IParapheurException {
		return getApiVersion(account, true, false);
	}

	/**
	 * Renvoie la version d'API du serveur i-Parapheur associé à ce compte.
	 * Cette méthode peut faire une requête au serveur, il faut donc l'appeler dans
	 * un thread (ex. AsyncTask).
	 *
	 * @param account le compte pour lequel on veur récupérer la version de l'API
	 * @return in entier représentant la version de l'API.
	 */
	private int getApiVersion(@NonNull Account account, boolean withTenant, boolean withAuthentication) throws IParapheurException {

		// Default check

		Integer apiVersion = account.getApiVersion();
		if ((apiVersion != null))
			return apiVersion;

		// Request

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
				try { getApiVersion(account, false, withAuthentication); }
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
				try { getApiVersion(account, false, withAuthentication); }
				catch (IParapheurException subEx) { isReachableWithoutTenant = (subEx.getResId() != R.string.error_server_not_configured); }

				if (isReachableWithoutTenant)
					throw new IParapheurException(R.string.error_server_not_configured_for_tenant, null);

				throw e;
			}

			// Authentication is mandatory on (API < 3)
			// So we have to retrieve a ticket, and recursive call the method
			else if ((e.getResId() == R.string.http_error_401) && !withAuthentication) {

				restClientAPI1.getTicket(account);
				apiVersion = getApiVersion(account, withTenant, true);
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

	@Override public List<Bureau> getBureaux(@NonNull Account currentAccount) throws IParapheurException {
		return getRESTClient(currentAccount).getBureaux(currentAccount);
	}

	private IParapheurAPI getRESTClient(@NonNull Account account) throws IParapheurException {

		Integer apiVersion = account.getApiVersion();
		IParapheurAPI apiClient = null;

		if ((account.getApiVersion() == null) || (account.getApiVersion() < 0))
			apiVersion = this.getApiVersion(account);

		if (apiVersion > API_VERSION_MAX)
			throw new IParapheurException(R.string.Error_forward_parapheur_version, AccountUtils.SELECTED_ACCOUNT.getTitle());

		switch (apiVersion) {
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

	@Override public Dossier getDossier(@NonNull Account currentAccount, String bureauId, String dossierId) throws IParapheurException {
		return getRESTClient(currentAccount).getDossier(currentAccount, bureauId, dossierId);
	}

	@Override public List<Dossier> getDossiers(@NonNull Account currentAccount, @NonNull String bureauId, @Nullable Filter filter) throws IParapheurException {
		return getRESTClient(currentAccount).getDossiers(currentAccount, bureauId, filter);
	}

	@Override public List<ParapheurType> getTypologie(@NonNull Account currentAccount) throws IParapheurException {
		return getRESTClient(currentAccount).getTypologie(currentAccount);
	}

	@Override public Circuit getCircuit(@NonNull Account currentAccount, String dossierId) throws IParapheurException {
		return getRESTClient(currentAccount).getCircuit(currentAccount, dossierId);
	}

	public SerializableSparseArray<PageAnnotations> getAnnotations(@NonNull Account currentAccount, @NonNull String dossierId,
																   @NonNull String documentId) throws IParapheurException {
		return getRESTClient(currentAccount).getAnnotations(currentAccount, dossierId, documentId);
	}

	@Override public String createAnnotation(@NonNull Account currentAccount, @NonNull String dossierId, @NonNull String documentId,
											 @NonNull Annotation annotation, int page) throws IParapheurException {
		return getRESTClient(currentAccount).createAnnotation(currentAccount, dossierId, documentId, annotation, page);
	}

	@Override public void updateAnnotation(@NonNull Account currentAccount, @NonNull String dossierId, @NonNull String documentId,
										   @NonNull Annotation annotation, int page) throws IParapheurException {
		getRESTClient(currentAccount).updateAnnotation(currentAccount, dossierId, documentId, annotation, page);
	}

	@Override public void deleteAnnotation(@NonNull Account currentAccount, @NonNull String dossierId, @NonNull String documentId, @NonNull String annotationId,
										   int page) throws IParapheurException {
		getRESTClient(currentAccount).deleteAnnotation(currentAccount, dossierId, documentId, annotationId, page);
	}

	@Override public boolean downloadFile(@NonNull Account currentAccount, @NonNull String url, @NonNull String path) throws IParapheurException {
		return getRESTClient(currentAccount).downloadFile(currentAccount, url, path);
	}

	@Override public boolean downloadCertificate(@NonNull Account currentAccount, @NonNull String urlString,
												 @NonNull String certificateLocalPath) throws IParapheurException {
		return getRESTClient(currentAccount).downloadCertificate(currentAccount, urlString, certificateLocalPath);
	}

	@Override public boolean viser(@NonNull Account currentAccount, Dossier dossier, String annotPub, String annotPriv,
								   String bureauId) throws IParapheurException {
		return getRESTClient(currentAccount).viser(currentAccount, dossier, annotPub, annotPriv, bureauId);
	}

	@Override public boolean signer(@NonNull Account currentAccount, String dossierId, String signValue, String annotPub, String annotPriv,
									String bureauId) throws IParapheurException {
		return getRESTClient(currentAccount).signer(currentAccount, dossierId, signValue, annotPub, annotPriv, bureauId);
	}

	@Override public boolean signPapier(@NonNull Account currentAccount, String dossierId, String bureauId) throws IParapheurException {
		return getRESTClient(currentAccount).signPapier(currentAccount, dossierId, bureauId);
	}

	@Override public boolean archiver(@NonNull Account currentAccount, String dossierId, String archiveTitle, boolean withAnnexes,
									  String bureauId) throws IParapheurException {
		return getRESTClient(currentAccount).archiver(currentAccount, dossierId, archiveTitle, withAnnexes, bureauId);
	}

	@Override public boolean envoiTdtHelios(@NonNull Account currentAccount, String dossierId, String annotPub, String annotPriv,
											String bureauId) throws IParapheurException {
		return getRESTClient(currentAccount).envoiTdtHelios(currentAccount, dossierId, annotPub, annotPriv, bureauId);
	}

	@Override public boolean envoiTdtActes(@NonNull Account currentAccount, String dossierId, String nature, String classification, String numero,
										   long dateActes, String objet, String annotPub, String annotPriv, String bureauId) throws IParapheurException {
		return getRESTClient(currentAccount).envoiTdtActes(currentAccount,
														   dossierId,
														   nature,
														   classification,
														   numero,
														   dateActes,
														   objet,
														   annotPub,
														   annotPriv,
														   bureauId
		);
	}

	@Override public boolean envoiMailSec(@NonNull Account currentAccount, String dossierId, List<String> destinataires, List<String> destinatairesCC,
										  List<String> destinatairesCCI, String sujet, String message, String password, boolean showPassword,
										  boolean annexesIncluded, String bureauId) throws IParapheurException {
		// TODO : manage annexes
		return getRESTClient(currentAccount).envoiMailSec(currentAccount,
														  dossierId,
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

	@Override public boolean rejeter(@NonNull Account currentAccount, String dossierId, String annotPub, String annotPriv,
									 String bureauId) throws IParapheurException {
		return getRESTClient(currentAccount).rejeter(currentAccount, dossierId, annotPub, annotPriv, bureauId);
	}

	@Override public SignInfo getSignInfo(@NonNull Account currentAccount, String dossierId, String bureauId) throws IParapheurException {
		return getRESTClient(currentAccount).getSignInfo(currentAccount, dossierId, bureauId);
	}

}
