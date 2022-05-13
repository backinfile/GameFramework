package com.backinfile.GameFramework.core.serialize;

import com.backinfile.GameFramework.Log;
import com.backinfile.support.SysException;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class InputStream {

    private final MessageUnpacker unpacker;

    public InputStream(byte[] bytes, int start, int length) {
        unpacker = MessagePack.newDefaultUnpacker(bytes, start, length);
    }

    public InputStream(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    @SuppressWarnings("unchecked")
    public <T> T read() {
        try {
            return (T) readObject();
        } catch (Exception e) {
            Log.serialize.error("read obj failed", e);
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object readObject() throws Exception {
        int tag = unpacker.unpackInt();
        if (tag == SerializeTag.NULL)
            return null;

        int size = 0;
        if (SerializeTag.isArrayType(tag)) {
            size = unpacker.unpackArrayHeader();
        }

        switch (tag) {
            case SerializeTag.BYTE:
                return unpacker.unpackByte();
            case SerializeTag.BYTE_ARRAY: {
                byte[] array = new byte[size];
                for (int i = 0; i < size; i++) {
                    array[i] = unpacker.unpackByte();
                }
                return array;
            }
            case SerializeTag.ARRAY: {
                Object[] array = new Object[size];
                for (int i = 0; i < size; i++) {
                    array[i] = read();
                }
                return array;
            }
            case SerializeTag.BOOL:
                return unpacker.unpackBoolean();
            case SerializeTag.BOOL_ARRAY: {
                boolean[] array = new boolean[size];
                for (int i = 0; i < size; i++) {
                    array[i] = unpacker.unpackBoolean();
                }
                return array;
            }
            case SerializeTag.INT:
                return unpacker.unpackInt();
            case SerializeTag.INT_ARRAY: {
                int[] array = new int[size];
                for (int i = 0; i < size; i++) {
                    array[i] = unpacker.unpackInt();
                }
                return array;
            }
            case SerializeTag.LONG:
                return unpacker.unpackLong();
            case SerializeTag.LONG_ARRAY: {
                long[] array = new long[size];
                for (int i = 0; i < size; i++) {
                    array[i] = unpacker.unpackLong();
                }
                return array;
            }
            case SerializeTag.FLOAT:
                return unpacker.unpackFloat();
            case SerializeTag.FLOAT_ARRAY: {
                float[] array = new float[size];
                for (int i = 0; i < size; i++) {
                    array[i] = unpacker.unpackFloat();
                }
                return array;
            }
            case SerializeTag.DOUBLE:
                return unpacker.unpackDouble();
            case SerializeTag.DOUBLE_ARRAY: {
                double[] array = new double[size];
                for (int i = 0; i < size; i++) {
                    array[i] = unpacker.unpackDouble();
                }
                return array;
            }
            case SerializeTag.STRING:
                return unpacker.unpackString();
            case SerializeTag.STRING_ARRAY: {
                String[] array = new String[size];
                for (int i = 0; i < size; i++) {
                    array[i] = unpacker.unpackString();
                }
                return array;
            }
            case SerializeTag.ENUM: {
                int id = unpacker.unpackInt();
                int ord = unpacker.unpackInt();
                Object[] enumValues = SerializableManager.getUnpackerObj(id);
                if (enumValues != null) {
                    return enumValues[ord];
                } else {
                    Log.serialize.error("未能反序列化枚举类型 {}", id);
                    return null;
                }
            }
            case SerializeTag.LIST: {
                List<?> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    list.add(read());
                }
                return list;
            }
            case SerializeTag.SET: {
                Set<?> set = new HashSet<>();
                for (int i = 0; i < size; i++) {
                    set.add(read());
                }
                return set;
            }
            case SerializeTag.MAP: {
                Map map = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    Object key = read();
                    Object value = read();
                    map.put(key, value);
                }
                return map;
            }
            case SerializeTag.SERIALIZE: {
                int id = unpacker.unpackInt();
                Constructor<?> constructor = SerializableManager.getUnpackerObj(id);
                if (constructor != null) {
                    ISerializable serializable = (ISerializable) constructor.newInstance();
                    serializable.readFrom(this);
                    return serializable;
                } else {
                    Log.serialize.error("未能反序列化" + id, new SysException());
                    return null;
                }
            }
            case SerializeTag.AUTO_SERIALIZE: {
                int id = unpacker.unpackInt();
                Method method = SerializableManager.getUnpackerObj(id);
                if (method == null) {
                    Log.serialize.error("未能反序列化" + id, new SysException());
                    return null;
                }
                try {
                    return method.invoke(null, this);
                } catch (Exception e) {
                    Log.serialize.error("unpackSerialize error", e);
                }
                return null;
            }
        }

        return null;
    }

    public void close() {
        try {
            unpacker.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MessageUnpacker getUnpacker() {
        return unpacker;
    }
}
