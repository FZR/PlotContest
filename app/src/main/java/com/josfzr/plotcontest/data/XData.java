package com.josfzr.plotcontest.data;

import java.util.ArrayList;
import java.util.List;

public class XData extends Column {
    private List<PlotData> mValues;

    public XData(int type, String id, int size) {
        super(type, id);
        mValues = new ArrayList<>(size);
    }

    public void addValue(long value) {
        mValues.add(new PlotData(value));
    }

    @Override
    public List<PlotData> getValues() {
        return mValues;
    }
}
