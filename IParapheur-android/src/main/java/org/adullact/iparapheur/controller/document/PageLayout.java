package org.adullact.iparapheur.controller.document;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
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
		FrameLayout.LayoutParams childLayoutParam = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mAnnotationsView = new AnnotationsLayout(getContext(), numPage, this);
        mAnnotationsView.setLayoutParams(childLayoutParam);

        mImageView = new ImageView(getContext());
        mImageView.setLayoutParams(childLayoutParam);
        mImageView.setAdjustViewBounds(true);
        mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        addView(mImageView);
        addView(mAnnotationsView);
    }

    private void scalePage() {
        mAnnotationsView.setScaleX(mChildScale);
        mAnnotationsView.setScaleY(mChildScale);
        mImageView.setScaleX(mChildScale);
        mImageView.setScaleY(mChildScale);

        ScrollView.LayoutParams layoutParam = new ScrollView.LayoutParams((int) (mInitialWidth * mPageScale), (int) (mInitialHeight * mPageScale));
        layoutParam.gravity = Gravity.CENTER_HORIZONTAL;

        setLayoutParams(layoutParam);
        setScaleX(mPageScale);
        setScaleY(mPageScale);
    }

    public void update(Bitmap pageImage, PageAnnotations annotations, Point initSize) {
        mAnnotationsView.setAnnotations(annotations);
        mImageView.setImageBitmap(pageImage);

        mInitialWidth = initSize.x;
        mInitialHeight = initSize.y;
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
