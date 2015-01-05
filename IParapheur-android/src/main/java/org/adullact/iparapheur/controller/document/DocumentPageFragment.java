package org.adullact.iparapheur.controller.document;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import org.adullact.iparapheur.model.PageAnnotations;

/**
 * Created by jmaire on 29/01/2014.
 */
public class DocumentPageFragment extends Fragment implements PageLayout.PageLayoutListener {

    private int mNumPage;
    private PageLayout mPageLayout;
    private ScaleType mCurrentScaleType = ScaleType.fitHeight;

    public DocumentPageFragment() {}

    //<editor-fold desc="LifeCycle">

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getArguments() != null) && getArguments().containsKey("NUM_PAGE")) {
            mNumPage = getArguments().getInt("NUM_PAGE");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Top layout : Relative layout.
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ScrollView scrollView= new ScrollView(getActivity());
        scrollView.setLayoutParams(lp);

        // PageLayout containing imageView for the page and the annotationsLayout
        mPageLayout = new PageLayout(getActivity(), mNumPage);
        mPageLayout.setPageLayoutListener(this);

        scrollView.addView(mPageLayout);
        return scrollView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();

        mPageLayout.setPageLayoutListener(null);
    }

    //</editor-fold desc="LifeCycle">

    public void updatePage(String dossierId, Bitmap pageImage, PageAnnotations annotations) {
        mPageLayout.update(pageImage, annotations);
    }

    private enum ScaleType {
        fitHeight, fitWidth // TODO : zoom
    }

    private void scale(ScaleType scaleType) {
        mPageLayout.setBackgroundColor(Color.RED);
        switch (scaleType)
        {
            case fitHeight:

                ScrollView.LayoutParams fitHeightLP = new ScrollView.LayoutParams(mPageLayout.getInitialWidth(), mPageLayout.getInitialHeight());
                fitHeightLP.gravity = Gravity.CENTER_HORIZONTAL;

                mPageLayout.setLayoutParams(fitHeightLP);
                mPageLayout.setTranslationY(0);

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

                break;
        }
    }

    //<editor-fold desc="PageLayoutListener">

    @Override
    public void onDoubleTap(MotionEvent me) {

        // Loop around mCurrentScaleType to the next available.
        int nextScaleTypeOrdinal = (mCurrentScaleType.ordinal() + 1) % ScaleType.values().length;
        mCurrentScaleType = ScaleType.values()[nextScaleTypeOrdinal];

        scale(mCurrentScaleType);
    }

    //</editor-fold desc="PageLayoutListener">

}
