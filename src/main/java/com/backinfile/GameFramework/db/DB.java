package com.backinfile.GameFramework.db;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.proxy.Proxy;
import com.backinfile.GameFramework.proxy.Task;

import java.util.List;

public class DB {

    public static <T extends EntityBase> Task<T> query(Class<T> clazz, int id) {
        return Proxy.getProxy(DBAsyncObject.class).query(DBManager.tableMap.get(clazz).tableName, id);
    }

    public static <T extends EntityBase> Task<List<T>> queryAll(Class<T> clazz) {
        return Proxy.getProxy(DBAsyncObject.class).queryAll(DBManager.tableMap.get(clazz).tableName);
    }

    public static <T extends EntityBase> Task<List<T>> queryAll(Class<T> clazz, int playerId) {
        return Proxy.getProxy(DBAsyncObject.class).queryAllByIndex(DBManager.tableMap.get(clazz).tableName, playerId);
    }

    public static void insert(EntityBase entityBase) {
        Proxy.getProxy(DBAsyncObject.class).insert(entityBase).whenComplete((r, ex) -> {
            if (ex != null || !r) {
                LogCore.db.error("insert {} error", entityBase.getClass().getSimpleName());
            }
        });
    }

    public static void update(EntityBase entityBase) {
        Proxy.getProxy(DBAsyncObject.class).update(entityBase).whenComplete((r, ex) -> {
            if (ex != null || !r) {
                LogCore.db.error("insert {} error", entityBase.getClass().getSimpleName());
            }
        });
    }

    public static void delete(EntityBase entityBase) {
        DBManager.DBTable table = DBManager.tableMap.get(entityBase.getClass());
        if (table == null) {
            LogCore.db.error("delete {} error no table", entityBase.getClass().getSimpleName());
            return;
        }
        String tableName = table.tableName;
        int id = (int) table.keyFiled.getter.invoke(entityBase);
        Proxy.getProxy(DBAsyncObject.class).delete(tableName, id).whenComplete((r, ex) -> {
            if (ex != null || !r) {
                LogCore.db.error("delete {}:{} error", tableName, id);
            }
        });
    }
}
