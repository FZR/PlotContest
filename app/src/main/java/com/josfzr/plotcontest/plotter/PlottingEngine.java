package com.josfzr.plotcontest.plotter;

import android.animation.TimeAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;

import com.josfzr.plotcontest.R;
import com.josfzr.plotcontest.data.Column;
import com.josfzr.plotcontest.data.DataSet;
import com.josfzr.plotcontest.data.LineData;
import com.josfzr.plotcontest.data.PlotData;
import com.josfzr.plotcontest.utils.NiceNum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;

//"Engine"
public class PlottingEngine {

    private static final int MIN = 0;
    private static final int MAX = 1;

    private static final float MIN_SCALE_X = 1f;

    private final int mPlotBottomMargin;

    private Matrix mPlotMatrix;
    private final Drawer mDrawer;
    private boolean mShowAll;

    private Point mVisibleViewPortSize;
    private Point mPlottingViewPortSize;

    private RectF mViewPort;

    private DataSet mDataSet;

    private Paint mPlotPaint;

    // Set scale only through setter;
    private float mScaleX = 5f, mScaleY = 1f;
    private float mInvScaleX = 1 / mScaleX, mInvScaleY = 1 / mScaleY;

    private NiceNum mNiceNum;

    private HashMap<String, Path> mPaths;
    private List<Line> mLines;

    private long mCurrentMax, mCurrentMin;
    private long mTargetMax;
    private int[] mPointsStartEndIndices = new int[2];

    private TimeAnimator mTimeAnimator;

    private MinMaxChangesListener mMinMaxChangeListener;

    public PlottingEngine(@NonNull Context context,  @NonNull Drawer drawer, boolean showAll) {
        mPlotMatrix = new Matrix();
        mDrawer = drawer;
        mShowAll = showAll;

        Resources r = context.getResources();
        mPlotBottomMargin = r.getDimensionPixelSize(R.dimen.plot_bottom_margin);
        mPlotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPlotPaint.setStrokeWidth(r.getDimension(R.dimen.plot_stroke_width));
        mPlotPaint.setPathEffect(null);
        mPlotPaint.setStrokeJoin(Paint.Join.ROUND);
        mPlotPaint.setStrokeCap(Paint.Cap.ROUND);
        mPlotPaint.setStyle(Paint.Style.STROKE);

        if (mShowAll) {
            setScale(MIN_SCALE_X, 1f);
        }

        mNiceNum = new NiceNum(0, 0);
    }

    public PlottingEngine(@NonNull Context context, @NonNull Drawer drawer) {
        this(context, drawer, false);
    }

    public void setMinMaxChangeListener(MinMaxChangesListener listener) {
        mMinMaxChangeListener = listener;
    }

    protected void requestDraw() {
        mDrawer.onDrawRequested();
    }

    public void onNewSizeAvailable(int w, int h) {
        if (mVisibleViewPortSize == null) {
            mVisibleViewPortSize = new Point();
        }

        if (mPlottingViewPortSize == null) {
            mPlottingViewPortSize = new Point();
        }

        mVisibleViewPortSize.x = w;
        mVisibleViewPortSize.y = h;

        mPlottingViewPortSize.x = w;
        mPlottingViewPortSize.y = h - mPlotBottomMargin;

        mViewPort = new RectF(
                0f, 0f, mVisibleViewPortSize.x,mVisibleViewPortSize.y
        );
    }

    public void setData(DataSet dataSet, boolean invalidate) {
        mDataSet = dataSet;

        long min = mDataSet.getMin();
        long max = mDataSet.getMax();

        mNiceNum.setMinMaxPoints(min, max);

        int dataPointsCount = mDataSet.getWidth();
        float offsetX = getValueToPixelX();

        mPaths = new HashMap<>(mDataSet.getColumns().length - 1);
        mLines = new ArrayList<>(mDataSet.getColumns().length - 1);

        for (Column column : mDataSet.getColumns()) {
            if (column.getType() != Column.TYPE_X) {
                mPaths.put(column.getId(), new Path());
                Line line = new Line();
                line.id = column.getId();
                line.color = ((LineData) column).getColor();
                line.points = new ArrayList<>(dataPointsCount);

                for (int i = 0; i < dataPointsCount; i++) {
                    PlotData dataPoint = column.getValues().get(i);
                    LinePoint lp = new LinePoint();
                    lp.plotData = dataPoint;
                    lp.x = (int) (i * offsetX);
                    lp.y = (int) (dataPoint.value.longValue());
                    line.points.add(lp);
                }

                mLines.add(line);
            }
        }

        long[] minMax = new long[2];
        getMinMaxInRect(mViewPort, minMax);

        if (isNewMinMax(minMax)) {
            setNewMinMax(minMax, false);
        }

        //if (invalidate) requestDraw();
    }

    public long getRealMax() {
        return mDataSet == null ? 0 : mDataSet.getMax();
    }

    public long getRealMin() {
        return mDataSet == null ? 0 : mDataSet.getMin();
    }

    public long getCurrentMax() {
        return mCurrentMax;
    }

    public long getCurrentMin() {
        return mCurrentMin;
    }

    double getNiceMax() {
        return mNiceNum.getNiceMax();
    }

    private float getValueToPixelY() {
        return (float) mPlottingViewPortSize.y / (float) getCurrentMax();
    }

    private float getMaxValueToPixelY() {
        return (float) mPlottingViewPortSize.y / (float) getRealMax();
    }

    private float getValueToPixelX() {
        return (float) mPlottingViewPortSize.x / (float) mDataSet.getWidth();
    }

    private float getPixelXToValue() {
        return (float) mDataSet.getWidth() / (float) mPlottingViewPortSize.x;
    }

    private float getPixelYToValue() {
        return (float) getCurrentMax() / (float) mPlottingViewPortSize.y;
    }

    public void onDraw(Canvas canvas) {
        if (mDataSet == null) return;

        canvas.save();
        canvas.concat(mPlotMatrix);

        getIndicesInRect(mViewPort, mPointsStartEndIndices);

        for (int i = mPointsStartEndIndices[0]; i < mPointsStartEndIndices[1]; i++) {
            for (Line line : mLines) {
                LinePoint point = line.points.get(i);
                Path p = mPaths.get(line.id);

                if (i == mPointsStartEndIndices[0]) {
                    p.reset();
                    p.moveTo(point.x * mScaleX, mPlottingViewPortSize.y - (point.y * getValueToPixelY()));
                } else {
                    p.lineTo(point.x * mScaleX, mPlottingViewPortSize.y - (point.y * getValueToPixelY()));
                }

                if (i == mPointsStartEndIndices[1] - 1) {
                    mPlotPaint.setColor(line.color);
                    canvas.drawPath(p, mPlotPaint);
                }
            }
        }

        canvas.restore();
    }

    public Point getVisibleViewPortSize() {
        return mVisibleViewPortSize;
    }

    public Point getPlottingViewPortSize() {
        return mPlottingViewPortSize;
    }

    public void pan(float dx, float dy) {
        float max = (mDataSet.getWidth() * getValueToPixelX() * mScaleX) - (mDataSet.getWidth() * getValueToPixelX());
        dx = Math.min(Math.max(dx * mScaleX, 0), max);
        mViewPort.offsetTo(dx, dy);
        mPlotMatrix.setTranslate(-dx, dy);
        requestDraw();
        long[] minMax = new long[2];
        getMinMaxInRect(mViewPort, minMax);

        if (isNewMinMax(minMax)) {
            setNewMinMax(minMax, true);
            if (mMinMaxChangeListener != null) {
                mMinMaxChangeListener.onMinMaxChanged(minMax);
            }
        }
        //Log.d("Plotting Engine", "Min in rect is: " + minMax[0] + " Max in rect is: " + minMax[1]);
    }

    public void setScale(float scaleX, float scaleY) {
        setScaleX(scaleX);
        setScaleY(scaleY);
    }

    public void setScaleX(float scaleX) {
        mScaleX = scaleX;
        mInvScaleX = 1 / scaleX;
    }

    public void setScaleY(float scaleY) {
        mScaleY = scaleY;
        mInvScaleY = 1 / scaleY;
    }

    /**
     * Searches for minimal and maximal values inside the given rect
     *
     * @param rect rect to check
     * @param out out array with size 2, where 0 - min and 1 - max
     */
    private void getMinMaxInRect(RectF rect, long[] out) {
        long max = -1;
        long min = Long.MAX_VALUE;

        int linesCount = mLines.size();

        int startIndex = (int) Math.floor(rect.left * getPixelXToValue() * mInvScaleX);
        int endIndex = (int) Math.ceil(rect.right * getPixelXToValue() * mInvScaleX) + 1;

        endIndex = Math.min(endIndex, mDataSet.getWidth());


        for (int i = 0; i < linesCount; i++) {
            Line line = mLines.get(i);
            for (int j = startIndex; j < endIndex; j++) {
                LinePoint point = line.points.get(j);
                max = Math.max(max, point.plotData.value.longValue());
                min = Math.min(min, point.plotData.value.longValue());
            }
        }

        out[MIN] = min;
        out[MAX] = max;
    }

    private void getIndicesInRect(RectF rect, int[] indices) {
        int startIndex = (int) Math.floor(rect.left * getPixelXToValue() * mInvScaleX);
        int endIndex = (int) Math.ceil(rect.right * getPixelXToValue() * mInvScaleX) + 1;

        startIndex = Math.min(Math.max(0, startIndex), startIndex);
        endIndex = Math.min(endIndex, mDataSet.getWidth());

        indices[0] = startIndex;
        indices[1] = endIndex;
    }

    boolean endAnimation;
    private void animateCurrentMax(long newMax) {
        //TODO: Work on target max instead. Create separate animation delegate
        //TODO: Put grid rendering into PlottingEngine
        mTargetMax = newMax;
        if (mTimeAnimator == null) {
            mTimeAnimator = new TimeAnimator();
            mTimeAnimator.setTimeListener((animation, totalTime, deltaTime) -> {
                if (mCurrentMax != mTargetMax) {
                    long value = mTargetMax - mCurrentMax;
                    long toAdd = (long) (value * (deltaTime * .01f));
                    if (toAdd == 0 && deltaTime != 0) {
                        mCurrentMax = mTargetMax;
                    } else {
                        mCurrentMax += toAdd;
                    }
                    //Log.d("ENGINE", "mTargetMax " + mTargetMax);
                    //Log.d("ENGINE", "mCurrentMax " + mCurrentMax);
                    requestDraw();
                } else {
                    if (endAnimation) {
                        mTimeAnimator.cancel();
                    }
                }



            });
            mTimeAnimator.start();
        } else {
            if (!mTimeAnimator.isRunning()) {
                mTimeAnimator.start();
            }
        }
    }

    private boolean isNewMinMax(long[] maxMin) {
        return maxMin[MIN] != mCurrentMin || maxMin[MAX] != mTargetMax;
    }

    private void setNewMinMax(long[] maxMin, boolean animate) {
        long min = maxMin[MIN];
        long max = maxMin[MAX];
        mNiceNum.setMinMaxPoints(min, max);
        if (!animate) {
            mCurrentMin = min;
            mCurrentMax = max;
            mTargetMax = max;
        } else {
            mCurrentMin = min;
            animateCurrentMax(max);
        }
    }

    public void stopPan() {
        endAnimation = true;
    }

    public interface Drawer {
        void onDrawRequested();
    }

    interface MinMaxChangesListener {
        void onMinMaxChanged(long[] minMax);
    }

    private class Line {
        private String id;
        private int color;
        private List<LinePoint> points;
    }

    private class LinePoint {
        private PlotData plotData;
        private int x;
        private int y;
    }
}
