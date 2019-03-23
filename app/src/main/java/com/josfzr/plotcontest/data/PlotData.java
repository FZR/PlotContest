package com.josfzr.plotcontest.data;

public class PlotData {
    private int x;
    private int y;
    public final long value;

    public PlotData(long value) {
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
