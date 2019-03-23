package com.josfzr.plotcontest.plotter.engine.data;

import com.josfzr.plotcontest.data.PlotData;

import java.util.List;

public class Line {

    private List<LinePoint> points;

    public List<LinePoint> getPoints() {
        return points;
    }

    public void setPoints(List<LinePoint> points) {
        this.points = points;
    }

    public static class LinePoint {
        private PlotData mPlotData;
        private String mText, mAltText;
        private int mX;
        private int mY;

        public PlotData getPlotData() {
            return mPlotData;
        }

        public void setPlotData(PlotData plotData) {
            this.mPlotData = plotData;
        }

        public String getText() {
            return mText;
        }

        public void setText(String text) {
            this.mText = text;
        }

        public void setAltText(String altText) {
            mAltText = altText;
        }

        public String getAltText() {
            return mAltText;
        }

        public int getX() {
            return mX;
        }

        public void setX(int x) {
            this.mX = x;
        }

        public int getY() {
            return mY;
        }

        public void setY(int y) {
            this.mY = y;
        }
    }
}
