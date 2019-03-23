package com.josfzr.plotcontest;

import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.josfzr.plotcontest.data.DataSet;
import com.josfzr.plotcontest.plotter.PopupItemView;
import com.josfzr.plotcontest.plotter.engine.data.CursorData;
import com.josfzr.plotcontest.themes.AppTheme;
import com.josfzr.plotcontest.themes.AppThemes;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements PopupController {

    private PopupWindow mPopup;
    private TextView mDateTextInPopup;
    private GridLayout mGridLayout;

    private Toolbar mToolbar;
    private LinearLayout mGraphs;
    private LinearLayout mRoot;
    private boolean mIsDark = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mRoot = findViewById(R.id.root);
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.findViewById(R.id.night_mode_switch).setOnClickListener(v -> {
            applyAppTheme(mIsDark ? AppThemes.LIGHT_THEME : AppThemes.DARK_THEME);
            mIsDark = !mIsDark;
        });
        //GraphLayoutView graphView = findViewById(R.id.graph_view);
        setSupportActionBar(mToolbar);

        List<DataSet> dataSets = TestDataProvider.provideDataSets(getAssets());

        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup, null);
        mDateTextInPopup = popupView.findViewById(R.id.date_text);
        mGridLayout = popupView.findViewById(R.id.grid);
        int marginStart = getResources().getDimensionPixelSize(R.dimen.popup_item_margin_start);

        for (int i = 0; i < 4; i++) {
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.setGravity(Gravity.CENTER);
            lp.setMarginStart(i % 2 != 0 ? marginStart : 0);
            lp.topMargin = marginStart;

            PopupItemView item = new PopupItemView(this);

            mGridLayout.addView(item, lp);
        }

        mPopup = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        mGraphs = findViewById(R.id.graphs);
        int count = dataSets.size();
        ScrollView scrollView = findViewById(R.id.scroll_view);
        scrollView.requestDisallowInterceptTouchEvent(true);
        mGraphs.post(() -> {
            for (int i = 0; i < count; i++) {
                GraphLayoutView v = (GraphLayoutView) mGraphs.getChildAt(i);
                v.setPopupController(this);
                v.setData(dataSets.get(i), mIsDark ? AppThemes.DARK_THEME : AppThemes.LIGHT_THEME);
            }
        });
    }

    private void applyAppTheme(AppTheme appTheme) {
        int count = mGraphs.getChildCount();
        int textColor = ContextCompat.getColor(this, appTheme.getGlobalTextsColor());
        mDateTextInPopup.setTextColor(textColor);
        mPopup.getContentView().setBackgroundResource(appTheme.getPopupBackground());
        mGraphs.setBackgroundResource(appTheme.getScrollBgColor());
        mRoot.setBackgroundResource(appTheme.getBgColor());
        mToolbar.setBackgroundResource(appTheme.getPrimaryColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, appTheme.getPrimaryDarkColor()));
        }
        for (int i = 0; i < count; i++) {
            GraphLayoutView v = (GraphLayoutView) mGraphs.getChildAt(i);
            v.setAppTheme(appTheme);
        }
    }

    @Override
    public void showPopupWithDataAt(View view, float x, float y, CursorData cursorData) {
        int count = mGridLayout.getChildCount();
        mDateTextInPopup.setText(cursorData.getXValue());
        for (int i = 0; i < count; i++) {
            PopupItemView v = (PopupItemView) mGridLayout.getChildAt(i);

            if (i < cursorData.getCursorPoints().length && cursorData.getCursorPoints()[i].getAlpha() > 0) {
                CursorData.CursorPoint point = cursorData.getCursorPoints()[i];
                v.setNameValueAndColor(
                        point.getName(),
                        String.valueOf(point.getValue()),
                        point.getColor()
                );
                v.setVisibility(View.VISIBLE);
            } else {
                v.setVisibility(View.GONE);
            }
        }

        if (mPopup.isShowing()) {
            mPopup.update(view, (int) x, (int) y, -2, -2);
        } else {
            mPopup.showAsDropDown(view, (int) x, (int) y);
        }
    }

    @Override
    public void hidePopup() {
        mPopup.dismiss();
    }
}
