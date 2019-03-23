package com.josfzr.plotcontest;

import android.view.View;

import com.josfzr.plotcontest.plotter.engine.data.CursorData;

public interface PopupController {
    void showPopupWithDataAt(View v, float x, float y, CursorData cursorData);
    void hidePopup();
}
