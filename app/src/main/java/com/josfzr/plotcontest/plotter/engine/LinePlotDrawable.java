package com.josfzr.plotcontest.plotter.engine;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import com.josfzr.plotcontest.data.Column;
import com.josfzr.plotcontest.data.DataSet;
import com.josfzr.plotcontest.data.LineData;
import com.josfzr.plotcontest.data.PlotData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;

class LinePlotDrawable implements PlotDrawable {
    private PlottingEngine mEngine;
    private Rect mRectToDraw;
    private float mXAxisTextHeight;
    private Paint mPlotPaint;

    private HashMap<String, Path> mPaths;
    private List<PlottingEngine.Line> mLines;

    private DataSet mDataSet;
    private int[] mStartEndIndices = new int[2];

    public LinePlotDrawable(@NonNull Context context,
                            @NonNull PlottingEngine mEngine,
                            float strokeWidth,
                            float xAxisTextHeight) {
        this.mEngine = mEngine;
        mXAxisTextHeight = xAxisTextHeight;

        Resources r = context.getResources();
        mPlotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPlotPaint.setStrokeWidth(strokeWidth);
        mPlotPaint.setPathEffect(null);
        mPlotPaint.setStrokeJoin(Paint.Join.ROUND);
        mPlotPaint.setStrokeCap(Paint.Cap.ROUND);
        mPlotPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void draw(Canvas canvas) {
        mEngine.getIndicesForCurrentViewport(mStartEndIndices);
        canvas.save();
        canvas.translate(-mEngine.getViewport().left, 0);

        for (int i = mStartEndIndices[0]; i < mStartEndIndices[1]; i++) {
            for (PlottingEngine.Line line : mLines) {
                PlottingEngine.LinePoint point = line.points.get(i);
                Path p = mPaths.get(line.id);

                if (p == null) {
                    throw new NullPointerException("Path is NULL");
                }

                float x = point.x * mEngine.getScaleX();
                float y = mRectToDraw.top
                        + mRectToDraw.height()
                        - (point.y * mEngine.getValueToPixelY());

                if (i == mStartEndIndices[0]) {
                    p.reset();
                    p.moveTo(x, y);
                } else {
                    p.lineTo(x, y);
                }

                if (i == mStartEndIndices[1] - 1) {
                    mPlotPaint.setColor(line.color);
                    canvas.drawPath(p, mPlotPaint);
                }
            }
        }

        canvas.restore();
    }

    @Override
    public boolean animate(float delta) {
        return false;
    }

    @Override
    public void onNewSize(Rect viewSize) {
        mRectToDraw = new Rect(viewSize);
        mRectToDraw.bottom = (int) (viewSize.bottom - mXAxisTextHeight);
    }

    @Override
    public void onNewMinMax(long[] minMax) {

    }

    @Override
    public void setData(DataSet dataSet) {
        mDataSet = dataSet;
        int dataPointsCount = mDataSet.getWidth();
        float offsetX = mEngine.getValueToPixelX();

        mPaths = new HashMap<>(mDataSet.getColumns().length - 1);
        mLines = new ArrayList<>(mDataSet.getColumns().length - 1);

        for (Column column : mDataSet.getColumns()) {
            if (column.getType() == Column.TYPE_X) continue;

            List<PlottingEngine.LinePoint> points = new ArrayList<>(dataPointsCount);
            mPaths.put(column.getId(), new Path());
            PlottingEngine.Line line = new PlottingEngine.Line();
            line.id = column.getId();
            line.color = ((LineData) column).getColor();
            line.points = points;
            mLines.add(line);

            for (int i = 0; i < dataPointsCount; i++) {
                PlotData dataPoint = column.getValues().get(i);
                PlottingEngine.LinePoint lp = new PlottingEngine.LinePoint();
                lp.plotData = dataPoint;
                lp.x = (int) (i * offsetX);
                lp.y = (int) (dataPoint.value.longValue());
                points.add(lp);
            }
        }
    }

    List<PlottingEngine.Line> getLines() {
        return mLines;
    }
}
