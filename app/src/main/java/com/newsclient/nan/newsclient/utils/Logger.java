package com.newsclient.nan.newsclient.utils;

import android.text.TextUtils;
import android.util.Log;

/**
 * Created by wzn on 2016/9/12.
 */
public class Logger {
    public static final String DEFAULT_TAG = "Logger";
    public static String TAG;

    private static int LOGLEVEL = 6;
    private static int VERBOSE = 1;
    private static int DEBUG = 2;
    private static int INFO = 3;
    private static int WARN = 4;
    private static int ERROR = 5;

    public static void setDevelopMode(boolean flag) {
        if (flag) {
            LOGLEVEL = 6;
        } else {
            LOGLEVEL = -1;
        }
    }

    public static void v(String tag, String msg) {
        if (LOGLEVEL > VERBOSE && !TextUtils.isEmpty(msg)) {
            if (TextUtils.isEmpty(tag)) tag = DEFAULT_TAG;
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (LOGLEVEL > DEBUG && !TextUtils.isEmpty(msg)) {
            if (TextUtils.isEmpty(tag)) tag = DEFAULT_TAG;
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (LOGLEVEL > INFO && !TextUtils.isEmpty(msg)) {
            if (TextUtils.isEmpty(tag)) tag = DEFAULT_TAG;
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (LOGLEVEL > WARN && !TextUtils.isEmpty(msg)) {
            if (TextUtils.isEmpty(tag)) tag = DEFAULT_TAG;
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (LOGLEVEL > ERROR && !TextUtils.isEmpty(msg)) {
            if (TextUtils.isEmpty(tag)) tag = DEFAULT_TAG;
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, Throwable throwable) {
        if (LOGLEVEL > ERROR && throwable != null) {
            if (TextUtils.isEmpty(tag)) tag = DEFAULT_TAG;
            Log.e(tag, throwable.getLocalizedMessage());
        }
    }

    public static void v(String str) {
        v(TAG, str);
    }

    public static void d(String str) {
        d(TAG, str);
    }

    public static void i(String str) {
        i(TAG, str);
    }

    public static void w(String str) {
        w(TAG, str);
    }

    public static void e(String str) {
        e(TAG, str);
    }

    public static void v(Number number) {
        v(TAG, "Number is :" + String.valueOf(number));
    }

    public static void d(Number number) {
        d(TAG, "Number is :" + String.valueOf(number));
    }

    public static void i(Number number) {
        i(TAG, "Number is :" + String.valueOf(number));
    }

    public static void w(Number number) {
        w(TAG, "Number is :" + String.valueOf(number));
    }

    public static void e(Number number) {
        e(TAG, "Number is :" + String.valueOf(number));
    }

    public static void e(Throwable throwable) {
        e(TAG, throwable);
    }
}
