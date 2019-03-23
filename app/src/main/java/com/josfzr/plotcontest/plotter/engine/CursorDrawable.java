package com.josfzr.plotcontest.plotter.engine;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.josfzr.plotcontest.R;
import com.josfzr.plotcontest.plotter.engine.data.CursorData;
import com.josfzr.plotcontest.plotter.engine.data.PlotEngineDataHandler;
import com.josfzr.plotcontest.themes.AppTheme;

import java.util.Arrays;

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

    private CursorData mCurrentCursor;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Rect mViewSize;

    private int[] mAlphas;

    public CursorDrawable(@NonNull Context context,
                          @NonNull PlottingEngine plottingEngine) {
        mEngine = plottingEngine;
        Resources r = context.getResources();

        mPaint.setStyle(Paint.Style.FILL);
        mInnerColor = ContextCompat.getColor(context, R.color.day_cursor_inner_circle_color);
        mCursorColor = ContextCompat.getColor(context, R.color.day_cursor_color);
        mDiameter = r.getDimension(R.dimen.cursor_circle_diameter);
        mCursorWidth = r.getDimension(R.dimen.cursor_width);
        mCircleStrokeWidth = r.getDimension(R.dimen.cursor_circle_stroke_width);
        mBottomTextHeight = r.getDimension(R.dimen.plot_x_axis_text_margin_top)
                + r.getDimension(R.dimen.plot_x_axis_text_size);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(mCursorWidth);
    }

    @Override
    public void draw(Canvas canvas, int recommendedStart, int recommendedEnd) {
        if (mCurrentCursor == null) return;

        int count = mCurrentCursor.getCursorPoints().length;
        float outerRadius = mDiameter * .5f;
        float innerRadius = (mDiameter - mCircleStrokeWidth) * .5f;

        canvas.save();
        canvas.translate(-mEngine.getViewport().left, 0);
        float x = mCurrentCursor.getX() * mEngine.getScaleX();
        mLinePaint.setColor(mCursorColor);
        canvas.drawLine(x - (mCursorWidth * .5f),
                mViewSize.top,
                x - (mCursorWidth - .5f),
                mViewSize.bottom,
                mLinePaint);

        for (int i = 0; i < count; i++) {
            CursorData.CursorPoint point = mCurrentCursor.getCursorPoints()[i];
            if (point.getAlpha() == 0) continue;
            mPaint.setColor(point.getColor());
            mPaint.setAlpha(point.getAlpha());
            float y = mViewSize.top + mViewSize.height() - (point.getY() * mEngine.getValueToPixelY(mViewSize));
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
        if (mCurrentCursor == null) return true;
        int count = mCurrentCursor.getCursorPoints().length;

        boolean result = true;
        float diff = 255 * delta * 2;
        for (int i = 0; i < count; i++) {
            CursorData.CursorPoint point = mCurrentCursor.getCursorPoints()[i];
            result = point.getAlpha() == mAlphas[i] && result;

            if (point.getAlpha() > mAlphas[i]) {
                point.setAlpha((int) (point.getAlpha() - diff));
            } else if (point.getAlpha() < mAlphas[i]) {
                point.setAlpha((int) (point.getAlpha() + diff));
            }
        }

        return result;
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
    public void setData(PlotEngineDataHandler dataHandler) {
        int lineCount = dataHandler.getPlottingLines().length;
        mAlphas = new int[lineCount];
        Arrays.fill(mAlphas, 255);
    }

    public void setCursor(CursorData data) {
        mCurrentCursor = data;
    }

    public CursorData getCurrentCursor() {
        return mCurrentCursor;
    }

    public void hideLine(String id) {
        if (mCurrentCursor == null) return;
        for (int i = 0; i < mCurrentCursor.getCursorPoints().length; i++) {
            if (mCurrentCursor.getCursorPoints()[i].getId().equals(id)) {
                mAlphas[i] = 0;
                break;
            }
        }
    }

    public void showLine(String id) {
        if (mCurrentCursor == null) return;
        for (int i = 0; i < mCurrentCursor.getCursorPoints().length; i++) {
            if (mCurrentCursor.getCursorPoints()[i].getId().equals(id)) {
                mAlphas[i] = 255;
                break;
            }
        }
    }

    @Override
    public void updateTheme(Context context, AppTheme appTheme) {
        mCursorColor = ContextCompat.getColor(context, appTheme.getSegmentLinesColor());
        mInnerColor = ContextCompat.getColor(context, appTheme.getCursorInnerCircleColor());
    }
}
