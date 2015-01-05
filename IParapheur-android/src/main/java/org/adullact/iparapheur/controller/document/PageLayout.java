package org.adullact.iparapheur.controller.document;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;

import org.adullact.iparapheur.controller.document.annotation.AnnotationsLayout;
import org.adullact.iparapheur.model.Annotation;
import org.adullact.iparapheur.model.PageAnnotations;

public class PageLayout extends FrameLayout implements View.OnTouchListener, AnnotationsLayout.AnnotationsLayoutListener {

    private AnnotationsLayout mAnnotationsView;
    private ImageView mImageView;
    private int mInitialWidth = 0;
    private int mInitialHeight = 0;
    private GestureDetector mGestureDetector;
    private float mPageScale, mChildScale;
    private PageLayoutListener mPageLayoutListener;

    public PageLayout(Context context, int numPage) {
        super(context);
        addChildViews(numPage);
        mGestureDetector = new GestureDetector(context, new AnnotationGestureListener());
        setOnTouchListener(this);
    }

    private void addChildViews(int numPage) {
        ViewGroup.LayoutParams childLayoutParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.mAnnotationsView = new AnnotationsLayout(getContext(), numPage, this);
        this.mAnnotationsView.setLayoutParams(childLayoutParam);

        this.mImageView = new ImageView(getContext());
        this.mImageView.setLayoutParams(childLayoutParam);
        this.mImageView.setAdjustViewBounds(true);
        this.mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        this.addView(this.mImageView);
        this.addView(this.mAnnotationsView);
    }

    private void scalePage() {
        mAnnotationsView.setScaleX(mChildScale);
        mAnnotationsView.setScaleY(mChildScale);
        mImageView.setScaleX(mChildScale);
        mImageView.setScaleY(mChildScale);

        ScrollView.LayoutParams layoutParam = new ScrollView.LayoutParams((int) (mInitialWidth * mPageScale), (int) (mInitialHeight * mPageScale));
        layoutParam.gravity = Gravity.CENTER_HORIZONTAL;

        this.setLayoutParams(layoutParam);
        setScaleX(mPageScale);
        setScaleY(mPageScale);
    }

    public void update(Bitmap pageImage, PageAnnotations annotations) {
        mAnnotationsView.setAnnotations(annotations);
        mImageView.setImageBitmap(pageImage);

        mInitialWidth = pageImage.getWidth();
        mInitialHeight = pageImage.getHeight();
        mChildScale = 1.0f;
        mPageScale = 1.0f;

        ScrollView.LayoutParams layoutParam = new ScrollView.LayoutParams(mInitialWidth, mInitialHeight);
        layoutParam.gravity = Gravity.CENTER_HORIZONTAL;
        setLayoutParams(layoutParam);
        scalePage();

        requestLayout();
        requestLayout();
        invalidate();
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
        boolean ret = mGestureDetector.onTouchEvent(event);
//        ret = ret || scaleGestureDetector.onTouchEvent(event);
        return ret || super.onTouchEvent(event);
    }

    public int getInitialWidth() {
        return mInitialWidth;
    }

    public int getInitialHeight() {
        return mInitialHeight;
    }

    private class AnnotationGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent me) {
            mAnnotationsView.unselectAnnotation(true);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent me) {

            if (mPageLayoutListener != null)
                mPageLayoutListener.onDoubleTap(me);

            return true;
        }

        @Override
        public void onLongPress(MotionEvent me) {
            mAnnotationsView.createAnnotation(me.getX(), me.getY());
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }
    }

    //<editor-fold desc="PageLayoutListener"

    public interface PageLayoutListener {

        public void onDoubleTap(MotionEvent me);
    }

    public void setPageLayoutListener(PageLayoutListener pageLayoutListener) {
        mPageLayoutListener = pageLayoutListener;
    }

    //</editor-fold desc="PageLayoutListener"

}
