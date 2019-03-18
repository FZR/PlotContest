package com.josfzr.plotcontest.plotter.engine;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;

import com.josfzr.plotcontest.R;
import com.josfzr.plotcontest.data.DataSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

class GridDrawable implements PlotDrawable {
    private static final int DEFAULT_LINE_COUNT = 6;

    private Paint mLinesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private PlottingEngine mEngine;

    private Rect mPlotViewSize;
    private final int mSegmentationLineHeight;

    private int mTextMarginBottom, mXAxisTextSize;

    private SegmentationLine[] mSegmentationLines;
    private List<SegmentationLine> mLinesForAnimation;

    GridDrawable(@NonNull Context context,
                        @NonNull PlottingEngine mEngine,
                        int xAxisTextSize) {
        mXAxisTextSize = xAxisTextSize;

        Resources r = context.getResources();
        this.mEngine = mEngine;

        mSegmentationLineHeight = r.getDimensionPixelSize(R.dimen.segmentation_line_height);
        mTextMarginBottom = r.getDimensionPixelSize(R.dimen.plot_text_bottom_margin);

        int segmentationLineColor = ContextCompat.getColor(context, R.color.day_segmentation_line_color);
        mLinesPaint.setColor(segmentationLineColor);

        int textColor = ContextCompat.getColor(context, R.color.day_plot_text_color);
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(r.getDimension(R.dimen.plot_text_size));
    }

    @Override
    public void draw(Canvas canvas) {
        if (mSegmentationLines == null) return;

        drawSegmentationLines(canvas);
    }

    @Override
    public boolean animate(float delta) {
        float diff = 255 * delta * 1.2f;
        for (int i = mLinesForAnimation.size() - 1; i >= 0; i--) {
            SegmentationLine line = mLinesForAnimation.get(i);
            if (line.mAlpha == 0) {
                mLinesForAnimation.remove(i);
                continue;
            }

            line.mAlpha = (int) Math.max(line.mAlpha - diff, 0);
        }

        for (SegmentationLine line : mSegmentationLines) {
            line.mAlpha = (int) Math.min(line.mAlpha + diff, 255);
        }

        return mLinesForAnimation == null || mLinesForAnimation.size() == 0;
    }

    @Override
    public void onNewSize(Rect viewSize) {
        mPlotViewSize = new Rect(viewSize);
        mPlotViewSize.bottom = viewSize.bottom - mXAxisTextSize;
    }

    @Override
    public void onNewMinMax(long[] minMax) {
        addOldSegmentsForAnimation(mSegmentationLines);
        addSegments(false, false);
    }

    @Override
    public void setData(DataSet dataSet) {
        mSegmentationLines = new SegmentationLine[DEFAULT_LINE_COUNT];
        addSegments(true, true);
    }

    private void addOldSegmentsForAnimation(SegmentationLine[] segments) {
        if (mLinesForAnimation == null) mLinesForAnimation = new ArrayList<>();
        mLinesForAnimation.addAll(Arrays.asList(segments).subList(1, segments.length));
    }

    private void drawSegmentationLines(Canvas canvas) {
        for (SegmentationLine line : mSegmentationLines) {
            float top = mPlotViewSize.top
                    + mPlotViewSize.height()
                    - (line.mValue * mEngine.getValueToPixelY())
                    - mSegmentationLineHeight;
            line.mTop = top;
            line.mBottom = top + mSegmentationLineHeight;
            mTextPaint.setAlpha(line.mAlpha);
            drawSegmentationLine(canvas, line);
            canvas.save();
            canvas.translate(0, line.mTop - mTextMarginBottom);
            canvas.drawText(line.mText, 25f, 0f, mTextPaint);
            canvas.restore();
        }

        if (mLinesForAnimation == null) return;

        for (SegmentationLine line : mLinesForAnimation) {
            mTextPaint.setAlpha(line.mAlpha);
            drawSegmentationLine(canvas, line);
            canvas.save();
            canvas.translate(0, line.mTop - mTextMarginBottom);
            canvas.drawText(line.mText, 25f, 0f, mTextPaint);
            canvas.restore();
        }
    }

    private void drawSegmentationLine(Canvas canvas, SegmentationLine line) {
        mLinesPaint.setAlpha(line.mAlpha);
        canvas.drawLine(
                0,
                line.mTop,
                mPlotViewSize.width(),
                line.mBottom,
                mLinesPaint
        );
    }

    private void addSegments(boolean isOpaque,
                             boolean includeZero) {
        double max = mEngine.getNiceMax();
        int segment = (int) (max / (DEFAULT_LINE_COUNT - 1));

        for (int i = includeZero ? 0 : 1; i < mSegmentationLines.length; i++) {
            SegmentationLine sl = new SegmentationLine();
            sl.mText = String.valueOf(segment * (i));
            sl.mValue = segment * i;
            float top = mPlotViewSize.top
                    + mPlotViewSize.height()
                    - (sl.mValue * mEngine.getValueToPixelY())
                    - mSegmentationLineHeight;
            sl.mTop = top;
            sl.mBottom = top + mSegmentationLineHeight;
            sl.mAlpha = isOpaque ? 255 : 0;

            mSegmentationLines[i] = sl;
        }
    }

    private class SegmentationLine {
        private String mText;
        private float mTop, mBottom;
        private long mValue;
        private int mAlpha = 0;
    }
}
