package com.josfzr.plotcontest.plotter.engine;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;

import com.josfzr.plotcontest.R;
import com.josfzr.plotcontest.plotter.engine.data.Line;
import com.josfzr.plotcontest.plotter.engine.data.PlotEngineDataHandler;
import com.josfzr.plotcontest.plotter.engine.data.PlottingLine;
import com.josfzr.plotcontest.themes.AppTheme;

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
    private PlottingLine[] mAllLines;
    private List<PlottingLine> mCurrentLines;

    private int[] mAlphas;

    private float mLastScaleX = 1, mLastValueToY = 0;
    private int mLastStart, mLastEnd;

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
    public void draw(Canvas canvas, int recommendedStart, int recommendedEnd) {
        canvas.save();
        canvas.translate(-mEngine.getViewport().left, mRectToDraw.top + mRectToDraw.height());
        float valueToY = mEngine.getValueToPixelY(mRectToDraw);
        //canvas.scale(mEngine.getScaleX(), valueToY);

        for (PlottingLine line : mAllLines) {
            if (line.getAlpha() == 0) continue;
            Path p = mPaths.get(line.getId());

            if (p == null) {
                throw new NullPointerException("Path is NULL");
            }

            if (mLastStart != recommendedStart
                    || mLastEnd != recommendedEnd
                    || mLastValueToY != valueToY
                    || mLastScaleX != mEngine.getScaleX()) {

                for (int i = recommendedStart; i < recommendedEnd; i++) {
                    Line.LinePoint point = line.getPoints().get(i);
                    float x = point.getX() * mEngine.getScaleX();
                    float y = - (point.getY() * valueToY);

                    if (i == recommendedStart) {
                        p.reset();
                        p.moveTo(x, y);
                    } else {
                        p.lineTo(x, y);
                    }
                }
            }

            mPlotPaint.setColor(line.getColor());
            mPlotPaint.setAlpha(line.getAlpha());
            canvas.drawPath(p, mPlotPaint);
        }


        mLastStart = recommendedStart;
        mLastEnd = recommendedEnd;
        mLastValueToY = valueToY;

        canvas.restore();
    }

    @Override
    public boolean animate(float delta) {
        if (delta <= 0) return false;

        boolean willFinishAnimation = true;

        int index = 0;
        for (PlottingLine l : mAllLines) {
            boolean isOk = l.getAlpha() == mAlphas[index];
            willFinishAnimation = willFinishAnimation && isOk;
            if (isOk) {
                index++;
                continue;
            }
            float diff = 255 * delta * .9f;
            int targetAlpha = mAlphas[index];
            Log.d("Alphas", "target: " + targetAlpha + " current: " + l.getAlpha() + " diff: " + diff);
            if (targetAlpha == 0) {
                l.setAlpha((int) Math.max((l.getAlpha() - diff), 0));
            } else {
                l.setAlpha((int) Math.min((l.getAlpha() + diff), 255));
            }

            index++;
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
    public void setData(PlotEngineDataHandler dataHandler) {
        PlottingLine[] lines = dataHandler.getPlottingLines();

        mPaths = new HashMap<>(lines.length);
        mAllLines = lines;
        for (PlottingLine line : lines) {
            mPaths.put(line.getId(), new Path());
        }

        mCurrentLines = new ArrayList<>();
        mCurrentLines.addAll(Arrays.asList(mAllLines));
        mAlphas = new int[mAllLines.length];
        Arrays.fill(mAlphas, 255);
    }

    void hideLine(String id) {
        for (int i = 0; i < mAllLines.length; i++) {
            PlottingLine l = mAllLines[i];
            if (l.getId().equals(id)) {
                mCurrentLines.remove(l);
                mAlphas[i] = 0;
                break;
            }
        }
    }

    void showLine(String id) {
        for (int i = 0; i < mAllLines.length; i++) {
            PlottingLine l = mAllLines[i];
            if (l.getId().equals(id)) {
                mCurrentLines.add(l);
                mAlphas[i] = 255;
                break;
            }
        }
    }

    @Override
    public void updateTheme(Context context, AppTheme appTheme) {

    }

    List<PlottingLine> getCurrentLines() {
        return mCurrentLines;
    }

    PlottingLine[] getAllLines() {
        return mAllLines;
    }
}
