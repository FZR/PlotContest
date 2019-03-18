package com.josfzr.plotcontest.plotter.engine;

import android.animation.TimeAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.josfzr.plotcontest.R;
import com.josfzr.plotcontest.data.DataSet;
import com.josfzr.plotcontest.data.PlotData;
import com.josfzr.plotcontest.utils.NiceNum;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

//"Engine"
public class PlottingEngine {

    private static final int MIN = 0;
    private static final int MAX = 1;

    private static final float MIN_SCALE_X = 1f;
    private static final float DEFAULT_SCALE_X_START = 7f;

    private final Drawer mDrawer;
    private boolean mShowAll;

    private Rect mViewPort, mViewRect;

    private DataSet mDataSet;

    private Paint mXAxisTextPaint;

    private NiceNum mNiceNum;

    private long mCurrentMax, mCurrentMin;
    private long mTargetMax;

    private TimeAnimator mTimeAnimator;

    private List<PlotDrawable> mDrawables;
    private LinePlotDrawable mLinePlotDrawable;
    private CursorDrawable mCursor;
    private XAxisDrawable mXAxis;

    private ScaleHandler mScaleHandler;

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
                mShowAll ? MIN_SCALE_X : DEFAULT_SCALE_X_START, 1f
        );

        mNiceNum = new NiceNum(0, 0);
        mDrawables = new ArrayList<>();
        if (!mShowAll) mDrawables.add(new GridDrawable(context, this, r.getDimensionPixelSize(R.dimen.plot_text_size)));
        mLinePlotDrawable = new LinePlotDrawable(context,
                this,
                strokeWidth,
                mShowAll ? 0 : xAxisTextSize);
        mDrawables.add(mLinePlotDrawable);

        if (!mShowAll) {
            mXAxis = new XAxisDrawable(context, this, xAxisTextSize);
            mDrawables.add(mXAxis);
        }

        if (!mShowAll) {
            mCursor = new CursorDrawable(context, this);
            mDrawables.add(mCursor);
        }
    }

    private void requestDraw() {
        mDrawer.onDrawRequested();
    }

    public void onNewSizeAvailable(Rect viewPort) {
        mViewRect = viewPort;
        mViewPort = new Rect(viewPort);
        if (mDrawables != null) {
            for (PlotDrawable drawable : mDrawables) {
                drawable.onNewSize(mViewRect);
            }
        }
    }

    public void setData(DataSet dataSet) {
        mDataSet = dataSet;

        long min = mDataSet.getMin();
        long max = mDataSet.getMax();

        mNiceNum.setMinMaxPoints(min, max);

        mLinePlotDrawable.setData(dataSet);

        long[] minMax = new long[2];
        getMinMaxInRect(mViewPort, minMax);

        if (isNewMinMax(minMax)) {
            setNewMinMax(minMax, false);
        }

        for (PlotDrawable d : mDrawables) {
            if (d != mLinePlotDrawable) {
                d.setData(dataSet);
            }
        }
    }

    long getRealMax() {
        return mDataSet == null ? 0 : mDataSet.getMax();
    }

    long getRealMin() {
        return mDataSet == null ? 0 : mDataSet.getMin();
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
        return DEFAULT_SCALE_X_START;
    }

    float getValueToPixelY() {
        return (float) mViewRect.height() / (float) getCurrentMax();
    }

    float getMaxValueToPixelY() {
        return (float) mViewRect.height() / (float) getRealMax();
    }

    float getValueToPixelX() {
        return (float) mViewRect.width() / (float) mDataSet.getWidth();
    }

    float getPixelXToValue() {
        return (float) mDataSet.getWidth() / (float) mViewRect.width();
    }

    float getPixelYToValue() {
        return (float) getCurrentMax() / (float) mViewRect.height();
    }

    public CursorData getCursorDataAt(float x) {
        float pointX = mViewPort.left + x;
        int index = (int) Math.floor(pointX * getPixelXToValue() * getScaleXInv());

        List<Line> lines = mLinePlotDrawable.getCurrentLines();
        CursorData data = mCursor.getCurrentCursor();
        if (data == null) {
            data = new CursorData();
        }
        XAxis axis = mXAxis.getLine();
        data.mXValue = axis.points.get(index).text;
        //data.x = x;
        CursorPoint[] cursorPoints = data.mCursorPoints;
        if (cursorPoints == null) {
            cursorPoints = new CursorPoint[lines.size()];
        }
        data.mCursorPoints = cursorPoints;
        for (int i = 0; i < lines.size(); i++) {
            data.x = lines.get(i).points.get(index).x;
            CursorPoint point = cursorPoints[i];
            if (point == null) {
                point = new CursorPoint();
            }
            point.y = lines.get(i).points.get(index).y;
            point.mColor = lines.get(i).color;
            point.mValue = lines.get(i).points.get(index).plotData.value.longValue();
            cursorPoints[i] = point;
        }

        return data;
    }

    public void onDraw(Canvas canvas) {
        if (mDataSet == null) return;
        if (mDrawables == null || mDrawables.size() == 0) return;

        for (PlotDrawable d : mDrawables) {
            d.draw(canvas);
        }
    }

    public void pan(RectF projection/*float dx, float dy*/) {
        float max = mViewRect.width() * getScaleX();
        //dx = Math.min(Math.max(dx * getScaleX(), 0), max);
        mViewPort.offsetTo((int) (projection.left * max), 0);
        requestDraw();
        long[] minMax = new long[2];
        getMinMaxInRect(mViewPort, minMax);

        if (isNewMinMax(minMax)) {
            setNewMinMax(minMax, true);
        }
        //Log.d("Plotting Engine", "Min in rect is: " + minMax[0] + " Max in rect is: " + minMax[1]);
    }

    public void updateProjection(RectF projection) {
        setScaleX(1 / projection.width());
        float max = mViewRect.width() * getScaleX();
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

        endIndex = Math.min(endIndex, mDataSet.getWidth());
        startIndex = Math.max(startIndex, 0);


        for (int i = 0; i < linesCount; i++) {
            Line line = mLinePlotDrawable.getCurrentLines().get(i);
            for (int j = startIndex; j < endIndex; j++) {
                LinePoint point = line.points.get(j);
                max = Math.max(max, point.plotData.value.longValue());
                min = Math.min(min, point.plotData.value.longValue());
            }
        }

        out[MIN] = min;
        out[MAX] = max;
    }

    private void getIndicesInRect(Rect rect, int[] indices) {
        int startIndex = (int) Math.floor(rect.left * getPixelXToValue() * getScaleXInv());
        int endIndex = (int) Math.ceil(rect.right * getPixelXToValue() * getScaleXInv()) + 1;

        startIndex = Math.max(0, startIndex);
        endIndex = Math.min(endIndex, mDataSet.getWidth());

        indices[0] = startIndex;
        indices[1] = endIndex;
    }

    void getIndicesForCurrentViewport(int[] outIndices) {
        getIndicesInRect(mViewPort, outIndices);
    }

    Rect getViewport() {
        return mViewPort;
    }

    boolean endAnimation;
    private long lastAnimationTime;
    private void animateCurrentMax(long newMax) {
        //TODO: Work on target max instead. Create separate animation delegate
        //TODO: Put grid rendering into PlottingEngine
        mTargetMax = newMax;
        if (mTimeAnimator == null) {
            mTimeAnimator = new TimeAnimator();
            mTimeAnimator.setTimeListener((animation, totalTime, deltaTime) -> {
                boolean canEnd = true;
                float delta = deltaTime * .01f;

                if (mCurrentMax != mTargetMax) {
                    long value = mTargetMax - mCurrentMax;
                    long toAdd = (long) (value * delta);
                    if (toAdd == 0 && deltaTime != 0) {
                        mCurrentMax = mTargetMax;
                    } else {
                        mCurrentMax += toAdd;
                    }
                    canEnd = false;
                    //Log.d("ENGINE", "mTargetMax " + mTargetMax);
                    //Log.d("ENGINE", "mCurrentMax " + mCurrentMax);
                }
                if (mDrawables != null) {
                    for (PlotDrawable drawable : mDrawables) {
                        canEnd = drawable.animate(delta) && canEnd;
                    }
                }

                if (endAnimation && canEnd) {
                    lastAnimationTime = mTimeAnimator.getCurrentPlayTime();
                    mTimeAnimator.cancel();
                } else {
                    requestDraw();
                }
            });
            mTimeAnimator.start();
        } else {
            if (!mTimeAnimator.isRunning()) {
                mTimeAnimator.start();
                mTimeAnimator.setCurrentPlayTime(lastAnimationTime);
            }
        }
    }

    private boolean isNewMinMax(long[] minMax) {
        return minMax[MIN] != mCurrentMin || minMax[MAX] != mTargetMax;
    }

    private void setNewMinMax(long[] minMax, boolean animate) {
        long min = minMax[MIN];
        long max = minMax[MAX];
        mNiceNum.setMinMaxPoints(min, max);
        if (!animate) {
            mCurrentMin = min;
            mCurrentMax = max;
            mTargetMax = max;
        } else {
            //TODO: do not react on min
            mCurrentMin = min;
            for (PlotDrawable d : mDrawables) {
                d.onNewMinMax(minMax);
            }
            animateCurrentMax(max);
        }
    }

    public void stopPan() {
        endAnimation = true;
    }

    public void setCursorAt(float x) {
        CursorData data = getCursorDataAt(x);
        mCursor.setCursor(data);
        requestDraw();
    }

    public interface Drawer {
        void onDrawRequested();
    }

    static class XAxis {
        List<LinePoint> points;
    }

    static class Line {
        String id;
        int color;
        List<LinePoint> points;
    }

    static class LinePoint {
        PlotData plotData;
        String text;
        int x;
        int y;
    }

    static class CursorPoint {
        int mColor;
        long mValue;
        int y;
    }

    static class CursorData {
        CursorPoint[] mCursorPoints;
        String mXValue;
        float x;
    }

}
