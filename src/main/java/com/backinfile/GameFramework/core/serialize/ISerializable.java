package com.backinfile.GameFramework.core.serialize;

/**
 * 序列化接口, 要求有无参初始化函数
 */
public interface ISerializable {
    void writeTo(OutputStream out);

    void readFrom(InputStream in);
}
