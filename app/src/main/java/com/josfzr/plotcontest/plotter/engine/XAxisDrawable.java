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
    private static final int ON_SCREEN_DATE_COUNT = 6;

    private PlottingEngine mEngine;
    private float mXAxisTextSize;
    private PlottingEngine.XAxis mLine;
    private Paint mXAxisTextPaint;
    private Rect mViewRect;
    private int mOverScroll;

    private int[] mStartEndIndices = new int[2];
    private XLabel[] mLabels;

    public XAxisDrawable(@NonNull Context context,
                         @NonNull PlottingEngine engine,
                         float axisTextSize) {
        this.mEngine = engine;
        mXAxisTextSize = axisTextSize;
        mXAxisTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mXAxisTextPaint.setTextSize(mXAxisTextSize);
        mXAxisTextPaint.setColor(Color.BLACK);
        Resources r = context.getResources();
        mOverScroll = r.getDimensionPixelSize(R.dimen.plot_over_scroll);
    }

    @Override
    public void draw(Canvas canvas) {
        mEngine.getIndicesForCurrentViewport(mStartEndIndices);
        canvas.save();
        canvas.translate(-mEngine.getViewport().left, 0);
        int start = Math.max(mStartEndIndices[0] - 1, 0);
        int end = mStartEndIndices[1];

        int span = (int) Math.ceil((mLine.points.size() / ON_SCREEN_DATE_COUNT) * mEngine.getScaleXInv());

        //span = closestNumber(span, 2);
        int bestStart = closestNumber(start, span);

        for (int i = bestStart; i < end; i += span) {
            PlottingEngine.LinePoint linePoint = mLine.points.get(i);

            //float width = i == bestStart ? 0 : mXAxisTextPaint.measureText(linePoint.text);

            //TODO: Try segmentation line approach

            float x = (linePoint.x * mEngine.getScaleX());
            float y = linePoint.y;
            canvas.drawText(linePoint.text, x, y, mXAxisTextPaint);
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

    public void onNewScale() {

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
                lp.y = (int) (mViewRect.height() + mViewRect.top);
                points.add(lp);
            }
        }
    }

    private int closestNumber(int number, int divider) {
        int quotient = number / divider;
        int firstPossibleNumber = divider * quotient;
        int secondPossibleNumber = (number * divider) > 0 ? (divider * (quotient + 1)) : (divider * (quotient - 1));
        return Math.abs(number - firstPossibleNumber) < Math.abs(divider - secondPossibleNumber) ? firstPossibleNumber : secondPossibleNumber;
    }

    float getTextSize() {
        return mXAxisTextPaint.measureText(mLine.points.get(0).text);
    }

    public PlottingEngine.XAxis getLine() {
        return mLine;
    }

    class XLabel {
        private String mText;
        private float mLeft, mRight;
        private long mValue;
        private int mAlpha = 0;
    }
}
