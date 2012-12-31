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
package org.adullact.iparapheur.tab.ui.folder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import com.artifex.mupdf.LinkInfo;
import com.artifex.mupdf.MuPDFCore;
import com.artifex.mupdf.PageView;
import de.akquinet.android.androlog.Log;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.adullact.iparapheur.tab.model.Annotation;

/**
 *
 * @author jmaire
 */
public class IParapheurPDFPageView extends PageView
                                   implements OnTouchListener {
    
    private final MuPDFCore mCore;
    private Map<Integer, List<Annotation>> annotations;
    private View annotationsView;
    private GestureDetector gestureDetector;
   
    private PointF offset; // offset from the top left corner when an annotation is selected
    private Annotation selectedAnnotation;
    private Mode mode;

    private enum Mode {
        NONE,
        MOVE,
        RESIZE
    }
    
    

    public IParapheurPDFPageView(Context c, MuPDFCore core, Point parentSize, Map<Integer, List<Annotation>> annotations) {
        super(c, parentSize);
        mCore = core;
        this.annotations = annotations;

        offset = new PointF();
        selectedAnnotation = null;
        mode = Mode.NONE;
        gestureDetector = new GestureDetector(c, new AnnotationGestureListener());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int w  = right-left;
        int h = bottom-top;
        if (annotationsView != null) {
            annotationsView.layout(0, 0, w, h);
        }
    }

    @Override
    protected void documentAvailable() {
        if (annotationsView == null) {
            annotationsView = new AnnotationView(this.getContext(), annotations.get(mPageNumber), this);
            addView(annotationsView);
        }
    }

    @Override
    protected void drawPage(Bitmap bm, int sizeX, int sizeY,
                    int patchX, int patchY, int patchWidth, int patchHeight) {
        mCore.drawPage(mPageNumber, bm, sizeX, sizeY, patchX, patchY, patchWidth, patchHeight);
    }

    @Override
    protected LinkInfo[] getLinkInfo() {
            return mCore.getPageLinks(mPageNumber);
    }

    private Annotation createAnnotation(float x, float y) {
        Annotation annotation = new Annotation("author", mPageNumber, false, new Date().toString(), x, y, "", 0); // FIXME
        if (!annotations.containsKey(mPageNumber)) {
            this.annotations.put(mPageNumber, new ArrayList<Annotation>());
        }
        annotations.get(mPageNumber).add(annotation);
        return annotation;
    }
    
    private void unselectAnnotation() {
        if (selectedAnnotation != null) {
            selectedAnnotation.unselect();
            selectedAnnotation = null;
        }
    }
    
    
    /**** Event listener callbacks ****/

    @Override
    public boolean onTouch(View view, MotionEvent me) {
        boolean result = gestureDetector.onTouchEvent(me);
        if (result) {
            view.invalidate();
        }
        return result;
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
            return true;
        }
        
        @Override
        public boolean onSingleTapConfirmed(MotionEvent me) {
            Log.i("IParapheurPDFPageView", "onSingleTapConfirmed");

            if (selectedAnnotation != null) {
                Log.i("IParapheurPDFPageView", "onSingleTapConfirmed area : " + selectedAnnotation.getArea(me.getX(), me.getY()));
                switch (selectedAnnotation.getArea(me.getX(), me.getY())) {
                    case EDIT :
                        selectedAnnotation.toggleText();
                        break;
                    case MAXIMIZE : // Same that MINIMIZED
                    case MINIMIZE :
                        selectedAnnotation.toggleSize();
                        break;
                    case INFO :
                        selectedAnnotation.toggleInfos();
                        break;
                    case DELETE :
                        // TODO : possibilité d'annuler la suppression?
                        annotations.get(mPageNumber).remove(selectedAnnotation);
                        unselectAnnotation();
                        break;
                    case NONE :
                        unselectAnnotation();
                        break;
                }
            }
            // Invalidate here because this function is not called on au touch event.
            invalidate();
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
                        break;
                    case RESIZE :
                        selectedAnnotation.resize(current.getX(), current.getY());
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

                if (annotations.containsKey(mPageNumber)) {   
                    for (Annotation annotation : annotations.get(mPageNumber)) {
                        if (annotation.touched(me.getX(), me.getY())) {

                            if (selectedAnnotation != null) {
                                selectedAnnotation.unselect();
                            }
                            selectedAnnotation = annotation;
                            break;
                        }
                    }
                }
                if (selectedAnnotation == null) {
                    selectedAnnotation = createAnnotation(me.getX(), me.getY());
                }
                selectedAnnotation.select();
                RectF rect = selectedAnnotation.getRect();
                offset.set(me.getX() - rect.left, me.getY() - rect.top);
                // Invalidate here because this function is not called on au touch event.
                invalidate();
            }
        }
        
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
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
    
    
    
    private class AnnotationView extends View {

        private List<Annotation> annotations;
        private Context context;

        public AnnotationView(Context context, List<Annotation> annotations, View listener) {
            super(context);
            this.annotations = annotations;
            this.setOnTouchListener((OnTouchListener) listener);
            this.context = context;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (!isBlanck() && (annotations != null)) {
                for (Annotation annotation : annotations) {
                    annotation.draw(canvas);
                    if (annotation.isTextVisible()) {
                        EditText text = new EditText(context);
                        text.setVisibility(View.VISIBLE);
                        text.setText(annotation.getText());
                        text.setX(annotation.getRect().left);
                        text.setY(annotation.getRect().bottom);
                        text.setWidth((int)annotation.getRect().width());
                        text.setHeight((int)annotation.getRect().height());
                        text.setCursorVisible(true);
                        addView(text);
                        invalidate();
                    }
                }
            }
        }
        
    }
}