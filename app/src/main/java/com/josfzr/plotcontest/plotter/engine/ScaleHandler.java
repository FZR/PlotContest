package com.josfzr.plotcontest.plotter.engine;

class ScaleHandler {
    private float mScaleX, mScaleY;
    private float mScaleXInv, mScaleYInv;

    ScaleHandler(float scaleX, float scaleY) {
        setScaleX(scaleX);
        setScaleY(scaleY);
    }

    float getScaleX() {
        return mScaleX;
    }

    float getScaleY() {
        return mScaleY;
    }

    float getScaleXInv() {
        return mScaleXInv;
    }

    float getScaleYInv() {
        return mScaleYInv;
    }

    void setScaleX(float scaleX) {
        this.mScaleX = scaleX;
        this.mScaleXInv = 1f / scaleX;
    }

    void setScaleY(float scaleY) {
        this.mScaleY = scaleY;
        this.mScaleYInv = 1f / scaleY;
    }
}
