package com.josfzr.plotcontest.plotter.engine;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import com.josfzr.plotcontest.R;
import com.josfzr.plotcontest.data.Column;
import com.josfzr.plotcontest.data.DataSet;
import com.josfzr.plotcontest.data.LineData;
import com.josfzr.plotcontest.data.PlotData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;

class LinePlotDrawable implements PlotDrawable {
    private PlottingEngine mEngine;
    private Rect mRectToDraw;
    private float mXAxisTextHeight;
    private Paint mPlotPaint;

    private HashMap<String, Path> mPaths;
    private List<PlottingEngine.Line> mAllLines;
    private List<PlottingEngine.Line> mCurrentLines;

    private int[] mAlphas;

    private DataSet mDataSet;
    private int[] mStartEndIndices = new int[2];

    public LinePlotDrawable(@NonNull Context context,
                            @NonNull PlottingEngine mEngine,
                            float strokeWidth) {
        this.mEngine = mEngine;

        Resources r = context.getResources();
        mPlotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPlotPaint.setStrokeWidth(strokeWidth);
        mPlotPaint.setPathEffect(null);
        mPlotPaint.setStrokeJoin(Paint.Join.ROUND);
        mPlotPaint.setStrokeCap(Paint.Cap.ROUND);
        mPlotPaint.setStyle(Paint.Style.STROKE);

        mXAxisTextHeight = mEngine.isShowAll() ? 0 : (r.getDimension(R.dimen.plot_x_axis_text_size)
                + r.getDimension(R.dimen.plot_x_axis_text_margin_top));
    }

    @Override
    public void draw(Canvas canvas) {
        mEngine.getIndicesForCurrentViewport(mStartEndIndices);
        canvas.save();
        canvas.translate(-mEngine.getViewport().left, 0);


        for (PlottingEngine.Line line : mAllLines) {
            if (line.alpha == 0) continue;
            Path p = mPaths.get(line.id);

            if (p == null) {
                throw new NullPointerException("Path is NULL");
            }

            for (int i = mStartEndIndices[0]; i < mStartEndIndices[1]; i++) {
                PlottingEngine.LinePoint point = line.points.get(i);
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
            }

            mPlotPaint.setColor(line.color);
            mPlotPaint.setAlpha(line.alpha);
            canvas.drawPath(p, mPlotPaint);
        }

        canvas.restore();
    }

    @Override
    public boolean animate(float delta) {
        //if (mCurrentLines.size() == mAllLines.size()) return true;

        int linesSize = mAllLines.size();
        boolean willFinishAnimation = true;

        for (int i = 0; i < linesSize; i++) {
            boolean isOk = mAllLines.get(i).alpha == mAlphas[i];
            willFinishAnimation = willFinishAnimation && isOk;
            if (isOk) continue;
            float diff = 255 * delta * .6f;
            int targetAlpha = mAlphas[i];
            if (targetAlpha == 0) {
                mAllLines.get(i).alpha = (int) Math.max((mAllLines.get(i).alpha - diff), 0);
            } else {
                mAllLines.get(i).alpha = (int) Math.min((mAllLines.get(i).alpha + diff), 255);
            }

        }

        return willFinishAnimation;
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
        mAllLines = new ArrayList<>(mDataSet.getColumns().length - 1);

        for (Column column : mDataSet.getColumns()) {
            if (column.getType() == Column.TYPE_X) continue;

            List<PlottingEngine.LinePoint> points = new ArrayList<>(dataPointsCount);
            mPaths.put(column.getId(), new Path());
            PlottingEngine.Line line = new PlottingEngine.Line();
            line.id = column.getId();
            line.color = ((LineData) column).getColor();
            line.points = points;
            mAllLines.add(line);

            for (int i = 0; i < dataPointsCount; i++) {
                PlotData dataPoint = column.getValues().get(i);
                PlottingEngine.LinePoint lp = new PlottingEngine.LinePoint();
                lp.plotData = dataPoint;
                lp.x = (int) (i * offsetX);
                lp.y = (int) (dataPoint.value.longValue());
                points.add(lp);
            }
        }

        mCurrentLines = new ArrayList<>(mAllLines);
        mAlphas = new int[mAllLines.size()];
        Arrays.fill(mAlphas, 255);
    }

    void hideLine(String id) {
        for (int i = mAllLines.size() - 1; i >= 0; i--) {
            PlottingEngine.Line l = mAllLines.get(i);
            if (l.id.equals(id)) {
                mCurrentLines.remove(l);
                mAlphas[i] = 0;
                break;
            }
        }
    }

    void showLine(String id) {
        for (int i = 0; i < mAllLines.size(); i++) {
            PlottingEngine.Line l = mAllLines.get(i);
            if (l.id.equals(id)) {
                mCurrentLines.add(Math.min(i, mCurrentLines.size()), l);
                mAlphas[i] = 255;
                break;
            }
        }
    }

    List<PlottingEngine.Line> getCurrentLines() {
        return mCurrentLines;
    }

    List<PlottingEngine.Line> getAllLines() {
        return mAllLines;
    }
}
