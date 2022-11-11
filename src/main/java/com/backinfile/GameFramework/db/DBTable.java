package com.backinfile.GameFramework.db;

import com.backinfile.support.func.Function0;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

class DBTable {
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
