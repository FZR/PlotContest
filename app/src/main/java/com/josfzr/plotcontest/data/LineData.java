package com.josfzr.plotcontest.data;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class LineData extends Column {

    private List<PlotData> mValues;
    private String mName;
    private int mColor;

    private long _min = Long.MAX_VALUE, _max;

    public LineData(int type, String id, int size, String name, int color) {
        super(type, id);
        mValues = new ArrayList<>(size);
        mName = name;
        mColor = color;
    }

    public void addNumber(@NonNull Number number) {
        mValues.add(new PlotData(number));
        _max = Math.max(number.longValue(), _max);
        _min = Math.min(number.longValue(), _min);
    }

    public int getColor() {
        return mColor;
    }

    public String getName() {
        return mName;
    }

    public long getMin() {
        return _min;
    }

    public long getMax() {
        return _max;
    }

    @Override
    public List<PlotData> getValues() {
        return mValues;
    }
}
