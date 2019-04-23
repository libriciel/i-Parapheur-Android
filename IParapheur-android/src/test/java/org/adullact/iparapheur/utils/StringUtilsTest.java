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

import android.text.TextUtils;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Date;

import static org.mockito.Matchers.any;


@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class StringUtilsTest {

	@Before public void setUp() throws Exception {
		PowerMockito.mockStatic(TextUtils.class);

		PowerMockito.when(TextUtils.equals(any(CharSequence.class), any(CharSequence.class))).thenAnswer(new Answer<Object>() {
			@Override public Object answer(InvocationOnMock invocation) throws Throwable {
				CharSequence a = (CharSequence) invocation.getArguments()[0];
				CharSequence b = (CharSequence) invocation.getArguments()[1];
				return org.adullact.iparapheur.mock.TextUtils.equals(a, b);
			}
		});

		PowerMockito.when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Object>() {
			@Override public Object answer(InvocationOnMock invocation) throws Throwable {
				CharSequence a = (CharSequence) invocation.getArguments()[0];
				return org.adullact.iparapheur.mock.TextUtils.isEmpty(a);
			}
		});
	}

	// <editor-fold desc="TextUtils">

	@Test public void areNotEmpty() throws Exception {

		Assert.assertTrue(StringUtils.areNotEmpty("Test"));

		Assert.assertFalse(StringUtils.areNotEmpty(null, "Test"));
		Assert.assertFalse(StringUtils.areNotEmpty("", "Test"));
		Assert.assertFalse(StringUtils.areNotEmpty(""));
		Assert.assertFalse(StringUtils.areNotEmpty(null, ""));
		Assert.assertFalse(StringUtils.areNotEmpty());
	}

	@Test public void endsWithIgnoreCase() throws Exception {

		Assert.assertTrue(StringUtils.endsWithIgnoreCase("test 123", "test 123"));
		Assert.assertTrue(StringUtils.endsWithIgnoreCase("test 123", "T 123"));
		Assert.assertTrue(StringUtils.endsWithIgnoreCase("test 123", "123"));
		Assert.assertTrue(StringUtils.endsWithIgnoreCase("test 123", ""));

		Assert.assertFalse(StringUtils.endsWithIgnoreCase("test 123", "test 1234"));
		Assert.assertFalse(StringUtils.endsWithIgnoreCase("test 123", "2"));
		Assert.assertFalse(StringUtils.endsWithIgnoreCase("test 123", null));
		Assert.assertFalse(StringUtils.endsWithIgnoreCase(null, null));
	}

	// </editor-fold desc="TextUtils">

	@Test public void parseIso8601Date() {

		Date parsedDate = StringUtils.parseIso8601Date("2016-12-25T23:45:00");
		Assert.assertNotNull(parsedDate);
		Assert.assertEquals(parsedDate.getTime(), 1482705900000L);

		Assert.assertNull(StringUtils.parseIso8601Date("999999"));
		Assert.assertNull(StringUtils.parseIso8601Date(""));
		Assert.assertNull(StringUtils.parseIso8601Date(null));
	}

	@Test public void serializeToIso8601Date() {
		Assert.assertEquals(StringUtils.serializeToIso8601Date(new Date(1482705900000L)), "2016-12-25T23:45:00");
	}

	@Test public void fixIssuerDnX500NameStringOrder() throws Exception {

		String input = "OU=ADULLACT-Projet,E=systeme@adullact.org,CN=AC ADULLACT Projet\\, g2,O=ADULLACT-Projet,ST=Herault,C=FR";
		String value = StringUtils.fixIssuerDnX500NameStringOrder(input);
		String expected = "EMAIL=systeme@adullact.org,CN=AC ADULLACT Projet\\, g2,OU=ADULLACT-Projet,O=ADULLACT-Projet,ST=Herault,C=FR";

		Assert.assertEquals(value, expected);
	}

	@Test public void fixUrl() {
		Assert.assertEquals(StringUtils.fixUrl("https://m.parapheur/iparapheur.plop//"), "parapheur");
		Assert.assertEquals(StringUtils.fixUrl("http://parapheur.test.adullact.org/parapheur/test"), "parapheur.test.adullact.org");
		Assert.assertEquals(StringUtils.fixUrl("m.parapheur.test.adullact.org/"), "parapheur.test.adullact.org");
		Assert.assertEquals(StringUtils.fixUrl("parapheur.test.adullact.org"), "parapheur.test.adullact.org");
		Assert.assertEquals(StringUtils.fixUrl("https://parapheur"), "parapheur");
		Assert.assertEquals(StringUtils.fixUrl("https://m.parapheur"), "parapheur");
		Assert.assertEquals(StringUtils.fixUrl("m.parapheur"), "parapheur");
		Assert.assertEquals(StringUtils.fixUrl("parapheur"), "parapheur");
	}

	@Test public void getLocalizedSmallDate() {
		Assert.assertEquals(StringUtils.getLocalizedSmallDate(new Date(1482705900000L)), "25/12/16");
		Assert.assertEquals(StringUtils.getLocalizedSmallDate(null), "???");
	}

	@Test public void getVerySmallDate() {
		Assert.assertEquals(StringUtils.getVerySmallDate(new Date(1482705900000L)), "25/12");
		Assert.assertEquals(StringUtils.getVerySmallDate(null), "???");
	}

	@Test public void getSmallTime() {
		Assert.assertEquals(StringUtils.getSmallTime(new Date(1482705900000L)), "23:45");
		Assert.assertEquals(StringUtils.getSmallTime(null), "???");
	}

	@Test public void isUrlValid() {
		Assert.assertTrue(StringUtils.isUrlValid("parapheur"));
		Assert.assertTrue(StringUtils.isUrlValid("parapheur.test.adullact.org"));
		Assert.assertFalse(StringUtils.isUrlValid(":::::"));
		Assert.assertFalse(StringUtils.isUrlValid(""));
		Assert.assertFalse(StringUtils.isUrlValid(null));
	}

	@Test public void nullableBooleanValueOf() {

	}
}