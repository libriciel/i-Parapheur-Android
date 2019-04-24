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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class PageAnnotations implements Serializable {

	// <editor-fold desc="Static methods">

	// We may want to sort annotations, to have small annotations over big ones,
	// To ease touch events, and be able to select every one.
	private static Comparator<Annotation> ANNOTATIONS_SIZE_COMPARATOR = new Comparator<Annotation>() {

		@Override public int compare(Annotation lhs, Annotation rhs) {

			int lhsArea = Math.round(Math.abs(lhs.getRect().width()) * Math.abs(lhs.getRect().height()));
			int rhsArea = Math.round(Math.abs(rhs.getRect().width()) * Math.abs(rhs.getRect().height()));

			return (rhsArea - lhsArea);
		}
	};

	// </editor-fold desc="Static methods">

	private List<Annotation> mAnnotations;

	public PageAnnotations() {
		mAnnotations = new ArrayList<>();
	}

	// <editor-fold desc="Accessors">

	public List<Annotation> getAnnotations() {
		return mAnnotations;
	}

	public void add(Annotation annotation) {
		mAnnotations.add(annotation);
		Collections.sort(mAnnotations, ANNOTATIONS_SIZE_COMPARATOR);
	}

	// </editor-fold desc="Accessors">

	@Override public String toString() {
		return "{PageAnnotations - mAnnotations:\n" + mAnnotations + "}";
	}
}
