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
package org.adullact.iparapheur.controller.document;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.TextView;

import com.artifex.mupdfdemo.LinkInfo;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.PageView;
import com.artifex.mupdfdemo.TextWord;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.controller.document.annotation.AnnotationView;
import org.adullact.iparapheur.model.Annotation;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jmaire
 */
public class PDFPageView extends PageView implements OnTouchListener {

    private final MuPDFCore mCore;
    private Map<Integer, List<Annotation>> annotations;
    private View annotationsView;
    private Map<Annotation, View> textViews;
    private GestureDetector gestureDetector;

    private PointF offset; // offset from the top left corner when an annotation is selected
    private Annotation selectedAnnotation;
    private Mode mode;

    private enum Mode {
        NONE,
        MOVE,
        RESIZE
    }

    public PDFPageView(Context c, MuPDFCore core, Point parentSize, Map<Integer, List<Annotation>> annotations) {
        // FIXME : bitmap not null
        super(c, parentSize, null);
        mCore = core;
        this.annotations = annotations;

        offset = new PointF();
        selectedAnnotation = null;
        mode = Mode.NONE;
        gestureDetector = new GestureDetector(c, new AnnotationGestureListener());
        textViews = new HashMap<Annotation, View>();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int w  = right-left;
        int h = bottom-top;
        if (annotationsView != null) {
            annotationsView.layout(0, 0, w, h);
        }
        for (Map.Entry<Annotation, View> entry : textViews.entrySet()) {
            if (entry.getKey().isTextVisible()) {
                Rect textRect = entry.getKey().getTextRect();
                entry.getValue().layout(textRect.left, textRect.top, textRect.right, textRect.bottom);
            }
        }
    }
    
    @Override
    protected void documentAvailable() {
        if (annotationsView == null) {
            annotationsView = new AnnotationView(this, this.getContext(), annotations.get(mPageNumber), this);
            addView(annotationsView);
        }
    }

    @Override
    public void blank(int page) {
        super.blank(page);
        setBackgroundColor(getResources().getColor(R.color.canvas));
    }

    public void clean() {
        if (selectedAnnotation != null) {
            unselectAnnotation();
        }
        mode = Mode.NONE;
        if (annotationsView != null) {
            annotationsView.invalidate();
        }
    }

    @Override
    protected void drawPage(Bitmap bm, int sizeX, int sizeY,
                    int patchX, int patchY, int patchWidth, int patchHeight) {
        mCore.drawPage(bm, mPageNumber, sizeX, sizeY, patchX, patchY, patchWidth, patchHeight);
    }

    @Override
    protected void updatePage(Bitmap bm, int sizeX, int sizeY, int patchX, int patchY, int patchWidth, int patchHeight) {
        mCore.updatePage(bm, mPageNumber, sizeX, sizeY, patchX, patchY, patchWidth, patchHeight);
    }

    @Override
    protected LinkInfo[] getLinkInfo() {
        return mCore.getPageLinks(mPageNumber);
    }

    @Override
    protected TextWord[][] getText() {
        return mCore.textLines(mPageNumber);
    }

    public float getScale() {
        return mSourceScale*(float)getWidth()/(float)mSize.x;
    }

    private void createAnnotation(float x, float y) {
        String date = DateFormat.getDateTimeInstance().format(new Date());
        Annotation annotation = new Annotation(MyAccounts.INSTANCE.getSelectedAccount().getLogin(), mPageNumber, false, date, x, y, "", 0, mSourceScale); // FIXME
        if (!annotations.containsKey(mPageNumber)) {
            this.annotations.put(mPageNumber, new ArrayList<Annotation>());
            ((AnnotationView)annotationsView).setAnnotations(this.annotations.get(mPageNumber));
        }
        annotations.get(mPageNumber).add(annotation);
        selectAnnotation(annotation);
    }
    
    private void deleteAnnotation() {
        if (selectedAnnotation != null) {
            selectedAnnotation.delete();
            selectedAnnotation = null;
        }
    }
    
    private void unselectAnnotation() {
        selectedAnnotation.unselect();
        selectedAnnotation = null;
    }

    private void selectAnnotation(Annotation annotation) {
        selectedAnnotation = annotation;
        selectedAnnotation.select();
    }
    
    private void toggleText() {
        selectedAnnotation.toggleText();
        if (selectedAnnotation.isTextVisible()) {
            showText();
        }
        else {
            hideText();
        }
    }
    
    private void hideText() {
        Log.i("IParapheurPDFPageView", "hidding text");
        textViews.get(selectedAnnotation).setVisibility(View.INVISIBLE);
    }
    
    private void showText() {
        
        createTextView();
        Log.i("IParapheurPDFPageView", "Showing text");
        textViews.get(selectedAnnotation).setVisibility(View.VISIBLE);
        textViews.get(selectedAnnotation).bringToFront();
    }

    private void createTextView() {
        if (!textViews.containsKey(selectedAnnotation)) {
            Log.i("IParapheurPDFPageView", "Creating editText");
            EditText editText = new EditText(this.getContext());
            editText.setCursorVisible(true);
            editText.setTextSize(Annotation.TEXT_SIZE);
            editText.setTextColor(Annotation.TEXT_COLOR);
            editText.setBackgroundColor(Annotation.TEXT_RECT_COLOR);
            editText.setClickable(true);
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            editText.setText(selectedAnnotation.getText(), TextView.BufferType.EDITABLE);
            editText.addTextChangedListener(new AnnotationTextWatcher());
            textViews.put(selectedAnnotation, editText);
            addView(editText);
        }
    }

    private void annotationMoved() {
        Rect textRect = selectedAnnotation.getTextRect();
        if (selectedAnnotation.isTextVisible()) {
            textViews.get(selectedAnnotation).layout(textRect.left, textRect.top, textRect.right, textRect.bottom);
        }
    }
    
    
    /**** Event listener callbacks ****/

    public boolean onTouch(View view, MotionEvent me) {
        if (gestureDetector.onTouchEvent(me)) {
            annotationsView.invalidate();
            return true;
        }
        return false;
    }    
    
    
    private class AnnotationGestureListener extends GestureDetector.SimpleOnGestureListener {
        
        @Override
        public boolean onDown(MotionEvent me) {
            Log.i("IParapheurPDFPageView", "onDown");
            mode = Mode.NONE;
            if (selectedAnnotation != null) {
                Log.i("IParapheurPDFPageView", "onDown area of selected annotation : " + selectedAnnotation.getArea(me.getX(), me.getY()).name());
                switch (selectedAnnotation.getArea(me.getX(), me.getY())) {
                    case CENTER :
                        mode = Mode.MOVE;
                        offset.set(me.getX() - selectedAnnotation.getRect().left, me.getY() - selectedAnnotation.getRect().top);
                        break;
                    case RESIZE :
                        mode = Mode.RESIZE;
                        break;
                }
            }
            // Pour empêcher de tourner les pages.
            if (!mode.equals(Mode.NONE)) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            return true;
        }
        
        @Override
        public boolean onSingleTapConfirmed(MotionEvent me) {
            Log.i("IParapheurPDFPageView", "onSingleTapConfirmed");
            boolean invalidate = false;
            // Nouvelle selection ou déselection
            if ((selectedAnnotation == null) || !selectedAnnotation.touched(me.getX(), me.getY())) {
                if (selectedAnnotation != null) {
                    unselectAnnotation();
                    invalidate = true;
                }
                if (annotations.containsKey(mPageNumber)) {
                    for (Annotation annotation : annotations.get(mPageNumber)) {
                        Annotation.Area touched = annotation.getArea(me.getX(), me.getY());
                        if (!touched.equals(Annotation.Area.NONE)) {
                            selectAnnotation(annotation);
                            if (touched.equals(Annotation.Area.MINIMIZE)) {
                                selectedAnnotation.maximize();
                            }
                            invalidate = true;
                            break;
                        }
                    }
                }
            }
            else if (selectedAnnotation != null) { // On veut interagir avec l'annotation
                Log.i("IParapheurPDFPageView", "onSingleTapConfirmed area : " + selectedAnnotation.getArea(me.getX(), me.getY()));
                switch (selectedAnnotation.getArea(me.getX(), me.getY())) {
                    case EDIT :
                        toggleText();
                        break;
                    case MINIMIZE :
                        selectedAnnotation.toggleSize();
                        break;
                    case INFO :
                        selectedAnnotation.toggleInfos();
                        break;
                    case DELETE :
                        // TODO : possibilité d'annuler la suppression?
                        deleteAnnotation();
                        break;
                }
                invalidate = true;
            }
            // Invalidate here because this function is not called on au touch event.
            if (invalidate) {
                annotationsView.invalidate();
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent start, MotionEvent current, float f, float f1) {
            Log.i("IParapheurPDFPageView", "onScroll");
            if (selectedAnnotation != null) {
                Log.i("IParapheurPDFPageView", "onScroll mode : " + mode.name());
                switch (mode) {
                    case MOVE :
                        selectedAnnotation.moveTo(current.getX(), current.getY(), offset);
                        annotationMoved();
                        break;
                    case RESIZE :
                        selectedAnnotation.resize(current.getX(), current.getY());
                        annotationMoved();
                        break;
                }
            }
            return !mode.equals(Mode.NONE);
        }

        @Override
        public boolean onDoubleTap(MotionEvent me) {
            Log.i("IParapheurPDFPageView", "onDoubleTap");
            onLongPress(me);
            return true;
        }
        
        @Override
        public void onLongPress(MotionEvent me) {
            Log.i("IParapheurPDFPageView", "onLongPress");
            if ((selectedAnnotation == null) || !selectedAnnotation.touched(me.getX(), me.getY())) {
                if (selectedAnnotation != null) {
                    unselectAnnotation();
                }
                if (annotations.containsKey(mPageNumber)) {   
                    for (Annotation annotation : annotations.get(mPageNumber)) {
                        if (annotation.touched(me.getX(), me.getY())) {
                            selectedAnnotation = annotation;
                            break;
                        }
                    }
                }
                if (selectedAnnotation == null) {
                    createAnnotation(me.getX(), me.getY());
                }
                RectF rect = selectedAnnotation.getRect();
                offset.set(me.getX() - rect.left, me.getY() - rect.top);
                // Invalidate here because this function is not called on au touch event.
                annotationsView.invalidate();
            }
        }
        
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
        
        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }
    }


    private class AnnotationTextWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            selectedAnnotation.setText(s.toString());
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
    
}