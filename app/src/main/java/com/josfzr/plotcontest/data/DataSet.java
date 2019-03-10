package com.josfzr.plotcontest.data;

public class DataSet {

    private Column[] mColumns;
    private Column _xColumn;

    private long _min = Long.MAX_VALUE, _max;

    public DataSet(Column[] columns) {
        this.mColumns = columns;
        for (Column c : columns) {
            if (c.getType() == Column.TYPE_X) {
                _xColumn = c;
            } else {
                _min = Math.min(_min, ((LineData) c).getMin());
                _max = Math.max(_max, ((LineData) c).getMax());
            }
        }
    }

    public long getMin() {
        return _min;
    }

    public long getMax() {
        return _max;
    }

    public Column getXColumn() {
        return _xColumn;
    }

    public Column[] getColumns() {
        return mColumns;
    }

    public int getWidth() {
        return getXColumn().getValues().size();
    }
}
