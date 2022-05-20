package com.backinfile.GameFramework.serialize;

public class SerializeTag {

    public static final int NULL = 0;
    public static final int ARRAY = 1;
    public static final int BYTE = 2;
    public static final int BOOL = 3;
    public static final int INT = 4;
    public static final int LONG = 5;
    public static final int FLOAT = 6;
    public static final int DOUBLE = 7;
    public static final int STRING = 8;
    public static final int ENUM = 9;
    public static final int LIST = 10;
    public static final int SET = 11;
    public static final int MAP = 13;
    public static final int SERIALIZE = 14;
    public static final int BYTE_ARRAY = 16;
    public static final int BOOL_ARRAY = 17;
    public static final int INT_ARRAY = 18;
    public static final int LONG_ARRAY = 19;
    public static final int FLOAT_ARRAY = 20;
    public static final int DOUBLE_ARRAY = 21;
    public static final int STRING_ARRAY = 22;
    public static final int AUTO_SERIALIZE = 23;

    public static boolean isArrayType(int tag) {
        switch (tag) {
            case SerializeTag.BYTE_ARRAY:
            case SerializeTag.ARRAY:
            case SerializeTag.INT_ARRAY:
            case SerializeTag.LONG_ARRAY:
            case SerializeTag.BOOL_ARRAY:
            case SerializeTag.FLOAT_ARRAY:
            case SerializeTag.DOUBLE_ARRAY:
            case SerializeTag.STRING_ARRAY:
            case SerializeTag.SET:
            case SerializeTag.LIST:
            case SerializeTag.MAP:
                return true;
        }
        return false;
    }
}
