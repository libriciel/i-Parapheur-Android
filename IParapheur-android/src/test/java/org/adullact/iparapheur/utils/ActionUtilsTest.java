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
package org.adullact.iparapheur.utils;

import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.adullact.iparapheur.model.Action.ARCHIVAGE;
import static org.adullact.iparapheur.model.Action.CACHET;
import static org.adullact.iparapheur.model.Action.EMAIL;
import static org.adullact.iparapheur.model.Action.ENCHAINER_CIRCUIT;
import static org.adullact.iparapheur.model.Action.JOURNAL;
import static org.adullact.iparapheur.model.Action.MAILSEC;
import static org.adullact.iparapheur.model.Action.REJET;
import static org.adullact.iparapheur.model.Action.SECRETARIAT;
import static org.adullact.iparapheur.model.Action.SIGNATURE;
import static org.adullact.iparapheur.model.Action.TDT;
import static org.adullact.iparapheur.model.Action.TDT_ACTES;
import static org.adullact.iparapheur.model.Action.TDT_HELIOS;
import static org.adullact.iparapheur.model.Action.VISA;


public class ActionUtilsTest {

	@Test public void computePositiveAction() throws Exception {

		Dossier dossierWithRejetVisa = new Dossier();
		dossierWithRejetVisa.setActions(new HashSet<>(Arrays.asList(REJET, VISA)));
		Dossier dossierWithRejetSign = new Dossier();
		dossierWithRejetSign.setActions(new HashSet<>(Arrays.asList(REJET, SIGNATURE)));
		Dossier dossierWithRejetSeal = new Dossier();
		dossierWithRejetSeal.setActions(new HashSet<>(Arrays.asList(REJET, CACHET)));
		Dossier dossierWithVisa = new Dossier();
		dossierWithVisa.setActions(new HashSet<>(Collections.singletonList(VISA)));
		Dossier dossierWithTdt = new Dossier();
		dossierWithTdt.setActions(new HashSet<>(Collections.singletonList(TDT)));

		//

		List<Dossier> dossierList01 = Arrays.asList(dossierWithRejetVisa, dossierWithRejetSeal);
		Assert.assertEquals(ActionUtils.computePositiveAction(dossierList01), CACHET);

		List<Dossier> dossierList02 = Arrays.asList(dossierWithRejetVisa, dossierWithRejetSign);
		Assert.assertEquals(ActionUtils.computePositiveAction(dossierList02), SIGNATURE);

		List<Dossier> dossierList03 = Arrays.asList(dossierWithRejetVisa, dossierWithRejetVisa);
		Assert.assertEquals(ActionUtils.computePositiveAction(dossierList03), VISA);

		List<Dossier> dossierList04 = Arrays.asList(dossierWithRejetSeal, dossierWithRejetSign);
		Assert.assertEquals(ActionUtils.computePositiveAction(dossierList04), SIGNATURE);

		List<Dossier> dossierList05 = Arrays.asList(dossierWithRejetSeal, dossierWithTdt);
		Assert.assertNull(ActionUtils.computePositiveAction(dossierList05));
	}

	@Test public void computeNegativeAction() throws Exception {

		Dossier dossierWithRejetSeal = new Dossier();
		dossierWithRejetSeal.setActions(new HashSet<>(Arrays.asList(REJET, CACHET)));
		Dossier dossierWithVisa = new Dossier();
		dossierWithVisa.setActions(new HashSet<>(Collections.singletonList(CACHET)));

		//

		List<Dossier> dossierList01 = Arrays.asList(dossierWithRejetSeal, dossierWithRejetSeal);
		Assert.assertEquals(ActionUtils.computeNegativeAction(dossierList01), REJET);

		List<Dossier> dossierList02 = Arrays.asList(dossierWithRejetSeal, dossierWithVisa);
		Assert.assertNull(ActionUtils.computeNegativeAction(dossierList02));
	}

	@Test public void computeSecondaryActions() throws Exception {

		Dossier dossierWithVisaSecretary = new Dossier();
		dossierWithVisaSecretary.setActions(new HashSet<>(Arrays.asList(VISA, SECRETARIAT)));
		Dossier dossierWithVisaSecretaryChain = new Dossier();
		dossierWithVisaSecretaryChain.setActions(new HashSet<>(Arrays.asList(VISA, SECRETARIAT, ENCHAINER_CIRCUIT)));
		Dossier dossierWithVisaSecretaryChainJournal = new Dossier();
		dossierWithVisaSecretaryChainJournal.setActions(new HashSet<>(Arrays.asList(VISA, SECRETARIAT, ENCHAINER_CIRCUIT, JOURNAL)));
		Dossier dossierWithVisa = new Dossier();
		dossierWithVisa.setActions(new HashSet<>(Collections.singletonList(CACHET)));

		//

		List<Dossier> dossierList01 = Arrays.asList(dossierWithVisaSecretary, dossierWithVisaSecretaryChain);
		Assert.assertTrue(ActionUtils.computeSecondaryActions(dossierList01).contains(SECRETARIAT));
		Assert.assertEquals(ActionUtils.computeSecondaryActions(dossierList01).size(), 1);

		List<Dossier> dossierList02 = Arrays.asList(dossierWithVisaSecretaryChain, dossierWithVisaSecretaryChainJournal);
		Assert.assertTrue(ActionUtils.computeSecondaryActions(dossierList02).contains(SECRETARIAT));
		Assert.assertTrue(ActionUtils.computeSecondaryActions(dossierList02).contains(ENCHAINER_CIRCUIT));
		Assert.assertEquals(ActionUtils.computeSecondaryActions(dossierList02).size(), 2);

		List<Dossier> dossierList03 = Arrays.asList(dossierWithVisaSecretaryChain, dossierWithVisa);
		Assert.assertTrue(ActionUtils.computeSecondaryActions(dossierList03).isEmpty());

		List<Dossier> dossierList04 = Arrays.asList(dossierWithVisaSecretaryChainJournal, dossierWithVisaSecretaryChainJournal);
		Assert.assertEquals(ActionUtils.computeSecondaryActions(dossierList04).size(), 3);
	}

	@Test public void getPositiveAction() throws Exception {

		Dossier emptyDossier = new Dossier(null, null, null, null, null, null, null, null, false);

		Dossier dossier = new Dossier(
				null,
				null,
				null,
				new HashSet<>(Arrays.asList(VISA, MAILSEC, TDT, TDT_HELIOS, SIGNATURE, REJET, TDT_ACTES, ARCHIVAGE)),
				null,
				null,
				null,
				null,
				false
		);

		Dossier dossierWithActionDemandee = new Dossier(
				null,
				null,
				TDT,
				new HashSet<>(Arrays.asList(VISA, MAILSEC, TDT, TDT_HELIOS, SIGNATURE, REJET, TDT_ACTES, ARCHIVAGE)),
				null,
				null,
				null,
				null,
				false
		);

		// Checks

		Assert.assertNull(ActionUtils.getPositiveAction(emptyDossier));
		Assert.assertEquals(ActionUtils.getPositiveAction(dossierWithActionDemandee), TDT);
		Assert.assertEquals(ActionUtils.getPositiveAction(dossier), SIGNATURE);

		dossier.getActions().remove(SIGNATURE);
		Assert.assertEquals(ActionUtils.getPositiveAction(dossier), VISA);

		dossier.getActions().remove(VISA);
		Assert.assertEquals(ActionUtils.getPositiveAction(dossier), ARCHIVAGE);

		dossier.getActions().remove(ARCHIVAGE);
		Assert.assertEquals(ActionUtils.getPositiveAction(dossier), MAILSEC);

		dossier.getActions().remove(MAILSEC);
		Assert.assertEquals(ActionUtils.getPositiveAction(dossier), TDT_ACTES);

		dossier.getActions().remove(TDT_ACTES);
		Assert.assertEquals(ActionUtils.getPositiveAction(dossier), TDT_HELIOS);

		dossier.getActions().remove(TDT_HELIOS);
		Assert.assertEquals(ActionUtils.getPositiveAction(dossier), TDT);

		dossier.getActions().remove(TDT);
		Assert.assertNull(ActionUtils.getPositiveAction(dossier));
	}

	@Test public void getNegativeAction() throws Exception {

		Dossier emptyDossier = new Dossier(null, null, null, null, null, null, null, null, false);
		Dossier dossier = new Dossier(null, null, SIGNATURE, new HashSet<>(Arrays.asList(VISA, SIGNATURE, REJET, TDT)), null, null, null, null, false);

		// Checks

		Assert.assertNull(ActionUtils.getNegativeAction(emptyDossier));
		Assert.assertEquals(ActionUtils.getNegativeAction(dossier), REJET);

		dossier.getActions().remove(REJET);
		Assert.assertNull(ActionUtils.getNegativeAction(dossier));
	}

	@Test public void fixActions() throws Exception {

		Dossier dossier01 = new Dossier(null, null, null, null, null, null, null, null, false);

		HashSet<Action> dossier02ActionsSet = new HashSet<>(Arrays.asList(VISA, SIGNATURE));
		Dossier dossier02 = new Dossier(null, null, SIGNATURE, dossier02ActionsSet, null, null, null, null, false);

		HashSet<Action> dossier03ActionsSet = new HashSet<>();
		Dossier dossier03 = new Dossier(null, null, ARCHIVAGE, dossier03ActionsSet, null, null, null, null, false);

		ActionUtils.fixActions(dossier01);
		ActionUtils.fixActions(dossier02);
		ActionUtils.fixActions(dossier03);

		// Checks

		Assert.assertTrue(dossier01.getActions().contains(VISA));
		Assert.assertTrue(dossier01.getActions().contains(SIGNATURE));
		Assert.assertEquals(dossier01.getActionDemandee(), VISA);
		Assert.assertEquals(dossier01.getActions().size(), 2);

		Assert.assertTrue(dossier02.getActions().contains(SIGNATURE));
		Assert.assertEquals(dossier02.getActionDemandee(), SIGNATURE);
		Assert.assertEquals(dossier02.getActions().size(), 1);

		Assert.assertTrue(dossier03.getActions().contains(ARCHIVAGE));
		Assert.assertEquals(dossier03.getActions().size(), 1);
	}

}