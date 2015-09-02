package org.adullact.iparapheur.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

public class Document implements Parcelable {

	public static Creator<Document> CREATOR = new Creator<Document>() {
		public Document createFromParcel(Parcel source) {
			return new Document(source);
		}

		public Document[] newArray(int size) {
			return new Document[size];
		}
	};

	private final String id;
	private final String dossierId;
	private final String name;
	private final int size;                  // TODO : download image instead of too heavy files
	private final String url;                // URL of the document (its content)
	private SparseArray<PageAnnotations> pagesAnnotations;
	private String path;                     // Path of the file (if downloaded) on the device's storage

	public Document(String id, String dossierId, String name, int size, String url) {
		this.id = id;
		this.dossierId = dossierId;
		this.name = name;
		this.url = url;
		this.size = size;
		this.pagesAnnotations = new SparseArray<>();
	}

	@SuppressWarnings("unchecked")
	private Document(Parcel in) {
		this.id = in.readString();
		this.dossierId = in.readString();
		this.name = in.readString();
		this.pagesAnnotations = (SparseArray<PageAnnotations>) in.readBundle().get("pagesAnnotations");
		this.url = in.readString();
		this.size = in.readInt();
		this.path = in.readString();
	}

	// <editor-fold desc="Setters / Getters">

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

	// </editor-fold desc="Setters / Getters">

	// <editor-fold desc="Parcelable">

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

	// </editor-fold desc="Parcelable">

	@Override
	public String toString() {
		return "{Document id:" + id + " name:" + name + "}";
	}

	@Override
	public boolean equals(Object o) {
		return (o != null) && (o instanceof Document) && (id.contentEquals(((Document) o).getId()));
	}
}
