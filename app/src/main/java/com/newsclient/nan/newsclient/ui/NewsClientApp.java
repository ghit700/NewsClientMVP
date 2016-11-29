package com.newsclient.nan.newsclient.ui;

import android.app.Application;

import com.newsclient.nan.newsclient.injector.component.ApplicationComponet;
import com.newsclient.nan.newsclient.injector.component.DaggerApplicationComponet;
import com.newsclient.nan.newsclient.injector.module.ApplicationModule;
import com.squareup.leakcanary.LeakCanary;

import static okhttp3.internal.Internal.instance;

/**
 * Created by wzn on 2016/9/12.
 */
public class NewsClientApp extends Application {

    private NewsClientApp instance;
    private ApplicationComponet mApplicationComponet;

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
        instance = this;
        mApplicationComponet = DaggerApplicationComponet.builder()
                .applicationModule(new ApplicationModule(getApplicationContext()))
                .build();
    }

    public NewsClientApp getInstance() {
        return instance;
    }

    public ApplicationComponet getComponet() {
        return mApplicationComponet;
    }
}
