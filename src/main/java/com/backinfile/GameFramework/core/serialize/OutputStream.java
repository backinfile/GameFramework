package com.backinfile.GameFramework.core.serialize;

import com.backinfile.GameFramework.Log;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OutputStream {

    private final MessageBufferPacker packer;

    public OutputStream() {
        this.packer = MessagePack.newDefaultBufferPacker();
    }

    public byte[] getBytes() {
        return packer.toByteArray();
    }

    public void write(Object obj) {
        try {
            writeObject(obj);
            packer.flush();
        } catch (Exception e) {
            Log.serialize.error("writeObject failed", e);
        }
    }

    @SuppressWarnings("rawtypes")
    private void writeObject(Object obj) throws Exception {
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
                write(ele);
            }
        } else if (obj instanceof Set) {
            Set<?> set = (Set<?>) obj;
            packer.packInt(SerializeTag.SET);
            packer.packArrayHeader(set.size());
            for (Object ele : set) {
                write(ele);
            }
        } else if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;

            packer.packInt(SerializeTag.MAP);
            packer.packArrayHeader(map.size());

            for (Map.Entry ele : map.entrySet()) {
                write(ele.getKey());
                write(ele.getValue());
            }
        } else if (obj instanceof ISerializable) {
            packer.packInt(SerializeTag.SERIALIZE);
            packer.packInt(SerializableManager.getCommonSerializeID(obj));
            ((ISerializable) obj).writeTo(this);
        } else if (obj instanceof Object[]) {
            Object[] array = (Object[]) obj;
            packer.packInt(SerializeTag.ARRAY);
            packer.packArrayHeader(array.length);
            for (Object ele : array) {
                write(ele);
            }
        } else if (obj.getClass().getAnnotation(Serializable.class) != null) {
            packer.packInt(SerializeTag.AUTO_SERIALIZE);
            int id = SerializableManager.getCommonSerializeID(obj);
            packer.packInt(id);
            Method method = SerializableManager.getPackerObj(id);
            if (method == null) {
                throw new Exception("尚未注册" + obj.getClass().getName());
            }
            try {
                method.invoke(null, obj, this);
            } catch (Exception e) {
                Log.serialize.error("packSerialize error", e);
            }
        } else {
            throw new Exception("无法序列化" + obj.getClass().getName());
        }
    }

    public void close() {
        try {
            packer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MessageBufferPacker getPacker() {
        return packer;
    }
}
