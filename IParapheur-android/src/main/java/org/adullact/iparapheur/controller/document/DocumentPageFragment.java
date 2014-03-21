package org.adullact.iparapheur.controller.document;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.adullact.iparapheur.controller.document.annotation.AnnotationsLayout;
import org.adullact.iparapheur.model.PageAnnotations;

/**
 * Created by jmaire on 29/01/2014.
 */
public class DocumentPageFragment extends Fragment {

    private int numPage;
    private ImageView imageView;
    private AnnotationsLayout annotationsView;
    private FrameLayout layout;

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
        layout = new FrameLayout(getActivity());
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void updatePage(Bitmap pageImage, PageAnnotations annotations, float scale) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(pageImage.getWidth(), pageImage.getHeight());
        ViewGroup.LayoutParams groupParams = new ViewGroup.LayoutParams(layoutParams);

        float x = this.layout.getX() + (this.layout.getWidth() - pageImage.getWidth()) / 2;
        float y = this.layout.getY() + (this.layout.getHeight() - pageImage.getHeight()) / 2;

        this.annotationsView = new AnnotationsLayout(getActivity(), numPage, annotations, scale);
        this.annotationsView.setLayoutParams(groupParams);
        this.annotationsView.setX(x);
        this.annotationsView.setY(y);

        this.imageView = new ImageView(getActivity());
        this.imageView.setLayoutParams(layoutParams);
        this.imageView.setImageBitmap(pageImage);
        this.imageView.setX(x);
        this.imageView.setY(y);

        this.layout.addView(this.imageView);
        this.layout.addView(this.annotationsView);
        this.layout.requestLayout();
        this.layout.invalidate();
    }
}
