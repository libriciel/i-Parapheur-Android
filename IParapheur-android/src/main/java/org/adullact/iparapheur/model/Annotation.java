package org.adullact.iparapheur.model;

import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import org.adullact.iparapheur.controller.document.annotation.AnnotationView;

/**
 * Created by jmaire on 05/11/2013.
 */
public class Annotation implements Parcelable {

    public enum State {
        NEW,
        UPDATED,
        DELETED,
        UNCHANGE
    }

    private String uuid;
    private int page;
    private String author;
    private boolean secretaire;
    private String date;
    private RectF rect;
    private float scale = 1f;
    private String text;
    private String type;
    private int step;

    private boolean updated = false;
    private boolean deleted = false;


    public Annotation(String author, int page, boolean secretaire, String date,
                      float x, float y, String text, int step, float scale) {

        this.author = author;
        this.page = page;
        this.secretaire = secretaire;
        this.date = date;
        this.rect = new RectF(x - AnnotationView.MIN_WIDTH / 2, y - AnnotationView.MIN_HEIGHT / 2, x + AnnotationView.MIN_WIDTH / 2, y + AnnotationView.MIN_HEIGHT / 2);
        this.scale = scale;
        this.text = text;
        this.step = step;
    }

    public Annotation(String uuid, String author, int page, boolean secretaire, String date,
                      RectF rect, String text, String type, int step) {

        this.uuid = uuid;
        this.author = author;
        this.page = page;
        this.secretaire = secretaire;
        this.date = date;
        this.rect = rect;
        this.text = text;
        this.type = type;
        this.step = step;
    }

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

    public RectF getUnscaledRect() {
        return new RectF(rect.left / scale,
                rect.top / scale,
                rect.right / scale,
                rect.bottom / scale);
    }

    public String getText() {
        return this.text;
    }

    public State getState() {
        if (this.uuid == null) {
            return State.NEW;
        }
        else if (!this.updated) {
            return State.UNCHANGE;
        }
        else if (this.deleted) {
            return State.DELETED;
        }
        else {
            return State.UPDATED;
        }
    }

    public void setText(String text) {
        this.text = text;
        this.updated = true;
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
        return date;
    }

    public void moveTo(float x, float y, PointF offset) {
        this.rect.offsetTo(x - offset.x, y - offset.y);
        this.updated = true;
    }

    public void delete() {
        this.updated = true;
        this.deleted = true;
    }

    public boolean isEditable(int step) {
        return true; // TODO
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        rect.set(rect.left / this.scale * scale,
                rect.top / this.scale * scale,
                rect.right / this.scale * scale,
                rect.bottom / this.scale * scale);
        this.scale = scale;
    }

    @Override
    public String toString() {
        return "{uuid : " + uuid +
                ", author : " + author +
                ", page : " + page +
                ", secretaire : " + secretaire +
                ", date : " + date +
                ", rect : " + rect +
                ", text : " + text +
                ", type : " + type + "}";
    }


    // PARCELABLE IMPLEMENTATION

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uuid);
        dest.writeInt(this.page);
        dest.writeString(this.author);
        dest.writeByte(secretaire ? (byte) 1 : (byte) 0);
        dest.writeString(this.date);
        dest.writeParcelable(this.rect, 0);
        dest.writeFloat(this.scale);
        dest.writeString(this.text);
        dest.writeString(this.type);
        dest.writeInt(this.step);
        dest.writeByte(updated ? (byte) 1 : (byte) 0);
        dest.writeByte(deleted ? (byte) 1 : (byte) 0);
    }

    private Annotation(Parcel in) {
        this.uuid = in.readString();
        this.page = in.readInt();
        this.author = in.readString();
        this.secretaire = in.readByte() != 0;
        this.date = in.readString();
        this.rect = in.readParcelable(((Object) rect).getClass().getClassLoader());
        this.scale = in.readFloat();
        this.text = in.readString();
        this.type = in.readString();
        this.step = in.readInt();
        this.updated = in.readByte() != 0;
        this.deleted = in.readByte() != 0;
    }

    public static Parcelable.Creator<Annotation> CREATOR = new Parcelable.Creator<Annotation>() {
        public Annotation createFromParcel(Parcel source) {
            return new Annotation(source);
        }

        public Annotation[] newArray(int size) {
            return new Annotation[size];
        }
    };
}
