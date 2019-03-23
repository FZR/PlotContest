package com.josfzr.plotcontest.plotter.engine.data;

public class CursorData {
    private CursorPoint[] mCursorPoints = null;
    private String mXValue;
    private float x;

    public String getXValue() {
        return mXValue;
    }

    public CursorPoint[] getCursorPoints() {
        return mCursorPoints;
    }

    public float getX() {
        return x;
    }

    public void setCursorPoints(CursorPoint[] mCursorPoints) {
        this.mCursorPoints = mCursorPoints;
    }

    public void setXValue(String mXValue) {
        this.mXValue = mXValue;
    }

    public void setX(float x) {
        this.x = x;
    }

    public static class CursorPoint {
        String mName;
        int mColor;
        long mValue;
        int y;
        int mAlpha = 255;
        String id;

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            this.mName = name;
        }

        public int getColor() {
            return mColor;
        }

        public void setColor(int color) {
            this.mColor = color;
        }

        public long getValue() {
            return mValue;
        }

        public void setValue(long value) {
            this.mValue = value;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getAlpha() {
            return mAlpha;
        }

        public void setAlpha(int alpha) {
            this.mAlpha = Math.min(Math.max(0, alpha), 255);
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
