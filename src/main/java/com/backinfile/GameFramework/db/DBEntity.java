package com.backinfile.GameFramework.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个类为DB类
 * 目前支持的类型 int long float double String
 * 需要有默认构造器; 不要有逻辑，只定义字段!
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DBEntity {
    /**
     * 表名
     */
    String tableName();


    /**
     * 额外的查询字段
     */
    String extraIndex() default "playerId";

}
