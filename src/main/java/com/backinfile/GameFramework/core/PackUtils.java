package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.Log;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import java.lang.reflect.Constructor;
import java.util.*;

public class PackUtils {

    @SuppressWarnings("unchecked")
    public static <T> T unpack(MessageUnpacker unpacker) {
        try {
            return (T) unpackObject(unpacker);
        } catch (Exception e) {
            Log.serialize.error("读取数据失败", e);
        }
        return null;
    }

    public static void pack(MessagePacker packer, Object obj) {
        try {
            packObject(packer, obj);
            packer.flush();
        } catch (Exception e) {
            Log.serialize.error("写入数据失败", e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object unpackObject(MessageUnpacker unpacker) throws Exception {
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
                    array[i] = unpack(unpacker);
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
                Object[] enumValues = SerializableManager.parseFromSerializeID(id);
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
                    list.add(unpack(unpacker));
                }
                return list;
            }
            case SerializeTag.SET: {
                Set<?> set = new HashSet<>();
                for (int i = 0; i < size; i++) {
                    set.add(unpack(unpacker));
                }
                return set;
            }
            case SerializeTag.MAP: {
                Map map = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    Object key = unpack(unpacker);
                    Object value = unpack(unpacker);
                    map.put(key, value);
                }
                return map;
            }
            case SerializeTag.SERIALIZE: {
                int id = unpacker.unpackInt();
                Constructor<?> constructor = SerializableManager.parseFromSerializeID(id);
                if (constructor != null) {
                    ISerializable serializable = (ISerializable) constructor.newInstance();
                    serializable.unpack(unpacker);
                    return serializable;
                } else {
                    Log.serialize.error("未能反序列化{}", id);
                    return null;
                }
            }
        }

        return null;
    }

    @SuppressWarnings("rawtypes")
    private static void packObject(MessagePacker packer, Object obj) throws Exception {
        if (obj == null) {
            packer.packInt(SerializeTag.NULL);
            return;
        }

        Class<?> clazz = obj.getClass();

        if (clazz == Byte.class) {
            packer.packInt(SerializeTag.BYTE);
            packer.packByte((byte) obj);
        } else if (clazz == byte[].class) {
            byte[] bytes = (byte[]) obj;
            packer.packInt(SerializeTag.BYTE_ARRAY);
            packer.packArrayHeader(bytes.length);
            for (Byte b : bytes) {
                packer.packByte(b);
            }
        } else if (clazz == Boolean.class) {
            packer.packInt(SerializeTag.BOOL);
            packer.packBoolean((boolean) obj);
        } else if (clazz == boolean[].class) {
            boolean[] booleans = (boolean[]) obj;
            packer.packInt(SerializeTag.BOOL_ARRAY);
            packer.packArrayHeader(booleans.length);
            for (boolean b : booleans) {
                packer.packBoolean(b);
            }
        } else if (clazz == Integer.class) {
            packer.packInt(SerializeTag.INT);
            packer.packInt((int) obj);
        } else if (clazz == int[].class) {
            int[] ints = (int[]) obj;
            packer.packInt(SerializeTag.INT_ARRAY);
            packer.packArrayHeader(ints.length);
            for (int i : ints) {
                packer.packInt(i);
            }
        } else if (clazz == Long.class) {
            packer.packLong(SerializeTag.LONG);
            packer.packLong((long) obj);
        } else if (clazz == long[].class) {
            long[] ints = (long[]) obj;
            packer.packInt(SerializeTag.LONG_ARRAY);
            packer.packArrayHeader(ints.length);
            for (long i : ints) {
                packer.packLong(i);
            }
        } else if (clazz == Float.class) {
            packer.packInt(SerializeTag.FLOAT);
            packer.packFloat((float) obj);
        } else if (clazz == float[].class) {
            float[] floats = (float[]) obj;
            packer.packInt(SerializeTag.FLOAT_ARRAY);
            packer.packArrayHeader(floats.length);
            for (float i : floats) {
                packer.packFloat(i);
            }
        } else if (clazz == Double.class) {
            packer.packInt(SerializeTag.DOUBLE);
            packer.packDouble((double) obj);
        } else if (clazz == double[].class) {
            double[] doubles = (double[]) obj;
            packer.packInt(SerializeTag.DOUBLE_ARRAY);
            packer.packArrayHeader(doubles.length);
            for (double i : doubles) {
                packer.packDouble(i);
            }
        } else if (clazz == String.class) {
            packer.packInt(SerializeTag.STRING);
            packer.packString((String) obj);
        } else if (clazz == String[].class) {
            String[] strings = (String[]) obj;
            packer.packInt(SerializeTag.STRING_ARRAY);
            packer.packArrayHeader(strings.length);
            for (String s : strings) {
                packer.packString(s);
            }
        } else if (obj instanceof Enum) {
            packer.packInt(SerializeTag.ENUM);
            Enum<?> anEnum = (Enum<?>) obj;
            packer.packInt(SerializableManager.getCommonSerializeID(anEnum));
            packer.packInt(anEnum.ordinal());
        } else if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            packer.packInt(SerializeTag.LIST);
            packer.packArrayHeader(list.size());
            for (Object ele : list) {
                packObject(packer, ele);
            }
        } else if (obj instanceof Set) {
            Set<?> set = (Set<?>) obj;
            packer.packInt(SerializeTag.SET);
            packer.packArrayHeader(set.size());
            for (Object ele : set) {
                packObject(packer, ele);
            }
        } else if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;

            packer.packInt(SerializeTag.MAP);
            packer.packArrayHeader(map.size());

            for (Map.Entry ele : map.entrySet()) {
                packObject(packer, ele.getKey());
                packObject(packer, ele.getValue());
            }
        } else if (obj instanceof ISerializable) {
            packer.packInt(SerializeTag.SERIALIZE);
            packer.packInt(SerializableManager.getCommonSerializeID(obj));
            ((ISerializable) obj).pack(packer);
        } else if (obj instanceof Object[]) {
            Object[] array = (Object[]) obj;
            packer.packInt(SerializeTag.ARRAY);
            packer.packArrayHeader(array.length);
            for (Object ele : array) {
                packObject(packer, ele);
            }
        } else {
            throw new Exception("无法序列化" + obj.getClass().getName());
        }
    }
}
