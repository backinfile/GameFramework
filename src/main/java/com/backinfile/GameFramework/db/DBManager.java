package com.backinfile.GameFramework.db;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.support.Utils;
import com.backinfile.support.func.Action2;
import com.backinfile.support.func.Function1;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class DBManager {
    public static final String DB_NAME = "world";

    private final static Map<Class<?>, DBTable> tableMap = new HashMap<>();

    private static class DBTable {
        public static DBTable EmptyInstance = new DBTable("");
        public String tableName;
        public DBField keyFiled;
        public List<DBField> fields = new ArrayList<>();

        public DBTable(String tableName) {
            this.tableName = tableName;
        }

        public DBField getFiledByName(String filedName) {
            for (DBField field : fields) {
                if (field.name.equals(filedName)) {
                    return field;
                }
            }
            return null;
        }
    }

    private static class DBField {
        public boolean index;
        public String name;
        public FieldType type;
        public Action2<Object, Object> setter;
        public Function1<Object, Object> getter;
    }

    private enum FieldType {
        Int(int.class, "INTEGER"),
        Long(long.class, "BIGINT"),
        Float(float.class, "FLOAT"),
        Double(double.class, "DOUBLE"),
        String(String.class, "TEXT"),
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

            int indexCount = 0;
            DBTable table = new DBTable(annotation.table());
            for (Field field : getFields(clazz)) {
                for (FieldType fieldType : FieldType.values()) {
                    if (field.getType() == fieldType.javaType) {
                        DBField dbField = new DBField();
                        dbField.index = field.getName().equals(annotation.key());
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
                        if (dbField.index) {
                            indexCount++;
                            table.keyFiled = dbField;
                        }
                    }
                }
            }
            if (indexCount != 1) {
                LogCore.db.error("{} index count = {}", clazz.getSimpleName(), indexCount);
                continue;
            }
            tableMap.put(clazz, table);
        }
    }

    // 更新表结构
    public static void updateTableStruct(Connection connection) {
        for (DBTable table : tableMap.values()) {
            DBTable oldTable = queryOldTableStruct(connection, table.tableName);
            if (oldTable == null) { // 不存在 直接新建
                String sql = getInsertTableSql(table);
                executeSql(connection, sql);
            } else { // 已存在 检查修改
                for (String sql : getModifySqlString(oldTable, table)) {
                    executeSql(connection, sql);
                }
            }
        }

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
                field.index = key == 1;

                table.fields.add(field);
                if (field.index) {
                    table.keyFiled = field;
                }
            }
        } catch (Exception e) {
            LogCore.db.error("execute sql " + sql + " error", e);
        }
        if (table.fields.isEmpty()) {
            return null;
        }
        return table;
    }

    private static String getInsertTableSql(DBTable table) {
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
            if (field.index) {
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

    private static List<String> getModifySqlString(DBTable oldTable, DBTable table) {
        List<String> result = new ArrayList<>();
        Set<String> updateFieldNameSet = new HashSet<>();
        updateFieldNameSet.addAll(oldTable.fields.stream().map(f -> f.name).collect(Collectors.toSet()));
        updateFieldNameSet.addAll(table.fields.stream().map(f -> f.name).collect(Collectors.toSet()));

        for (String filedName : updateFieldNameSet) {
            DBField oldField = oldTable.getFiledByName(filedName);
            DBField newField = table.getFiledByName(filedName);
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
        return result;
    }

    private static void executeSql(Connection connection, String sql) {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            LogCore.db.info("execute sql " + sql);
        } catch (Exception e) {
            LogCore.db.error("execute sql " + sql + " error", e);
        }
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

    public static String getTableName(Class<? extends EntityBase> clazz) {
        return tableMap.getOrDefault(clazz, DBTable.EmptyInstance).tableName;
    }

    public static String getKeyName(Class<? extends EntityBase> clazz) {
        return tableMap.getOrDefault(clazz, DBTable.EmptyInstance).keyFiled.name;
    }
}
