package com.backinfile.GameFramework.db;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.support.SysException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

// 提供直接与数据库交互的工具
@SuppressWarnings("unchecked")
public class DBDirectProvider implements ISaveProvider, ILoadProvider {
    public static final String DB_PATH_PREFIX = "jdbc:sqlite:";
    private static final String DEFAULT_DB_FILE_NAME = "game.db";
    private static final String DEFAULT_DB_FILE_NAME_BACKUP = "game.db.bak";
    private Connection connection = null;
    private static volatile DBDirectProvider instance = null;

    public static DBDirectProvider getInstance() {
        if (instance == null) {
            instance = new DBDirectProvider();
        }
        return instance;
    }

    private DBDirectProvider() {
    }

    @Override
    public void insert(EntityBase obj) {
        DBManager.insert(getConnection(), obj);
    }

    @Override
    public void update(EntityBase obj) {
        DBManager.update(getConnection(), obj);
    }

    @Override
    public void delete(EntityBase obj) {
        DBTable dbTable = findDBTable(obj.getClass());
        DBManager.delete(getConnection(), dbTable.tableName, dbTable.keyFiled.getFieldValue(obj));
    }

    private Connection getConnection() {
        if (connection == null) {
            try {
                String url = DB_PATH_PREFIX + DEFAULT_DB_FILE_NAME;
                connection = DriverManager.getConnection(url);
                LogCore.db.info("新建链接 {}", url);
            } catch (Exception e) {
                throw new SysException("创建数据库链接失败", e);
            }
            DBManager.updateTableStruct(connection);
        }
        return connection;
    }

    public void open() {
        getConnection();
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LogCore.db.error("关闭数据库链接时发生错误", e);
            }
            connection = null;
        }
    }

    @Override
    public <T extends EntityBase> T querySingle(Class<T> clazz, long id) {
        return (T) DBManager.query(getConnection(), findDBTable(clazz).tableName, id);
    }

    @Override
    public <T extends EntityBase> List<T> queryAll(Class<T> clazz) {
        return (List<T>) (List<?>) DBManager.queryAll(getConnection(), findDBTable(clazz).tableName);
    }

    @Override
    public <T extends EntityBase> List<T> queryAllByPlayerId(Class<T> clazz, long playerId) {
        return (List<T>) (List<?>) DBManager.queryAll(getConnection(), findDBTable(clazz).tableName, playerId);
    }

    private <T extends EntityBase> DBTable findDBTable(Class<T> clazz) {
        return DBManager.tableMap.get(clazz);
    }

    public void backup(String path) {
        try {
            Path target = Paths.get(path, DEFAULT_DB_FILE_NAME_BACKUP);
            Files.copy(Paths.get(DEFAULT_DB_FILE_NAME), target, StandardCopyOption.REPLACE_EXISTING);
            LogCore.db.info("backupDatabase success path:{}", target.toAbsolutePath());
        } catch (Exception e) {
            LogCore.db.error("backupDatabase error", e);
        }
    }
}
