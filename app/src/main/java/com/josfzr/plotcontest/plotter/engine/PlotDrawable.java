package com.josfzr.plotcontest.plotter.engine;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.josfzr.plotcontest.data.DataSet;

public interface PlotDrawable {
    void draw(Canvas canvas);

    /**
     * @param delta - delta between frames
     * @return {@link Boolean#TRUE} if drawable finished animation or didn't animate.
     * {@link Boolean#FALSE} if it didn't finish animating
     */
    boolean animate(float delta);
    void onNewSize(Rect viewSize);
    void onNewMinMax(long[] minMax);
    void setData(DataSet dataSet);
}
