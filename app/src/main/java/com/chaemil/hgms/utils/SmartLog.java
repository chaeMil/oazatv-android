package com.chaemil.hgms.utils;

import android.util.Log;

import com.chaemil.hgms.OazaApp;

public class SmartLog {

    public static boolean LOG = OazaApp.DEVELOPMENT;
    public static String SMART_LOG_TAG = "SmartLog[";
    public static String SMART_LOG_TAG_END = "]";

    public static enum LogLevel {
        DEBUG, INFO, WARN, ERROR;
    }

    public static void Log(LogLevel logLevel, String tag, String log) {
        tag = SMART_LOG_TAG + tag + SMART_LOG_TAG_END;
        if (log == null) {
            log = "";
        }
        if (LOG) {
            switch (logLevel) {
                case DEBUG:
                    Log.d(tag, log);
                    break;
                case INFO:
                    Log.i(tag, log);
                    break;
                case WARN:
                    Log.w(tag, log);
                    break;
                case ERROR:
                    Log.e(tag, log);
                    break;
            }
        }
    }
}
