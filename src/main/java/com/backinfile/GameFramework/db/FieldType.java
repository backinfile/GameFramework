package com.backinfile.GameFramework.db;

import com.backinfile.GameFramework.LogCore;

import java.sql.ResultSet;
import java.sql.SQLException;

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
