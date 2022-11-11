package com.backinfile.GameFramework.db;

import com.backinfile.support.func.Action2;
import com.backinfile.support.func.Function1;

class DBField {
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

    @SuppressWarnings("unchecked")
    public <T> T getFieldValue(Object obj) {
        return (T) getter.invoke(obj);
    }
}
