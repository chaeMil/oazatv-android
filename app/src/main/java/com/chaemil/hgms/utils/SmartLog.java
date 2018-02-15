package com.chaemil.hgms.utils;

import com.chaemil.hgms.BuildConfig;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.orhanobut.logger.Logger;
import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SmartLog {
    public static final int DEBUG = 0;
    public static final int INFO = 1;
    public static final int WARNING = 2;
    public static final int ERROR = 3;
    public static final int VERBOSE = 4;

    private static void log(int type, String tag, Object log) {
        if (log != null) {
            if (log instanceof JsonObject) {
                Logger.t(tag).json(log.toString());
            } else if (log instanceof ArrayList || log instanceof List || log instanceof HashMap) {
                Logger.t(tag).d(log);
            } else if (log instanceof SugarRecord) {
                Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
                String json = gson.toJson(log);
                Logger.t(tag).d(json);
            } else {
                switch (type) {
                    case DEBUG:
                        Logger.t(tag).d(log);
                        break;
                    case INFO:
                        Logger.t(tag).i(String.valueOf(log));
                        break;
                    case WARNING:
                        Logger.t(tag).e(String.valueOf(log));
                        break;
                    case ERROR:
                        Logger.t(tag).e(String.valueOf(log));
                        break;
                    case VERBOSE:
                        Logger.t(tag).v(String.valueOf(log));
                        break;
                }
            }
        }
    }

    public static void d(String tag, Object log) {
        if (BuildConfig.DEBUG) {
            log(DEBUG, tag, log);
        }
    }

    public static void i(String tag, Object log) {
        if (BuildConfig.DEBUG) {
            log(INFO, tag, log);
        }
    }

    public static void w(String tag, Object log) {
        String logString = "";
        if (log != null) {
            logString = log.toString();
        }
        //Crashlytics.log(2, tag, logString);
        //Crashlytics.logException(new Throwable("<W>[" + tag + "] " + logString));
        if (BuildConfig.DEBUG) {
            log(WARNING, tag, log);
        }
    }

    public static void e(String tag, Object log) {
        String logString = "";
        if (log != null) {
            logString = log.toString();
        }
        if (BuildConfig.DEBUG) {
            log(ERROR, tag, log);
        }
    }
}
