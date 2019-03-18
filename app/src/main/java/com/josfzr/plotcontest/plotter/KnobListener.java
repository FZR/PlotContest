package com.josfzr.plotcontest.plotter;

import android.graphics.RectF;

public interface KnobListener {
    void onPanned(RectF projection/*float dx, float dy*/);
    void onPanStop();
    void onScale(float x, float y);

    void updateProjection(RectF projection);
}
