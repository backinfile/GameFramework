package com.backinfile.GameFramework.db;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.support.Utils;
import com.backinfile.support.func.Action2;
import com.backinfile.support.func.Function0;
import com.backinfile.support.func.Function1;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class DBManager {
    final static Map<Class<?>, DBTable> tableMap = new HashMap<>();
    final static Map<String, DBTable> tableNameMap = new HashMap<>();
    private static boolean SQL_LOG = true;

    static class DBTable {
        public String tableName;
        public DBField keyFiled;
        public DBField extraIndexFiled;
        public List<DBField> fields = new ArrayList<>();
        public Function0<Object> constructor;

        public DBTable(String tableName) {
            this.tableName = tableName;
        }

        public DBField getFieldByName(String filedName) {
            for (DBField field : fields) {
                if (field.name.equals(filedName)) {
                    return field;
                }
            }
            return null;
        }

        public Object parseResult(ResultSet resultSet) {
            Object result = constructor.invoke();
            if (result != null) {
                for (DBField field : fields) {
                    Object value = field.type.parseResult(resultSet, field.name);
                    field.setter.invoke(result, value);
                }
            }
            return result;
        }
    }

    static class DBField {
        public boolean key;
        public boolean extraIndex;
        public String name;
        public FieldType type;
        public Action2<Object, Object> setter;
        public Function1<Object, Object> getter;

        public String getStringFieldValue(Object obj) {
            if (type == FieldType.String) {
                return "'" + getter.invoke(obj) + "'";
            }
            return getter.invoke(obj).toString();
        }
    }

    enum FieldType {
        Int(int.class, "INTEGER") {
            @Override
            public Object getResult(ResultSet rs, java.lang.String p) throws SQLException {
                return rs.getInt(p);
            }
        },
        Long(long.class, "BIGINT") {
            @Override
            public Object getResult(ResultSet rs, java.lang.String p) throws SQLException {
                return rs.getLong(p);
            }
        },
        Float(float.class, "FLOAT") {
            @Override
            public Object getResult(ResultSet rs, java.lang.String p) throws SQLException {
                return rs.getFloat(p);
            }
        },
        Double(double.class, "DOUBLE") {
            @Override
            public Object getResult(ResultSet rs, java.lang.String p) throws SQLException {
                return rs.getDouble(p);
            }
        },
        String(String.class, "TEXT") {
            @Override
            public Object getResult(ResultSet rs, java.lang.String p) throws SQLException {
                return rs.getString(p);
            }
        },
        ;
        public final Class<?> javaType;
        public final String sqlType;

        FieldType(Class<?> javaType, java.lang.String sqlType) {
            this.javaType = javaType;
            this.sqlType = sqlType;
        }

        public static FieldType getTypeBySqlType(String sqlType) {
            for (FieldType fieldType : FieldType.values()) {
                if (fieldType.sqlType.equalsIgnoreCase(sqlType)) {
                    return fieldType;
                }
            }
            return null;
        }

        protected abstract Object getResult(ResultSet rs, String p) throws SQLException;

        public Object parseResult(ResultSet rs, String p) {
            try {
                return getResult(rs, p);
            } catch (SQLException e) {
                LogCore.db.error("error in get " + p, e);
            }
            return "";
        }
    }

    public static void enableSqlLog(boolean log) {
        SQL_LOG = log;
    }

    public static void registerAll(ClassLoader... classLoaders) {
        Reflections reflections = new Reflections(
                new SubTypesScanner(false),
                classLoaders);
        for (Class<?> clazz : reflections.getSubTypesOf(EntityBase.class)) {
            DBEntity annotation = clazz.getAnnotation(DBEntity.class);
            if (annotation == null) {
                LogCore.db.warn("class {} has no annotation {}", clazz.getName(), EntityBase.class.getName());
                continue;
            }
            if (!clazz.getSimpleName().endsWith("DB")) {
                LogCore.db.warn("entity class {} should end with \"DB\"", clazz.getName());
            }


            Constructor<?> constructor = null;
            try {
                constructor = clazz.getConstructor();
            } catch (NoSuchMethodException e) {
                LogCore.db.warn("class {} has no default constructor", clazz.getName());
                continue;
            }

            DBTable table = new DBTable(annotation.table());
            Constructor<?> finalConstructor = constructor;
            table.constructor = () -> {
                try {
                    return (Object) finalConstructor.newInstance();
                } catch (Exception e) {
                    LogCore.db.error("create db entity error " + clazz.getSimpleName(), e);
                }
                return null;
            };
            int indexCount = 0;
            for (Field field : getFields(clazz)) {
                for (FieldType fieldType : FieldType.values()) {
                    if (field.getType() == fieldType.javaType) {
                        DBField dbField = new DBField();
                        dbField.key = field.getName().equals("id");
                        dbField.extraIndex = field.getName().equals(annotation.extraIndex());
                        dbField.name = field.getName();
                        dbField.type = fieldType;
                        dbField.getter = obj -> {
                            try {
                                return field.get(obj);
                            } catch (IllegalAccessException e) {
                                LogCore.db.error("invoke getter error on " + field.getName() + " of " + clazz.getSimpleName());
                            }
                            return null;
                        };
                        dbField.setter = (obj, value) -> {
                            try {
                                field.set(obj, value);
                            } catch (IllegalAccessException e) {
                                LogCore.db.error("invoke getter error on " + field.getName() + " of " + clazz.getSimpleName());
                            }
                        };
                        table.fields.add(dbField);
                        if (dbField.key) {
                            indexCount++;
                            table.keyFiled = dbField;
                        }
                        if (dbField.extraIndex) {
                            table.extraIndexFiled = dbField;
                        }
                    }
                }
            }
            if (indexCount != 1) {
                LogCore.db.error("{} index count = {}", clazz.getSimpleName(), indexCount);
                continue;
            }
            tableMap.put(clazz, table);
            tableNameMap.put(table.tableName, table);
        }
    }

    // ???????????????
    public static void updateTableStruct(Connection connection) {
        for (DBTable table : tableMap.values()) {
            DBTable oldTable = queryOldTableStruct(connection, table.tableName);
            if (oldTable == null) { // ????????? ????????????
                String sql = getCreateTableSql(table);
                executeSql(connection, sql);
                // ????????????
                String createIndexSql = getCreateIndexSql(table);
                if (!Utils.isNullOrEmpty(createIndexSql)) {
                    executeSql(connection, createIndexSql);
                }
            } else { // ????????? ????????????
                for (String sql : getModifySqlString(oldTable, table)) {
                    executeSql(connection, sql);
                }
            }
        }
    }

    public static Object query(Connection connection, String tableName, int id) {
        DBTable table = tableNameMap.get(tableName);
        if (table == null) {
            LogCore.db.error("query unknown table {}", tableName);
            return null;
        }
        String sql = Utils.format("select * from {} where `{}`= {};", table.tableName, table.keyFiled.name, id);

        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            if (!resultSet.next()) {
                return null;
            }
            return table.parseResult(resultSet);
        } catch (Exception e) {
            LogCore.db.error("execute sql " + sql + " error", e);
        }
        return null;
    }

    public static List<Object> queryAll(Connection connection, String tableName) {
        DBTable table = tableNameMap.get(tableName);
        if (table == null) {
            LogCore.db.error("query unknown table {}", tableName);
            return null;
        }
        String sql = Utils.format("select * from {};", table.tableName);

        List<Object> resultList = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                resultList.add(table.parseResult(resultSet));
            }
        } catch (Exception e) {
            LogCore.db.error("execute sql " + sql + " error", e);
        }
        return resultList;
    }

    public static List<Object> queryAll(Connection connection, String tableName, int playerId) {
        DBTable table = tableNameMap.get(tableName);
        if (table == null || table.extraIndexFiled == null) {
            LogCore.db.error("query unknown table {}", tableName);
            return null;
        }
        String sql = Utils.format("select * from {} where `{}` = {};", table.tableName, table.extraIndexFiled.name, playerId);

        List<Object> resultList = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                resultList.add(table.parseResult(resultSet));
            }
        } catch (Exception e) {
            LogCore.db.error("execute sql " + sql + " error", e);
        }
        return resultList;
    }

    public static int insert(Connection connection, Object obj) {
        DBTable table = tableMap.get(obj.getClass());
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("insert into ");
        sqlBuilder.append(table.tableName);
        sqlBuilder.append(" ");

        {
            StringJoiner keyString = new StringJoiner(",", "(", ")");
            for (DBField field : table.fields) {
                keyString.add(field.name);
            }
            sqlBuilder.append(keyString);
        }
        sqlBuilder.append(" values ");

        {
            StringJoiner valueString = new StringJoiner(",", "(", ")");
            for (DBField field : table.fields) {
                valueString.add(field.getStringFieldValue(obj));
            }
            sqlBuilder.append(valueString);
        }
        sqlBuilder.append(";");
        String sql = sqlBuilder.toString();

        return executeUpdateSql(connection, sql);
    }

    public static int update(Connection connection, Object obj) {
        DBTable table = tableMap.get(obj.getClass());
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("UPDATE ");
        sqlBuilder.append(table.tableName);
        sqlBuilder.append(" SET ");

        {
            StringJoiner setString = new StringJoiner(",");
            for (DBField field : table.fields) {
                setString.add(Utils.format("`{}` = {}", field.name, field.getStringFieldValue(obj)));
            }
            sqlBuilder.append(setString);
        }

        sqlBuilder.append(Utils.format(" WHERE `{}` = {};", table.keyFiled.name, table.keyFiled.getStringFieldValue(obj)));
        String sql = sqlBuilder.toString();

        return executeUpdateSql(connection, sql);
    }

//    DELETE FROM Customers
//    WHERE CustomerName='Alfreds Futterkiste';

    public static int delete(Connection connection, String tableName, int id) {
        DBTable table = tableNameMap.get(tableName);
        String sql = Utils.format("DELETE FROM {} WHERE `{}`={}", table.tableName, table.keyFiled.name, id);
        return executeUpdateSql(connection, sql);
    }


    private static DBTable queryOldTableStruct(Connection connection, String tableName) {
        DBTable table = new DBTable(tableName);
        String sql = "PRAGMA table_info(\"" + tableName + "\")";
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String filedName = resultSet.getString(2);
                String filedType = resultSet.getString(3);
                int key = resultSet.getInt(6);
                DBField field = new DBField();
                field.name = filedName;
                field.type = FieldType.getTypeBySqlType(filedType);
                field.key = key == 1;

                table.fields.add(field);
                if (field.key) {
                    table.keyFiled = field;
                }
                if (field.extraIndex) {
                    table.extraIndexFiled = field;
                }
            }
        } catch (Exception e) {
            LogCore.db.error("execute sql " + sql + " error", e);
        }
        if (table.fields.isEmpty()) {
            return null;
        }
        queryTableIndex(connection, tableName, table);
        return table;
    }

    private static void queryTableIndex(Connection connection, String tableName, DBTable table) {
        String sql = "SELECT name FROM sqlite_master WHERE type = 'index' and tbl_name = '" + tableName + "';";
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String indexName = resultSet.getString(1);
                String[] split = indexName.split("_");
                if (split.length == 2 && tableName.equals(split[0])) {
                    String fieldName = split[1];
                    DBField field = table.getFieldByName(fieldName);
                    if (field != null) {
                        field.extraIndex = true;
                    }
                }
            }
        } catch (Exception e) {
            LogCore.db.error("execute sql " + sql + " error", e);
        }
    }

    private static String getCreateTableSql(DBTable table) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("Create table ");
        sqlBuilder.append(table.tableName);
        sqlBuilder.append(" (");
        for (DBField field : table.fields) {
            sqlBuilder.append("`");
            sqlBuilder.append(field.name);
            sqlBuilder.append("` ");
            sqlBuilder.append(field.type.sqlType);
            sqlBuilder.append(" ");
            if (field.key) {
                sqlBuilder.append("PRIMARY KEY");
            }
            sqlBuilder.append(", ");
        }

        if (table.fields.size() > 0) {
            sqlBuilder.setLength(sqlBuilder.length() - 2);
        }
        sqlBuilder.append(")");
        return sqlBuilder.toString();
    }

    private static String getCreateIndexSql(DBTable table) {
        if (table.extraIndexFiled == null) {
            return "";
        }
        String indexName = table.tableName + "_" + table.extraIndexFiled.name;
        return "CREATE INDEX " + indexName + " on " + table.tableName + "(" + table.extraIndexFiled.name + ");";
    }

    private static List<String> getModifySqlString(DBTable oldTable, DBTable table) {
        List<String> result = new ArrayList<>();
        Set<String> updateFieldNameSet = new HashSet<>();
        updateFieldNameSet.addAll(oldTable.fields.stream().map(f -> f.name).collect(Collectors.toSet()));
        updateFieldNameSet.addAll(table.fields.stream().map(f -> f.name).collect(Collectors.toSet()));

        for (String filedName : updateFieldNameSet) {
            DBField oldField = oldTable.getFieldByName(filedName);
            DBField newField = table.getFieldByName(filedName);
            if (newField != null && oldField == null) {
                result.add(Utils.format("alter table {} add `{}` {};", table.tableName, filedName, newField.type.sqlType));
            } else if (newField == null && oldField != null) {
                result.add(Utils.format("alter table {} drop `{}`;", table.tableName, filedName));
            } else if (newField != null) {
                if (newField.type != oldField.type) {
                    result.add(Utils.format("alter table {} drop `{}`;", table.tableName, filedName));
                    result.add(Utils.format("alter table {} add `{}` {};", table.tableName, filedName, newField.type.sqlType));
                }
            }
        }

        // ??????????????????
        for (String filedName : updateFieldNameSet) {
            DBField oldField = oldTable.getFieldByName(filedName);
            DBField newField = table.getFieldByName(filedName);
            boolean oldIndex = oldField != null && oldField.extraIndex;
            boolean newIndex = newField != null && newField.extraIndex;
            if (newIndex != oldIndex) {
                if (newIndex) {
                    result.add(getCreateIndexSql(table));
                } else {
                    String sql = "DROP INDEX " + table.tableName + "_" + oldField.name + ";";
                    result.add(sql);
                }
            }
        }

        return result;
    }

    private static void executeSql(Connection connection, String sql) {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            if (SQL_LOG) {
                LogCore.db.info("execute sql success: " + sql);
            }
        } catch (Exception e) {
            LogCore.db.error("execute sql error: " + sql, e);
        }
    }

    private static int executeUpdateSql(Connection connection, String sql) {
        int result = 0;
        try (Statement statement = connection.createStatement()) {
            result = statement.executeUpdate(sql);
            if (SQL_LOG) {
                LogCore.db.info("execute sql result:" + result + " sql:" + sql);
            }
        } catch (Exception e) {
            LogCore.db.error("execute sql error: " + sql, e);
        }
        return result;
    }

    private static List<Field> getFields(Class<?> clazz) {
        List<Field> result = new ArrayList<>();
        for (Field field : clazz.getFields()) {
            if (!Modifier.isPublic(field.getModifiers())) {
                continue;
            }
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            if (Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            result.add(field);
        }
        return result;
    }
}
