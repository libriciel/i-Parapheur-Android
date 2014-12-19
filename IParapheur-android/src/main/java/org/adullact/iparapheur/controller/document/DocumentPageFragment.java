package org.adullact.iparapheur.controller.document;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

    private int numPage;
    private PageLayout pageLayout;
    private ScaleType mCurrentScaleType = ScaleType.fitHeight;

    public DocumentPageFragment() {}

    //<editor-fold desc="LifeCycle">

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getArguments() != null) && getArguments().containsKey("NUM_PAGE")) {
            this.numPage = getArguments().getInt("NUM_PAGE");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Top layout : Relative layout.
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ScrollView scrollView= new ScrollView(getActivity());
        scrollView.setLayoutParams(lp);

        // PageLayout containing imageView for the page and the annotationsLayout
        pageLayout = new PageLayout(getActivity(), numPage);
        pageLayout.setPageLayoutListener(this);

        scrollView.addView(pageLayout);
        return scrollView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();

        pageLayout.setPageLayoutListener(null);
    }

    //</editor-fold desc="LifeCycle">

    public void updatePage(String dossierId, Bitmap pageImage, PageAnnotations annotations) {
        pageLayout.update(pageImage, annotations);
    }

    private enum ScaleType {
        fitHeight, fitWidth; // TODO : zoom
    }

    private void scale(ScaleType scaleType) {
        pageLayout.setBackgroundColor(Color.RED);
        switch (scaleType)
        {
            case fitHeight:

                ScrollView.LayoutParams fitHeightLP = new ScrollView.LayoutParams(pageLayout.getInitialWidth(), pageLayout.getInitialHeight());
                fitHeightLP.gravity = Gravity.CENTER_HORIZONTAL;

                pageLayout.setLayoutParams(fitHeightLP);
                pageLayout.setTranslationY(0);

                break;

            case fitWidth:

                float parentWidth = ((View) pageLayout.getParent()).getWidth();
                float initialWidth =pageLayout.getInitialWidth();
                float ratio = parentWidth / initialWidth;
                int scaledWidth = Math.round(pageLayout.getInitialWidth() * ratio);
                int scaledHeight = Math.round(pageLayout.getInitialHeight() * ratio);

                ScrollView.LayoutParams fitWidthLP = new ScrollView.LayoutParams(scaledWidth, scaledHeight);
                fitWidthLP.gravity = Gravity.CENTER_HORIZONTAL;
                pageLayout.setLayoutParams(fitWidthLP);

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
