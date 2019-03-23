package com.josfzr.plotcontest.plotter.engine;

import android.animation.TimeAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.josfzr.plotcontest.R;
import com.josfzr.plotcontest.data.DataSet;
import com.josfzr.plotcontest.plotter.engine.data.CursorData;
import com.josfzr.plotcontest.plotter.engine.data.Line;
import com.josfzr.plotcontest.plotter.engine.data.PlotEngineDataHandler;
import com.josfzr.plotcontest.plotter.engine.data.PlottingLine;
import com.josfzr.plotcontest.plotter.engine.data.XAxis;
import com.josfzr.plotcontest.themes.AppTheme;
import com.josfzr.plotcontest.utils.NiceNum;

import androidx.annotation.NonNull;

//"Engine"
public class PlottingEngine {

    private static final int MIN = 0;
    private static final int MAX = 1;

    public static final float MIN_SCALE_X = 1f;
    public static final float MAX_SCALE_X = 4f;

    private final Drawer mDrawer;
    private boolean mShowAll;

    private Rect mViewPort, mViewRect;

    private Paint mXAxisTextPaint;

    private NiceNum mNiceNum;

    private long mCurrentMax, mCurrentMin;
    private long mTargetMax;

    private TimeAnimator mTimeAnimator;

    private LinePlotDrawable mLinePlotDrawable;
    private CursorDrawable mCursor;
    private XAxisDrawable mXAxis;
    private GridDrawable mGridDrawable;

    private long mLastAnimationTime;

    private ScaleHandler mScaleHandler;

    private int[] mIndicesHolder = new int[2];
    private PlotEngineDataHandler mDataHandler = new PlotEngineDataHandler();

    public PlottingEngine(@NonNull Context context,
                          @NonNull Drawer drawer,
                          boolean showAll,
                          float strokeWidth) {

        mDrawer = drawer;
        mShowAll = showAll;

        Resources r = context.getResources();
        float xAxisTextSize = r.getDimension(R.dimen.plot_text_size);
        mXAxisTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mXAxisTextPaint.setTextSize(xAxisTextSize);
        mXAxisTextPaint.setColor(Color.BLACK);

        mScaleHandler = new ScaleHandler(
                mShowAll ? MIN_SCALE_X : MIN_SCALE_X, 1f
        );

        mNiceNum = new NiceNum(0, 0);
        if (!mShowAll) {
            mGridDrawable = new GridDrawable(context, this);
        }

        mLinePlotDrawable = new LinePlotDrawable(context,
                this,
                strokeWidth);

        if (!mShowAll) {
            mXAxis = new XAxisDrawable(context, this);
        }

        if (!mShowAll) {
            mCursor = new CursorDrawable(context, this);
        }
    }

    boolean isShowAll() {
        return mShowAll;
    }

    private void requestDraw() {
        mDrawer.onDrawRequested();
    }

    public void onNewSizeAvailable(Rect viewPort) {
        mViewRect = new Rect(viewPort);
        mViewPort = new Rect(viewPort);
        if (mGridDrawable != null) mGridDrawable.onNewSize(mViewRect);
        mLinePlotDrawable.onNewSize(mViewRect);
        if (mXAxis != null) mXAxis.onNewSize(mViewRect);
        if (mCursor != null) mCursor.onNewSize(mViewRect);
    }

    public void setData(DataSet dataSet) {
        mDataHandler.setDataSet(dataSet, (float) mViewRect.width() / (float) dataSet.getWidth());

        long min = mDataHandler.getRawDataSet().getMin();
        long max = mDataHandler.getRawDataSet().getMax();

        mNiceNum.setMinMaxPoints(min, max);

        if (mGridDrawable != null) mGridDrawable.setData(mDataHandler);
        mLinePlotDrawable.setData(mDataHandler);
        if (mXAxis != null) mXAxis.setData(mDataHandler);
        if (mCursor != null) mCursor.setData(mDataHandler);


        long[] minMax = new long[2];
        getMinMaxInRect(mViewPort, minMax);

        if (isNewMinMax(minMax)) {
            setNewMinMax(minMax, false);
        }
    }

    long getRealMax() {
        return mDataHandler.getRawDataSet() == null ? 0 : mDataHandler.getRawDataSet().getMax();
    }

    long getRealMin() {
        return mDataHandler.getRawDataSet() == null ? 0 : mDataHandler.getRawDataSet().getMin();
    }

    long getCurrentMax() {
        return mCurrentMax;
    }

    long getCurrentMin() {
        return mCurrentMin;
    }

    double getNiceMax() {
        return mNiceNum.getNiceMax();
    }

    float getDefaultScaleXStart() {
        return MAX_SCALE_X;
    }

    float getValueToPixelY(Rect rect) {
        return (float) rect.height() / getCurrentMax();
    }

    float getMaxValueToPixelY() {
        return (float) mViewRect.height() / (float) getRealMax();
    }

    float getValueToPixelX() {
        return (float) mViewRect.width() / (float) mDataHandler.getRawDataSet().getWidth();
    }

    float getPixelXToValue() {
        return (float) mDataHandler.getRawDataSet().getWidth() / (float) mViewRect.width();
    }

    public CursorData getCursorDataAt(float x) {
        float pointX = mViewPort.left + x;
        int index = (int) Math.floor(pointX * getPixelXToValue() * getScaleXInv());

        PlottingLine[] lines = mLinePlotDrawable.getAllLines();
        CursorData data = mCursor.getCurrentCursor();
        if (data == null) {
            data = new CursorData();
        }
        XAxis axis = mDataHandler.getXAxis();
        index = Math.min(Math.max(index, 0), axis.getPoints().size() - 1);
        data.setXValue(axis.getPoints().get(index).getAltText());

        CursorData.CursorPoint[] cursorPoints = data.getCursorPoints();
        if (cursorPoints == null) {
            cursorPoints = new CursorData.CursorPoint[lines.length];
        }
        data.setCursorPoints(cursorPoints);

        for (int i = 0; i < lines.length; i++) {
            data.setX(lines[i].getPoints().get(index).getX());
            CursorData.CursorPoint point = cursorPoints[i];
            if (point == null) {
                point = new CursorData.CursorPoint();
            }
            point.setY(lines[i].getPoints().get(index).getY());
            point.setColor(lines[i].getColor());
            point.setValue(lines[i].getPoints().get(index).getPlotData().value);
            point.setName(lines[i].getName());
            point.setId(lines[i].getId());
            cursorPoints[i] = point;
        }

        return data;
    }

    public void onDraw(Canvas canvas) {
        if (mDataHandler.getRawDataSet() == null) return;
        getIndicesForCurrentViewport(mIndicesHolder);
        if (mGridDrawable != null) mGridDrawable.draw(canvas, mIndicesHolder[0], mIndicesHolder[1]);
        mLinePlotDrawable.draw(canvas, mIndicesHolder[0], mIndicesHolder[1]);
        if (mXAxis != null) mXAxis.draw(canvas, mIndicesHolder[0], mIndicesHolder[1]);
        if (mCursor != null) mCursor.draw(canvas, mIndicesHolder[0], mIndicesHolder[1]);
    }

    public void pan(RectF projection/*float dx, float dy*/) {
        float max = mViewRect.width() * getScaleX();
        mViewPort.offsetTo((int) (projection.left * max), 0);
        requestDraw();
        long[] minMax = new long[2];
        getMinMaxInRect(mViewPort, minMax);

        if (isNewMinMax(minMax)) {
            setNewMinMax(minMax, true);
        }
    }

    public void updateProjection(RectF projection) {
        setScaleX(1 / projection.width());
        float max = mViewRect.right * getScaleX();
        mViewPort.left = (int) Math.ceil(max * projection.left);
        mViewPort.right = (int) Math.ceil(max * projection.right);

        requestDraw();
        long[] minMax = new long[2];
        getMinMaxInRect(mViewPort, minMax);

        if (isNewMinMax(minMax)) {
            setNewMinMax(minMax, true);
        }
    }

    public float getScaleX() {
        return mScaleHandler.getScaleX();
    }

    public float getScaleY() {
        return mScaleHandler.getScaleY();
    }

    float getScaleXInv() {
        return mScaleHandler.getScaleXInv();
    }

    float getScaleYInv() {
        return mScaleHandler.getScaleYInv();
    }

    public void setScale(float scaleX, float scaleY) {
        setScaleX(scaleX);
        setScaleY(scaleY);
    }

    public void setScaleX(float scaleX) {
        mScaleHandler.setScaleX(scaleX);
        long[] minMax = new long[2];
        getMinMaxInRect(mViewPort, minMax);

        if (isNewMinMax(minMax)) {
            setNewMinMax(minMax, true);
        }

        requestDraw();
    }

    public void setScaleY(float scaleY) {
        mScaleHandler.setScaleY(scaleY);
        long[] minMax = new long[2];
        getMinMaxInRect(mViewPort, minMax);

        if (isNewMinMax(minMax)) {
            setNewMinMax(minMax, true);
        }

        requestDraw();
    }

    /**
     * Searches for minimal and maximal values inside the given rect
     *
     * @param rect rect to check
     * @param out out array with size 2, where 0 - min and 1 - max
     */
    private void getMinMaxInRect(Rect rect, long[] out) {
        long max = -1;
        long min = Long.MAX_VALUE;

        int linesCount = mLinePlotDrawable.getCurrentLines().size();

        int startIndex = (int) Math.floor(rect.left * getPixelXToValue() * getScaleXInv());
        int endIndex = (int) Math.ceil(rect.right * getPixelXToValue() * getScaleXInv()) + 1;

        endIndex = Math.min(endIndex, mDataHandler.getRawDataSet().getWidth());
        startIndex = Math.max(startIndex, 0);


        for (int i = 0; i < linesCount; i++) {
            Line line = mLinePlotDrawable.getCurrentLines().get(i);
            for (int j = startIndex; j < endIndex; j++) {
                Line.LinePoint point = line.getPoints().get(j);
                max = Math.max(max, point.getPlotData().value);
                min = Math.min(min, point.getPlotData().value);
            }
        }

        out[MIN] = min;
        out[MAX] = max;
    }

    private void getIndicesInRect(Rect rect, int[] indices) {
        int startIndex = (int) Math.floor(rect.left * getPixelXToValue() * getScaleXInv());
        int endIndex = (int) Math.ceil(rect.right * getPixelXToValue() * getScaleXInv()) + 1;

        startIndex = Math.max(0, startIndex);
        endIndex = Math.min(endIndex, mDataHandler.getRawDataSet().getWidth());

        indices[0] = startIndex;
        indices[1] = endIndex;
    }

    void getIndicesForCurrentViewport(int[] outIndices) {
        getIndicesInRect(mViewPort, outIndices);
    }

    Rect getViewport() {
        return mViewPort;
    }

    private void animateIfNeed() {
        if (mTimeAnimator == null) {
            mTimeAnimator = new TimeAnimator();
            mTimeAnimator.setTimeListener((animation, totalTime, deltaTime) -> {
                if (deltaTime == 0) return;

                boolean canEnd = true;
                float delta = deltaTime * .01f;

                Log.d("Delta test", "delta: " + delta);

                if (mCurrentMax != mTargetMax) {
                    long value = mTargetMax - mCurrentMax;
                    long toAdd = (long) (value * delta);
                    if (toAdd == 0 && deltaTime != 0) {
                        mCurrentMax = mTargetMax;
                    } else {
                        mCurrentMax += toAdd;
                    }
                    canEnd = false;
                }

                if (mGridDrawable != null) canEnd = mGridDrawable.animate(delta) && canEnd;
                canEnd = mLinePlotDrawable.animate(delta) && canEnd;
                if (mXAxis != null) canEnd = mXAxis.animate(delta) && canEnd;
                if (mCursor != null) canEnd = mCursor.animate(delta) && canEnd;

                requestDraw();

                if (canEnd) {
                    mLastAnimationTime = mTimeAnimator.getCurrentPlayTime();
                    mTimeAnimator.cancel();
                }
            });
            mTimeAnimator.start();
        } else {
            if (!mTimeAnimator.isRunning()) {
                mTimeAnimator.start();
                mTimeAnimator.setCurrentPlayTime(mLastAnimationTime + 1600);
            }
        }
    }

    private boolean isNewMinMax(long[] minMax) {
        return minMax[MIN] != mCurrentMin || minMax[MAX] != mTargetMax;
    }

    private void setNewMinMax(long[] minMax, boolean animate, boolean forceAnimate) {
        long min = minMax[MIN];
        long max = minMax[MAX];
        double oldNiceMax = mNiceNum.getNiceMax();
        mNiceNum.setMinMaxPoints(min, max);
        if (!animate) {
            mCurrentMin = min;
            mCurrentMax = max;
            mTargetMax = mCurrentMax;
        } else {
            if (mGridDrawable != null) mGridDrawable.onNewMinMax(minMax);
            boolean shouldConsiderAnimation = oldNiceMax != mNiceNum.getNiceMax();
            mCurrentMin = min;
            mTargetMax = max;
            if (shouldConsiderAnimation || forceAnimate) {
                animateIfNeed();
            }
        }
    }

    private void setNewMinMax(long[] minMax, boolean animate) {
        setNewMinMax(minMax, animate, false);
    }

    public void stopPan() {

    }

    public CursorData cursorAt(float x) {
        CursorData data = getCursorDataAt(x);
        mCursor.setCursor(data);
        requestDraw();

        return data;
    }

    public double getNiceMin() {
        return mNiceNum.getNiceMin();
    }

    public void onRestoreLine(String id) {
        if (mLinePlotDrawable != null) {
            mLinePlotDrawable.showLine(id);
            if (mCursor != null) mCursor.showLine(id);
            long[] minMax = new long[2];
            getMinMaxInRect(mViewPort, minMax);

            if (isNewMinMax(minMax)) {
                setNewMinMax(minMax, true, true);
            } else {
                animateIfNeed();
            }

        }
    }

    public void onRemoveLine(String id) {
        if (mLinePlotDrawable != null) {
            mLinePlotDrawable.hideLine(id);
            if (mCursor != null) mCursor.hideLine(id);
            long[] minMax = new long[2];
            getMinMaxInRect(mViewPort, minMax);

            if (isNewMinMax(minMax)) {
                setNewMinMax(minMax, true, true);
            } else {
                animateIfNeed();
            }

        }
    }

    public void updateTheme(Context context, AppTheme appTheme) {
        if (mGridDrawable != null) mGridDrawable.updateTheme(context, appTheme);
        mLinePlotDrawable.updateTheme(context, appTheme);
        if (mXAxis != null) mXAxis.updateTheme(context, appTheme);
        if (mCursor != null) mCursor.updateTheme(context, appTheme);
    }

    public interface Drawer {
        void onDrawRequested();
    }
}
