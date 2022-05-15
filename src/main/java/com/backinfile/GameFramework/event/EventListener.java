package com.backinfile.GameFramework.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个静态函数为事件监听者，参数为要监听的事件
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventListener {
    Class<? extends EventBase> value();
    int priority() default 0; // 大的先执行
}
