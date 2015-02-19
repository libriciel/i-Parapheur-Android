package org.adullact.iparapheur.controller.document;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
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
	private Point mPdfSize;

	public PageLayout(Context context, int numPage) {
		super(context);
		addChildViews(numPage);
		mGestureDetector = new GestureDetector(context, new AnnotationGestureListener());
		setOnTouchListener(this);
	}

	public void setPageLayoutListener(PageLayoutListener pageLayoutListener) {
		mPageLayoutListener = pageLayoutListener;
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

	public void update(Bitmap pageImage, PageAnnotations annotations, Point initSize, Point pdfSize) {
		mInitialWidth = initSize.x;
		mInitialHeight = initSize.y;
		mChildScale = 1.0f;
		mPageScale = 1.0f;
		mPdfSize = pdfSize;

		ScrollView.LayoutParams layoutParam = new ScrollView.LayoutParams(mInitialWidth, mInitialHeight);
		layoutParam.gravity = Gravity.CENTER_HORIZONTAL;
		setLayoutParams(layoutParam);
		scalePage();

		requestLayout();
		requestLayout();
		invalidate();

		mAnnotationsView.setAnnotations(annotations);
		refreshDisplayedAnnotations(initSize);

		mImageView.setImageBitmap(pageImage);
	}

	public void refreshDisplayedAnnotations(Point targetSize) {
		mAnnotationsView.refreshDisplayedAnnotations(mPdfSize, targetSize);
	}

	// <editor-fold desc="AnnotationsLayoutListener">

	@Override
	public void onCreateAnnotation(Annotation annotation) {
		if (mPageLayoutListener != null)
			mPageLayoutListener.onCreateAnnotation(annotation);
	}

	@Override
	public void onUpdateAnnotation(Annotation annotation) {
		if (mPageLayoutListener != null)
			mPageLayoutListener.onUpdateAnnotation(annotation);
	}

	@Override
	public void onDeleteAnnotation(Annotation annotation) {
		if (mPageLayoutListener != null)
			mPageLayoutListener.onDeleteAnnotation(annotation);
	}

	// </editor-fold desc="AnnotationsLayoutListener">

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		boolean ret = mGestureDetector.onTouchEvent(event);
		return (ret || super.onTouchEvent(event));
	}

	public int getInitialWidth() {
		return mInitialWidth;
	}

	public int getInitialHeight() {
		return mInitialHeight;
	}

	//<editor-fold desc="Listener"

	public interface PageLayoutListener {

		public void onCreateAnnotation(Annotation annotation);

		public void onUpdateAnnotation(Annotation annotation);

		public void onDeleteAnnotation(Annotation annotation);

		public void onDoubleTap(MotionEvent me);
	}

	//</editor-fold desc="Listener"

	private class AnnotationGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent me) {
			mAnnotationsView.deselectAnnotation(true);
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
}
