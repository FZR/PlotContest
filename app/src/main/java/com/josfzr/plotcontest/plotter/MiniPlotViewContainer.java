package com.josfzr.plotcontest.plotter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.josfzr.plotcontest.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class MiniPlotViewContainer extends FrameLayout {

    //private KnobListener mPanListener;
    private RectF mKnobRect;
    private int mFillColor;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public MiniPlotViewContainer(@NonNull Context context) {
        this(context, null);
    }

    public MiniPlotViewContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiniPlotViewContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Resources r = context.getResources();
        mFillColor = ContextCompat.getColor(context, R.color.mini_plot_fill);

        setWillNotDraw(false);

    }

    /*public void setPanListener(KnobListener panListener) {
        mPanListener = panListener;
    }*/

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mKnobRect = new RectF(getPaddingLeft(),
                getPaddingTop(),
                (w - getPaddingRight()) / 5f,
                h - getPaddingBottom()
        );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean isConsumed = super.onTouchEvent(event);/*

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            isConsumed = true;
            if (mPanListener != null) {
                mPanListener.onPanned(event.getX(), 0f);
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            isConsumed = true;
            if (mPanListener != null) {
                mPanListener.onPanned(event.getX(), 0f);
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            isConsumed = true;
            if (mPanListener != null) {
                mPanListener.onPanStop();
            }
        }

        return isConsumed;*/
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setColor(mFillColor);
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);
    }
}
