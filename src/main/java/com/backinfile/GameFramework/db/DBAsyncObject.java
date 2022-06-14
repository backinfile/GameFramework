package com.backinfile.GameFramework.db;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.core.Port;
import com.backinfile.GameFramework.proxy.AsyncObject;
import com.backinfile.GameFramework.proxy.Task;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DBAsyncObject extends AsyncObject {
    private static final String DB_PATH_PREFIX = "jdbc:sqlite:";
    private String dbPath = "game.db";
    private Connection connection = null;

    public DBAsyncObject() {
    }

    public DBAsyncObject(String dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public void onAttach(Port port) {
        super.onAttach(port);
        try {
            connection = DriverManager.getConnection(DB_PATH_PREFIX + dbPath);
        } catch (Exception e) {
            LogCore.db.error("error in open connection", e);
        }
    }

    @Override
    public void onDetach(Port port) {
        super.onDetach(port);

        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                LogCore.db.error("error in close connection", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Task<T> query(String tableName, int id) {
        return Task.completedTask((T) DBManager.query(connection, tableName, id));
    }

    @SuppressWarnings("unchecked")
    public <T> Task<List<T>> queryAll(String tableName) {
        List<Object> resultObjects = DBManager.queryAll(connection, tableName);
        if (resultObjects == null || resultObjects.isEmpty()) {
            return Task.completedTask(Collections.emptyList());
        }
        return Task.completedTask(resultObjects.stream().map(t -> (T) t).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    public <T> Task<List<T>> queryAllByIndex(String tableName, int playerId) {
        List<Object> resultObjects = DBManager.queryAll(connection, tableName, playerId);
        if (resultObjects == null || resultObjects.isEmpty()) {
            return Task.completedTask(Collections.emptyList());
        }
        return Task.completedTask(resultObjects.stream().map(t -> (T) t).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    public Task<Boolean> insert(Object obj) {
        int result = DBManager.insert(connection, obj);
        return Task.completedTask(result > 0);
    }

    public Task<Boolean> update(Object obj) {
        int result = DBManager.update(connection, obj);
        return Task.completedTask(result > 0);
    }

    public Task<Boolean> delete(String tableName, int id) {
        int result = DBManager.delete(connection, tableName, id);
        return Task.completedTask(result > 0);
    }

    public Task<Boolean> backupDatabase() {
        try {
            int index = dbPath.lastIndexOf('.');
            String backupPath = dbPath.substring(0, index) + ".bak" + dbPath.substring(index);
            Files.copy(Paths.get(dbPath), Paths.get(backupPath));
            LogCore.db.info("backupDatabase success path:{}", backupPath);
        } catch (Exception e) {
            LogCore.db.error("backupDatabase error", e);
        }
        return Task.completedTask(false);
    }
}
