package com.josfzr.plotcontest.plotter.engine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.josfzr.plotcontest.data.DataSet;
import com.josfzr.plotcontest.plotter.engine.data.PlotEngineDataHandler;
import com.josfzr.plotcontest.themes.AppTheme;

public interface PlotDrawable {
    void draw(Canvas canvas, int recommendedStart, int recommendedEnd);

    /**
     * @param delta - delta between frames
     * @return {@link Boolean#TRUE} if drawable finished animation or didn't animate.
     * {@link Boolean#FALSE} if it didn't finish animating
     */
    boolean animate(float delta);
    void onNewSize(Rect viewSize);
    void onNewMinMax(long[] minMax);
    void setData(PlotEngineDataHandler dataHandler);
    void updateTheme(Context context, AppTheme appTheme);
}
