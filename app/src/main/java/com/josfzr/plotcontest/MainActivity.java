package com.josfzr.plotcontest;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.josfzr.plotcontest.data.Column;
import com.josfzr.plotcontest.data.DataSet;
import com.josfzr.plotcontest.data.LineData;
import com.josfzr.plotcontest.plotter.MiniPlotView;
import com.josfzr.plotcontest.plotter.PlotView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.widget.CompoundButtonCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.LightTheme);
        setContentView(R.layout.activity_main);
        LinearLayout ll = findViewById(R.id.root);
        PlotView plotView = findViewById(R.id.plot_view);
        MiniPlotView miniPlotView = findViewById(R.id.mini_plot_view);
        miniPlotView.setPanListener(plotView);
        plotView.post(() -> {
            DataSet dataSet = TestDataProvider.provideDataSet(getAssets(), 1);
            plotView.setDataSet(dataSet);
            miniPlotView.setDataSet(dataSet);
            Column[] columns = dataSet.getColumns();
            for (int i = 0; i < columns.length; i++) {
                if (columns[i].getType() == Column.TYPE_X) continue;
                LineData column = (LineData) columns[i];


                int color = column.getColor();
                String name = column.getName();
                AppCompatCheckBox checkBox = new AppCompatCheckBox(this);
                checkBox.setText(name);
                checkBox.setChecked(true);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        plotView.onRestoreLine(column.getId());
                    } else {
                        plotView.onRemoveLine(column.getId());
                    }
                });
                CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(color));
                ll.addView(checkBox);
            }
        });
    }
}
