package org.adullact.iparapheur.model;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;

import org.adullact.iparapheur.controller.document.annotation.AnnotationView;

/**
 * Created by jmaire on 05/11/2013.
 */
public class Annotation {

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
    private Color fillColor;
    private Color penColor;

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

}
