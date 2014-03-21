package org.adullact.iparapheur.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jmaire on 19/11/2013.
 */
public class Bureau implements Parcelable {

    private String id;
    private String title;

    public Bureau(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.title);
    }

    private Bureau(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
    }

    public static Parcelable.Creator<Bureau> CREATOR = new Parcelable.Creator<Bureau>() {
        public Bureau createFromParcel(Parcel source) {
            return new Bureau(source);
        }

        public Bureau[] newArray(int size) {
            return new Bureau[size];
        }
    };
}
