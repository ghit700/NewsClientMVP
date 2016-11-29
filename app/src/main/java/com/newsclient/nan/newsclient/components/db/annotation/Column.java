package com.newsclient.nan.newsclient.components.db.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 数据库中字段对应的名字，类型
 * Created by wzn on 2016/11/10.
 */
@Target(FIELD)
@Retention(RUNTIME)
@Inherited
public @interface Column {
    /**
     * 列名
     *
     * @return
     */
    public String value() default "";

    /**
     * 数据类型
     *
     * @return
     */
    public String type() default "";

    /**
     * 是否为主键
     *
     * @return
     */
    public boolean id() default false;

}
