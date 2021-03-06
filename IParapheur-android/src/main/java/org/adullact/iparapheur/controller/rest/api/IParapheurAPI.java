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

import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Annotation;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Circuit;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.Filter;
import org.adullact.iparapheur.model.PageAnnotations;
import org.adullact.iparapheur.model.ParapheurType;
import org.adullact.iparapheur.model.SignInfo;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.SerializableSparseArray;

import java.util.List;


public interface IParapheurAPI {

	String BASE_PATH = "https://m.";

	/**
	 * Test si un compte est accessible.
	 *
	 * @param account le compte à tester.
	 * @return l'id d'une ressource (string) indiquant l'état d'accessibilité du compte.
	 */
	int test(@NonNull Account account) throws IParapheurException;

	/**
	 * Retourne un ticket d'authentification associé au compte passé en paramètre
	 *
	 * @param account le compte sur lequel se connecter
	 * @return un ticket d'authentification
	 * @throws IParapheurException
	 */
	String getTicket(@NonNull Account account) throws IParapheurException;

	List<Bureau> getBureaux(@NonNull Account currentAccount) throws IParapheurException;

	Dossier getDossier(@NonNull Account currentAccount, String bureauId, String dossierId) throws IParapheurException;

	List<Dossier> getDossiers(@NonNull Account currentAccount, @NonNull String bureauId, @Nullable Filter filter) throws IParapheurException;

	List<ParapheurType> getTypologie(@NonNull Account currentAccount) throws IParapheurException;

	Circuit getCircuit(@NonNull Account currentAccount, String dossierId) throws IParapheurException;

	SignInfo getSignInfo(@NonNull Account currentAccount, String dossierId, String bureauId) throws IParapheurException;

	/**
	 * @return les annotations graphiques déposées sur le document principal du dossier
	 * @throws IParapheurException
	 */
	SerializableSparseArray<PageAnnotations> getAnnotations(@NonNull Account currentAccount, @NonNull String dossierId,
															@NonNull String documentId) throws IParapheurException;

	/**
	 * @return l'id de l'annotation crééeles annotations graphiques déposées sur le document principal du dossier
	 * @throws IParapheurException
	 */
	String createAnnotation(@NonNull Account currentAccount, @NonNull String dossierId, @NonNull String documentId, @NonNull Annotation annotation,
							int page) throws IParapheurException;

	void updateAnnotation(@NonNull Account currentAccount, @NonNull String dossierId, @NonNull String documentId, @NonNull Annotation annotation,
						  int page) throws IParapheurException;

	void deleteAnnotation(@NonNull Account currentAccount, @NonNull String dossierId, @NonNull String documentId, @NonNull String annotationId,
						  int page) throws IParapheurException;

	boolean updateAccountInformations(@NonNull Account currentAccount) throws IParapheurException;

	boolean downloadFile(@NonNull Account currentAccount, @NonNull String url, @NonNull String path) throws IParapheurException;

	boolean downloadCertificate(@NonNull Account currentAccount, @NonNull String urlString, @NonNull String certificateLocalPath) throws IParapheurException;

	boolean viser(@NonNull Account currentAccount, Dossier dossier, String annotPub, String annotPriv, String bureauId) throws IParapheurException;

	boolean seal(@NonNull Account currentAccount, Dossier dossier, String annotPub, String annotPriv, String bureauId) throws IParapheurException;

	boolean signer(@NonNull Account currentAccount, String dossierId, String signValue, String annotPub, String annotPriv,
				   String bureauId) throws IParapheurException;

	boolean signPapier(@NonNull Account currentAccount, String dossierId, String bureauId) throws IParapheurException;

	boolean archiver(@NonNull Account currentAccount, String dossierId, String archiveTitle, boolean withAnnexes, String bureauId) throws IParapheurException;

	boolean envoiTdtHelios(@NonNull Account currentAccount, String dossierId, String annotPub, String annotPriv, String bureauId) throws IParapheurException;

	boolean envoiTdtActes(@NonNull Account currentAccount, String dossierId, String nature, String classification, String numero, long dateActes, String objet,
						  String annotPub, String annotPriv, String bureauId) throws IParapheurException;

	boolean envoiMailSec(@NonNull Account currentAccount, String dossierId, List<String> destinataires, List<String> destinatairesCC,
						 List<String> destinatairesCCI, String sujet, String message, String password, boolean showPassword, boolean annexesIncluded,
						 String bureauId) throws IParapheurException;

	boolean rejeter(@NonNull Account currentAccount, String dossierId, String annotPub, String annotPriv, String bureauId) throws IParapheurException;
}
