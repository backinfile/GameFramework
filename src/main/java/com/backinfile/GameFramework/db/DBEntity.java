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
    /**
     * 主键 需求int类型
     */
    String key() default "id";

    /**
     * 表名
     */
    String table();
}
