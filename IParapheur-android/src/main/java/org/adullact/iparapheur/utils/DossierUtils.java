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

	/**
	 * Returns the main negative {@link Action} available, by coherent priority.
	 */
	public static @Nullable Action getPositiveAction(@NonNull Dossier dossier) {

		// Default case

		if (dossier.getActions() == null)
			return null;

		// Finding Action

		HashSet<Action> actions = new HashSet<>(Arrays.asList(Action.values()));
		actions.retainAll(dossier.getActions());

		if (dossier.getActionDemandee() != null)
			return dossier.getActionDemandee();

		if (actions.contains(Action.SIGNATURE))
			return Action.SIGNATURE;
		else if (actions.contains(VISA))
			return VISA;
		else if (actions.contains(Action.ARCHIVAGE))
			return Action.ARCHIVAGE;
		else if (actions.contains(Action.MAILSEC))
			return Action.MAILSEC;
		else if (actions.contains(Action.TDT_ACTES))
			return Action.TDT_ACTES;
		else if (actions.contains(Action.TDT_HELIOS))
			return Action.TDT_HELIOS;
		else if (actions.contains(Action.TDT))
			return Action.TDT;

		return null;
	}

	/**
	 * Returns the main negative {@link Action} available, by coherent priority.
	 */
	public static @Nullable Action getNegativeAction(@NonNull Dossier dossier) {

		// Default case

		if (dossier.getActions() == null)
			return null;

		// Finding Action

		HashSet<Action> actions = new HashSet<>(Arrays.asList(Action.values()));
		actions.retainAll(dossier.getActions());

		if (actions.contains(Action.REJET))
			return Action.REJET;

		return null;
	}

	/**
	 * Patching a weird Signature case :
	 * "actionDemandee" can have any "actions" value...
	 * ... Except when "actionDemandee=SIGNATURE", where "actions" only contains VISA, for some reason
	 *
	 * A SIGNATURE action is acceptable in VISA too...
	 *
	 * @param dossier , the dossier to fix
	 */
	public static void fixActions(@NonNull Dossier dossier) {

		// Default init
		// (Useful after Gson parsing)

		if (dossier.getActions() == null)
			dossier.setActions(new HashSet<Action>());

		if (dossier.getActionDemandee() == null)
			dossier.setActionDemandee(VISA);

		// Yep, sometimes it happens

		if (!dossier.getActions().contains(dossier.getActionDemandee()))
			dossier.getActions().add(dossier.getActionDemandee());

		// Fixing signature logic

		if (dossier.getActionDemandee() == Action.SIGNATURE) {
			dossier.getActions().remove(VISA);
			dossier.getActions().add(Action.SIGNATURE);
		}

		if (dossier.getActionDemandee() == VISA)
			dossier.getActions().add(Action.SIGNATURE);
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

		final List<Dossier> dossierToDeleteList = new ArrayList<>();

		for (Bureau bureau : parentBureauList)
			CollectionUtils.safeAddAll(dossierToDeleteList, bureau.getChildrenDossiers());

		dossierToDeleteList.removeAll(newDossierList);

		return dossierToDeleteList;
	}

	public static @NonNull List<Document> getAllChildrenDocuments(@Nullable List<Dossier> dossierList) {

		List<Document> result = new ArrayList<>();

		if (dossierList != null)
			for (Dossier dossier : dossierList)
				if (dossier.getChildrenDocuments() != null)
					CollectionUtils.safeAddAll(result, dossier.getChildrenDocuments());

		return result;
	}
}
