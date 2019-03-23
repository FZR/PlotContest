package com.josfzr.plotcontest.utils;

public class NiceNum {

    private double mMinPoint;
    private double mMaxPoint;
    private double mMaxTicks = 50;
    private double mTickSpacing;
    private double mRange;
    private double mNiceMin;
    private double mNiceMax;

    public NiceNum(double min, double max) {
        this.mMinPoint = min;
        this.mMaxPoint = max;
        calculate();
    }

    private void calculate() {
        this.mRange = niceNum(mMaxPoint - mMinPoint, false);
        this.mTickSpacing = niceNum(mRange / (mMaxTicks - 1), false);
        this.mNiceMin =
                Math.floor(mMinPoint / mTickSpacing) * mTickSpacing;
        this.mNiceMax =
                Math.floor(mMaxPoint / mTickSpacing) * mTickSpacing;
    }

    private double niceNum(double range, boolean round) {
        double exponent;
        double fraction;
        double niceFraction;

        exponent = Math.floor(Math.log10(range));
        fraction = range / Math.pow(10, exponent);

        if (round) {
            if (fraction < 1.5)
                niceFraction = 1;
            else if (fraction < 3)
                niceFraction = 2;
            else if (fraction < 7)
                niceFraction = 5;
            else
                niceFraction = 10;
        } else {
            if (fraction <= 1)
                niceFraction = 1;
            else if (fraction <= 2)
                niceFraction = 2;
            else if (fraction <= 5)
                niceFraction = 5;
            else
                niceFraction = 10;
        }

        return niceFraction * Math.pow(10, exponent);
    }

    public void setMinMaxPoints(double minPoint, double maxPoint) {
        this.mMinPoint = minPoint;
        this.mMaxPoint = maxPoint;
        calculate();
    }

    public void setMaxTicks(double maxTicks) {
        this.mMaxTicks = maxTicks;
        calculate();
    }

    public double getNiceMin() {
        return mNiceMin;
    }

    public double getNiceMax() {
        return mNiceMax;
    }
}
