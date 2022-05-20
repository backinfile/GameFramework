package com.backinfile.GameFramework.db;

import com.backinfile.GameFramework.core.Port;
import com.backinfile.GameFramework.proxy.AsyncObject;

import java.sql.Connection;
import java.sql.DriverManager;

class DBAsyncObject extends AsyncObject {
    @Override
    public void onAttach(Port port) {
        super.onAttach(port);

    }

    @DBEntity(table = "test")
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
