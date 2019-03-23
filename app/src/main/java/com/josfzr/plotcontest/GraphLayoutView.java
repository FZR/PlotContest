package com.josfzr.plotcontest;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.josfzr.plotcontest.data.Column;
import com.josfzr.plotcontest.data.DataSet;
import com.josfzr.plotcontest.data.LineData;
import com.josfzr.plotcontest.plotter.MiniPlotView;
import com.josfzr.plotcontest.plotter.PlotView;
import com.josfzr.plotcontest.themes.AppTheme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;
import androidx.core.widget.CompoundButtonCompat;

public class GraphLayoutView extends LinearLayout {

    private MiniPlotView mMiniPlotView;
    private PlotView mPlotView;
    private LinearLayout mCheckBoxContainer;

    private GestureDetector mGesturDetecor;
    private PopupController mPopupController;

    public GraphLayoutView(Context context) {
        this(context, null);
    }

    public GraphLayoutView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GraphLayoutView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);

        inflate(context, R.layout.graph_layout, this);
        mMiniPlotView = findViewById(R.id.mini_plot_view);
        mPlotView = findViewById(R.id.plot_view);
        mCheckBoxContainer = findViewById(R.id.container);

        mMiniPlotView.setPanListener(mPlotView);

        mGesturDetecor = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    requestDisallowInterceptTouchEvent(true);
                } else {
                    requestDisallowInterceptTouchEvent(false);
                    mPopupController.hidePopup();
                }
                return false;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mGesturDetecor.onTouchEvent(ev);
        return false;
    }

    public void setPopupController(PopupController controller) {
        mPopupController = controller;
        mPlotView.setPopupController(controller);
    }

    public void setData(@NonNull DataSet dataSet, @NonNull AppTheme appTheme) {
        mPlotView.setDataSet(dataSet);
        mMiniPlotView.setDataSet(dataSet);
        Column[] columns = dataSet.getColumns();
        mCheckBoxContainer.removeAllViews();
        int padding = getContext().getResources().getDimensionPixelSize(R.dimen.checkbox_text_padding);
        int textSize = getContext().getResources().getDimensionPixelSize(R.dimen.checkbox_text_size);
        int lineHeight = getContext().getResources().getDimensionPixelSize(R.dimen.segmentation_line_height);
        int realIndex = 0;

        for (int i = 0; i < columns.length; i++) {
            if (columns[i].getType() == Column.TYPE_X) {
                continue;
            }

            LineData column = (LineData) columns[i];

            int color = column.getColor();
            String name = column.getName();
            AppCompatCheckBox checkBox = new AppCompatCheckBox(getContext());

            checkBox.setText(name);
            checkBox.setChecked(true);
            checkBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    mPlotView.onRestoreLine(column.getId());
                    mMiniPlotView.onRestoreLine(column.getId());
                } else {
                    mPlotView.onRemoveLine(column.getId());
                    mMiniPlotView.onRemoveLine(column.getId());
                }
            });
            checkBox.setPaddingRelative(
                    padding, padding, getPaddingEnd(), padding
            );
            CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(color));
            checkBox.setTag("cb");
            mCheckBoxContainer.addView(checkBox);
            if (realIndex < columns.length - 2) {
                View v = new View(getContext());
                v.setTag("div");
                v.setBackgroundResource(appTheme.getSegmentLinesColor());
                LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        lineHeight);
                lp.setMarginStart(checkBox.getCompoundPaddingStart());
                mCheckBoxContainer.addView(v, lp);
            }

            realIndex++;
        }
    }

    public void setAppTheme(AppTheme appTheme) {
        int color = ContextCompat.getColor(getContext(), appTheme.getBgColor());
        setBackgroundColor(color);

        int count = mCheckBoxContainer.getChildCount();
        int textColor = ContextCompat.getColor(getContext(), appTheme.getGlobalTextsColor());
        for (int i = 0; i < count; i++) {
            View v = mCheckBoxContainer.getChildAt(i);
            if ("cb".equals(v.getTag())) {
                AppCompatCheckBox cb = (AppCompatCheckBox) mCheckBoxContainer.getChildAt(i);
                cb.setTextColor(textColor);
            } else if ("div".equals(v.getTag())) {
                v.setBackgroundResource(appTheme.getSegmentLinesColor());
            }
        }

        mPlotView.setAppTheme(appTheme);
        mMiniPlotView.setAppTheme(appTheme);
    }
}
