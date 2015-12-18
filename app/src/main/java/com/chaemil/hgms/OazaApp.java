package com.chaemil.hgms;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.chaemil.hgms.service.MyRequestService;

/**
 * Created by chaemil on 3.12.15.
 */
public class OazaApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        MyRequestService.init(this);
    }
}
