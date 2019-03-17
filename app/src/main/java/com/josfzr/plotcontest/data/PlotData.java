package com.josfzr.plotcontest.data;

public class PlotData {
    private int x;
    private int y;
    public final Number value;

    public PlotData(Number value) {
        this.value = value;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
