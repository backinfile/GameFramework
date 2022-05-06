package com.backinfile.GameFramework.core;

public class MessageManager {
    @SuppressWarnings("unused")
    private static byte[] getPositiveBytes(byte[] bytes) {
        byte[] result = new byte[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            result[i * 2] = (byte) (bytes[i] & 0xF);
            result[i * 2 + 1] = (byte) (bytes[i] >>> 4 & 0xF);
        }
        return result;
    }

    private static byte[] reversedBytes(byte[] bytes) {
        if (bytes.length % 2 != 0)
            return null;
        byte[] result = new byte[bytes.length / 2];
        for (int i = 0; i < result.length; i++) {
            byte b = (byte) ((bytes[i * 2] & 0xF) + (bytes[i * 2 + 1] << 4));
            result[i] = b;
        }
        return result;
    }

    public static int getHashCode(String str) {
        int hash = str.length();
        for (int i = 0; i < str.length(); i++) {
            hash = ((hash << 5) ^ (hash >>> 27)) ^ str.charAt(i);
        }
        return hash;
    }

    public static byte[] int2bytes(int a) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (a & 0xFF);
        bytes[1] = (byte) (a >>> 8 & 0xFF);
        bytes[2] = (byte) (a >>> 16 & 0xFF);
        bytes[3] = (byte) (a >>> 24 & 0xFF);
        return bytes;
    }

    public static String get10(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            StringBuilder tmp = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                String str = ((b & 1) == 1) ? "1" : "0";
                tmp.append(str);
                b = (byte) (b >>> 1);
            }
            sb.append(tmp.reverse());
            sb.append(",");
        }
        return sb.toString();
    }

}
