package com.josfzr.plotcontest.plotter;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.josfzr.plotcontest.PopupController;
import com.josfzr.plotcontest.R;
import com.josfzr.plotcontest.data.DataSet;
import com.josfzr.plotcontest.plotter.engine.PlottingEngine;
import com.josfzr.plotcontest.plotter.engine.data.CursorData;
import com.josfzr.plotcontest.themes.AppTheme;

import androidx.annotation.Nullable;

public class PlotView extends View implements PlottingEngine.Drawer,
        KnobListener {

    private Rect mPlotViewSize;

    private PlottingEngine mPlottingEngine;

    private boolean mShowAll, mIsPanning;

    @Nullable
    private PopupController mPopupController;

    public PlotView(Context context) {
        this(context, null, 0);
    }

    public PlotView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlotView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Resources r = context.getResources();
        mShowAll = false;
        float strokeWidth = r.getDimension(R.dimen.plot_stroke_width);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.PlotView,
                    0, 0);

            try {
                mShowAll = a.getBoolean(R.styleable.PlotView_showAll, false);
                strokeWidth = a.getDimension(R.styleable.PlotView_strokeWidth, strokeWidth);
            } finally {
                a.recycle();
            }
        }

        mPlottingEngine = new PlottingEngine(context, this, mShowAll, strokeWidth);

        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    public void setPopupController(PopupController controller) {
        mPopupController = controller;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPlotViewSize = new Rect(
                getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom()
        );
        mPlottingEngine.onNewSizeAvailable(mPlotViewSize);
    }

    public void setDataSet(DataSet dataSet) {
        mPlottingEngine.setData(dataSet);
        invalidate();
    }

    private long mLastTime = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        if (mLastTime == 0) mLastTime = System.currentTimeMillis();
        super.onDraw(canvas);
        mPlottingEngine.onDraw(canvas);

        if (mIsPanning) invalidate();
        Log.d("PLOT_VIEW", "MS per Frame " + (System.currentTimeMillis() - mLastTime));
        mLastTime = System.currentTimeMillis();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE
                || event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (mPopupController != null) {
                CursorData cursorData = mPlottingEngine.cursorAt(event.getX());
                mPopupController.showPopupWithDataAt(this, event.getX(), -getMeasuredHeight() + 50, cursorData);
            }
            return true;
        }
        mPopupController.hidePopup();
        return false;
    }

    @Override
    public void onPanned(RectF projection/*float dx, float dy*/) {
        mIsPanning = true;
        mPlottingEngine.pan(projection);
    }

    @Override
    public void onPanStop() {
        mPlottingEngine.stopPan();
        mIsPanning = false;
    }

    @Override
    public void onDrawRequested() {
        invalidate();
    }

    @Override
    public void onScale(float x, float y) {
        mPlottingEngine.setScale(x, y);
    }

    @Override
    public void updateProjection(RectF projection) {
        if (mPopupController != null) {
            mPopupController.hidePopup();
        }
        mPlottingEngine.updateProjection(projection);
    }

    public void onRestoreLine(String id) {
        if (mPopupController != null) {
            mPopupController.hidePopup();
        }
        mPlottingEngine.onRestoreLine(id);
    }

    public void onRemoveLine(String id) {
        if (mPopupController != null) {
            mPopupController.hidePopup();
        }
        mPlottingEngine.onRemoveLine(id);
    }

    public void setAppTheme(AppTheme appTheme) {
        mPlottingEngine.updateTheme(getContext(), appTheme);
        invalidate();
    }
}
