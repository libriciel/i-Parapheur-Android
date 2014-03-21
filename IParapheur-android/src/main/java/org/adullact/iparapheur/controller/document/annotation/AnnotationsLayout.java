package org.adullact.iparapheur.controller.document.annotation;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.model.Annotation;
import org.adullact.iparapheur.model.PageAnnotations;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
* Created by jmaire on 13/01/2014.
*/
public class AnnotationsLayout extends RelativeLayout implements View.OnTouchListener, AnnotationView.AnnotationViewListener {

    private int numPage;
    private PageAnnotations annotations;
    private AnnotationView selectedAnnotation;
    private static final AtomicInteger idValue = new AtomicInteger(1);

    private GestureDetector gestureDetector;
    private float scale;

    public AnnotationsLayout(Context context, int numPage, PageAnnotations annotations, float scale) {
        super(context);
        this.annotations = annotations;

        for (Annotation annotation : this.annotations.getAnnotations()) {
            AnnotationView av = new AnnotationView(getContext(), annotation, this);
            av.setId(idValue.getAndIncrement());
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int) annotation.getRect().width(), (int) annotation.getRect().height());
            lp.setMargins((int) annotation.getRect().left, (int) annotation.getRect().top, 0, 0);
            lp.alignWithParent = true;
            av.setLayoutParams(lp);
            addView(av);
        }
        this.numPage = numPage;
        this.scale = scale;
        gestureDetector = new GestureDetector(context, new AnnotationGestureListener());
        setOnTouchListener(this);
    }

    /**
     * Any layout manager that doesn't scroll will want this.
     */
    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    /**
     * Ask all chil@dren to measure themselves with AT_MOST the width and size stored in the
     * MeasureSpecs passed as parameters.
     * Finally, set our measure respecting the MeasureSpecs passed as parameters as the mode
     * is EXACTLY.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int childWidthMesureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
        int childHeightMesureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);

        measureChildren(childWidthMesureSpec, childHeightMesureSpec);
        // Report our final dimensions.
        setMeasuredDimension(width, height);
    }

    /**
     * Position all children within this layout.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int count = getChildCount();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {

                final int childLeft = ((LayoutParams)child.getLayoutParams()).leftMargin;
                final int childTop = ((LayoutParams)child.getLayoutParams()).topMargin;
                final int childWidth = child.getMeasuredWidth();
                final int childHeight = child.getMeasuredHeight();

                // Place the child.
                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
            }
        }
    }

    private void createAnnotation(float x, float y) {
        String date = DateFormat.getDateTimeInstance().format(new Date());
        Annotation annotation = new Annotation(MyAccounts.INSTANCE.getSelectedAccount().getLogin(), numPage, false, date, x, y, "", 0, scale); // FIXME
        this.annotations.add(annotation);
        AnnotationView annotationView = new AnnotationView(getContext(), annotation, this);
        annotationView.setId(idValue.getAndIncrement());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int) annotation.getRect().width(), (int) annotation.getRect().height());
        lp.setMargins((int) annotation.getRect().left, (int) annotation.getRect().top, 0, 0);
        lp.alignWithParent = true;
        annotationView.setLayoutParams(lp);
        addView(annotationView);
        selectAnnotation(annotationView, true);
        annotationView.requestLayout();
    }

    private void unselectAnnotation(boolean informChild) {
        if (informChild && (selectedAnnotation != null)) {
            selectedAnnotation.unselect();
        }
        selectedAnnotation = null;
    }

    private void selectAnnotation(AnnotationView annotation, boolean informChild) {
        selectedAnnotation = annotation;
        if (informChild && (selectedAnnotation != null)) {
            selectedAnnotation.select();
        }
    }

    // AnnotationViewListener implementation


    @Override
    public void onAnnotationSelected(AnnotationView annotationView) {
        unselectAnnotation(true);
        selectAnnotation(annotationView, false);
    }

    @Override
    public void onAnnotationDeleted(AnnotationView annotationView) {
        unselectAnnotation(false);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            invalidate();
            return true;
        }
        return false;
    }

    private class AnnotationGestureListener extends GestureDetector.SimpleOnGestureListener {

        private final PointF offset = new PointF(); // offset from the top left corner when an annotation is selected

        @Override
        public boolean onDown(MotionEvent me) {
            //Log.i("IParapheurPDFPageView", "onDown");

            if (selectedAnnotation != null) {
                unselectAnnotation(true);
            }
            return true;
        }


        @Override
        public boolean onDoubleTap(MotionEvent me) {
            //Log.i("IParapheurPDFPageView", "onDoubleTap");
            onLongPress(me);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent me) {
            //Log.i("IParapheurPDFPageView", "onLongPress");
            createAnnotation(me.getX(), me.getY());
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }
    }
}
