package org.adullact.iparapheur.controller.document.annotation;

import android.content.Context;
import android.graphics.Point;
import android.graphics.RectF;
import android.view.View;
import android.widget.RelativeLayout;

import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.model.Annotation;
import org.adullact.iparapheur.model.PageAnnotations;
import org.adullact.iparapheur.utils.DeviceUtils;

import java.text.DateFormat;
import java.util.Date;

public class AnnotationsLayout extends RelativeLayout implements AnnotationView.AnnotationViewListener {

	private int mNumPage;
	private PageAnnotations mAnnotations;
	private AnnotationView mSelectedAnnotation;
	private Point mPdfSize;
	private Point mInitialSize;

	private AnnotationsLayoutListener mListener;

	public AnnotationsLayout(Context context, int numPage, AnnotationsLayoutListener listener) {
		super(context);

		mListener = listener;
		mAnnotations = new PageAnnotations();
		mNumPage = numPage;
	}

	// <editor-fold desc="RelativeLayout">

	/**
	 * Any layout manager that doesn't scroll will want this.
	 */
	@Override
	public boolean shouldDelayChildPressedState() {
		return false;
	}

	/**
	 * Ask all children to measure themselves with AT_MOST the width and size stored in the
	 * MeasureSpecs passed as parameters.
	 * Finally, set our measure respecting the MeasureSpecs passed as parameters as the mode
	 * is EXACTLY.
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);

		int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
		int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);

		measureChildren(childWidthMeasureSpec, childHeightMeasureSpec);
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

				final int childLeft = ((LayoutParams) child.getLayoutParams()).leftMargin;
				final int childTop = ((LayoutParams) child.getLayoutParams()).topMargin;
				final int childWidth = child.getMeasuredWidth();
				final int childHeight = child.getMeasuredHeight();

				child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
			}
		}
	}

	// </editor-fold desc="RelativeLayout">

	public void setAnnotations(PageAnnotations annotations) {
		mAnnotations = annotations;
	}

	public void refreshDisplayedAnnotations(Point pdfSize, Point initialSize) {
		mPdfSize = pdfSize;
		mInitialSize = initialSize;

		if ((mAnnotations == null) || (mAnnotations.getAnnotations() == null))
			return;

		for (Annotation annotation : mAnnotations.getAnnotations()) {
			if (annotation != null) {
				View annotationView = findViewWithTag(annotation.getUuid());

				if (annotationView == null) {
					annotationView = createAnnotationView(annotation);
					addView(annotationView);
				}

				annotationView.setLayoutParams(computeLayoutParams((RelativeLayout.LayoutParams) annotationView.getLayoutParams(), annotation.getRect()));
				annotationView.requestLayout();
			}
		}
	}

	public void createAnnotation(float x, float y) {
		String date = DateFormat.getDateTimeInstance().format(new Date());

		int annotationHeight = Math.round(DeviceUtils.dipsToPixels(getContext(), AnnotationView.MIN_HEIGHT));
		int annotationWidth = Math.round(DeviceUtils.dipsToPixels(getContext(), AnnotationView.MIN_WIDTH));
		RectF annotationLayoutRect = new RectF(x - (annotationWidth / 2), y - (annotationHeight / 2), x + (annotationWidth / 2), y + (annotationHeight / 2));
		RectF annotationsPdfRect = annotationLayoutCoordinatesToPdfCoordinates(annotationLayoutRect);

		Annotation annotation = new Annotation(MyAccounts.INSTANCE.getSelectedAccount().getLogin(), mNumPage, false, date, annotationsPdfRect, "", 0);
		mAnnotations.add(annotation);

		AnnotationView annotationView = createAnnotationView(annotation);
		selectAnnotation(annotationView, true);
		addView(annotationView);
		annotationView.requestLayout();

		mListener.onCreateAnnotation(annotation);
	}

	private RectF annotationPdfCoordinatesToLayoutCoordinates(RectF rect) {

		float resultLeft = rect.left * mInitialSize.x / mPdfSize.x;
		float resultTop = rect.top * mInitialSize.x / mPdfSize.x;
		float resultBottom = rect.bottom * mInitialSize.x / mPdfSize.x;
		float resultRight = rect.right * mInitialSize.x / mPdfSize.x;

		return new RectF(resultLeft, resultTop, resultRight, resultBottom);
	}

	private RectF annotationLayoutCoordinatesToPdfCoordinates(RectF rect) {

		float resultLeft = rect.left * mPdfSize.x / mInitialSize.x;
		float resultTop = rect.top * mPdfSize.x / mInitialSize.x;
		float resultBottom = rect.bottom * mPdfSize.x / mInitialSize.x;
		float resultRight = rect.right * mPdfSize.x / mInitialSize.x;

		return new RectF(resultLeft, resultTop, resultRight, resultBottom);
	}

	private AnnotationView createAnnotationView(Annotation annotation) {

		AnnotationView annotationView = new AnnotationView(getContext(), annotation, this);
		annotationView.setTag(annotation.getUuid());
		annotationView.setLayoutParams(computeLayoutParams(null, annotation.getRect()));

		return annotationView;
	}

	private RelativeLayout.LayoutParams computeLayoutParams(RelativeLayout.LayoutParams recycledLP, RectF annotationRect) {
		RectF scaledAnnotationRect = annotationPdfCoordinatesToLayoutCoordinates(annotationRect);

		// Recycle previous LP, if any
		// (We may need to compute sizes each frame, and we don't want more lags)

		RelativeLayout.LayoutParams resultLP = recycledLP;
		if (resultLP == null)
			resultLP = new RelativeLayout.LayoutParams(Math.round(scaledAnnotationRect.width()), Math.round(scaledAnnotationRect.height()));

		// Compute sizes

		resultLP.width = Math.round(scaledAnnotationRect.width());
		resultLP.height = Math.round(scaledAnnotationRect.height());
		resultLP.setMargins(Math.round(scaledAnnotationRect.left), Math.round(scaledAnnotationRect.top), 0, 0);
		resultLP.alignWithParent = true;

		return resultLP;
	}

	public void deselectAnnotation(boolean informChild) {
		if (informChild && (mSelectedAnnotation != null))
			mSelectedAnnotation.deselect();

		mSelectedAnnotation = null;
	}

	private void selectAnnotation(AnnotationView annotation, boolean informChild) {
		mSelectedAnnotation = annotation;

		if (informChild && (mSelectedAnnotation != null))
			mSelectedAnnotation.select();
	}

	// <editor-fold desc="AnnotationViewListener">

	@Override
	public void onAnnotationSelected(AnnotationView annotationView) {
		deselectAnnotation(true);
		selectAnnotation(annotationView, false);
	}

	@Override
	public void onAnnotationEdited(AnnotationView annotationView) {
		mListener.onUpdateAnnotation(annotationView.getAnnotation());
	}

	@Override
	public void onAnnotationDeleted(AnnotationView annotationView) {
		deselectAnnotation(false);
		mListener.onDeleteAnnotation(annotationView.getAnnotation());
		removeView(annotationView);
	}

	@Override
	public void onAnnotationSizeChanged(AnnotationView annotationView, RectF currentLayoutSize) {
		RectF scaledCoordinates = annotationLayoutCoordinatesToPdfCoordinates(currentLayoutSize);
		annotationView.getAnnotation().setRect(scaledCoordinates.left, scaledCoordinates.top, scaledCoordinates.right, scaledCoordinates.bottom);
	}

	// </editor-fold desc="AnnotationViewListener">

	public interface AnnotationsLayoutListener {

		void onCreateAnnotation(Annotation annotation);

		void onUpdateAnnotation(Annotation annotation);

		void onDeleteAnnotation(Annotation annotation);
	}

}
