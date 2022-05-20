package com.backinfile.GameFramework.db;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.core.Port;
import com.backinfile.GameFramework.proxy.AsyncObject;
import com.backinfile.GameFramework.proxy.Task;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class DBAsyncObject extends AsyncObject {
    public static final String DB_PATH = "jdbc:sqlite:game.db";
    private Connection connection = null;

    @Override
    public void onAttach(Port port) {
        super.onAttach(port);
        try {
            connection = DriverManager.getConnection(DB_PATH);
        } catch (SQLException e) {
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


    @DBEntity(table = "test", key = "id")
    public static class TestDB extends EntityBase {
        public int id;
        public String name;
        public int value;
        public int value2;
    }

    public static void main(String[] args) throws Exception {
        DBManager.registerAll(DBAsyncObject.class.getClassLoader());
        Connection connection = DriverManager.getConnection("jdbc:sqlite:game.db");
        DBManager.updateTableStruct(connection);
        connection.close();
    }
}
