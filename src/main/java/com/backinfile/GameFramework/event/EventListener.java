package com.backinfile.GameFramework.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个静态函数为事件监听者
 * 该函数需求只有一个事件参数(继承EventBase) 无返回值 public
 * 例如 <br>
 * {@code  @EventListener
 * public static void onEventGo(EventGo eventGo) {}
 * }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventListener {

    int priority() default 1000; // 大的先执行

    Class<?> async() default void.class; // 异步触发事件 触发时发生在那个Service
}
