package org.adullact.iparapheur.controller.document;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.adullact.iparapheur.model.PageAnnotations;

/**
 * Created by jmaire on 29/01/2014.
 */
public class DocumentPageFragment extends Fragment  {

    private int numPage;
    private PageLayout pageLayout;

    public DocumentPageFragment() {}

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
        RelativeLayout rl = new RelativeLayout(getActivity());
        rl.setLayoutParams(lp);

        // PageLayout containing imageView for the page and the annotationsLayout
        pageLayout = new PageLayout(getActivity(), numPage);

        rl.addView(pageLayout);
        return rl;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void updatePage(String dossierId, Bitmap pageImage, PageAnnotations annotations) {
        pageLayout.update(pageImage, annotations);
    }
}
