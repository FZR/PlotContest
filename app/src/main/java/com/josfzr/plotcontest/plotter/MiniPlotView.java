package com.josfzr.plotcontest.plotter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.josfzr.plotcontest.R;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class MiniPlotView extends PlotView {

    private static final int NO_HANDLE = -1;
    private static final int LEFT = 0;
    private static final int RIGHT = 1;

    private KnobListener mKnobListener;
    private RectF mKnobRect, mProjection;
    private int mFillColor;
    private int mKnobHandlesColor;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mKnobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int mSelectedHandle = NO_HANDLE;

    private float mKnobTop, mKnobHandleWidth;

    private Canvas mFillCanvas;
    private Bitmap mFillBitmap;

    private float mStartX = 0f;

    private float mKnobMinSize;

    public MiniPlotView(Context context) {
        this(context, null);
    }

    public MiniPlotView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiniPlotView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mKnobPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        Resources r = context.getResources();
        mFillColor = ContextCompat.getColor(context, R.color.mini_plot_fill);
        mKnobHandlesColor = ContextCompat.getColor(context, R.color.knob_handles_color);

        mKnobTop = r.getDimension(R.dimen.knob_top);
        mKnobHandleWidth = r.getDimension(R.dimen.knob_side_handles_width);
        mProjection = new RectF(0, 0, .142f, 1f);
    }

    public void setPanListener(KnobListener knobListener) {
        mKnobListener = knobListener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mFillBitmap = Bitmap.createBitmap(
                w - getPaddingLeft() - getPaddingRight(),
                h - getPaddingTop() - getPaddingBottom(),
                Bitmap.Config.ARGB_8888
        );
        mFillCanvas = new Canvas(mFillBitmap);
        mKnobRect = new RectF(getPaddingLeft(),
                getPaddingTop(),
                ((w - getPaddingRight()) / 7f) + (mKnobHandleWidth * 2),
                h - getPaddingBottom()
        );

        mKnobMinSize = mKnobRect.width();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean isConsumed = false;

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            isConsumed = true;
            if (mKnobListener != null) {
                mSelectedHandle = testKnobsHitOnRect(mKnobRect, event.getX(), mKnobHandleWidth);
                if (mSelectedHandle != NO_HANDLE) {
                    return true;
                }
                if (mKnobRect.contains(event.getX(), 0f)) {
                    mStartX = event.getX() - mKnobRect.left;
                }
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            isConsumed = true;
            if (mKnobListener != null) {
                if (mSelectedHandle == NO_HANDLE) {
                    if (mKnobRect.contains(event.getX(), 0)) {
                        float x = event.getX() - mStartX;
                        x = Math.max(Math.min(x, getMeasuredWidth() - mKnobRect.width()), 0);
                        mKnobRect.offsetTo(x, 0f);
                        mProjection.offsetTo(mKnobRect.left / getMeasuredWidth(), 0f);
                        mKnobListener.updateProjection(mProjection);
                    }
                } else {
                    if (mSelectedHandle == RIGHT) {
                        mKnobRect.right = Math.min(Math.max(event.getX(), mKnobRect.left + mKnobMinSize), getMeasuredWidth());
                    } else {
                        mKnobRect.left = Math.max(Math.min(event.getX(), mKnobRect.right - mKnobMinSize), 0);
                    }

                    mProjection.left = mKnobRect.left / getMeasuredWidth();
                    mProjection.right = mKnobRect.right / getMeasuredWidth();

                    mKnobListener.updateProjection(mProjection);
                }
                invalidate();
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            isConsumed = true;
            if (mKnobListener != null) {
                mKnobListener.onPanStop();
            }

            mSelectedHandle = NO_HANDLE;
        }

        return isConsumed;
    }

    private int testKnobsHitOnRect(RectF rect, float hitX, float knobHandleSize) {
        if (hitX >= rect.left && hitX <= rect.left + knobHandleSize) {
            return LEFT;
        } else if (hitX >= rect.right - knobHandleSize && hitX <= rect.right) {
            return RIGHT;
        }

        return NO_HANDLE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mFillBitmap.eraseColor(Color.TRANSPARENT);
        mPaint.setColor(mFillColor);
        mFillCanvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);
        mPaint.setColor(mKnobHandlesColor);
        mFillCanvas.drawRect(mKnobRect, mPaint);
        mFillCanvas.drawRect(mKnobRect.left + mKnobHandleWidth,
                mKnobTop,
                mKnobRect.right - mKnobHandleWidth,
                mKnobRect.bottom - mKnobTop,
                mKnobPaint);

        canvas.drawBitmap(mFillBitmap, 0, 0, null);

    }
}
