package com.josfzr.plotcontest.plotter;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.josfzr.plotcontest.R;

import androidx.annotation.Nullable;

public class PopupItemView extends LinearLayout {

    private TextView mValue;
    private TextView mName;

    public PopupItemView(Context context) {
        this(context, null);
    }

    public PopupItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PopupItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.popup_item_data, this);

        mValue = findViewById(R.id.number);
        mName = findViewById(R.id.name);

        setOrientation(VERTICAL);
    }

    public void setNameValueAndColor(String name, String value, int color) {
        mName.setText(name);
        mValue.setText(value);

        mValue.setTextColor(color);
        mName.setTextColor(color);
    }
}
