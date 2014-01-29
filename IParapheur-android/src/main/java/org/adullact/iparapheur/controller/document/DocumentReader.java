package org.adullact.iparapheur.controller.document;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import com.artifex.mupdfdemo.ReaderView;

import org.adullact.iparapheur.model.Document;

/**
 * Created by jmaire on 05/11/2013.
 */
public class DocumentReader extends ReaderView {

    public interface OnReaderStateChangeListener {
        void onPageChange(int numPage);
    }

    private int tapPageMargin;
    private static final int MIN_TAP_MARGIN = 50;
    private final OnReaderStateChangeListener listener;

    public DocumentReader(Context context, OnReaderStateChangeListener listener) {
        super(context);
        calculateTapPageMargin();
        this.listener = listener;
    }

    /**
     * Calcule la marge servant à tourner les pages (singleTapUp).
     * La marge est comprise entre 50 pixels et 1/7 de l'écran.
     */
    private void calculateTapPageMargin() {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
        tapPageMargin = (int)dm.xdpi;
        if (tapPageMargin < MIN_TAP_MARGIN)
            tapPageMargin = MIN_TAP_MARGIN;
        if (tapPageMargin > dm.widthPixels/7)
            tapPageMargin = dm.widthPixels/7;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        onTouchEvent(ev);
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (e.getX() < tapPageMargin) {
            super.smartMoveBackwards();
        } else if (e.getX() > super.getWidth() - tapPageMargin) {
            super.smartMoveForwards();
        } else if (e.getY() < tapPageMargin) {
            super.smartMoveBackwards();
        } else if (e.getY() > super.getHeight() - tapPageMargin) {
            super.smartMoveForwards();
        }
        return super.onSingleTapUp(e);
    }

    @Override
    protected void onMoveToChild(int i) {
        listener.onPageChange(i);
        super.onMoveToChild(i);
    }

    @Override
    protected void onMoveOffChild(int i) {
        ((PDFPageAdapter) getAdapter()).clean();
        super.onMoveOffChild(i);
    }

    public void setDocument(Document document) throws Exception {
        if (document.getPath() != null) {
            setAdapter(new PDFPageAdapter(getContext(), document.getAnnotations(), document.getPath()));
            //refresh(true);
        }
    }

    public void clean() {
        if (getAdapter() != null) {
            ((PDFPageAdapter) getAdapter()).clean();
        }
    }

    public int getPagesCount() {
        return (getAdapter() != null)?
                getAdapter().getCount() :
                0;
    }
}