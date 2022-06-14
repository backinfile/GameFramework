package com.backinfile.GameFramework.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个类为DB类
 * 目前支持的类型 int long String
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DBEntity {
    String extraIndex() default "playerId";

    /**
     * 表名
     */
    String table();
}
