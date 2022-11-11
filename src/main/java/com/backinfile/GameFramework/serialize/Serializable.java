package com.backinfile.GameFramework.serialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此类的public字段可以自动序列化
 * 需要有空构造函数
 * 若同时继承了ISerializable接口，优先使用该接口进行序列化
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Serializable {
}
