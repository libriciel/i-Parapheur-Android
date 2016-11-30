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
package org.adullact.iparapheur.database;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.j256.ormlite.dao.Dao;

import junit.framework.Assert;

import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.PageAnnotations;
import org.adullact.iparapheur.utils.SerializableSparseArray;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import static org.adullact.iparapheur.model.Action.AVIS_COMPLEMENTAIRE;
import static org.adullact.iparapheur.model.Action.EMAIL;
import static org.adullact.iparapheur.model.Action.ENREGISTRER;
import static org.adullact.iparapheur.model.Action.JOURNAL;
import static org.adullact.iparapheur.model.Action.REJET;
import static org.adullact.iparapheur.model.Action.SECRETARIAT;
import static org.adullact.iparapheur.model.Action.SIGNATURE;
import static org.adullact.iparapheur.model.Action.TDT;
import static org.adullact.iparapheur.model.Action.TRANSFERT_SIGNATURE;
import static org.adullact.iparapheur.model.Action.VISA;


@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
public class DatabaseHelperTest {

	private static DatabaseHelper sDbHelper;

	@BeforeClass public static void setup() {
		Context context = InstrumentationRegistry.getTargetContext();
		context.deleteDatabase(DatabaseHelper.DATABASE_NAME);
		sDbHelper = new DatabaseHelper(context);
	}

	@Test public void order01_onCreate() throws Exception {

		Dao<Bureau, Integer> bureauDao = sDbHelper.getBureauDao();
		Dao<Dossier, Integer> dossierDao = sDbHelper.getDossierDao();
		Dao<Document, Integer> documentDao = sDbHelper.getDocumentDao();

		// Checks

		Assert.assertEquals(bureauDao.getTableName(), "Desk");
		Assert.assertEquals(dossierDao.getTableName(), "Folder");
		Assert.assertEquals(documentDao.getTableName(), "Document");
	}

	@Test public void order02_getBureauDao() throws Exception {

		Bureau bureau01 = new Bureau("id_01", "Bureau 01", 21, 11);
		Bureau bureau02 = new Bureau("id_02", "Bureau 02 \"/%@&éè", 22, 12);
		Bureau bureau03 = new Bureau("id_03", null, 23, 13);

		sDbHelper.getBureauDao().create(Arrays.asList(bureau01, bureau02, bureau03));

		Bureau bureau01db = sDbHelper.getBureauDao().queryForSameId(bureau01);
		Bureau bureau02db = sDbHelper.getBureauDao().queryForSameId(bureau02);
		Bureau bureau03db = sDbHelper.getBureauDao().queryForSameId(bureau03);

		// Checks

		Assert.assertEquals(sDbHelper.getBureauDao().queryForAll().size(), 3);

		Assert.assertEquals(bureau01db.getTitle(), bureau01.getTitle());
		Assert.assertEquals(bureau02db.getTitle(), bureau02.getTitle());
		Assert.assertEquals(bureau03db.getTitle(), "");
	}

	@Test public void order03_getDossierDao() throws Exception {

		Bureau bureau01 = sDbHelper.getBureauDao().queryBuilder().where().eq("Id", "id_01").query().get(0);
		Bureau bureau02 = sDbHelper.getBureauDao().queryBuilder().where().eq("Id", "id_02").query().get(0);
		Bureau bureau03 = sDbHelper.getBureauDao().queryBuilder().where().eq("Id", "id_03").query().get(0);

		Assert.assertNotNull(bureau01);
		Assert.assertNotNull(bureau02);
		Assert.assertNotNull(bureau03);

		HashSet<Action> actionsSet = new HashSet<>(Arrays.asList(ENREGISTRER,
																 EMAIL,
																 JOURNAL,
																 SECRETARIAT,
																 REJET,
																 TDT,
																 SIGNATURE,
																 VISA,
																 TRANSFERT_SIGNATURE,
																 AVIS_COMPLEMENTAIRE
		));

		Dossier dossier01 = new Dossier("id_01", "Title 01", VISA, actionsSet, "t01", "st01", new Date(1392829477205L), new Date(1392829477205L), true);
		dossier01.setParent(bureau01);
		Dossier dossier02 = new Dossier("id_02", "Title 02 \"/%@&éè\"", TDT, actionsSet, "t01", "st02", new Date(1392829477205L), null, true);
		dossier02.setParent(bureau01);
		Dossier dossier03 = new Dossier("id_03", null, null, null, null, null, null, null, false);
		dossier03.setParent(bureau02);

		sDbHelper.getDossierDao().create(Arrays.asList(dossier01, dossier02, dossier03));

		Dossier dossier01db = sDbHelper.getDossierDao().queryBuilder().where().eq("Id", "id_01").query().get(0);
		Dossier dossier02db = sDbHelper.getDossierDao().queryBuilder().where().eq("Id", "id_02").query().get(0);
		Dossier dossier03db = sDbHelper.getDossierDao().queryBuilder().where().eq("Id", "id_03").query().get(0);

		// Checks

		Assert.assertEquals(sDbHelper.getDossierDao().queryForAll().size(), 3);

		Assert.assertEquals(dossier01db.getName(), dossier01.getName());
		Assert.assertEquals(dossier02db.getName(), dossier02.getName());
		Assert.assertEquals(dossier03db.getName(), "");
		Assert.assertEquals(dossier01db.getActionDemandee(), dossier01.getActionDemandee());
		Assert.assertEquals(dossier02db.getActionDemandee(), dossier02.getActionDemandee());
		Assert.assertEquals(dossier03db.getActionDemandee(), VISA);
		Assert.assertEquals(dossier01db.getActions(), dossier01.getActions());
		Assert.assertEquals(dossier02db.getActions(), dossier02.getActions());
		Assert.assertEquals(dossier03db.getActions(), dossier03.getActions());
		Assert.assertEquals(dossier01db.getDateCreation(), dossier01.getDateCreation());
		Assert.assertEquals(dossier02db.getDateCreation(), dossier02.getDateCreation());
		Assert.assertEquals(dossier03db.getDateCreation(), dossier03.getDateCreation());

		sDbHelper.getBureauDao().update(bureau01);
		sDbHelper.getBureauDao().update(bureau02);
		sDbHelper.getBureauDao().update(bureau03);

		Assert.assertEquals(bureau01.getChildrenDossiers().size(), 2);
		Assert.assertEquals(bureau02.getChildrenDossiers().size(), 1);
		Assert.assertEquals(bureau03.getChildrenDossiers().size(), 0);
	}

	@Test public void order04_getDocumentDao() throws Exception {

		Dossier dossier01 = sDbHelper.getDossierDao().queryBuilder().where().eq("Id", "id_01").query().get(0);
		Dossier dossier02 = sDbHelper.getDossierDao().queryBuilder().where().eq("Id", "id_02").query().get(0);
		Dossier dossier03 = sDbHelper.getDossierDao().queryBuilder().where().eq("Id", "id_03").query().get(0);

		Assert.assertNotNull(dossier01);
		Assert.assertNotNull(dossier02);
		Assert.assertNotNull(dossier03);

		Document document01 = new Document("id_01", "name 01.pdf", 50000, true, true);
		document01.setPath("/test/path/1/");
		document01.setPagesAnnotations(new SerializableSparseArray<PageAnnotations>());
		document01.setParent(dossier01);

		Document document02 = new Document("id_02", "name 02.pdf", 0, true, true);
		document02.setPath("/test/path/2/");
		document02.setPagesAnnotations(new SerializableSparseArray<PageAnnotations>());
		document02.setParent(dossier01);

		Document document03 = new Document("id_03", null, 50000, false, false);
		document03.setPath("/test/path/3/");
		document03.setPagesAnnotations(null);
		document03.setParent(dossier02);

		sDbHelper.getDocumentDao().create(Arrays.asList(document01, document02, document03));

		Document document01db = sDbHelper.getDocumentDao().queryBuilder().where().eq("Id", "id_01").query().get(0);
		Document document02db = sDbHelper.getDocumentDao().queryBuilder().where().eq("Id", "id_02").query().get(0);
		Document document03db = sDbHelper.getDocumentDao().queryBuilder().where().eq("Id", "id_03").query().get(0);

		// Checks

		Assert.assertEquals(sDbHelper.getDocumentDao().queryForAll().size(), 3);

		Assert.assertEquals(document01db.getName(), document01.getName());
		Assert.assertEquals(document02db.getName(), document02.getName());
		Assert.assertEquals(document03db.getName(), "");
		Assert.assertEquals(String.valueOf(document01db.getPagesAnnotations()), String.valueOf(document01.getPagesAnnotations()));
		Assert.assertEquals(String.valueOf(document02db.getPagesAnnotations()), String.valueOf(document02.getPagesAnnotations()));
		Assert.assertEquals(String.valueOf(document03db.getPagesAnnotations()), String.valueOf(document03.getPagesAnnotations()));

		sDbHelper.getDossierDao().update(dossier01);
		sDbHelper.getDossierDao().update(dossier02);
		sDbHelper.getDossierDao().update(dossier03);

		Assert.assertEquals(dossier01.getChildrenDocuments().size(), 2);
		Assert.assertEquals(dossier02.getChildrenDocuments().size(), 1);
		Assert.assertEquals(dossier03.getChildrenDocuments().size(), 0);
	}

}