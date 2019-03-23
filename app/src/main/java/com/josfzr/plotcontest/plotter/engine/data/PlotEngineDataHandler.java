package com.josfzr.plotcontest.plotter.engine.data;

import com.josfzr.plotcontest.data.Column;
import com.josfzr.plotcontest.data.DataSet;
import com.josfzr.plotcontest.data.LineData;
import com.josfzr.plotcontest.data.PlotData;
import com.josfzr.plotcontest.data.XData;
import com.josfzr.plotcontest.utils.DateTimeUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlotEngineDataHandler {

    private DataSet mDataSet;
    private PlottingLine[] mPlotLines;
    private XAxis mAXis;

    public void setDataSet(DataSet dataSet, float xScale) {
        mDataSet = dataSet;
        mPlotLines = new PlottingLine[dataSet.getColumns().length - 1];
        int dataWidth = dataSet.getWidth();
        int index = 0;

        for (Column rawColumn : dataSet.getColumns()) {
            if (rawColumn.getType() == Column.TYPE_X) {
                mAXis = new XAxis();
                createXLinePoints((XData) rawColumn, mAXis, dataWidth, xScale);
                continue;
            }

            LineData column = (LineData) rawColumn;

            PlottingLine line = new PlottingLine();
            line.setId(column.getId());
            line.setName(column.getName());
            line.setColor(column.getColor());

            createPlottingLinePoints(column, line, dataWidth, xScale);

            mPlotLines[index] = line;
            index++;
        }
    }

    public DataSet getRawDataSet() {
        return mDataSet;
    }

    public PlottingLine[] getPlottingLines() {
        return mPlotLines;
    }

    public XAxis getXAxis() {
        return mAXis;
    }

    private void createXLinePoints(XData xData,
                                   XAxis xAxis,
                                   int dataWidth,
                                   float xScale) {
        List<Line.LinePoint> points = new ArrayList<>(dataWidth);

        DateFormat textFormat = DateTimeUtils.getDataPointAxisDateFormat();
        DateFormat altTextFormat = DateTimeUtils.getDataPointLongDateFormat();

        for (int i = 0; i < dataWidth; i++) {
            PlotData rawData = xData.getValues().get(i);
            Line.LinePoint linePoint = new Line.LinePoint();
            linePoint.setPlotData(rawData);
            linePoint.setText(textFormat.format(new Date(rawData.value)));
            linePoint.setAltText(altTextFormat.format(new Date(rawData.value)));
            linePoint.setX((int) (i * xScale));
            linePoint.setY(-1);
            points.add(linePoint);
        }

        xAxis.setPoints(points);
    }

    private void createPlottingLinePoints(LineData lineData,
                                          PlottingLine line,
                                          int dataWidth,
                                          float xScale) {
        List<Line.LinePoint> points = new ArrayList<>(dataWidth);

        for (int i = 0; i < dataWidth; i++) {
            PlotData rawDataPoint = lineData.getValues().get(i);
            Line.LinePoint point = new Line.LinePoint();
            point.setPlotData(rawDataPoint);
            point.setX((int) (i * xScale));
            point.setY((int) rawDataPoint.value);
            points.add(point);
        }

        line.setPoints(points);
    }

}
