package org.adullact.iparapheur.controller.document;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Annotation;
import org.adullact.iparapheur.model.PageAnnotations;
import org.adullact.iparapheur.utils.IParapheurException;


public class DocumentPageFragment extends Fragment implements PageLayout.PageLayoutListener {

	public static final String ARGUMENT_PAGE_NUMBER = "page_number";

	private int mNumPage;
	private PageLayout mPageLayout;
	private ScaleType mCurrentScaleType = ScaleType.fitHeight;
	private String mCurrentDossierId;
	private String mCurrentDocumentId;

	public DocumentPageFragment() {}

	//<editor-fold desc="LifeCycle">

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if ((getArguments() != null) && getArguments().containsKey(ARGUMENT_PAGE_NUMBER))
			mNumPage = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// Top layout : Relative layout.
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		ScrollView scrollView = new ScrollView(getActivity());
		scrollView.setLayoutParams(lp);

		// PageLayout containing imageView for the page and the annotationsLayout
		mPageLayout = new PageLayout(getActivity(), mNumPage);
		mPageLayout.setPageLayoutListener(this);

		scrollView.addView(mPageLayout);
		return scrollView;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override public void onStop() {
		super.onStop();

		mPageLayout.setPageLayoutListener(null);
	}

	//</editor-fold desc="LifeCycle">

	public void updatePage(@NonNull String dossierId, @NonNull String documentId, @NonNull Bitmap pageImage, @NonNull PageAnnotations annotations, @NonNull Point initSize, @NonNull Point pdfSize) {
		mCurrentDossierId = dossierId;
		mCurrentDocumentId = documentId;
		mPageLayout.update(pageImage, annotations, initSize, pdfSize);
	}

	private void scale(@NonNull ScaleType scaleType) {
		mPageLayout.setBackgroundColor(Color.RED);

		switch (scaleType) {

			case fitHeight:

				ScrollView.LayoutParams fitHeightLP = new ScrollView.LayoutParams(mPageLayout.getInitialWidth(), mPageLayout.getInitialHeight());
				fitHeightLP.gravity = Gravity.CENTER_HORIZONTAL;

				mPageLayout.setLayoutParams(fitHeightLP);
				mPageLayout.setTranslationY(0);
				mPageLayout.refreshDisplayedAnnotations(new Point(mPageLayout.getInitialWidth(), mPageLayout.getInitialHeight()));

				break;

			case fitWidth:

				float parentWidth = ((View) mPageLayout.getParent()).getWidth();
				float initialWidth = mPageLayout.getInitialWidth();
				float ratio = parentWidth / initialWidth;
				int scaledWidth = Math.round(mPageLayout.getInitialWidth() * ratio);
				int scaledHeight = Math.round(mPageLayout.getInitialHeight() * ratio);

				ScrollView.LayoutParams fitWidthLP = new ScrollView.LayoutParams(scaledWidth, scaledHeight);
				fitWidthLP.gravity = Gravity.CENTER_HORIZONTAL;
				mPageLayout.setLayoutParams(fitWidthLP);
				mPageLayout.refreshDisplayedAnnotations(new Point(scaledWidth, scaledHeight));

				break;
		}
	}

	//<editor-fold desc="PageLayoutListener">

	@Override public void onDoubleTap(@NonNull MotionEvent me) {

		// Loop around mCurrentScaleType to the next available.
		int nextScaleTypeOrdinal = (mCurrentScaleType.ordinal() + 1) % ScaleType.values().length;
		mCurrentScaleType = ScaleType.values()[nextScaleTypeOrdinal];

		scale(mCurrentScaleType);
	}

	@Override public void onCreateAnnotation(@NonNull final Annotation annotation) {

		new AsyncTask<Void, Void, Void>() {

			private IParapheurException mException;
			private String mUuidResponse;

			@Override protected Void doInBackground(Void... params) {
				try {
					mUuidResponse = RESTClient.INSTANCE.createAnnotation(mCurrentDossierId, mCurrentDocumentId, annotation, mNumPage);
				}
				catch (IParapheurException e) {
					mException = e;
					e.printStackTrace();
				}

				return null;
			}

			@Override protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);

				if (mException != null)
					Toast.makeText(getActivity(), mException.getResId(), Toast.LENGTH_LONG).show();
				else
					annotation.setUuid(mUuidResponse);
			}

		}.execute(null, null, null);
	}

	@Override public void onUpdateAnnotation(@NonNull final Annotation annotation) {

		new AsyncTask<Void, Void, Void>() {

			private IParapheurException mException;

			@Override protected Void doInBackground(Void... params) {
				try {
					RESTClient.INSTANCE.updateAnnotation(mCurrentDossierId, mCurrentDocumentId, annotation, mNumPage);
				}
				catch (IParapheurException e) {
					mException = e;
					e.printStackTrace();
				}

				return null;
			}

			@Override protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);

				if (mException != null)
					Toast.makeText(getActivity(), mException.getResId(), Toast.LENGTH_LONG).show();
			}

		}.execute(null, null, null);
	}

	@Override public void onDeleteAnnotation(@NonNull final Annotation annotation) {

		new AsyncTask<Void, Void, Void>() {

			private IParapheurException mException;

			@Override protected Void doInBackground(Void... params) {
				try {
					RESTClient.INSTANCE.deleteAnnotation(mCurrentDossierId, mCurrentDocumentId, annotation.getUuid(), mNumPage);
				}
				catch (IParapheurException e) {
					mException = e;
					e.printStackTrace();
				}

				return null;
			}

			@Override protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);

				if (mException != null)
					Toast.makeText(getActivity(), mException.getResId(), Toast.LENGTH_LONG).show();
			}

		}.execute(null, null, null);
	}

	//</editor-fold desc="PageLayoutListener">

	private enum ScaleType {
		fitHeight, fitWidth // TODO : zoom
	}
}
