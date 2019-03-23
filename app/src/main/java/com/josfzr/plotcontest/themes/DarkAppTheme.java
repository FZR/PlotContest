package com.josfzr.plotcontest.themes;

import com.josfzr.plotcontest.R;

class DarkAppTheme extends LightAppTheme {
    @Override
    public int getBgColor() {
        return R.color.night_global_background;
    }

    @Override
    public int getSegmentLinesColor() {
        return R.color.night_segmentation_line_color;
    }

    @Override
    public int getPopupBackground() {
        return R.drawable.popup_dark;
    }

    @Override
    public int getPlotTextColor() {
        return R.color.night_plot_text_color;
    }

    @Override
    public int getMiniPlotFillColor() {
        return R.color.night_mini_plot_fill;
    }

    @Override
    public int getKnobHandlesColor() {
        return R.color.night_knob_handles_color;
    }

    @Override
    public int getCursorInnerCircleColor() {
        return R.color.night_cursor_inner_circle_color;
    }

    @Override
    public int getGlobalTextsColor() {
        return R.color.night_global_texts_color;
    }

    @Override
    public int getScrollBgColor() {
        return R.color.night_scroll_bg;
    }

    @Override
    public int getPrimaryColor() {
        return R.color.night_primary;
    }

    @Override
    public int getPrimaryDarkColor() {
        return R.color.night_primary_dark;
    }
}
