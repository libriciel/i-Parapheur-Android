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
package org.adullact.iparapheur.model;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.adullact.iparapheur.utils.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;

import static org.mockito.Matchers.any;


@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class DocumentTest {

	private static Gson sGson = CollectionUtils.buildGsonWithLongToDate();

	@Before public void setUp() throws Exception {
		PowerMockito.mockStatic(TextUtils.class);

		PowerMockito.when(TextUtils.equals(any(CharSequence.class), any(CharSequence.class))).thenAnswer(new Answer<Object>() {
			@Override public Object answer(InvocationOnMock invocation) throws Throwable {
				CharSequence a = (CharSequence) invocation.getArguments()[0];
				CharSequence b = (CharSequence) invocation.getArguments()[1];
				return org.adullact.iparapheur.mock.TextUtils.equals(a, b);
			}
		});
	}

	@Test public void gsonParse() throws Exception {

		String incorrectJsonString = "[[{]   \"id\": \"Value 01\" , \"collectivite\": [\"Value 01-01\"  ]]";
		String correctJsonString = "{" +
				"    \"size\": 50000," +
				"    \"visuelPdf\": true," +
				"    \"isMainDocument\": true," +
				"    \"pageCount\": 5," +
				"    \"attestState\": 0," +
				"    \"id\": \"id_01\"," +
				"    \"name\": \"name 01.pdf\"," +
				"    \"canDelete\": true," +
				"    \"isLocked\": true" +
				"}";

		Document incorrectObjectParsed;
		try { incorrectObjectParsed = sGson.fromJson(incorrectJsonString, Document.class); }
		catch (JsonSyntaxException ex) { incorrectObjectParsed = null; }

		Document correctObjectParsed = sGson.fromJson(correctJsonString, Document.class);
		Document document = new Document("id_01", "name 01.pdf", 50000, true, true);
		document.setPath(null);
		document.setPagesAnnotations(null);

		// Checks

		Assert.assertNull(incorrectObjectParsed);
		Assert.assertNotNull(correctObjectParsed);

		Assert.assertTrue(document.equals(correctObjectParsed));
		Assert.assertEquals(correctObjectParsed.toString(), document.toString());
		Assert.assertEquals(correctObjectParsed.getId(), document.getId());
		Assert.assertEquals(correctObjectParsed.getName(), document.getName());
		Assert.assertEquals(correctObjectParsed.getSize(), document.getSize());
		Assert.assertEquals(correctObjectParsed.isMainDocument(), document.isMainDocument());
		Assert.assertEquals(correctObjectParsed.isPdfVisual(), document.isPdfVisual());
		Assert.assertEquals(correctObjectParsed.getPagesAnnotations(), document.getPagesAnnotations());
		Assert.assertEquals(correctObjectParsed.getPath(), document.getPath());
	}

	// <editor-fold desc="Static utils">

	@Test public void generateContentUrl() throws Exception {

		Document doc01 = new Document("id_01", null, 0, false, false);
		Document doc02 = new Document("id_02", null, 0, false, true);
		Document doc03 = new Document(null, null, 0, false, false);

		// Checks

		Assert.assertEquals(Document.generateContentUrl(doc01), "/api/node/workspace/SpacesStore/id_01/content");
		Assert.assertEquals(Document.generateContentUrl(doc02), "/api/node/workspace/SpacesStore/id_02/content;ph:visuel-pdf");
		Assert.assertNull(Document.generateContentUrl(doc03));
	}

	@Test public void isMainDocument() throws Exception {

		Document doc01 = new Document("id_01", null, 0, false, false);
		Document doc02 = new Document("id_02", null, 0, true, false);
		Document doc03 = new Document("id_03", null, 0, false, false);

		ArrayList<Document> documentList01 = new ArrayList<>();
		documentList01.add(doc01);
		documentList01.add(doc02);

		Dossier dossier01 = new Dossier(null, null, null, null, null, null, null, null, false);
		dossier01.setDocumentList(documentList01);

		ArrayList<Document> documentList02 = new ArrayList<>();
		documentList02.add(doc01);
		documentList02.add(doc03);

		Dossier dossier02 = new Dossier(null, null, null, null, null, null, null, null, false);
		dossier02.setDocumentList(documentList02);

		Dossier dossier03 = new Dossier(null, null, null, null, null, null, null, null, false);

		// Checks

		Assert.assertFalse(Document.isMainDocument(dossier01, doc01));
		Assert.assertTrue(Document.isMainDocument(dossier01, doc02));

		Assert.assertTrue(Document.isMainDocument(dossier02, doc01));
		Assert.assertFalse(Document.isMainDocument(dossier02, doc02));

		Assert.assertFalse(Document.isMainDocument(dossier03, doc01));
	}

	// </editor-fold desc="Static utils">
}