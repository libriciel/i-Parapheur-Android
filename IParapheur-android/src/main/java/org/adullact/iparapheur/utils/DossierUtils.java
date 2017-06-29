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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import static org.adullact.iparapheur.model.Action.VISA;


public class DossierUtils {

	public static boolean haveActions(@NonNull Dossier dossier) {

		HashSet<Action> actionsAvailable = new HashSet<>();

		if (dossier.getActions() != null)
			actionsAvailable.addAll(dossier.getActions());

		actionsAvailable.remove(Action.EMAIL);
		actionsAvailable.remove(Action.JOURNAL);
		actionsAvailable.remove(Action.ENREGISTRER);

		return actionsAvailable.size() > 0;
	}

	public static boolean areDetailsAvailable(@NonNull Dossier dossier) {

		return (dossier.getCircuit() != null)                                     //
				&& (dossier.getCircuit().getEtapeCircuitList() != null)           //
				&& (!dossier.getCircuit().getEtapeCircuitList().isEmpty())        //
				&& (!dossier.getDocumentList().isEmpty());
	}

	public static @Nullable Document findCurrentDocument(@Nullable Dossier dossier, @Nullable String documentId) {

		// Default case

		if (dossier == null)
			return null;

		// Finding doc

		if (!TextUtils.isEmpty(documentId))
			for (Document document : dossier.getDocumentList())
				if (TextUtils.equals(document.getId(), documentId))
					return document;

		// Else, finding any document

		return dossier.getDocumentList().isEmpty() ? null : dossier.getDocumentList().get(0);
	}

	public static @NonNull List<Document> getMainDocuments(@Nullable Dossier dossier) {

		// Default case

		if ((dossier == null) || (dossier.getDocumentList()) == null || (dossier.getDocumentList().isEmpty()))
			return new ArrayList<>();

		//

		ArrayList<Document> result = new ArrayList<>();
		for (Document document : dossier.getDocumentList())
			if (DocumentUtils.isMainDocument(dossier, document))
				result.add(document);

		return result;
	}

	public static @NonNull List<Document> getAnnexes(@Nullable Dossier dossier) {

		// Default case

		if ((dossier == null) || (dossier.getDocumentList()) == null || (dossier.getDocumentList().isEmpty()))
			return new ArrayList<>();

		//

		ArrayList<Document> result = new ArrayList<>();
		for (Document document : dossier.getDocumentList())
			if (!DocumentUtils.isMainDocument(dossier, document))
				result.add(document);

		return result;
	}

	public static @NonNull List<Dossier> getDeletableDossierList(@NonNull List<Bureau> parentBureauList, @NonNull List<Dossier> newDossierList) {

		List<Dossier> dossierToDeleteList = new ArrayList<>();

		for (Bureau parentBureau : parentBureauList)
			CollectionUtils.safeAddAll(dossierToDeleteList, parentBureau.getChildrenDossiers());

		dossierToDeleteList.removeAll(newDossierList);
		return dossierToDeleteList;
	}

	public static @NonNull List<Dossier> getAllChildrenFrom(@Nullable List<Bureau> bureauList) {

		List<Dossier> result = new ArrayList<>();

		if (bureauList != null)
			for (Bureau bureau : bureauList)
				CollectionUtils.safeAddAll(result, bureau.getChildrenDossiers());

		return result;
	}

	public static @NonNull Comparator<Dossier> buildCreationDateComparator() {

		return new Comparator<Dossier>() {
			@Override public int compare(Dossier lhs, Dossier rhs) {

				if ((lhs == null) || (lhs.getDateCreation() == null))
					return Integer.MIN_VALUE;

				if ((rhs == null) || (rhs.getDateCreation() == null))
					return Integer.MAX_VALUE;

				return lhs.getDateCreation().compareTo(rhs.getDateCreation());
			}
		};
	}

}
