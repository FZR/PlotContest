package com.josfzr.plotcontest.plotter.engine;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.josfzr.plotcontest.R;
import com.josfzr.plotcontest.data.DataSet;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

class CursorDrawable implements PlotDrawable {
    private float mDiameter;
    private float mCursorWidth;
    private float mCircleStrokeWidth;
    private float mCurrentX;
    private float mBottomTextHeight;

    private int mInnerColor, mCursorColor;

    private PlottingEngine mEngine;

    private PlottingEngine.CursorData mCurrentCursor;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Rect mViewSize;

    public CursorDrawable(@NonNull Context context,
                          @NonNull PlottingEngine plottingEngine) {
        mEngine = plottingEngine;
        Resources r = context.getResources();

        mPaint.setStyle(Paint.Style.FILL);
        mInnerColor = ContextCompat.getColor(context, R.color.white);
        mCursorColor = ContextCompat.getColor(context, R.color.cursor_color);
        mDiameter = r.getDimension(R.dimen.cursor_circle_diameter);
        mCursorWidth = r.getDimension(R.dimen.cursor_width);
        mCircleStrokeWidth = r.getDimension(R.dimen.cursor_circle_stroke_width);
        mBottomTextHeight = r.getDimension(R.dimen.plot_x_axis_text_margin_top)
                + r.getDimension(R.dimen.plot_x_axis_text_size);
    }

    @Override
    public void draw(Canvas canvas) {
        if (mCurrentCursor == null) return;

        int count = mCurrentCursor.mCursorPoints.length;
        float outerRadius = mDiameter * .5f;
        float innerRadius = (mDiameter - mCircleStrokeWidth) * .5f;

        canvas.save();
        canvas.translate(-mEngine.getViewport().left, 0);
        float x = mCurrentCursor.x * mEngine.getScaleX();
        mPaint.setColor(mCursorColor);
        canvas.drawLine((x) - (mCursorWidth * .5f),
                0,
                (x) + (mCursorWidth * .5f),
                mViewSize.bottom,
                mPaint);

        for (int i = 0; i < count; i++) {
            PlottingEngine.CursorPoint point = mCurrentCursor.mCursorPoints[i];
            mPaint.setColor(point.mColor);
            float y = mViewSize.top + mViewSize.height() - (point.y * mEngine.getValueToPixelY());
            canvas.drawCircle(
                    x,
                    y,
                    outerRadius,
                    mPaint);
            mPaint.setColor(mInnerColor);
            canvas.drawCircle(
                    x,
                    y,
                    innerRadius,
                    mPaint);
        }

        canvas.restore();
    }

    @Override
    public boolean animate(float delta) {
        return true;
    }

    @Override
    public void onNewSize(Rect viewSize) {
        mViewSize = new Rect(viewSize);
        mViewSize.bottom -= mBottomTextHeight;
    }

    @Override
    public void onNewMinMax(long[] minMax) {

    }

    @Override
    public void setData(DataSet dataSet) {

    }

    public void setCursor(PlottingEngine.CursorData data) {
        mCurrentCursor = data;
    }

    public PlottingEngine.CursorData getCurrentCursor() {
        return mCurrentCursor;
    }
}
