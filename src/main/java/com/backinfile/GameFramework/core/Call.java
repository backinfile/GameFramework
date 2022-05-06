package com.backinfile.GameFramework.core;

import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class Call implements ISerializable{
    private int portId;
    private int objId;

    @Override
    public void pack(MessagePacker packer) throws IOException {
        packer.packInt(portId);
        packer.packInt(objId);
    }

    @Override
    public void unpack(MessageUnpacker unpacker) throws IOException {
        portId = unpacker.unpackInt();
        objId = unpacker.unpackInt();
    }
}
