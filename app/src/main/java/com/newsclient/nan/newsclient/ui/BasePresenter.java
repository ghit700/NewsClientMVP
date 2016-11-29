package com.newsclient.nan.newsclient.ui;

import android.support.annotation.NonNull;
import android.view.View;

import static android.os.Build.VERSION_CODES.N;

/**
 * Created by wzn on 2016/11/28.
 */

public interface BasePresenter<T extends BaseView> {

    void attachView(@NonNull T view);

    void detachView();
}
