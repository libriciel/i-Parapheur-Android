package org.adullact.iparapheur.controller.document;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.adullact.iparapheur.controller.document.annotation.AnnotationsLayout;
import org.adullact.iparapheur.model.Annotation;
import org.adullact.iparapheur.model.PageAnnotations;

/**
 * Created by jmaire on 28/11/2014.
 */
public class PageLayout extends FrameLayout implements View.OnTouchListener, AnnotationsLayout.AnnotationsLayoutListener {

    private AnnotationsLayout annotationsView;
    private ImageView imageView;
    private int initialWidth = 0;
    private int initialHeight = 0;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private boolean scaling = false;
    private float scale = 1.0f;
    private float pageMaxScale, pageScale, childScale;

    public PageLayout(Context context, int numPage) {
        super(context);
        addChildViews(numPage);
        scaleGestureDetector = new ScaleGestureDetector(context, new AnnotationScaleGestureListener());
        gestureDetector = new GestureDetector(context, new AnnotationGestureListener());
        setOnTouchListener(this);
    }

    private void addChildViews(int numPage) {
        ViewGroup.LayoutParams childLayoutParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.annotationsView = new AnnotationsLayout(getContext(), numPage, this);
        this.annotationsView.setLayoutParams(childLayoutParam);

        this.imageView = new ImageView(getContext());
        this.imageView.setLayoutParams(childLayoutParam);
        this.imageView.setAdjustViewBounds(true);
        this.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        this.addView(this.imageView);
        this.addView(this.annotationsView);
    }

    private void scalePage() {
        annotationsView.setScaleX(childScale);
        annotationsView.setScaleY(childScale);
        imageView.setScaleX(childScale);
        imageView.setScaleY(childScale);

        //RelativeLayout.LayoutParams layoutParam = new RelativeLayout.LayoutParams((int) (initialWidth * pageScale), (int) (initialHeight * pageScale));
        //layoutParam.addRule(RelativeLayout.CENTER_IN_PARENT);
        //this.setLayoutParams(layoutParam);
        //setScaleX(pageScale);
        //setScaleY(pageScale);

        //Log.i("debug", "child X : " + annotationsView.getX() + "child Y : " + annotationsView.getY());
        //Log.i("debug", "Y | SCROLL Y | TOP | BOTTOM | TRANSLATION Y | SCALE Y" + getY());
        //Log.i("debug", "" + getY() + " | "+ getScrollY() + " | " + getTop() + " | " + getBottom() + " | " + getTranslationY() + " | " + getScaleY());
    }

    public void update(Bitmap pageImage, PageAnnotations annotations) {
        this.annotationsView.setAnnotations(annotations);
        this.imageView.setImageBitmap(pageImage);

        this.initialWidth = pageImage.getWidth();
        this.initialHeight = pageImage.getHeight();
        this.pageMaxScale = 1.0f; //((RelativeLayout) getParent()).getWidth() * 0.85f / initialWidth;
        this.childScale = 1.0f;
        this.pageScale = 1.0f;

        RelativeLayout.LayoutParams layoutParam = new RelativeLayout.LayoutParams(initialWidth, initialHeight);
        layoutParam.addRule(RelativeLayout.CENTER_IN_PARENT);
        this.setLayoutParams(layoutParam);
        scalePage();

        this.requestLayout();
        this.requestLayout();
        this.invalidate();
    }

    //AnnotationsLayout callbacks implementation

    @Override
    public void onCreateAnnotation(Annotation annotation) {
        // TODO : save annotation
        Log.i("debug", "onCreateAnnotation");
        /*try {
            RESTClient.INSTANCE.createAnnotation(dossierId, annotation, numPage);
        } catch (IParapheurException e) {
            Toast.makeText(getActivity(), e.getResId(), Toast.LENGTH_LONG).show();
        }*/
    }

    @Override
    public void onUpdateAnnotation(Annotation annotation) {
        // TODO : save annotation
        Log.i("debug", "onUpdateAnnotation");
        /*try {
            RESTClient.INSTANCE.updateAnnotation(dossierId, annotation, numPage);
        } catch (IParapheurException e) {
            Toast.makeText(getActivity(), e.getResId(), Toast.LENGTH_LONG).show();
        }*/
    }

    @Override
    public void onDeleteAnnotation(Annotation annotation) {
        // TODO : save annotation
        Log.i("debug", "onDeleteAnnotation");
        /*try {
            RESTClient.INSTANCE.deleteAnnotation(dossierId, annotation.getUuid(), numPage);
        } catch (IParapheurException e) {
            Toast.makeText(getActivity(), e.getResId(), Toast.LENGTH_LONG).show();
        }*/
    }


    // Scaling gestures
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean ret = gestureDetector.onTouchEvent(event);
        ret = ret || scaleGestureDetector.onTouchEvent(event);
        return  ret || super.onTouchEvent(event);
    }

    private class AnnotationGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent me) {
            annotationsView.unselectAnnotation(true);
            return true;
        }


        @Override
        public boolean onDoubleTap(MotionEvent me) {
            onLongPress(me);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent me) {
            annotationsView.createAnnotation(me.getX(), me.getY());
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //getParent().requestDisallowInterceptTouchEvent(true);
            if (!scaling && scale > 1.0f) {
                int diste1e2X = (int) (e2.getRawX() - e1.getRawX());
                int diste1e2Y = (int) (e2.getRawY() - e1.getRawY());
                //setTranslationY(diste1e2Y);
                return true;
            }
            return false;
        }
    }

    private class AnnotationScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= detector.getScaleFactor();
            // Don't let the object get too small or too large.
            scale = Math.max(1.0f, Math.min(scale, 3.0f));
            if (scale > pageMaxScale) {
                childScale = 1 + scale - pageMaxScale;
                pageScale = pageMaxScale;

            }
            else {
                pageScale = scale;
                childScale = 1.0f;
            }
            scalePage();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            scaling = true;
            Log.i("debug", "FOCUS : (" + detector.getFocusX() + ", " + detector.getFocusY() + ")");
            int[] parentLocation = new int[2];
            int[] childLocation = new int[2];
            getLocationInWindow(parentLocation);
            imageView.getLocationInWindow(childLocation);
            Log.i("debug", "PARENT LOC : (" + parentLocation[0] + ", " + parentLocation[1] + ")");
            Log.i("debug", "CHILD LOC : (" + childLocation[0] + ", " + childLocation[1] + ")");
            float childPivotX = detector.getFocusX() + parentLocation[0] - childLocation[0];
            float childPivotY = detector.getFocusY() + parentLocation[1] - childLocation[1];
            //setPivotX(detector.getFocusX());
            //setPivotY(detector.getFocusY());*/
            float previousPivotX = annotationsView.getPivotX();
            float previousPivotY = annotationsView.getPivotY();
            annotationsView.setPivotX(detector.getFocusX());
            annotationsView.setPivotY(detector.getFocusY());
            imageView.setPivotX(detector.getFocusX());
            imageView.setPivotY(detector.getFocusY());
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            scaling = false;
            super.onScaleEnd(detector);
        }
    }
}
