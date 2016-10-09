package com.basmapp.marshal.localdb.entities;

import com.basmapp.marshal.localdb.DBObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FkData extends ColumnData {

    private Class<? extends DBObject> fkClass;
    private String valueColumn;

    public FkData(Field field, String valueColumn, String fkColumn, Class<? extends DBObject> fkClass, Method setter) {
        super(fkColumn, field, setter);
        this.fkClass = fkClass;
        this.valueColumn = valueColumn;
    }

    public Class<? extends DBObject> getFkClass() {
        return fkClass;
    }

    public String getValueColumn() {
        return valueColumn;
    }
}
