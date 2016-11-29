package com.newsclient.nan.newsclient.ui.login;

import com.newsclient.nan.newsclient.injector.component.ActivityComponent;
import com.newsclient.nan.newsclient.injector.component.ApplicationComponet;

import dagger.Component;

/**
 * Created by wzn on 2016/11/28.
 */
@Component(dependencies = ApplicationComponet.class)
public interface LoginComponent {
    void inject(LoginActivity activity);
}
