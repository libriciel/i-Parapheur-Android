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

import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import static org.adullact.iparapheur.model.Action.ARCHIVAGE;
import static org.adullact.iparapheur.model.Action.CACHET;
import static org.adullact.iparapheur.model.Action.JOURNAL;
import static org.adullact.iparapheur.model.Action.MAILSEC;
import static org.adullact.iparapheur.model.Action.REJET;
import static org.adullact.iparapheur.model.Action.SECRETARIAT;
import static org.adullact.iparapheur.model.Action.SIGNATURE;
import static org.adullact.iparapheur.model.Action.TDT;
import static org.adullact.iparapheur.model.Action.TDT_ACTES;
import static org.adullact.iparapheur.model.Action.TDT_HELIOS;
import static org.adullact.iparapheur.model.Action.VISA;


public class ActionUtils {

	/**
	 * Returns the main negative {@link Action} available, by coherent priority.
	 */
	public static @Nullable Action computePositiveAction(@NonNull Iterable<Dossier> dossierList) {

		LinkedHashSet<Action> results = new LinkedHashSet<>();
		results.addAll(Arrays.asList(VISA, CACHET, SIGNATURE, TDT_ACTES, TDT_HELIOS, TDT, ARCHIVAGE, MAILSEC, SECRETARIAT));

		for (Dossier dossier : dossierList) {

			LinkedHashSet<Action> tempSet = new LinkedHashSet<>(dossier.getActions());

			if (tempSet.contains(VISA))
				tempSet.add(CACHET);

			if (tempSet.contains(CACHET))
				tempSet.add(SIGNATURE);

			results.retainAll(tempSet);
		}

		return (results.isEmpty() ? null : results.iterator().next());
	}

	public static @Nullable Action computeNegativeAction(@NonNull Iterable<Dossier> dossierList) {

		LinkedHashSet<Action> results = new LinkedHashSet<>();
		results.addAll(Collections.singletonList(REJET));

		for (Dossier dossier : dossierList) {
			LinkedHashSet<Action> tempSet = new LinkedHashSet<>(dossier.getActions());
			results.retainAll(tempSet);
		}

		return (results.isEmpty() ? null : results.iterator().next());
	}

	public static @NonNull List<Action> computeSecondaryActions(@NonNull Iterable<Dossier> dossierList) {

		LinkedHashSet<Action> results = new LinkedHashSet<>();
		results.addAll(new ArrayList<>(Arrays.asList(Action.values())));

		for (Dossier dossier : dossierList) {
			LinkedHashSet<Action> tempSet = new LinkedHashSet<>(dossier.getActions());
			results.retainAll(tempSet);
		}

		results.removeAll(Arrays.asList(VISA, CACHET, SIGNATURE, REJET));
		return new ArrayList<>(results);
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

}
