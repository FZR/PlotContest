package com.josfzr.plotcontest.data;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import androidx.annotation.IntDef;

public abstract class Column {

    public static final int TYPE_X = 0;
    public static final int TYPE_LINE = 1;

    @IntDef({TYPE_X, TYPE_LINE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ColumnType {}

    @ColumnType
    private int mType;

    private String id;

    public Column(@ColumnType int type, String id) {
        this.mType = type;
        this.id = id;
    }

    public abstract List<PlotData> getValues();

    @ColumnType
    public int getType() {
        return mType;
    }

    public void setType(@ColumnType int type) {
        mType = type;
    }

    public String getId() {
        return id;
    }
}
