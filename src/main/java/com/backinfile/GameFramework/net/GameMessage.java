package com.backinfile.GameFramework.net;


import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.serialize.ISerializable;
import com.backinfile.GameFramework.serialize.InputStream;
import com.backinfile.GameFramework.serialize.OutputStream;
import com.backinfile.GameFramework.serialize.Serializable;
import com.backinfile.support.SysException;
import com.backinfile.support.Utils;

/**
 * 用于包装socket中传递的消息
 */
public class GameMessage {
    private static final int CHECK_CODE = Utils.getHashCode("GameMessage");
    private final Object obj;

    private GameMessage(Object obj) {
        this.obj = obj;
    }

    public static GameMessage build(Object obj) {
        if (!isMessageObjType(obj)) {
            throw new SysException("obj " + obj + " is not message type");
        }
        return new GameMessage(obj);
    }

    public static boolean isMessageObjType(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof ISerializable) {
            return true;
        }
        if (obj.getClass().getAnnotation(Serializable.class) != null) {
            return true;
        }
        return false;
    }

    public static GameMessage build(byte[] bytes, int offset, int len) {
        if (len < 8 || bytes.length < 8)
            return null;
        int byteSize = Utils.bytes2Int(bytes, 0);
        int msgHash = Utils.bytes2Int(bytes, 4);
        if (msgHash != CHECK_CODE) {
            LogCore.net.error("hash code not match in buildGameMessage");
            return null;
        }
        InputStream in = new InputStream(bytes, 8, len - 8);
        Object obj = in.read();
        in.close();
        if (obj == null) {
            LogCore.net.error("read null from stream");
            return null;
        } else if (!isMessageObjType(obj)) {
            LogCore.net.error("read obj from stream error read:{} readClass:{}", obj, obj.getClass());
            return null;
        }
        return new GameMessage(obj);
    }

    public byte[] getBytes() {
        OutputStream out = new OutputStream();
        out.write(obj);
        out.close();
        byte[] byteArray = out.getBytes();
        byte[] contentBytes = new byte[byteArray.length + 8];
        Utils.int2bytes(byteArray.length + 8, contentBytes, 0);
        Utils.int2bytes(CHECK_CODE, contentBytes, 4);
        System.arraycopy(byteArray, 0, contentBytes, 8, byteArray.length);
        return contentBytes;
    }

    @SuppressWarnings("unchecked")
    public <T> T getMessage() {
        return (T) obj;
    }
}
