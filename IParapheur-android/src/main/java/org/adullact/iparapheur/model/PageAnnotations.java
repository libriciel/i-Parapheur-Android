package org.adullact.iparapheur.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PageAnnotations implements Parcelable {

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

	// <editor-fold desc="Parcelable">

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(mAnnotations);
	}

	// </editor-fold desc="Parcelable">
}
