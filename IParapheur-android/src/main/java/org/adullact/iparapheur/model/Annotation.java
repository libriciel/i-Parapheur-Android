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

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.JsonObject;

import org.adullact.iparapheur.utils.JsonExplorer;

import java.io.Serializable;


public class Annotation implements Parcelable, Serializable {

	private static final String ID = "id";
	private static final String IS_SECRETAIRE = "secretaire";
	private static final String AUTHOR = "author";
	private static final String DATE = "date";
	private static final String TYPE = "type";
	private static final String TEXT = "text";
	private static final String RECT = "rect";
	private static final String PEN_COLOR = "penColor";
	private static final String FILL_COLOR = "fillColor";
	private static final String TOP_LEFT = "topLeft";
	private static final String BOTTOM_RIGHT = "bottomRight";
	private static final String X = "x";
	private static final String Y = "y";

	public static Parcelable.Creator<Annotation> CREATOR = new Parcelable.Creator<Annotation>() {

		public Annotation createFromParcel(Parcel source) {
			return new Annotation(source);
		}

		public Annotation[] newArray(int size) {
			return new Annotation[size];
		}
	};

	private String uuid;
	private int mPage;
	private String author;
	private boolean secretaire;
	private String mDate;
	private RectF rect;
	private String text;
	private String type;
	private String mPenColor;
	private String mFillColor;
	private int mStep;
	private boolean updated = false;
	private boolean deleted = false;

	// <editor-fold desc="Constructors">

	public Annotation(@NonNull JsonObject json, int page, int step) {

		JsonExplorer jsonExplorer = new JsonExplorer(json);

		uuid = jsonExplorer.optString(ID);
		author = jsonExplorer.optString(AUTHOR, "");
		mPage = page;
		secretaire = jsonExplorer.optBoolean(IS_SECRETAIRE, false);
		mDate = jsonExplorer.optString(DATE);
		rect = new RectF(
				jsonExplorer.findObject(RECT).findObject(TOP_LEFT).optLong(X, 0),
				jsonExplorer.findObject(RECT).findObject(TOP_LEFT).optLong(Y, 0),
				jsonExplorer.findObject(RECT).findObject(BOTTOM_RIGHT).optLong(X, 0),
				jsonExplorer.findObject(RECT).findObject(BOTTOM_RIGHT).optLong(Y, 0)
		);
		text = jsonExplorer.optString(TEXT, "");
		type = jsonExplorer.optString(TYPE, "rect");
		mStep = step;
		mPenColor = jsonExplorer.optString(PEN_COLOR, "blue");
		mFillColor = jsonExplorer.optString(FILL_COLOR, "undefined");
	}

	public Annotation(String author, int page, boolean secretaire, String date, RectF rect, String text, int step) {

		this.author = author;
		mPage = page;
		this.secretaire = secretaire;
		mDate = date;
		this.rect = rect;
		this.text = text;
		mStep = step;
	}

	public Annotation(String uuid, String author, int page, boolean secretaire, String date, RectF rect, String text, String type, int step) {

		this.uuid = uuid;
		this.author = author;
		mPage = page;
		this.secretaire = secretaire;
		mDate = date;
		this.rect = rect;
		this.text = text;
		this.type = type;
		mStep = step;
	}

	private Annotation(Parcel in) {
		this.uuid = in.readString();
		mPage = in.readInt();
		this.author = in.readString();
		this.secretaire = in.readByte() != 0;
		mDate = in.readString();
		this.rect = in.readParcelable(((Object) rect).getClass().getClassLoader());
		this.text = in.readString();
		this.type = in.readString();
		mStep = in.readInt();
		this.updated = in.readByte() != 0;
		this.deleted = in.readByte() != 0;
	}

	// </editor-fold desc="Constructors">

	// <editor-fold desc="Getters / Setters">

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public RectF getRect() {
		return rect;
	}

	public void setRect(float x, float y, float r, float b) {
		this.rect.set(x, y, r, b);
		this.updated = true;
	}

	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
		this.updated = true;
	}

	public boolean isUpdated() {
		return updated;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public String getAuthor() {
		return author;
	}

	public String getDate() {
		return mDate;
	}

	public int getStep() {
		return mStep;
	}

	public void setStep(int step) {
		mStep = step;
	}

	// </editor-fold desc="Getters / Setters">

	// <editor-fold desc="Parcelable">

	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.uuid);
		dest.writeInt(mPage);
		dest.writeString(this.author);
		dest.writeByte(secretaire ? (byte) 1 : (byte) 0);
		dest.writeString(mDate);
		dest.writeParcelable(this.rect, 0);
		dest.writeString(this.text);
		dest.writeString(this.type);
		dest.writeInt(mStep);
		dest.writeByte(updated ? (byte) 1 : (byte) 0);
		dest.writeByte(deleted ? (byte) 1 : (byte) 0);
	}

	// </editor-fold desc="Parcelable">

	@Override public String toString() {
		return "{Annotation uuid:" + uuid + " author:" + author + " page:" + mPage + " mDate=" + mDate + " text=" + text + "}";
	}
}
