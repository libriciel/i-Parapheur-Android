package org.adullact.iparapheur.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class PageAnnotations implements Parcelable {

	public static Parcelable.Creator<PageAnnotations> CREATOR = new Parcelable.Creator<PageAnnotations>() {
		public PageAnnotations createFromParcel(Parcel source) {
			return new PageAnnotations(source);
		}

		public PageAnnotations[] newArray(int size) {
			return new PageAnnotations[size];
		}
	};

	private List<Annotation> mAnnotations;

	public PageAnnotations(List<Annotation> annotations) {
		mAnnotations = (annotations == null) ? new ArrayList<Annotation>() : annotations;
	}

	public PageAnnotations() {
		mAnnotations = new ArrayList<Annotation>();
	}

	private PageAnnotations(Parcel in) {
		in.readTypedList(mAnnotations, Annotation.CREATOR);
	}

	public List<Annotation> getAnnotations() {
		return mAnnotations;
	}

	public void setAnnotations(List<Annotation> annotations) {
		mAnnotations = annotations;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(mAnnotations);
	}

	public void add(Annotation annotation) {
		mAnnotations.add(annotation);
	}

	public void remove(Annotation annotation) {
		mAnnotations.remove(annotation);
	}
}
