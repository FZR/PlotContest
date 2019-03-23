package com.josfzr.plotcontest.plotter.engine;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.josfzr.plotcontest.R;
import com.josfzr.plotcontest.plotter.engine.data.Line;
import com.josfzr.plotcontest.plotter.engine.data.PlotEngineDataHandler;
import com.josfzr.plotcontest.plotter.engine.data.XAxis;
import com.josfzr.plotcontest.themes.AppTheme;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

class XAxisDrawable implements PlotDrawable {
    private static final float MAX_ON_SCREEN_DATE_COUNT = 8f;

    private PlottingEngine mEngine;
    private float mXAxisTextSize, mTextMargin;
    private XAxis mLine;
    private Paint mXAxisTextPaint;
    private Rect mViewRect;

    private XLabel[] mLabels;

    private final int mMaxCompleteSets;
    private int mPower;

    private float mMinSpan;

    public XAxisDrawable(@NonNull Context context,
                         @NonNull PlottingEngine engine) {
        this.mEngine = engine;
        Resources r = context.getResources();
        mXAxisTextSize = r.getDimension(R.dimen.plot_x_axis_text_size);
        mXAxisTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mXAxisTextPaint.setTextSize(mXAxisTextSize);
        mXAxisTextPaint.setColor(ContextCompat.getColor(context, R.color.day_plot_text_color));

        mTextMargin = r.getDimension(R.dimen.plot_x_axis_text_margin_top);

        double sets = ((MAX_ON_SCREEN_DATE_COUNT * Math.pow(2, PlottingEngine.MAX_SCALE_X - 1))
                    - MAX_ON_SCREEN_DATE_COUNT)
                / MAX_ON_SCREEN_DATE_COUNT;
        mMaxCompleteSets = (int) Math.ceil(sets);
    }

    @Override
    public void draw(Canvas canvas, int recommendedStart, int recommendedEnd) {
        canvas.save();
        canvas.translate(-mEngine.getViewport().left, 0);

        int start = Math.max(recommendedStart - 1, 0);


        double rawAdditionalSets = ((MAX_ON_SCREEN_DATE_COUNT * Math.pow(2, mEngine.getScaleX() - 1)) - MAX_ON_SCREEN_DATE_COUNT) / MAX_ON_SCREEN_DATE_COUNT;
        int additionalCompleteSets = (int) Math.min(Math.ceil(rawAdditionalSets), mMaxCompleteSets);
        float scaleX = mEngine.getScaleX();
        float distribution;
        if (scaleX >= PlottingEngine.MAX_SCALE_X - 1f) {
            distribution = 1f;
        } else {
            distribution = (float) ((scaleX - Math.floor(scaleX)) / (Math.pow(2, Math.floor(scaleX - 1)) - Math.floor(scaleX - 1)));
        }

        float span = (float) (Math.pow(2, mPower + 1) / Math.pow(2, Math.min(Math.ceil(mEngine.getScaleX()), PlottingEngine.MAX_SCALE_X - 1)));

        int bestStart = closestNumber(start, (int) span);
        boolean b = false;
        for (int i = 0; i < recommendedEnd; i += (span)) {

            if (i < bestStart) {
                b = !b;
                continue;
            }

            XLabel label = mLabels[(int) (i / mMinSpan)];

            float x = label.mPoint.getX() * mEngine.getScaleX();
            float y = label.mPoint.getY();

            if (b && additionalCompleteSets > 0 && additionalCompleteSets - mMaxCompleteSets != 0) {
                mXAxisTextPaint.setAlpha((int) ((distribution) * 255));
                b = false;
            } else {
                mXAxisTextPaint.setAlpha(255);
                b = true;
            }

            canvas.drawText(label.mPoint.getText(), x, y, mXAxisTextPaint);
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
    public void setData(PlotEngineDataHandler dataHandler) {
        double max = MAX_ON_SCREEN_DATE_COUNT * Math.pow(2, PlottingEngine.MAX_SCALE_X - 1);
        mLabels = new XLabel[(int) Math.ceil(max)];

        mPower = (int) Math.ceil(Math.log(dataHandler.getXAxis().getPoints().size() / MAX_ON_SCREEN_DATE_COUNT) / Math.log(2));
        mMinSpan = (float) (Math.pow(2, mPower + 1) / Math.pow(2, Math.min(Math.ceil(PlottingEngine.MAX_SCALE_X), PlottingEngine.MAX_SCALE_X - 1)));

        XAxis xAxis = dataHandler.getXAxis();
        int size = xAxis.getPoints().size();

        int index = 0;
        for (int i = 0; i < size; i += mMinSpan) {
            Line.LinePoint point = xAxis.getPoints().get(i);
            point.setY((int) (mViewRect.height() + mViewRect.top - mTextMargin));
            XLabel xLabel = new XLabel();
            xLabel.mAlpha = 255;
            xLabel.mPoint = point;
            mLabels[index] = xLabel;
            index++;
        }
    }

    private int closestNumber(int number, int divider) {
        int quotient = number / divider;
        int firstPossibleNumber = divider * quotient;
        int secondPossibleNumber = (number * divider) > 0 ? (divider * (quotient + 1)) : (divider * (quotient - 1));
        return Math.abs(number - firstPossibleNumber) < Math.abs(divider - secondPossibleNumber) ? firstPossibleNumber : secondPossibleNumber;
    }

    @Override
    public void updateTheme(Context context, AppTheme appTheme) {
        mXAxisTextPaint.setColor(ContextCompat.getColor(context, appTheme.getPlotTextColor()));
    }

    public XAxis getLine() {
        return mLine;
    }

    static class XLabel {
        Line.LinePoint mPoint;
        int mAlpha = 0;
    }
}
