package com.josfzr.plotcontest.utils;

public class NiceNum {

    private double mMinPoint;
    private double mMaxPoint;
    private double mMaxTicks = 50;
    private double mTickSpacing;
    private double mRange;
    private double mNiceMin;
    private double mNiceMax;

    /**
     * Instantiates a new instance of the NiceScale class.
     *
     * @param min the minimum data point on the axis
     * @param max the maximum data point on the axis
     */
    public NiceNum(double min, double max) {
        this.mMinPoint = min;
        this.mMaxPoint = max;
        calculate();
    }

    /**
     * Calculate and update values for tick spacing and nice
     * minimum and maximum data points on the axis.
     */
    private void calculate() {
        this.mRange = niceNum(mMaxPoint - mMinPoint, false);
        this.mTickSpacing = niceNum(mRange / (mMaxTicks - 1), false);
        this.mNiceMin =
                Math.floor(mMinPoint / mTickSpacing) * mTickSpacing;
        this.mNiceMax =
                Math.floor(mMaxPoint / mTickSpacing) * mTickSpacing;
    }

    /**
     * Returns a "nice" number approximately equal to mRange Rounds
     * the number if round = true Takes the ceiling if round = false.
     *
     * @param range the data mRange
     * @param round whether to round the result
     * @return a "nice" number to be used for the data mRange
     */
    private double niceNum(double range, boolean round) {
        double exponent; /** exponent of mRange */
        double fraction; /** fractional part of mRange */
        double niceFraction; /** nice, rounded fraction */

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

    /**
     * Sets the minimum and maximum data points for the axis.
     *
     * @param minPoint the minimum data point on the axis
     * @param maxPoint the maximum data point on the axis
     */
    public void setMinMaxPoints(double minPoint, double maxPoint) {
        this.mMinPoint = minPoint;
        this.mMaxPoint = maxPoint;
        calculate();
    }

    /**
     * Sets maximum number of tick marks we're comfortable with
     *
     * @param maxTicks the maximum number of tick marks for the axis
     */
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
