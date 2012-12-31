/*
 * Version 2.1
 * CeCILL Copyright (c) 2008-2012, ADULLACT-projet
 * Initiated by ADULLACT-projet S.A.
 * Developed by ADULLACT-projet S.A.
 * 
 * contact@adullact-projet.coop
 * 
 * Ce logiciel est un programme informatique servant à faire circuler des
 * documents au travers d'un circuit de validation, où chaque acteur vise
 * le dossier, jusqu'à l'étape finale de signature.
 * 
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 * 
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 * 
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 * 
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 * 
 */
package org.adullact.iparapheur.tab.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import org.adullact.iparapheur.tab.ui.folder.IParapheurPDFPageView;

/**
 *
 * @author jmaire
 */
public class Annotation {
    
    public static final int BORDER_WIDTH = 4;
    public static final int CORNER_RADIUS = 10;
    public static final int HALF_MIN_WIDTH = 35;
    public static final int HALF_MIN_HEIGHT = 20;
    
    public static final int LINE_WIDTH = 3;
    public static final int LINE_HALF_LENGTH = 4;
    
    // Space between two buttons from center to center
    public static final int BUTTONS_SPACE = 28;
    
    public static final int COLOR_SELECTED = Color.RED;
    public static final int COLOR_BORDER = Color.BLACK;
    
    public static final int BUTTON_RADIUS = 10;
    public static final int BUTTON_COLOR = Color.BLACK;
    public static final int BUTTON_BORDER_COLOR = Color.WHITE;
    
    public static final int TEXT_RECT_WIDTH = 85;
    public static final int TEXT_RECT_HEIGHT = 55;
    public static final int TEXT_RECT_COLOR = Color.YELLOW;
    public static final int TEXT_RECT_ALPHA = 230; //90%
    
    public static final int TEXT_COLOR = Color.BLACK;
    public static final int TEXT_SIZE = 8;
    
    public static final int INFOS_RECT_WIDTH = 50;
    public static final int INFOS_RECT_HEIGHT = 35;
    public static final int INFOS_COLOR = Color.CYAN;
    public static final int INFOS_ALPHA = 179; //70%
    public static final int INFOS_BORDER_WIDTH = 3;
    public static final int INFOS_BORDER_COLOR = Color.BLACK;
    public static final int INFOS_BORDER_ALPHA = 179; //70%
    
    public static final int ALPHA_SELECTED = 51; //20%
    public static final int ALPHA_BORDER = 179; //70%
    
    private enum Button {
        DELETE,
        MINIMIZE,
        MAXIMIZE,
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
        MAXIMIZE,
        INFO
    }
    
    private String uuid;
    private int page;
    private String author;
    private boolean secretaire;
    private String date;
    private RectF rect;
    private String text;
    private String type;
    private int step;
    private Color fillColor;
    private Color penColor;
    
    private boolean selected = false;
    private boolean minimized = false;
    private boolean textVisible = false;
    private boolean infosVisible = false;
    
    
    
    public Annotation(String author, int page, boolean secretaire, String date,
                      float x, float y, String text, int step) {
        
        this.author = author;
        this.page = page;
        this.secretaire = secretaire;
        this.date = date;
        this.rect = new RectF(x - Annotation.HALF_MIN_WIDTH, y - Annotation.HALF_MIN_HEIGHT, x + Annotation.HALF_MIN_WIDTH, y + Annotation.HALF_MIN_HEIGHT);
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
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public RectF getRect() {
        return rect;
    }
    
    public String getText() {
        return this.text;
    }
    
    public void moveTo(float x, float y, PointF offset) {
        this.rect.offsetTo(x - offset.x, y - offset.y);
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
    }
    
    public boolean isEditable(int step) {
        return true; // TODO
    }
    
    /*public void setScale(int scale) {
        int offset = 5;
        if ((scale % 2) == 0) {
            offset = - 5;
        }
        rect.set(rect.left - offset, rect.top - offset, rect.right + offset, rect.bottom + offset);
    }*/
    
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
    
    public void showText() {
        this.textVisible = true;
    }
    
    public void hideText() {
        this.textVisible = false;
    }
    
    public void toggleText() {
        this.textVisible = !textVisible;
    }
    
    public void showInfos() {
        this.infosVisible = true;
    }
    
    public void hideInfos() {
        this.infosVisible = false;
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
        return !getArea(x, y).equals(Annotation.Area.NONE);
    }
    
    // Methods and utilities for drawing
    
    private float distance(float x, float y, PointF point2) {
        float dx = x - point2.x;
        float dy = y - point2.y;
        return (float)Math.sqrt((dx * dx) + (dy * dy));
    }
    
    public Area getArea(float x, float y) {
        Area area = Area.NONE;
        if (rect.contains(x, y)) {
            area = Area.CENTER;
        }
        for (Button button : Button.values()) {
            if (distance(x, y, getButtonCenter(rect, button)) < Annotation.BUTTON_RADIUS) {
                area = Area.valueOf(button.name());
                break;
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
            case MAXIMIZE : // MAXIMIZE and MINIMIZE are on the same location
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
    
    private RectF getInfosRect(RectF annot) {
        return new RectF(annot.left, annot.bottom + 5, annot.left + INFOS_RECT_WIDTH, annot.bottom + INFOS_RECT_HEIGHT);
    }
    
    private RectF getTextRect(RectF annot) {
        return new RectF(annot.left, annot.bottom + 5, annot.left + TEXT_RECT_WIDTH, annot.bottom + TEXT_RECT_HEIGHT);
    }
    
    
    public void draw(Canvas canvas) {
        
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        if (isMinimized()) {
            drawButton(canvas, paint, Button.DELETE);
            drawButton(canvas, paint, Button.MAXIMIZE);
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
                drawText(canvas, paint);
            }
            if (infosVisible) {
                drawInfos(canvas, paint);
            }
        }
    }
    
    protected void drawRect(Canvas canvas, Paint paint) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(COLOR_BORDER);
        paint.setAlpha(ALPHA_SELECTED);
        paint.setStrokeWidth(Annotation.BORDER_WIDTH);
        canvas.drawRoundRect(rect, Annotation.CORNER_RADIUS, Annotation.CORNER_RADIUS, paint);
        if (selected) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(COLOR_SELECTED);
            paint.setAlpha(ALPHA_SELECTED);
            canvas.drawRoundRect(rect, Annotation.CORNER_RADIUS, Annotation.CORNER_RADIUS, paint);
        }
    }
    
    private void drawInfos(Canvas canvas, Paint paint) {
        RectF infosRect = getInfosRect(rect);
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
        String infos = author + "  " + date;
        canvas.drawText(infos, infosRect.left + 10, infosRect.top + 15, paint);
        
    }
    
    private void drawText(Canvas canvas, Paint paint) {
        RectF textRect = getTextRect(rect);
        
        // Text Rect
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(TEXT_RECT_COLOR);
        paint.setAlpha(TEXT_RECT_ALPHA);
        canvas.drawRoundRect(textRect, 0f, 0f, paint);
        
        // Text
        paint.setColor(TEXT_COLOR);
        paint.setTextSize(TEXT_SIZE);
        canvas.drawText(text, textRect.left + 10, textRect.top + 15, paint);
    }
    
    private void drawButton(Canvas canvas, Paint paint, Button type) {
        PointF center = getButtonCenter(rect, type);
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
                break;

            case MAXIMIZE :
                // Plus
                canvas.drawLine(center.x - Annotation.LINE_HALF_LENGTH, center.y,
                                center.x + Annotation.LINE_HALF_LENGTH, center.y, paint);
                canvas.drawLine(center.x, center.y - Annotation.LINE_HALF_LENGTH,
                                center.x, center.y + Annotation.LINE_HALF_LENGTH, paint);
                break;

            case RESIZE :
                // Two oblic straits
                canvas.drawLine(center.x - Annotation.LINE_HALF_LENGTH, center.y + Annotation.LINE_HALF_LENGTH,
                                center.x + Annotation.LINE_HALF_LENGTH, center.y - Annotation.LINE_HALF_LENGTH, paint);
                canvas.drawLine(center.x, center.y + Annotation.LINE_HALF_LENGTH,
                                center.x + Annotation.LINE_HALF_LENGTH, center.y, paint);
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
