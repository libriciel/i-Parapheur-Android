package org.adullact.iparapheur.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

/**
 * Created by jmaire on 03/11/2013.
 */
public class Document implements Parcelable {

    private final String id;
    private final String dossierId;
    private final String name;
    private final int size; // TODO : utiliser pour ne pas telecharger des dossiers trop lourds (images à la place)
    private SparseArray<PageAnnotations> pagesAnnotations;

    /**
     * URL of the document (its content)
     */
    private final String url;
    /**
     * path of the file (if downloaded) on the device's storage
     */
    private String path;

    public Document(String id, String dossierId, String name, int size, String url) {
        this.id = id;
        this.dossierId = dossierId;
        this.name = name;
        this.url = url;
        this.size = size;
        this.pagesAnnotations = new SparseArray<PageAnnotations>();
    }

    public String getId() {
        return id;
    }

    public String getDossierId() {
        return dossierId;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public int getSize() {
        return size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public SparseArray<PageAnnotations> getPagesAnnotations() {
        return pagesAnnotations;
    }

    public void setPagesAnnotations(SparseArray<PageAnnotations> pagesAnnotations) {
        this.pagesAnnotations = pagesAnnotations;
    }

    // PARCELABLE IMPLEMENTATION


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putSparseParcelableArray("pagesAnnotations", this.pagesAnnotations);
        dest.writeString(this.id);
        dest.writeString(this.dossierId);
        dest.writeString(this.name);
        dest.writeBundle(bundle);
        dest.writeString(this.url);
        dest.writeInt(this.size);
        dest.writeString(this.path);
    }

    private Document(Parcel in) {
        this.id = in.readString();
        this.dossierId = in.readString();
        this.name = in.readString();
        this.pagesAnnotations = (SparseArray<PageAnnotations>) in.readBundle().get("pagesAnnotations");
        this.url = in.readString();
        this.size = in.readInt();
        this.path = in.readString();
    }

    public static Creator<Document> CREATOR = new Creator<Document>() {
        public Document createFromParcel(Parcel source) {
            return new Document(source);
        }

        public Document[] newArray(int size) {
            return new Document[size];
        }
    };
}
