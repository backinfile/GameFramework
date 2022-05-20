package com.backinfile.GameFramework.db;

import com.backinfile.GameFramework.proxy.Task;
import com.backinfile.support.SysException;

import java.util.List;

public class DB {
    public static <T extends EntityBase> Task<T> query(Class<T> clazz, int id) {
        throw new SysException("not implement");
    }

    public static <T extends EntityBase> Task<List<T>> queryAll(Class<T> clazz) {
        throw new SysException("not implement");
    }

    public static void insert(EntityBase entityBase) {
        throw new SysException("not implement");
    }

    public static void update(EntityBase entityBase) {
        throw new SysException("not implement");
    }

    public static void remove(EntityBase entityBase) {
        throw new SysException("not implement");
    }
}
