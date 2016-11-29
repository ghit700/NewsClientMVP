package com.newsclient.nan.newsclient.injector.module;

import android.content.Context;

import com.newsclient.nan.newsclient.components.db.Db;
import com.newsclient.nan.newsclient.components.db.TableHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by wzn on 2016/11/28.
 */
@Module
public class ApplicationModule {
    private final Context mContext;

    public ApplicationModule(Context mContext) {
        this.mContext = mContext;
    }

    @Singleton
    @Provides
    public Db provideDb() {
        Db.getInstance().init(new TableHelper(mContext));
        return Db.getInstance();
    }
}
