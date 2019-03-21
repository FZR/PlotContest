package com.josfzr.plotcontest.plotter.engine;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.josfzr.plotcontest.R;
import com.josfzr.plotcontest.data.Column;
import com.josfzr.plotcontest.data.DataSet;
import com.josfzr.plotcontest.data.PlotData;
import com.josfzr.plotcontest.utils.DateTimeUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;

class XAxisDrawable implements PlotDrawable {
    private static final float MIN_ON_SCREEN_DATE_COUNT = 3f;
    private static final float MAX_ON_SCREEN_DATE_COUNT = 6f;

    private PlottingEngine mEngine;
    private float mXAxisTextSize, mTextMargin;
    private PlottingEngine.XAxis mLine;
    private Paint mXAxisTextPaint;
    private Rect mViewRect;

    private int[] mStartEndIndices = new int[2];
    private XLabel[] mLabels;

    private float mBestOnScreen = MIN_ON_SCREEN_DATE_COUNT;

    public XAxisDrawable(@NonNull Context context,
                         @NonNull PlottingEngine engine) {
        this.mEngine = engine;
        Resources r = context.getResources();
        mXAxisTextSize = r.getDimension(R.dimen.plot_x_axis_text_size);
        mXAxisTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mXAxisTextPaint.setTextSize(mXAxisTextSize);
        mXAxisTextPaint.setColor(Color.BLACK);

        mTextMargin = r.getDimension(R.dimen.plot_x_axis_text_margin_top);
    }

    @Override
    public void draw(Canvas canvas) {
        mEngine.getIndicesForCurrentViewport(mStartEndIndices);
        canvas.save();
        canvas.translate(-mEngine.getViewport().left, 0);

        int start = Math.max(mStartEndIndices[0] - 1, 0);
        int end = mStartEndIndices[1];


        int maxCompleteSets = (int) Math.ceil(((mBestOnScreen * Math.pow(2, PlottingEngine.MAX_SCALE_X - 1)) - mBestOnScreen) / 6);
        double rawAdditionalSets = ((mBestOnScreen * Math.pow(2, mEngine.getScaleX() - 1)) - mBestOnScreen) / 6;
        int additionalCompleteSets = (int) Math.min(Math.ceil(rawAdditionalSets), maxCompleteSets);
        float scaleX = mEngine.getScaleX();
        float distribution = scaleX >= PlottingEngine.MAX_SCALE_X - 1f ? 1f : (float) ((scaleX - Math.floor(scaleX)) / (Math.pow(2, Math.floor(scaleX - 1)) - Math.floor(scaleX - 1)));

        int power = (int) Math.ceil(Math.log(mLabels.length / mBestOnScreen) / Math.log(2));
        float span = (float) (Math.pow(2, power + 1) / Math.pow(2, Math.min(Math.ceil(mEngine.getScaleX()), PlottingEngine.MAX_SCALE_X - 1)));

        int bestStart = closestNumber(start, (int) span);
        boolean b = false;
        for (int i = 0; i < end; i += (span)) {

            if (i < bestStart) {
                b = !b;
                continue;
            }

            XLabel label = mLabels[i];

            float x = label.mX * mEngine.getScaleX();
            float y = label.mY;

            if (b && additionalCompleteSets > 0 && additionalCompleteSets - maxCompleteSets != 0) {
                mXAxisTextPaint.setAlpha((int) ((distribution) * 255));
                b = false;
            } else {
                mXAxisTextPaint.setAlpha(255);
                b = true;
            }

            canvas.drawText(label.mText, x, y, mXAxisTextPaint);
        }

        canvas.restore();
    }

    @Override
    public boolean animate(float delta) {
        return true;
    }

    @Override
    public void onNewSize(Rect viewSize) {
        mViewRect = new Rect(viewSize);
    }

    @Override
    public void onNewMinMax(long[] minMax) {

    }

    @Override
    public void setData(DataSet dataSet) {
        int dataPointsCount = dataSet.getWidth();
        float offsetX = mEngine.getValueToPixelX();
        DateFormat dateFormat = DateTimeUtils.getDataPointAxisDateFormat();


        for (Column column : dataSet.getColumns()) {
            if (column.getType() != Column.TYPE_X) continue;
            List<PlottingEngine.LinePoint> points = new ArrayList<>(dataPointsCount);
            mLine = new PlottingEngine.XAxis();
            mLine.points = points;

            for (int i = 0; i < dataPointsCount; i++) {
                PlotData dataPoint = column.getValues().get(i);
                PlottingEngine.LinePoint lp = new PlottingEngine.LinePoint();
                lp.plotData = dataPoint;
                lp.text = dateFormat.format(new Date(lp.plotData.value.longValue()));
                lp.x = (int) (i * offsetX);
                lp.y = (int) (mViewRect.height() + mViewRect.top - mTextMargin);
                points.add(lp);
            }
        }

        mLabels = new XLabel[mLine.points.size()];

        for (int i = 7; i >= 0; i--) {
            double res = Math.pow(2, i);
            double onScreenDates = (mLabels.length / res);
            if (onScreenDates >= MIN_ON_SCREEN_DATE_COUNT
                    && onScreenDates <= MAX_ON_SCREEN_DATE_COUNT) {
                mBestOnScreen = (float) Math.ceil(onScreenDates);
                break;
            }
        }

        for (int i = 0; i < mLabels.length; i++) {
            PlottingEngine.LinePoint linePoint = mLine.points.get(i);

            float x = (linePoint.x);
            float y = linePoint.y;

            XLabel label = new XLabel();
            label.mX = x;
            label.mY = y;
            label.mAlpha = 255;
            label.mText = linePoint.text;
            mLabels[i] = label;
        }
    }

    private int closestNumber(int number, int divider) {
        int quotient = number / divider;
        int firstPossibleNumber = divider * quotient;
        int secondPossibleNumber = (number * divider) > 0 ? (divider * (quotient + 1)) : (divider * (quotient - 1));
        return Math.abs(number - firstPossibleNumber) < Math.abs(divider - secondPossibleNumber) ? firstPossibleNumber : secondPossibleNumber;
    }

    private float measureText(String text) {
        return mXAxisTextPaint.measureText(text);
    }

    public PlottingEngine.XAxis getLine() {
        return mLine;
    }

    class XLabel {
        private String mText;
        private float mX, mY;
        private long mValue;
        private int mAlpha = 0;
    }
}
