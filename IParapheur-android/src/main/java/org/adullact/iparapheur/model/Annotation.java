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
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.JsonObject;

import org.adullact.iparapheur.utils.JsonExplorer;

import java.io.IOException;
import java.io.Serializable;


public class Annotation implements Serializable {

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

	private String mUuid;
	private int mPage;
	private String mAuthor;
	private boolean mSecretaire;
	private String mDate;
	private RectF mRect;
	private String mText;
	private String mType;
	private String mPenColor;
	private String mFillColor;
	private int mStep;
	private boolean mUpdated = false;
	private boolean mDeleted = false;

	// <editor-fold desc="Constructors">

	public Annotation(@NonNull JsonObject json, int page, int step) {

		JsonExplorer jsonExplorer = new JsonExplorer(json);

		mUuid = jsonExplorer.optString(ID);
		mAuthor = jsonExplorer.optString(AUTHOR, "");
		mPage = page;
		mSecretaire = jsonExplorer.optBoolean(IS_SECRETAIRE, false);
		mDate = jsonExplorer.optString(DATE);
		mRect = new RectF(
				jsonExplorer.findObject(RECT).findObject(TOP_LEFT).optLong(X, 0),
				jsonExplorer.findObject(RECT).findObject(TOP_LEFT).optLong(Y, 0),
				jsonExplorer.findObject(RECT).findObject(BOTTOM_RIGHT).optLong(X, 0),
				jsonExplorer.findObject(RECT).findObject(BOTTOM_RIGHT).optLong(Y, 0)
		);
		mText = jsonExplorer.optString(TEXT, "");
		mType = jsonExplorer.optString(TYPE, "rect");
		mStep = step;
		mPenColor = jsonExplorer.optString(PEN_COLOR, "blue");
		mFillColor = jsonExplorer.optString(FILL_COLOR, "undefined");
	}

	public Annotation(String author, int page, boolean secretaire, String date, RectF rect, String text, int step) {

		mAuthor = author;
		mPage = page;
		mSecretaire = secretaire;
		mDate = date;
		mRect = rect;
		mText = text;
		mStep = step;
	}

	public Annotation(String uuid, String author, int page, boolean secretaire, String date, RectF rect, String text, String type, int step) {

		mUuid = uuid;
		mAuthor = author;
		mPage = page;
		mSecretaire = secretaire;
		mDate = date;
		mRect = rect;
		mText = text;
		mType = type;
		mStep = step;
	}

	private Annotation(Parcel in) {
		mUuid = in.readString();
		mPage = in.readInt();
		mAuthor = in.readString();
		mSecretaire = in.readByte() != 0;
		mDate = in.readString();
		mRect = in.readParcelable(((Object) mRect).getClass().getClassLoader());
		mText = in.readString();
		mType = in.readString();
		mStep = in.readInt();
		mUpdated = in.readByte() != 0;
		mDeleted = in.readByte() != 0;
	}

	// </editor-fold desc="Constructors">

	// <editor-fold desc="Getters / Setters">

	public String getUuid() {
		return mUuid;
	}

	public void setUuid(String uuid) {
		mUuid = uuid;
	}

	public RectF getRect() {
		return mRect;
	}

	public void setRect(float x, float y, float r, float b) {
		mRect.set(x, y, r, b);
		mUpdated = true;
	}

	public String getText() {
		return mText;
	}

	public void setText(String text) {
		mText = text;
		mUpdated = true;
	}

	public boolean isUpdated() {
		return mUpdated;
	}

	public void setUpdated(boolean updated) {
		mUpdated = updated;
	}

	public boolean isDeleted() {
		return mDeleted;
	}

	public void setDeleted(boolean deleted) {
		mDeleted = deleted;
	}

	public String getAuthor() {
		return mAuthor;
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

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(mUuid);
		out.writeInt(mPage);
		out.writeObject(mAuthor);
		out.writeBoolean(mSecretaire);
		out.writeObject(mDate);
		out.writeFloat(mRect.left);
		out.writeFloat(mRect.top);
		out.writeFloat(mRect.bottom);
		out.writeFloat(mRect.right);
		out.writeObject(mText);
		out.writeObject(mType);
		out.writeObject(mPenColor);
		out.writeObject(mFillColor);
		out.writeInt(mStep);
		out.writeBoolean(mUpdated);
		out.writeBoolean(mDeleted);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		mUuid = (String) in.readObject();
		mPage = in.readInt();
		mAuthor = (String) in.readObject();
		mSecretaire = in.readBoolean();
		mDate = (String) in.readObject();

		float left = in.readFloat();
		float top = in.readFloat();
		float right = in.readFloat();
		float bottom = in.readFloat();
		mRect = new RectF(left, top, right, bottom);

		mText = (String) in.readObject();
		mType = (String) in.readObject();
		mPenColor = (String) in.readObject();
		mFillColor = (String) in.readObject();
		mStep = in.readInt();
		mUpdated = in.readBoolean();
		mDeleted = in.readBoolean();
	}

	@Override public String toString() {
		return "{Annotation uuid:" + mUuid + " author:" + mAuthor + " page:" + mPage + " mDate=" + mDate + " text=" + mText + "}";
	}
}
