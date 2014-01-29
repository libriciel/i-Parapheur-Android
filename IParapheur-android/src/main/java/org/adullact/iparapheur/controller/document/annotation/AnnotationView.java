package org.adullact.iparapheur.controller.document.annotation;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import org.adullact.iparapheur.controller.document.PDFPageView;
import org.adullact.iparapheur.model.Annotation;

import java.util.List;

/**
* Created by jmaire on 13/01/2014.
*/
public class AnnotationView extends View {

    private PDFPageView pdfPageView;
    private List<Annotation> annotations;
    private Context context;

    public AnnotationView(PDFPageView pdfPageView, Context context, List<Annotation> annotations, View listener) {
        super(context);
        this.pdfPageView = pdfPageView;
        this.annotations = annotations;
        this.setOnTouchListener((OnTouchListener) listener);
        this.context = context;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!pdfPageView.isBlank() && (annotations != null)) {
            for (Annotation annotation : annotations) {
                if (annotation.getScale() != pdfPageView.getScale()) {
                    annotation.setScale(pdfPageView.getScale());
                }
                annotation.draw(canvas);
            }
        }
    }
}
