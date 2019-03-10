package com.josfzr.plotcontest.utils;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

@SuppressLint("ConstantLocale")
public class DateTimeUtils {
    private static DateFormat sDataPointLongDateFormat = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());
    private static DateFormat sDataPointAxisDateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

    public static DateFormat getDataPointAxisDateFormat() {
        return sDataPointAxisDateFormat;
    }

    public static DateFormat getDataPointLongDateFormat() {
        return sDataPointLongDateFormat;
    }
}
