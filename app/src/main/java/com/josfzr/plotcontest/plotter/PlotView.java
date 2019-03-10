package com.josfzr.plotcontest.plotter;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Scroller;

import com.josfzr.plotcontest.R;
import com.josfzr.plotcontest.data.DataSet;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class PlotView extends View implements PlottingEngine.Drawer,
        MiniPlotViewContainer.PanListener, PlottingEngine.MinMaxChangesListener {

    private static final int DEFAULT_LINE_COUNT = 6;

    private Point mCurrentViewSize, mPlotViewSize;
    private final int mSegmentationLineHeight, mPlotBottomMargin;

    private Paint mLinesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private SegmentationLine[] mSegmentationLines;

    private int mTextMarginBottom;

    private float mVisibleRatio = .2f;

    private float xScale = 1f;
    private float yScale = 1f;

    private Matrix mLegendMatrix;

    private float mLastTranslateX = 0f;

    private PlottingEngine mPlottingEngine;

    private boolean mShowAll, mIsPanning;

    public PlotView(Context context) {
        this(context, null, 0);
    }

    public PlotView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlotView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mShowAll = false;

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.PlotView,
                    0, 0);

            try {
                mShowAll = a.getBoolean(R.styleable.PlotView_showAll, false);
            } finally {
                a.recycle();
            }
        }

        mPlottingEngine = new PlottingEngine(context, this, mShowAll);
        mPlottingEngine.setMinMaxChangeListener(this);

        mLegendMatrix = new Matrix(getMatrix());

        Resources r = context.getResources();
        mPlotBottomMargin = r.getDimensionPixelSize(R.dimen.plot_bottom_margin);
        mSegmentationLineHeight = r.getDimensionPixelSize(R.dimen.segmentation_line_height);
        mTextMarginBottom = r.getDimensionPixelSize(R.dimen.plot_text_bottom_margin);

        int segmentationLineColor = ContextCompat.getColor(context, R.color.day_segmentation_line_color);
        mLinesPaint.setColor(segmentationLineColor);

        int textColor = ContextCompat.getColor(context, R.color.day_plot_text_color);
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(r.getDimension(R.dimen.plot_text_size));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPlottingEngine.onNewSizeAvailable(w, h);

        mCurrentViewSize = mPlottingEngine.getVisibleViewPortSize();
        mPlotViewSize = mPlottingEngine.getPlottingViewPortSize();
    }

    public void setDataSet(DataSet dataSet) {
        mPlottingEngine.setData(dataSet, false);

        mSegmentationLines = new SegmentationLine[DEFAULT_LINE_COUNT];
        double max = mPlottingEngine.getNiceMax();
        int segment = (int) (max / (DEFAULT_LINE_COUNT - 1));

        int offsetY = mPlotViewSize.y / (DEFAULT_LINE_COUNT);

        for (int i = 0; i < mSegmentationLines.length; i++) {
            SegmentationLine sl = new SegmentationLine();
            sl.text = String.valueOf(segment * (i));
            float top = mPlotViewSize.y - (offsetY * (i)) - mSegmentationLineHeight;
            sl.mRect = new RectF(
                    0,
                    top,
                    mPlotViewSize.x,
                    top + mSegmentationLineHeight

            );
            mSegmentationLines[i] = sl;
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mShowAll) drawSegmentationLines(canvas);
        if (!mShowAll) drawDateTexts(canvas);
        mPlottingEngine.onDraw(canvas);

        if (mIsPanning) invalidate();
    }

    private void drawDateTexts(Canvas canvas) {

    }

    private void drawSegmentationLines(Canvas canvas) {
        for (SegmentationLine line : mSegmentationLines) {
            drawSegmentationLine(canvas, line);
            canvas.save();
            canvas.translate(0, line.mRect.top - mTextMarginBottom);
            canvas.drawText(line.text, 25f, 0f, mTextPaint);
            canvas.restore();
        }
    }

    private void drawSegmentationLine(Canvas canvas, SegmentationLine line) {
        canvas.drawLine(
                line.mRect.left,
                line.mRect.top,
                line.mRect.right,
                line.mRect.bottom,
                mLinesPaint
        );
    }

    @Override
    public void onPanned(float dx, float dy) {
        mIsPanning = true;
        mPlottingEngine.pan(dx, dy);
    }

    @Override
    public void onPanStop() {
        mPlottingEngine.stopPan();
        mIsPanning = false;
    }

    @Override
    public void onDrawRequested() {
        invalidate();
    }

    @Override
    public void onMinMaxChanged(long[] minMax) {

    }

    private class SegmentationLine {
        private String text;
        private RectF mRect;
    }
}
