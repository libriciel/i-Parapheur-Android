package org.adullact.iparapheur.controller.document;

import android.content.Context;
import android.view.MotionEvent;

import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.ReaderView;

import org.adullact.iparapheur.model.Document;

/**
 * Created by jmaire on 05/11/2013.
 */
public class DocumentReader extends ReaderView {

    private MuPDFCore core;

    public DocumentReader(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        onTouchEvent(ev);
        return false;
    }

    public void setDocument(Document document) {
        if (document.getPath() != null) {
            try
            {
                core = new MuPDFCore(getContext(), document.getPath());
                setAdapter(new PDFPageAdapter(getContext(), document.getAnnotations(), core));
            }
            catch (Exception e) {
                // TODO : handle errors
            }
        }
    }

    // TODO : links on onSingleTapUp()
}