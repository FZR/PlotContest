package com.josfzr.plotcontest;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.util.Log;

import com.josfzr.plotcontest.data.Column;
import com.josfzr.plotcontest.data.DataSet;
import com.josfzr.plotcontest.data.LineData;
import com.josfzr.plotcontest.data.XData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class TestDataProvider {

    @Nullable
    public static List<DataSet> provideDataSets(AssetManager assetManager) {
        List<DataSet> dataSets = null;
        InputStream is = null;
        Column[] columns = null;

        try {
            is = assetManager.open("chart_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");

            JSONArray array = new JSONArray(json);

            int arrayLength = array.length();
            if (arrayLength == 0) return null;
            dataSets = new ArrayList<>();

            for (int i = 0; i < arrayLength; i++) {
                JSONObject object = array.getJSONObject(i);
                columns = createColumns(object);
                dataSets.add(new DataSet(columns));
            }

        } catch (Exception e) {
            Log.e("TEST_DATA_PROVIDER", "error loading data", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e("TEST_DATA_PROVIDER", "error closing stream", e);
                }
            }
        }

        return dataSets;
    }

    private static Column[] createColumns(JSONObject object) throws JSONException {

        JSONArray columnsArray = object.getJSONArray("columns");
        Column[] columns = new Column[columnsArray.length()];

        for (int i = 0; i < columnsArray.length(); i++) {
            JSONArray columnValues = columnsArray.getJSONArray(i);
            String id = columnValues.getString(0);
            String type = object.getJSONObject("types").getString(id);

            if (type.equals("x")) {
                XData xData = new XData(Column.TYPE_X, id, columnValues.length() - 1);
                for (int j = 1; j < columnValues.length(); j++) {
                    xData.addValue(columnValues.getLong(j));
                }
                columns[i] = xData;
            } else if (type.equals("line")) {
                String name = getName(object, id);
                int color = getColor(object, id);
                LineData lineData = new LineData(
                        Column.TYPE_LINE,
                        id,
                        columnValues.length() - 1,
                        name,
                        color
                );

                for (int j = 1; j < columnValues.length(); j++) {
                    lineData.addNumber(columnValues.getLong(j));
                }

                columns[i] = lineData;
            }
        }


        return columns;
    }

    private static String getName(JSONObject object, String id) throws JSONException {
        return object.getJSONObject("names").getString(id);
    }

    private static int getColor(JSONObject object, String id) throws JSONException {
        return Color.parseColor(object.getJSONObject("colors").getString(id));
    }
}
