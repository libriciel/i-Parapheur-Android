package org.adullact.iparapheur.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jmaire on 20/03/2014.
 */
public class PageAnnotations implements Parcelable {

    private List<Annotation> annotations;

    public PageAnnotations(List<Annotation> annotations) {
        this.annotations = (annotations == null)?
                new ArrayList<Annotation>() :
                annotations;
    }

    public PageAnnotations() {
        this.annotations = new ArrayList<Annotation>();
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(annotations);
    }

    private PageAnnotations(Parcel in) {
        in.readTypedList(annotations, Annotation.CREATOR);
    }

    public static Parcelable.Creator<PageAnnotations> CREATOR = new Parcelable.Creator<PageAnnotations>() {
        public PageAnnotations createFromParcel(Parcel source) {
            return new PageAnnotations(source);
        }

        public PageAnnotations[] newArray(int size) {
            return new PageAnnotations[size];
        }
    };

    public void add(Annotation annotation) {
        this.annotations.add(annotation);
    }
}
