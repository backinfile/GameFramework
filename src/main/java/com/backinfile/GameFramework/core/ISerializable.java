package com.backinfile.GameFramework.core;

import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

/**
 * 序列化接口, 要求有无参初始化函数
 */
public interface ISerializable {
    void pack(MessagePacker packer) throws IOException;

    void unpack(MessageUnpacker unpacker) throws IOException;

}
