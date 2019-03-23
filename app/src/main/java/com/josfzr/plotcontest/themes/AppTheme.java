package com.josfzr.plotcontest.themes;

import com.josfzr.plotcontest.R;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

public class AppTheme {

    @ColorRes
    public int getPrimaryColor() {
        return R.color.day_primary;
    }

    @ColorRes
    public int getPrimaryDarkColor() {
        return R.color.day_primary_dark;
    }

    @ColorRes
    public int getBgColor() {
        return R.color.day_global_background;
    }

    @ColorRes
    public int getSegmentLinesColor() {
        return R.color.day_segmentation_line_color;
    }

    @DrawableRes
    public int getPopupBackground() {
        return R.drawable.popup_light;
    }

    @ColorRes
    public int getPlotTextColor() {
        return R.color.day_plot_text_color;
    }

    @ColorRes
    public int getMiniPlotFillColor() {
        return R.color.day_mini_plot_fill;
    }

    @ColorRes
    public int getKnobHandlesColor() {
        return R.color.day_knob_handles_color;
    }

    @ColorRes
    public int getCursorInnerCircleColor() {
        return R.color.day_cursor_inner_circle_color;
    }

    @ColorRes
    public int getGlobalTextsColor() {
        return R.color.day_global_texts_color;
    }

    @ColorRes
    public int getScrollBgColor() {
        return R.color.day_scroll_bg;
    }
}
