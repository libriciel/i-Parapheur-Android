package org.adullact.iparapheur.controller.document.annotation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.adullact.iparapheur.model.Annotation;

/**
 * Created by jmaire on 31/01/2014.
 */
public class AnnotationView extends View implements View.OnTouchListener, TextWatcher {

    public interface AnnotationViewListener {
        void onAnnotationSelected(AnnotationView annotationView);
        void onAnnotationDeleted(AnnotationView annotationView);
    }

    public static final int BORDER_WIDTH = 4;
    public static final int CORNER_RADIUS = 10;
    public static final int MIN_WIDTH = 160;
    public static final int MIN_HEIGHT = 120;

    public static final int LINE_WIDTH = 3;
    // Button's drawings line size
    public static final int LINE_HALF_LENGTH = 6;

    // Space between two buttons from center to center
    public static final int BUTTONS_SPACE = 42;

    public static final int COLOR_SELECTED = Color.RED;
    public static final int COLOR_BORDER = Color.BLACK;

    public static final int BUTTON_RADIUS = 14;
    public static final int BUTTON_COLOR = Color.BLACK;
    public static final int BUTTON_BORDER_COLOR = Color.WHITE;

    public static final int POSTIT_WIDTH = 120;
    public static final int POSTIT_HEIGHT = 80;
    public static final int POSTIT_COLOR = Color.YELLOW;

    public static final int TEXT_COLOR = Color.BLACK;
    public static final int TEXT_SIZE = 12;
    public static final int TEXT_PADDING = 5;

    public static final int INFOS_WIDTH = 150;
    public static final int INFOS_HEIGHT = 60;
    public static final int INFOS_COLOR = Color.CYAN;
    public static final int INFOS_ALPHA = 230; //90%
    public static final int INFOS_BORDER_WIDTH = 3;
    public static final int INFOS_BORDER_COLOR = Color.BLACK;

    public static final int ALPHA_SELECTED = 51; //20%
    public static final int ALPHA_BORDER = 179; //70%

    private enum Button {
        DELETE,
        MINIMIZE,
        RESIZE,
        INFO,
        EDIT
    }

    public enum Area {
        NONE,
        CENTER,
        RESIZE,
        DELETE,
        EDIT,
        MINIMIZE,
        INFO
    }

    private enum Mode {
        NONE,
        MOVE,
        RESIZE
    }

    private Annotation annotation;
    private EditText postItEditText;
    private TextView infosTextView;
    private boolean selected = false;
    private boolean minimized = false;
    private GestureDetector gestureDetector;
    private Mode mode;
    private AnnotationViewListener listener;


    public AnnotationView(Context context, Annotation annotation, AnnotationViewListener listener) {
        super(context);
        this.listener = listener;
        this.annotation = annotation;
        gestureDetector = new GestureDetector(context, new AnnotationViewGestureListener());
        setOnTouchListener(this);
        mode = mode.NONE;
    }

    public AnnotationView(Context context, AttributeSet attrs, Annotation annotation) {
        super(context, attrs);
        this.annotation = annotation;
    }

    public AnnotationView(Context context, AttributeSet attrs, int defStyleAttr, Annotation annotation) {
        super(context, attrs, defStyleAttr);
        this.annotation = annotation;
    }

    public void select() {
        this.selected = true;
        this.listener.onAnnotationSelected(this);
    }

    public void unselect() {
        this.selected = false;
        if (this.postItEditText != null) {
            this.postItEditText.setVisibility(GONE);
        }
        if (this.infosTextView != null) {
            this.infosTextView.setVisibility(GONE);
        }
        invalidate();
    }

    public void delete() {
        this.annotation.delete();
        this.selected = false;
        if (infosTextView != null) {
            ((ViewGroup) getParent()).removeView(infosTextView);
            infosTextView = null;
        }
        if (postItEditText != null) {
            ((ViewGroup) getParent()).removeView(postItEditText);
            postItEditText = null;
        }
        this.setVisibility(GONE);
    }

    public void toggleSize() {
        this.minimized = !minimized;
    }

    public void toggleText() {
        if (postItEditText == null) {
            createEditText();
        }
        if (postItEditText.getVisibility() == GONE) {
            postItEditText.setVisibility(VISIBLE);
        }
        else {
            postItEditText.setVisibility(GONE);
        }
    }

    private void createEditText() {
        postItEditText = new EditText(getContext());
        postItEditText.setCursorVisible(true);
        postItEditText.setTextSize(TEXT_SIZE);
        postItEditText.setTextColor(TEXT_COLOR);
        postItEditText.setBackgroundColor(POSTIT_COLOR);
        postItEditText.setClickable(true);
        postItEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        postItEditText.setText(annotation.getText(), TextView.BufferType.EDITABLE);
        postItEditText.addTextChangedListener(this);
        placeTextView();
        postItEditText.setVisibility(GONE);
        ((ViewGroup) getParent()).addView(postItEditText);
    }

    public void toggleInfos() {
        if (infosTextView == null) {
            createInfos();
        }
        if (infosTextView.getVisibility() == GONE) {
            infosTextView.setVisibility(VISIBLE);
        }
        else {
            infosTextView.setVisibility(GONE);
        }
    }

    private void createInfos() {
        infosTextView = new TextView(getContext());

        infosTextView.setTextSize(TEXT_SIZE);
        infosTextView.setTextColor(TEXT_COLOR);
        infosTextView.setBackgroundColor(INFOS_COLOR);
        infosTextView.setClickable(false);
        //infosTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        infosTextView.setText(annotation.getText(), TextView.BufferType.NORMAL);
        placeInfosView();
        infosTextView.setVisibility(GONE);
        ((ViewGroup) getParent()).addView(infosTextView);
    }

    private void placeTextView() {
        if ((postItEditText != null) && (postItEditText.getVisibility() != GONE)) {
            int parentWidth = ((AnnotationsLayout) getParent()).getWidth();
            int parentHeight = ((AnnotationsLayout) getParent()).getHeight();
            RelativeLayout.LayoutParams annotationLP = (RelativeLayout.LayoutParams) getLayoutParams();
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(POSTIT_WIDTH, POSTIT_HEIGHT);
            int left = (annotationLP.leftMargin + annotationLP.width);
            int top = annotationLP.topMargin;
            int maxLeft = (parentWidth - POSTIT_WIDTH);
            if (left > maxLeft) {
                left = maxLeft;
                top = annotationLP.topMargin + annotationLP.height;
                if (top > parentHeight - POSTIT_HEIGHT) {
                    top = (annotationLP.topMargin - POSTIT_HEIGHT);
                    if (top < 0) {
                        top = annotationLP.topMargin + BUTTON_RADIUS * 2;
                    }
                }
            }
            lp.leftMargin = left;
            lp.topMargin = top;
            postItEditText.setLayoutParams(lp);
        }
    }

    private void placeInfosView() {
        if ((infosTextView != null) && (infosTextView.getVisibility() != GONE)) {
            int parentHeight = ((AnnotationsLayout) getParent()).getHeight();
            RelativeLayout.LayoutParams annotationLP = (RelativeLayout.LayoutParams) getLayoutParams();
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(INFOS_WIDTH, INFOS_HEIGHT);
            int left = annotationLP.leftMargin;
            int top = annotationLP.topMargin + annotationLP.height;
            if (top > (parentHeight - INFOS_HEIGHT)) {
                top = annotationLP.topMargin - INFOS_HEIGHT;
                if (top < 0) {
                    top = (parentHeight - INFOS_HEIGHT);
                    left += BUTTON_RADIUS * 2;
                }
            }
            lp.leftMargin = left;
            lp.topMargin = top;
            infosTextView.setLayoutParams(lp);
        }
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public PointF moveTo(float x, float y, PointF offset) {
        int parentWidth = ((AnnotationsLayout) getParent()).getWidth();
        int parentHeight = ((AnnotationsLayout) getParent()).getHeight();
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
        PointF newOffset = new PointF(
                Math.max(0, Math.min(parentWidth - lp.width, x + offset.x)),
                Math.max(0, Math.min(parentHeight - lp.height, y + offset.y)));

        if (lp != null) {
            lp.leftMargin = (int) newOffset.x;
            lp.topMargin = (int) newOffset.y;
        }
        setLayoutParams(lp);
        return newOffset;
    }

    public void resize(float x, float y) {
        int parentWidth = ((AnnotationsLayout) getParent()).getWidth();
        int parentHeight = ((AnnotationsLayout) getParent()).getHeight();
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
        if (lp == null) return;
        int newWidth = (int) (x + BUTTON_RADIUS * 2);
        int newHeight = (int) (y + BUTTON_RADIUS * 2);
        boolean changed = false;
        if ((newWidth > MIN_WIDTH) && ((newWidth + lp.leftMargin) < parentWidth)) {
            lp.width = newWidth;
            changed = true;
        }
        if ((newHeight > MIN_HEIGHT) && ((newHeight + lp.topMargin) < parentHeight)) {
            lp.height = newHeight;
            changed = true;
        }
        if (changed) {
            setLayoutParams(lp);
        }
    }

    // Methods and utilities for drawing

    private float distance(float x, float y, PointF point2) {
        float dx = x - point2.x;
        float dy = y - point2.y;
        return (float)Math.sqrt((dx * dx) + (dy * dy));
    }

    public Area getArea(float x, float y) {
        Area area = Area.NONE;
        if (!annotation.isDeleted()) {
            area = Area.CENTER;
            for (Button button : Button.values()) {
                if (distance(x, y, getButtonCenter(button)) < BUTTON_RADIUS) {
                    area = Area.valueOf(button.name());
                    break;
                }
            }
        }
        return area;
    }

    private PointF getButtonCenter(Button button) {
        PointF center = new PointF();
        switch (button) {
            case DELETE :
                center.set(BUTTON_RADIUS, BUTTON_RADIUS);
                break;
            case EDIT :
                center.set(getWidth() - BUTTON_RADIUS, BUTTON_RADIUS);
                break;
            case INFO :
                center.set(BUTTON_RADIUS, getHeight() - BUTTON_RADIUS);
                break;
            case MINIMIZE :
                center.set(BUTTON_RADIUS + BUTTONS_SPACE, BUTTON_RADIUS);
                break;
            case RESIZE :
                center.set(getWidth() - (BUTTON_RADIUS * 2), getHeight() - (BUTTON_RADIUS * 2));
                break;
            default :
                center.set(BUTTON_RADIUS, BUTTON_RADIUS);
                break;
        }
        return center;
    }

    private RectF getInfosRect() {
        return new RectF(annotation.getRect().left, annotation.getRect().bottom + 5, annotation.getRect().left + INFOS_WIDTH, annotation.getRect().bottom + INFOS_HEIGHT);
    }

    public Rect getTextRect() {
        return new Rect((int)annotation.getRect().left, (int)annotation.getRect().bottom + 10, (int)annotation.getRect().left + POSTIT_WIDTH, (int)annotation.getRect().bottom + POSTIT_HEIGHT);
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
        placeTextView();
        placeInfosView();
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)params;
        this.annotation.setRect(lp.leftMargin,
                lp.topMargin,
                lp.leftMargin + lp.width,
                lp.topMargin + lp.height);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredWidth = (int) annotation.getRect().width();
        int desiredHeight = (int) annotation.getRect().height();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d("debug", "ONDRAW");
        if (!annotation.isDeleted()) {
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            if (minimized) {
                drawButton(canvas, paint, Button.DELETE);
                drawButton(canvas, paint, Button.MINIMIZE);
            }
            else {
                drawRect(canvas, paint);
                if (selected) {
                    drawButton(canvas, paint, Button.DELETE);
                    drawButton(canvas, paint, Button.MINIMIZE);
                    drawButton(canvas, paint, Button.INFO);
                    drawButton(canvas, paint, Button.EDIT);
                    drawButton(canvas, paint, Button.RESIZE);
                }
            }
        }
        super.onDraw(canvas);
    }

    protected void drawRect(Canvas canvas, Paint paint) {
        paint.setStrokeWidth(BORDER_WIDTH);
        if (selected) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(COLOR_SELECTED);
            paint.setAlpha(ALPHA_SELECTED);
        }
        else {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(COLOR_BORDER);
            paint.setAlpha(ALPHA_BORDER);
        }
        ViewGroup.LayoutParams lp = getLayoutParams();
        RectF rect = new RectF(0f, 0f, lp.width, lp.height);
        rect.inset(BUTTON_RADIUS - BORDER_WIDTH / 2, BUTTON_RADIUS - BORDER_WIDTH / 2);
        canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, paint);
    }

    private void drawButton(Canvas canvas, Paint paint, Button type) {
        PointF center = getButtonCenter(type);
        if (!type.equals(Button.RESIZE)) {
            // Inner black circle with shadow
            paint.setColor(BUTTON_COLOR);
            paint.setStyle(Paint.Style.FILL);
            //paint.setShadowLayer(8f, 0.5f, 0.5f, BUTTON_COLOR);
            canvas.drawCircle(center.x, center.y, BUTTON_RADIUS, paint);
            //paint.clearShadowLayer();
            // white border
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(LINE_WIDTH);
            paint.setColor(BUTTON_BORDER_COLOR);
            canvas.drawCircle(center.x, center.y, BUTTON_RADIUS, paint);
        }

        paint.setStrokeCap(Paint.Cap.ROUND);
        switch (type) {
            case DELETE :
                // Cross
                canvas.drawLine(center.x - LINE_HALF_LENGTH, center.y + LINE_HALF_LENGTH,
                        center.x + LINE_HALF_LENGTH, center.y - LINE_HALF_LENGTH, paint);
                canvas.drawLine(center.x - LINE_HALF_LENGTH, center.y - LINE_HALF_LENGTH,
                        center.x + LINE_HALF_LENGTH, center.y + LINE_HALF_LENGTH, paint);

                break;

            case MINIMIZE :
                // Minus
                canvas.drawLine(center.x - LINE_HALF_LENGTH, center.y,
                        center.x + LINE_HALF_LENGTH, center.y, paint);
                if (minimized) { // Plus
                    canvas.drawLine(center.x, center.y - LINE_HALF_LENGTH,
                            center.x, center.y + LINE_HALF_LENGTH, paint);
                }
                break;

            case RESIZE :
                paint.setColor(Color.BLACK);
                // Two oblic straits
                canvas.drawLine(center.x - LINE_HALF_LENGTH, center.y + LINE_HALF_LENGTH,
                        center.x + LINE_HALF_LENGTH, center.y - LINE_HALF_LENGTH, paint);
                canvas.drawLine(center.x + (LINE_HALF_LENGTH / 2), center.y + LINE_HALF_LENGTH,
                        center.x + LINE_HALF_LENGTH, center.y + (LINE_HALF_LENGTH / 2), paint);
                // TODO : Path for arrow
                break;

            case INFO :
                canvas.drawLine(center.x, center.y - LINE_HALF_LENGTH + 2,
                        center.x, center.y + LINE_HALF_LENGTH, paint);
                canvas.drawPoint(center.x, center.y - LINE_HALF_LENGTH + 1, paint);
                break;

            case EDIT :
                // Three horizontal straits
                canvas.drawLine(center.x - LINE_HALF_LENGTH, center.y - LINE_HALF_LENGTH,
                        center.x + LINE_HALF_LENGTH, center.y - LINE_HALF_LENGTH, paint);
                canvas.drawLine(center.x - LINE_HALF_LENGTH, center.y,
                        center.x + LINE_HALF_LENGTH, center.y, paint);
                canvas.drawLine(center.x - LINE_HALF_LENGTH, center.y + LINE_HALF_LENGTH,
                        center.x + LINE_HALF_LENGTH, center.y + LINE_HALF_LENGTH, paint);
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            invalidate();
            return true;
        }
        return false;
    }

    private class AnnotationViewGestureListener extends GestureDetector.SimpleOnGestureListener {

        private PointF offset = new PointF(); // offset from the top left corner when an annotation is selected

        @Override
        public boolean onDown(MotionEvent me) {
            Log.d("debug", "onDown");
            switch (getArea(me.getX(), me.getY())) {
                case CENTER :
                    mode = Mode.MOVE;
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
                    offset.set(lp.leftMargin, lp.topMargin);
                    break;
                case RESIZE :
                    mode = Mode.RESIZE;
                    break;
            }
            // Pour empêcher de tourner les pages.
            getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent me) {
            Log.i("IParapheurPDFPageView", "onSingleTapConfirmed");
            boolean invalidate = false;
            // Nouvelle selection ou déselection
            if (selected) {
                switch (getArea(me.getX(), me.getY())) {
                    case EDIT :
                        toggleText();
                        break;
                    case MINIMIZE :
                        toggleSize();
                        break;
                    case INFO :
                        toggleInfos();
                        break;
                    case DELETE :
                        // TODO : possibilité d'annuler la suppression?
                        delete();
                        break;
                }
            }
            else {
                select();
            }
            // Invalidate here because this function is not called on au touch event.
            invalidate();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent start, MotionEvent current, float f, float f1) {
            if (selected) {
                switch (mode) {
                    case MOVE :
                        offset = moveTo(current.getX() - start.getX(), current.getY() - start.getY(), offset);
                        break;
                    case RESIZE :
                        Log.d("move", "" + current.getX() + " ; " + current.getY());
                        resize(current.getX(), current.getY());
                        break;
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        annotation.setText(s.toString());
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
}
