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
package org.adullact.iparapheur.model;

import android.graphics.RectF;

import org.junit.Assert;

import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;


@PrepareForTest(RectF.class)
public class PageAnnotationsTest {

	@Test public void add() throws Exception {

		// Fake RectF

		RectF mockedRect01 = Mockito.mock(RectF.class);
		Mockito.when(mockedRect01.height()).thenReturn(1F);
		Mockito.when(mockedRect01.width()).thenReturn(1F);

		RectF mockedRect02 = Mockito.mock(RectF.class);
		Mockito.when(mockedRect02.height()).thenReturn(10F);
		Mockito.when(mockedRect02.width()).thenReturn(10F);

		RectF mockedRect03 = Mockito.mock(RectF.class);
		Mockito.when(mockedRect03.height()).thenReturn(-15F);
		Mockito.when(mockedRect03.width()).thenReturn(-15F);

		RectF mockedRect04 = Mockito.mock(RectF.class);
		Mockito.when(mockedRect04.height()).thenReturn(20F);
		Mockito.when(mockedRect04.width()).thenReturn(-20F);

		// Build objects

		PageAnnotations pageAnnotationsRandomOrder = new PageAnnotations();
		pageAnnotationsRandomOrder.add(new Annotation(null, 0, false, null, mockedRect03, "03", 0));
		pageAnnotationsRandomOrder.add(new Annotation(null, 0, false, null, mockedRect01, "01", 0));
		pageAnnotationsRandomOrder.add(new Annotation(null, 0, false, null, mockedRect02, "02", 0));
		pageAnnotationsRandomOrder.add(new Annotation(null, 0, false, null, mockedRect04, "04", 0));

		PageAnnotations pageAnnotationsOrdered = new PageAnnotations();
		pageAnnotationsOrdered.add(new Annotation(null, 0, false, null, mockedRect04, "04", 0));
		pageAnnotationsOrdered.add(new Annotation(null, 0, false, null, mockedRect03, "03", 0));
		pageAnnotationsOrdered.add(new Annotation(null, 0, false, null, mockedRect02, "02", 0));
		pageAnnotationsOrdered.add(new Annotation(null, 0, false, null, mockedRect01, "01", 0));

		// Checks

		Assert.assertEquals(pageAnnotationsRandomOrder.getAnnotations().get(0).getText(), "04");
		Assert.assertEquals(pageAnnotationsRandomOrder.getAnnotations().get(1).getText(), "03");
		Assert.assertEquals(pageAnnotationsRandomOrder.getAnnotations().get(2).getText(), "02");
		Assert.assertEquals(pageAnnotationsRandomOrder.getAnnotations().get(3).getText(), "01");

		Assert.assertEquals(pageAnnotationsRandomOrder.getAnnotations().size(), 4);
		Assert.assertEquals(pageAnnotationsRandomOrder.toString(), pageAnnotationsOrdered.toString());

	}

}