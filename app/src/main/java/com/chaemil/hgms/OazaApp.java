package com.chaemil.hgms;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.fragment.AudioPlayerFragment;
import com.chaemil.hgms.service.MyRequestService;
import com.orm.SugarContext;

/**
 * Created by chaemil on 3.12.15.
 */
public class OazaApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        MyRequestService.init(this);
        SugarContext.init(this);
    }
}
