package com.josfzr.plotcontest.plotter.engine;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.josfzr.plotcontest.data.DataSet;

public interface PlotDrawable {
    void draw(Canvas canvas);

    /**
     * @param delta - delta between frames
     * @return {@link Boolean#TRUE} if drawable did animate. {@link Boolean#FALSE} if it didn't animate
     */
    boolean animate(float delta);
    void onNewSize(Rect viewSize);
    void onNewMinMax(long[] minMax);
    void setData(DataSet dataSet);
}
