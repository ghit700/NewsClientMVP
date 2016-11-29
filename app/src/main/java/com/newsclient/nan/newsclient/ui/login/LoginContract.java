package com.newsclient.nan.newsclient.ui.login;

import com.newsclient.nan.newsclient.ui.BasePresenter;
import com.newsclient.nan.newsclient.ui.BaseView;

/**
 * Created by wzn on 2016/11/28.
 */

public interface LoginContract {
    interface View extends BaseView<Presenter>{

    }

    interface Presenter extends BasePresenter<View>{

    }
}
