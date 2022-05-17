package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.core.serialize.ISerializable;
import com.backinfile.support.Utils;

public interface IResult extends ISerializable {
    /**
     * 以key，value形式获取参数
     */
    <T> T getResult(String key);

    /**
     * 获取第index个参数
     */
    <T> T getResult(int index);

    /**
     * 获取第0个参数
     */
    default <T> T getResult() {
        return getResult(0);
    }

    /**
     * 获取错误描述
     */
    String getErrorString();

    /**
     * 是否有错误产生了
     */
    default boolean hasError() {
        return !Utils.isNullOrEmpty(getErrorString());
    }
}
