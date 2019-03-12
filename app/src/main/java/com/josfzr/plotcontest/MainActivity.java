package com.josfzr.plotcontest;

import android.os.Bundle;

import com.josfzr.plotcontest.data.DataSet;
import com.josfzr.plotcontest.plotter.MiniPlotViewContainer;
import com.josfzr.plotcontest.plotter.PlotView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.LightTheme);
        setContentView(R.layout.activity_main);

        PlotView plotView = findViewById(R.id.plot_view);
        MiniPlotViewContainer container = findViewById(R.id.mini_plot_container);
        container.setPanListener(plotView);
        plotView.post(() -> {
            /*List<DataSet_Old> dataSets = new ArrayList<>();
            dataSets.add(TestDataProvider.provideTestData());
            dataSets.add(TestDataProvider.provideTestData());*/
            DataSet dataSet = TestDataProvider.provideDataSet(getAssets(), 1);
            //dataSets.add(TestDataProvider.provideTestData());
            //dataSets.add(TestDataProvider.provideTestData());
            plotView.setDataSet(dataSet);
            PlotView mini = findViewById(R.id.plot_view_mini);
            mini.setDataSet(dataSet);
        });
    }
}
