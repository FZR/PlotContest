package com.josfzr.plotcontest.plotter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MiniPlotViewContainer extends FrameLayout {

    private PanListener mPanListener;

    public MiniPlotViewContainer(@NonNull Context context) {
        this(context, null);
    }

    public MiniPlotViewContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiniPlotViewContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPanListener(PanListener panListener) {
        mPanListener = panListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean isConsumed = super.onTouchEvent(event);

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

        return isConsumed;
    }

    interface PanListener {
        void onPanned(float dx, float dy);
        void onPanStop();
    }
}
