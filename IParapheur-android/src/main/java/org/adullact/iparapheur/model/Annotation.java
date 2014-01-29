package org.adullact.iparapheur.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by jmaire on 05/11/2013.
 */
public class Annotation {
    public static final int BORDER_WIDTH = 4;
    public static final int CORNER_RADIUS = 10;
    public static final int HALF_MIN_WIDTH = 80;
    public static final int HALF_MIN_HEIGHT = 40;

    public static final int LINE_WIDTH = 3;
    // Button's drawings line size
    public static final int LINE_HALF_LENGTH = 6;

    // Space between two buttons from center to center
    public static final int BUTTONS_SPACE = 42;

    public static final int COLOR_SELECTED = Color.RED;
    public static final int COLOR_BORDER = Color.BLACK;

    public static final int BUTTON_RADIUS = 14;
    public static final int BUTTON_COLOR = Color.BLACK;
    public static final int BUTTON_BORDER_COLOR = Color.WHITE;

    public static final int TEXT_RECT_WIDTH = 200;
    public static final int TEXT_RECT_HEIGHT = 100;
    public static final int TEXT_RECT_COLOR = Color.YELLOW;
    public static final int TEXT_RECT_ALPHA = 230; //90%

    public static final int TEXT_COLOR = Color.BLACK;
    public static final int TEXT_SIZE = 12;
    public static final int TEXT_PADDING = 5;

    public static final int INFOS_RECT_WIDTH = 150;
    public static final int INFOS_RECT_HEIGHT = 60;
    public static final int INFOS_COLOR = Color.CYAN;
    public static final int INFOS_ALPHA = 230; //90%
    public static final int INFOS_BORDER_WIDTH = 3;
    public static final int INFOS_BORDER_COLOR = Color.BLACK;

    public static final int ALPHA_SELECTED = 51; //20%
    public static final int ALPHA_BORDER = 179; //70%

    private enum Button {
        DELETE,
        MINIMIZE,
        RESIZE,
        INFO,
        EDIT
    }

    public enum Area {
        NONE,
        CENTER,
        RESIZE,
        DELETE,
        EDIT,
        MINIMIZE,
        INFO
    }

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
    private boolean selected = false;
    private boolean minimized = false;
    private boolean textVisible = false;
    private boolean infosVisible = false;



    public Annotation(String author, int page, boolean secretaire, String date,
                      float x, float y, String text, int step, float scale) {

        this.author = author;
        this.page = page;
        this.secretaire = secretaire;
        this.date = date;
        this.rect = new RectF(x - Annotation.HALF_MIN_WIDTH, y - Annotation.HALF_MIN_HEIGHT, x + Annotation.HALF_MIN_WIDTH, y + Annotation.HALF_MIN_HEIGHT);
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

    public void moveTo(float x, float y, PointF offset) {
        this.rect.offsetTo(x - offset.x, y - offset.y);
        this.updated = true;
    }

    public void resize(float x, float y) {
        float right = rect.right;
        float bottom = rect.bottom;
        if ((x - rect.left) >= HALF_MIN_WIDTH) {
            right = x;
        }
        if ((y - rect.top) >= HALF_MIN_HEIGHT) {
            bottom = y;
        }
        this.rect.set(rect.left, rect.top, right, bottom);
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

    public void select() {
        this.selected = true;
    }

    public void unselect() {
        this.selected = false;
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

    public void minimize() {
        this.minimized = true;
    }

    public void maximize() {
        this.minimized = false;
    }

    public void toggleSize() {
        this.minimized = !minimized;
    }

    public void toggleText() {
        this.textVisible = !textVisible;
    }

    public void toggleInfos() {
        this.infosVisible = !infosVisible;
    }

    public boolean isTextVisible() {
        return textVisible;
    }

    public boolean areInfosVisible() {
        return infosVisible;
    }

    public boolean isMinimized() {
        return minimized;
    }

    public boolean touched(float x, float y) {
        return (!getArea(x, y).equals(Annotation.Area.NONE));
    }

    // Methods and utilities for drawing

    private float distance(float x, float y, PointF point2) {
        float dx = x - point2.x;
        float dy = y - point2.y;
        return (float)Math.sqrt((dx * dx) + (dy * dy));
    }

    public Area getArea(float x, float y) {
        Area area = Area.NONE;
        if (!deleted) {
            if (rect.contains(x, y)) {
                area = Area.CENTER;
            }
            for (Button button : Button.values()) {
                if (distance(x, y, getButtonCenter(rect, button)) < Annotation.BUTTON_RADIUS) {
                    area = Area.valueOf(button.name());
                    break;
                }
            }
        }
        return area;
    }

    private PointF getButtonCenter(RectF rect, Button button) {
        PointF center = new PointF();
        switch (button) {
            case DELETE :
                center.set(rect.left, rect.top);
                break;
            case EDIT :
                center.set(rect.left + Annotation.BUTTONS_SPACE, rect.bottom);
                break;
            case INFO :
                center.set(rect.left, rect.bottom);
                break;
            case MINIMIZE :
                center.set(rect.left + Annotation.BUTTONS_SPACE, rect.top);
                break;
            case RESIZE :
                center.set(rect.right - Annotation.BUTTON_RADIUS, rect.bottom - Annotation.BUTTON_RADIUS);
                break;
            default :
                center.set(rect.left, rect.top);
                break;
        }
        return center;
    }

    private RectF getInfosRect() {
        return new RectF(rect.left, rect.bottom + 5, rect.left + INFOS_RECT_WIDTH, rect.bottom + INFOS_RECT_HEIGHT);
    }

    public Rect getTextRect() {
        return new Rect((int)rect.left, (int)rect.bottom + 10, (int)rect.left + TEXT_RECT_WIDTH, (int)rect.bottom + TEXT_RECT_HEIGHT);
    }


    public void draw(Canvas canvas) {
        if (!deleted) {
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            if (isMinimized()) {
                drawButton(canvas, paint, Button.DELETE);
                drawButton(canvas, paint, Button.MINIMIZE);
            }
            else {
                drawRect(canvas, paint);
                if (selected) {
                    drawButton(canvas, paint, Button.DELETE);
                    drawButton(canvas, paint, Button.MINIMIZE);
                    drawButton(canvas, paint, Button.INFO);
                    drawButton(canvas, paint, Button.EDIT);
                    drawButton(canvas, paint, Button.RESIZE);
                }
                if (textVisible) {
                    //drawText(view, canvas, paint);
                }
                if (infosVisible) {
                    drawInfos(canvas, paint);
                }
            }
        }
    }

    protected void drawRect(Canvas canvas, Paint paint) {
        paint.setStrokeWidth(Annotation.BORDER_WIDTH);
        if (selected) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(COLOR_SELECTED);
            paint.setAlpha(ALPHA_SELECTED);
        }
        else {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(COLOR_BORDER);
            paint.setAlpha(ALPHA_BORDER);
        }
        canvas.drawRoundRect(rect, Annotation.CORNER_RADIUS, Annotation.CORNER_RADIUS, paint);
    }

    private void drawInfos(Canvas canvas, Paint paint) {
        RectF infosRect = getInfosRect();
        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(INFOS_BORDER_COLOR);
        paint.setAlpha(INFOS_ALPHA);
        paint.setStrokeWidth(Annotation.INFOS_BORDER_WIDTH);
        canvas.drawRoundRect(infosRect, Annotation.CORNER_RADIUS, Annotation.CORNER_RADIUS, paint);

        // FILL
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(INFOS_COLOR);
        paint.setAlpha(INFOS_ALPHA);
        canvas.drawRoundRect(infosRect, Annotation.CORNER_RADIUS, Annotation.CORNER_RADIUS, paint);

        // INFOS
        paint.setColor(TEXT_COLOR);
        paint.setTextSize(TEXT_SIZE);
        canvas.drawText(author, infosRect.left + TEXT_PADDING, infosRect.top + TEXT_SIZE, paint);
        canvas.drawText(date, infosRect.left + TEXT_PADDING, infosRect.top + (TEXT_SIZE * 3), paint);

    }

    private void drawButton(Canvas canvas, Paint paint, Button type) {
        PointF center = getButtonCenter(rect, type);
        if (!type.equals(Button.RESIZE)) {
            // Inner black circle with shadow
            paint.setColor(BUTTON_COLOR);
            paint.setStyle(Paint.Style.FILL);
            paint.setShadowLayer(8f, 0.5f, 0.5f, BUTTON_COLOR);
            canvas.drawCircle(center.x, center.y, Annotation.BUTTON_RADIUS, paint);
            paint.clearShadowLayer();
            // white border
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Annotation.LINE_WIDTH);
            paint.setColor(BUTTON_BORDER_COLOR);
            canvas.drawCircle(center.x, center.y, Annotation.BUTTON_RADIUS, paint);
        }

        paint.setStrokeCap(Paint.Cap.ROUND);
        switch (type) {
            case DELETE :
                // Cross
                canvas.drawLine(center.x - Annotation.LINE_HALF_LENGTH, center.y + Annotation.LINE_HALF_LENGTH,
                        center.x + Annotation.LINE_HALF_LENGTH, center.y - Annotation.LINE_HALF_LENGTH, paint);
                canvas.drawLine(center.x - Annotation.LINE_HALF_LENGTH, center.y - Annotation.LINE_HALF_LENGTH,
                        center.x + Annotation.LINE_HALF_LENGTH, center.y + Annotation.LINE_HALF_LENGTH, paint);

                break;

            case MINIMIZE :
                // Minus
                canvas.drawLine(center.x - Annotation.LINE_HALF_LENGTH, center.y,
                    center.x + Annotation.LINE_HALF_LENGTH, center.y, paint);
                if (minimized) { // Plus
                    canvas.drawLine(center.x, center.y - Annotation.LINE_HALF_LENGTH,
                        center.x, center.y + Annotation.LINE_HALF_LENGTH, paint);
                }
                break;

            case RESIZE :
                paint.setColor(Color.BLACK);
                // Two oblic straits
                canvas.drawLine(center.x - Annotation.LINE_HALF_LENGTH, center.y + Annotation.LINE_HALF_LENGTH,
                        center.x + Annotation.LINE_HALF_LENGTH, center.y - Annotation.LINE_HALF_LENGTH, paint);
                canvas.drawLine(center.x + (Annotation.LINE_HALF_LENGTH / 2), center.y + Annotation.LINE_HALF_LENGTH,
                        center.x + Annotation.LINE_HALF_LENGTH, center.y + (Annotation.LINE_HALF_LENGTH / 2), paint);
                // TODO : Path for arrow
                break;

            case INFO :
                canvas.drawLine(center.x, center.y - Annotation.LINE_HALF_LENGTH + 2,
                        center.x, center.y + Annotation.LINE_HALF_LENGTH, paint);
                canvas.drawPoint(center.x, center.y - Annotation.LINE_HALF_LENGTH + 1, paint);
                break;

            case EDIT :
                // Three horizontal straits
                canvas.drawLine(center.x - Annotation.LINE_HALF_LENGTH, center.y - Annotation.LINE_HALF_LENGTH,
                        center.x + Annotation.LINE_HALF_LENGTH, center.y - Annotation.LINE_HALF_LENGTH, paint);
                canvas.drawLine(center.x - Annotation.LINE_HALF_LENGTH, center.y,
                        center.x + Annotation.LINE_HALF_LENGTH, center.y, paint);
                canvas.drawLine(center.x - Annotation.LINE_HALF_LENGTH, center.y + Annotation.LINE_HALF_LENGTH,
                        center.x + Annotation.LINE_HALF_LENGTH, center.y + Annotation.LINE_HALF_LENGTH, paint);
                break;
        }
    }
}
