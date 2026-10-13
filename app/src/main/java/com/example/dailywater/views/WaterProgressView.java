package com.example.dailywater.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.example.dailywater.R;

public class WaterProgressView extends View {

    private static final long ANIMATION_DURATION_MS = 700L;
    private static final int DEFAULT_PROGRESS_COLOR = 0xFF2196F3;
    private static final int DEFAULT_TEXT_COLOR = 0xFF212121;
    private static final int DEFAULT_ALERT_COLOR = 0xFFE53935;
    private static final int DEFAULT_MAX_VALUE = 2000;
    private static final int OUTLINE_COLOR = 0xFF546E7A;

    private final Paint remainingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint alertPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path bottlePath = new Path();
    private final RectF capRect = new RectF();

    private int progressColor = DEFAULT_PROGRESS_COLOR;
    private int textColor = DEFAULT_TEXT_COLOR;
    private int alertColor = DEFAULT_ALERT_COLOR;
    private int maxValue = DEFAULT_MAX_VALUE;
    private String title = "";
    private String alertLabel = "";

    private int consumed = 0;
    private float displayedPercent = 0f;
    private boolean absoluteMode = false;
    private ValueAnimator animator;

    private float centerX;
    private float titleY;
    private float titleSize;
    private float waterBottom;
    private float waterTopFull;
    private float valueY;
    private float alertY;
    private float bodyWidth;

    public WaterProgressView(Context context) {
        super(context);
        init(context, null);
    }

    public WaterProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WaterProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        title = context.getString(R.string.default_title);
        alertLabel = context.getString(R.string.goal_exceeded_alert);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WaterProgressView);
            try {
                progressColor = a.getColor(R.styleable.WaterProgressView_progressColor, progressColor);
                textColor = a.getColor(R.styleable.WaterProgressView_textColor, textColor);
                alertColor = a.getColor(R.styleable.WaterProgressView_alertColor, alertColor);
                maxValue = Math.max(1, a.getInt(R.styleable.WaterProgressView_maxValue, maxValue));
                String attrTitle = a.getString(R.styleable.WaterProgressView_titleText);
                if (attrTitle != null) {
                    title = attrTitle;
                }
            } finally {
                a.recycle();
            }
        }

        setClickable(true);

        remainingPaint.setStyle(Paint.Style.FILL);
        remainingPaint.setColor((progressColor & 0x00FFFFFF) | 0x33000000);

        progressPaint.setStyle(Paint.Style.FILL);

        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(dp(3));
        outlinePaint.setStrokeJoin(Paint.Join.ROUND);

        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setFakeBoldText(true);
        titlePaint.setColor(textColor);

        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setFakeBoldText(true);

        alertPaint.setTextAlign(Paint.Align.CENTER);
        alertPaint.setFakeBoldText(true);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float percentOf(int value) {
        return value * 100f / maxValue;
    }

    public void setProgress(int percent) {
        int safePercent = Math.max(0, percent);
        consumed = Math.round(safePercent * maxValue / 100f);
        animateTo(safePercent);
    }

    public int getProgress() {
        return Math.round(percentOf(consumed));
    }

    public void setConsumed(int milliliters) {
        consumed = Math.max(0, milliliters);
        animateTo(percentOf(consumed));
    }

    public int getConsumed() {
        return consumed;
    }

    public void setMaxValue(int milliliters) {
        maxValue = Math.max(1, milliliters);
        animateTo(percentOf(consumed));
    }

    public int getMaxValue() {
        return maxValue;
    }

    public boolean isGoalExceeded() {
        return consumed > maxValue;
    }

    public void setAbsoluteMode(boolean absoluteMode) {
        this.absoluteMode = absoluteMode;
        invalidate();
    }

    public boolean isAbsoluteMode() {
        return absoluteMode;
    }

    public void setTitleText(String titleText) {
        title = titleText == null ? "" : titleText;
        buildGeometry();
        invalidate();
    }

    private void animateTo(float targetPercent) {
        if (animator != null) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(displayedPercent, targetPercent);
        animator.setDuration(ANIMATION_DURATION_MS);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(valueAnimator -> {
            displayedPercent = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.cancel();
        }
        displayedPercent = percentOf(consumed);
        super.onDetachedFromWindow();
    }

    @Override
    public boolean performClick() {
        absoluteMode = !absoluteMode;
        invalidate();
        return super.performClick();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = resolveSize((int) dp(200), widthMeasureSpec);
        int height = resolveSize((int) dp(320), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        buildGeometry();
    }

    private void buildGeometry() {
        float w = getWidth();
        float h = getHeight();
        if (w <= 0f || h <= 0f) {
            return;
        }

        centerX = w / 2f;

        float top = title.isEmpty() ? h * 0.02f : h * 0.14f;
        float bottleHeight = h * 0.98f - top;

        titleY = top / 2f;
        titleSize = Math.max(top * 0.4f, 1f);

        bodyWidth = Math.min(w * 0.8f, bottleHeight * 0.55f);
        float bodyLeft = centerX - bodyWidth / 2f;
        float bodyRight = centerX + bodyWidth / 2f;
        float neckWidth = bodyWidth * 0.38f;
        float neckLeft = centerX - neckWidth / 2f;
        float neckRight = centerX + neckWidth / 2f;

        float capTop = top;
        float neckTop = capTop + bottleHeight * 0.05f;
        float neckBottom = top + bottleHeight * 0.24f;
        float shoulderBottom = top + bottleHeight * 0.38f;
        float bottom = top + bottleHeight;
        float radius = bodyWidth * 0.16f;
        float shoulderHeight = shoulderBottom - neckBottom;

        bottlePath.reset();
        bottlePath.moveTo(neckLeft, neckTop);
        bottlePath.lineTo(neckLeft, neckBottom);
        bottlePath.cubicTo(
                neckLeft, neckBottom + shoulderHeight * 0.6f,
                bodyLeft, shoulderBottom - shoulderHeight * 0.6f,
                bodyLeft, shoulderBottom);
        bottlePath.lineTo(bodyLeft, bottom - radius);
        bottlePath.quadTo(bodyLeft, bottom, bodyLeft + radius, bottom);
        bottlePath.lineTo(bodyRight - radius, bottom);
        bottlePath.quadTo(bodyRight, bottom, bodyRight, bottom - radius);
        bottlePath.lineTo(bodyRight, shoulderBottom);
        bottlePath.cubicTo(
                bodyRight, shoulderBottom - shoulderHeight * 0.6f,
                neckRight, neckBottom + shoulderHeight * 0.6f,
                neckRight, neckBottom);
        bottlePath.lineTo(neckRight, neckTop);

        capRect.set(neckLeft - neckWidth * 0.12f, capTop, neckRight + neckWidth * 0.12f, neckTop);

        waterBottom = bottom;
        waterTopFull = neckBottom;

        float contentCenter = (shoulderBottom + bottom) / 2f;
        valueY = contentCenter - bodyWidth * 0.08f;
        alertY = valueY + bodyWidth * 0.24f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean exceeded = isGoalExceeded();
        int stateColor = exceeded ? alertColor : progressColor;

        float fill = Math.min(displayedPercent, 100f) / 100f;
        float waterTop = waterBottom - fill * (waterBottom - waterTopFull);

        progressPaint.setColor(stateColor);
        outlinePaint.setColor(exceeded ? alertColor : OUTLINE_COLOR);

        canvas.save();
        canvas.clipPath(bottlePath);
        canvas.drawRect(0f, 0f, getWidth(), waterBottom, remainingPaint);
        canvas.drawRect(0f, waterTop, getWidth(), waterBottom, progressPaint);
        canvas.restore();

        canvas.drawPath(bottlePath, outlinePaint);
        canvas.drawRoundRect(capRect, dp(4), dp(4), progressPaint);

        if (!title.isEmpty()) {
            drawFitted(canvas, title, titlePaint, titleSize, getWidth() * 0.96f, centerX, titleY);
        }

        String valueText;
        if (absoluteMode) {
            valueText = Math.round(displayedPercent * maxValue / 100f) + "/" + maxValue + " ml";
        } else {
            valueText = Math.round(displayedPercent) + "%";
        }

        int contentColor = exceeded ? Color.WHITE : textColor;
        valuePaint.setColor(contentColor);
        drawFitted(canvas, valueText, valuePaint, bodyWidth * 0.28f, bodyWidth * 0.9f, centerX, valueY);

        if (exceeded) {
            alertPaint.setColor(Color.WHITE);
            drawFitted(canvas, alertLabel, alertPaint, bodyWidth * 0.12f, bodyWidth * 0.9f, centerX, alertY);
            drawFitted(canvas, "+" + (consumed - maxValue) + " ml", alertPaint,
                    bodyWidth * 0.12f, bodyWidth * 0.9f, centerX, alertY + bodyWidth * 0.14f);
        }
    }

    private void drawFitted(Canvas canvas, String text, Paint paint, float baseSize,
                            float maxWidth, float x, float centerY) {
        paint.setTextSize(baseSize);
        float width = paint.measureText(text);
        if (width > maxWidth) {
            paint.setTextSize(baseSize * maxWidth / width);
        }
        float baseline = centerY - (paint.descent() + paint.ascent()) / 2f;
        canvas.drawText(text, x, baseline, paint);
    }
}