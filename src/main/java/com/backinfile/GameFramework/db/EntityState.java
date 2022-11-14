package com.backinfile.GameFramework.db;

public class EntityState {
    public static final int STATE_NEW = 0;
    public static final int STATE_NORMAL = 1;
    public static final int STATE_DELETE = 3;
    public static final int STATE_SERIALIZE = 4; // 序列化状态，不可修改
}
