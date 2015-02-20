package org.adullact.iparapheur.controller.document.annotation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.adullact.iparapheur.model.Annotation;

public class AnnotationView extends View implements View.OnTouchListener, TextWatcher {

	public static final int BORDER_WIDTH = 4;
	public static final int CORNER_RADIUS = 10;
	public static final int MIN_WIDTH = 160;
	public static final int MIN_HEIGHT = 120;
	public static final int LINE_WIDTH = 3;
	public static final int LINE_HALF_LENGTH = 7; // Button's drawings line size
	public static final int BUTTONS_SPACE = 42; // Space between two buttons from center to center
	public static final int COLOR_SELECTED = Color.parseColor("#ffff4444");
	public static final int COLOR_BORDER = Color.BLACK;
	public static final int BUTTON_RADIUS = 16;
	public static final int BUTTON_TOUCH_RADIUS = 19;
	public static final int BUTTON_COLOR = Color.BLACK;
	public static final int BUTTON_BORDER_COLOR = Color.WHITE;
	public static final int POST_IT_WIDTH = 220;
	public static final int POST_IT_HEIGHT = 180;
	public static final int POST_IT_COLOR = Color.parseColor("#ffffbb33");
	public static final int TEXT_COLOR = Color.BLACK;
	public static final int TEXT_SIZE = 12;
	public static final int INFO_WIDTH = 180;
	public static final int INFO_HEIGHT = 120;
	public static final int INFO_COLOR = Color.parseColor("#ff33b5e5");
	public static final int ALPHA_SELECTED = 51; //20%
	public static final int ALPHA_BORDER = 179; //70%

	private Paint paint = new Paint();
	private Annotation annotation;
	private EditText mPostItEditText;
	private TextView infoTextView;
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
		mode = AnnotationView.Mode.NONE;
	}

	private void selectInternal() {
		this.selected = true;
		this.listener.onAnnotationSelected(this);
	}

	public void select() {
		this.selected = true;
		invalidate();
	}

	public void deselect() {
		this.selected = false;
		if (this.mPostItEditText != null) {
			this.mPostItEditText.setVisibility(GONE);
		}
		if (this.infoTextView != null) {
			this.infoTextView.setVisibility(GONE);
		}
		invalidate();
	}

	public void delete() {
		this.annotation.setDeleted(true);
		this.selected = false;
		if (infoTextView != null) {
			((ViewGroup) getParent()).removeView(infoTextView);
			infoTextView = null;
		}
		if (mPostItEditText != null) {
			((ViewGroup) getParent()).removeView(mPostItEditText);
			mPostItEditText = null;
		}
		this.setVisibility(GONE);
	}

	public void toggleSize() {
		this.minimized = !minimized;
	}

	public void toggleText() {
		if (mPostItEditText == null) {
			createEditText();
		}
		if (mPostItEditText.getVisibility() == GONE) {
			mPostItEditText.setVisibility(VISIBLE);
		}
		else {
			mPostItEditText.setVisibility(GONE);
		}
	}

	private void createEditText() {
		mPostItEditText = new EditText(getContext());
		mPostItEditText.setCursorVisible(true);
		mPostItEditText.setTextSize(TEXT_SIZE);
		mPostItEditText.setTextColor(TEXT_COLOR);
		mPostItEditText.setBackgroundColor(POST_IT_COLOR);
		mPostItEditText.setClickable(true);
		mPostItEditText.setHorizontallyScrolling(false);
		mPostItEditText.setTextSize(14);
		mPostItEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		mPostItEditText.setText(annotation.getText(), TextView.BufferType.EDITABLE);
		mPostItEditText.addTextChangedListener(this);
		placeTextView();
		mPostItEditText.setVisibility(GONE);
		((ViewGroup) getParent()).addView(mPostItEditText);
	}

	public void toggleInfo() {
		if (infoTextView == null) {
			createInfo();
		}
		if (infoTextView.getVisibility() == GONE) {
			infoTextView.setVisibility(VISIBLE);
		}
		else {
			infoTextView.setVisibility(GONE);
		}
	}

	private void createInfo() {
		infoTextView = new TextView(getContext());

		infoTextView.setTextSize(TEXT_SIZE);
		infoTextView.setTextColor(TEXT_COLOR);
		infoTextView.setBackgroundColor(INFO_COLOR);
		infoTextView.setClickable(false);
		infoTextView.setSingleLine(false);
		infoTextView.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		infoTextView.setTextSize(12);
		infoTextView.setHorizontallyScrolling(false);
		infoTextView.setText(Html.fromHtml(annotation.getAuthor() + "<br />" + annotation.getDate()), TextView.BufferType.NORMAL);
		placeInfosView();
		infoTextView.setVisibility(GONE);
		((ViewGroup) getParent()).addView(infoTextView);
	}

	private void placeTextView() {
		if ((mPostItEditText != null) && (mPostItEditText.getVisibility() != GONE)) {
			int parentHeight = ((AnnotationsLayout) getParent()).getHeight();
			RelativeLayout.LayoutParams annotationLP = (RelativeLayout.LayoutParams) getLayoutParams();
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(POST_IT_WIDTH, POST_IT_HEIGHT);
			int left = annotationLP.leftMargin + BUTTON_RADIUS;
			int top = annotationLP.topMargin + annotationLP.height;
			if (top > (parentHeight - POST_IT_HEIGHT)) {
				top = annotationLP.topMargin - POST_IT_HEIGHT;
				if (top < 0) {
					top = (parentHeight - POST_IT_HEIGHT);
					left += BUTTON_RADIUS;
				}
			}
			lp.leftMargin = left;
			lp.topMargin = top;
			mPostItEditText.setLayoutParams(lp);
		}
	}

	private void placeInfosView() {
		if ((infoTextView != null) && (infoTextView.getVisibility() != GONE)) {
			int parentWidth = ((AnnotationsLayout) getParent()).getWidth();
			int parentHeight = ((AnnotationsLayout) getParent()).getHeight();
			RelativeLayout.LayoutParams annotationLP = (RelativeLayout.LayoutParams) getLayoutParams();
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(INFO_WIDTH, INFO_HEIGHT);
			int left = (annotationLP.leftMargin + annotationLP.width);
			int top = annotationLP.topMargin + BUTTON_RADIUS;
			int maxLeft = (parentWidth - INFO_WIDTH);
			if (left > maxLeft) {
				left = maxLeft;
				top = annotationLP.topMargin + annotationLP.height;
				if (top > parentHeight - INFO_HEIGHT) {
					top = (annotationLP.topMargin - INFO_HEIGHT);
					if (top < 0) {
						top = annotationLP.topMargin;
					}
				}
			}
			lp.leftMargin = left;
			lp.topMargin = top;
			infoTextView.setLayoutParams(lp);
		}
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public PointF moveTo(float x, float y, PointF offset) {
		int parentWidth = ((AnnotationsLayout) getParent()).getWidth();
		int parentHeight = ((AnnotationsLayout) getParent()).getHeight();
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
		PointF newOffset = new PointF(0, 0);

		if (lp != null) {
			newOffset = new PointF(Math.max(0, Math.min(parentWidth - lp.width, x + offset.x)), Math.max(0, Math.min(parentHeight - lp.height, y + offset.y)));
			lp.leftMargin = (int) newOffset.x;
			lp.topMargin = (int) newOffset.y;
			setLayoutParams(lp);
		}

		return newOffset;
	}

	public void resize(float x, float y) {
		int parentWidth = ((AnnotationsLayout) getParent()).getWidth();
		int parentHeight = ((AnnotationsLayout) getParent()).getHeight();
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
		if (lp == null)
			return;
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

	private float distance(float x, float y, PointF point2) {
		float dx = x - point2.x;
		float dy = y - point2.y;
		return (float) Math.sqrt((dx * dx) + (dy * dy));
	}

	public Area getArea(float x, float y) {
		Area area = Area.NONE;
		if (!annotation.isDeleted()) {
			area = Area.CENTER;
			for (Button button : Button.values()) {
				if (distance(x, y, getButtonCenter(button)) < BUTTON_TOUCH_RADIUS) {
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
			case DELETE:
				center.set(BUTTON_RADIUS, BUTTON_RADIUS);
				break;
			case INFO:
				center.set(getWidth() - BUTTON_RADIUS, BUTTON_RADIUS);
				break;
			case EDIT:
				center.set(BUTTON_RADIUS, getHeight() - BUTTON_RADIUS);
				break;
			case MINIMIZE:
				center.set(BUTTON_RADIUS + BUTTONS_SPACE, BUTTON_RADIUS);
				break;
			case RESIZE:
				center.set(getWidth() - (BUTTON_RADIUS * 2), getHeight() - (BUTTON_RADIUS * 2));
				break;
			default:
				center.set(BUTTON_RADIUS, BUTTON_RADIUS);
				break;
		}
		return center;
	}

	// Methods and utilities for drawing

	@Override
	public void setLayoutParams(ViewGroup.LayoutParams params) {
		super.setLayoutParams(params);
		placeTextView();
		placeInfosView();

		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) params;
		if (listener != null)
			listener.onAnnotationSizeChanged(this, new RectF(lp.leftMargin, lp.topMargin, lp.leftMargin + lp.width, lp.topMargin + lp.height));
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
		}
		else if (widthMode == MeasureSpec.AT_MOST) {
			//Can't be bigger than
			width = Math.min(desiredWidth, widthSize);
		}
		else {
			//Be whatever you want
			width = desiredWidth;
		}

		//Measure Height
		if (heightMode == MeasureSpec.EXACTLY) {
			//Must be this size
			height = heightSize;
		}
		else if (heightMode == MeasureSpec.AT_MOST) {
			//Can't be bigger than...
			height = Math.min(desiredHeight, heightSize);
		}
		else {
			//Be whatever you want
			height = desiredHeight;
		}

		//MUST CALL THIS
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if (!annotation.isDeleted()) {
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
			case DELETE:
				// Cross
				canvas.drawLine(center.x - LINE_HALF_LENGTH, center.y + LINE_HALF_LENGTH, center.x + LINE_HALF_LENGTH, center.y - LINE_HALF_LENGTH, paint);
				canvas.drawLine(center.x - LINE_HALF_LENGTH, center.y - LINE_HALF_LENGTH, center.x + LINE_HALF_LENGTH, center.y + LINE_HALF_LENGTH, paint);

				break;

			case MINIMIZE:
				// Minus
				canvas.drawLine(center.x - LINE_HALF_LENGTH, center.y, center.x + LINE_HALF_LENGTH, center.y, paint);
				if (minimized) { // Plus
					canvas.drawLine(center.x, center.y - LINE_HALF_LENGTH, center.x, center.y + LINE_HALF_LENGTH, paint);
				}
				break;

			case RESIZE:
				paint.setColor(Color.BLACK);
				// Two oblic straits
				canvas.drawLine(center.x - LINE_HALF_LENGTH, center.y + LINE_HALF_LENGTH, center.x + LINE_HALF_LENGTH, center.y - LINE_HALF_LENGTH, paint);
				canvas.drawLine(center.x + (LINE_HALF_LENGTH / 2), center.y + LINE_HALF_LENGTH, center.x + LINE_HALF_LENGTH, center.y + (LINE_HALF_LENGTH / 2), paint);
				// TODO : Path for arrow
				break;

			case INFO:
				canvas.drawLine(center.x, center.y - LINE_HALF_LENGTH + 2, center.x, center.y + LINE_HALF_LENGTH, paint);
				canvas.drawPoint(center.x, center.y - LINE_HALF_LENGTH + 1, paint);
				break;

			case EDIT:
				// Three horizontal straits
				canvas.drawLine(center.x - LINE_HALF_LENGTH, center.y - LINE_HALF_LENGTH, center.x + LINE_HALF_LENGTH, center.y - LINE_HALF_LENGTH, paint);
				canvas.drawLine(center.x - LINE_HALF_LENGTH, center.y, center.x + LINE_HALF_LENGTH, center.y, paint);
				canvas.drawLine(center.x - LINE_HALF_LENGTH, center.y + LINE_HALF_LENGTH, center.x + LINE_HALF_LENGTH, center.y + LINE_HALF_LENGTH, paint);
				break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (gestureDetector.onTouchEvent(event)) {
			invalidate();
			return true;
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (selected) {
				if (annotation.isUpdated()) {
					listener.onAnnotationEdited(this);
					annotation.setUpdated(false);
				}
				else if (annotation.isDeleted()) {
					listener.onAnnotationDeleted(this);
				}
			}
		}
		return false;
	}

	@Override
	public void afterTextChanged(Editable s) {
		annotation.setText(s.toString());
		listener.onAnnotationEdited(this);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}

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

	public interface AnnotationViewListener {

		void onAnnotationSelected(@NonNull AnnotationView annotationView);

		void onAnnotationEdited(@NonNull AnnotationView annotationView);

		void onAnnotationDeleted(@NonNull AnnotationView annotationView);

		void onAnnotationSizeChanged(@NonNull AnnotationView annotationView, @NonNull RectF currentLayoutSize);
	}

	private class AnnotationViewGestureListener extends GestureDetector.SimpleOnGestureListener {

		private PointF offset = new PointF(); // offset from the top left corner when an annotation is selected

		@Override
		public boolean onDown(MotionEvent me) {
			//Log.d("debug", "onDown");
			switch (getArea(me.getX(), me.getY())) {
				case CENTER:
					if (!selected) {
						selectInternal();
					}
					mode = Mode.MOVE;
					RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
					offset.set(lp.leftMargin, lp.topMargin);
					break;
				case RESIZE:
					mode = Mode.RESIZE;
					break;
			}
			// Pour empêcher de tourner les pages.
			getParent().requestDisallowInterceptTouchEvent(true);
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent me) {
			//Log.i("IParapheurPDFPageView", "onSingleTapConfirmed");
			// Nouvelle selection ou déselection
			if (selected) {
				switch (getArea(me.getX(), me.getY())) {
					case EDIT:
						toggleText();
						break;
					case MINIMIZE:
						toggleSize();
						break;
					case INFO:
						toggleInfo();
						break;
					case DELETE:
						// TODO : cancelling ?
						delete();
						break;
				}
			}
			else {
				selectInternal();
			}
			// Invalidate here because this function is not called on au touch event.
			invalidate();
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent start, MotionEvent current, float f, float f1) {
			if (selected) {
				switch (mode) {
					case MOVE:
						offset = moveTo(current.getX() - start.getX(), current.getY() - start.getY(), offset);
						break;
					case RESIZE:
						//Log.d("move", "" + current.getX() + " ; " + current.getY());
						resize(current.getX(), current.getY());
						break;
				}
				return true;
			}
			return false;
		}
	}
}
