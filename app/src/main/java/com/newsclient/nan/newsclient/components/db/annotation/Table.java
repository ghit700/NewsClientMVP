package com.newsclient.nan.newsclient.components.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by wzn on 2016/11/10.
 */
@Target(ElementType.TYPE)
@Retention(RUNTIME)
@Inherited
public @interface Table {
    public String value() ;
}
