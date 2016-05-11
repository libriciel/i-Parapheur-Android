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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class PageAnnotations implements Parcelable {

	//<editor-fold desc="Static methods">

	// We may want to sort annotations, to have small annotations over big ones,
	// To ease touch events, and be able to select every one.
	public static Comparator<Annotation> ANNOTATIONS_SIZE_COMPARATOR = new Comparator<Annotation>() {

		@Override public int compare(Annotation lhs, Annotation rhs) {

			int lhsArea = Math.round(lhs.getRect().width() * lhs.getRect().height());
			int rhsArea = Math.round(rhs.getRect().width() * rhs.getRect().height());

			return (rhsArea - lhsArea);
		}
	};

	public static Parcelable.Creator<PageAnnotations> CREATOR = new Parcelable.Creator<PageAnnotations>() {

		public PageAnnotations createFromParcel(Parcel source) {
			return new PageAnnotations(source);
		}

		public PageAnnotations[] newArray(int size) {
			return new PageAnnotations[size];
		}
	};

	//</editor-fold desc="Static methods">

	private List<Annotation> mAnnotations;

	public PageAnnotations(@Nullable List<Annotation> annotations) {

		if (annotations == null) {
			mAnnotations = new ArrayList<>();
		}
		else {
			Collections.sort(annotations, ANNOTATIONS_SIZE_COMPARATOR);
			mAnnotations = annotations;
		}
	}

	public PageAnnotations() {
		mAnnotations = new ArrayList<>();
	}

	private PageAnnotations(Parcel in) {
		in.readTypedList(mAnnotations, Annotation.CREATOR);
	}

	public List<Annotation> getAnnotations() {
		return mAnnotations;
	}

	public void add(Annotation annotation) {
		mAnnotations.add(annotation);
		Collections.sort(mAnnotations, ANNOTATIONS_SIZE_COMPARATOR);
	}

	@Override public String toString() {
		return "{PageAnnotations - mAnnotations:\n" + mAnnotations + "}";
	}

	// <editor-fold desc="Parcelable">

	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(mAnnotations);
	}

	// </editor-fold desc="Parcelable">
}
