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

import org.adullact.iparapheur.model.Bureau;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;


@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class BureauUtilsTest {

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

	@Test public void findInList() throws Exception {

		List<Bureau> bureauList = new ArrayList<>();
		bureauList.add(new Bureau("id_01", "Name 01", 10, 5));
		bureauList.add(new Bureau("id_02", "Name 02", 10, 5));
		bureauList.add(null);
		bureauList.add(new Bureau("id_03", "Name 03", 10, 5));

		List<Bureau> emptyList = new ArrayList<>();

		// Checks

		org.junit.Assert.assertNull(BureauUtils.findInList(null, null));
		org.junit.Assert.assertNull(BureauUtils.findInList(bureauList, null));
		org.junit.Assert.assertNull(BureauUtils.findInList(bureauList, "id_missing"));
		org.junit.Assert.assertNull(BureauUtils.findInList(emptyList, "id_01"));
		//noinspection ConstantConditions
		org.junit.Assert.assertEquals(BureauUtils.findInList(bureauList, "id_02").getTitle(), "Name 02");
	}

}