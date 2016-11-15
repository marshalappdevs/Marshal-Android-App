package com.basmapp.marshal.localdb.entities;

import com.basmapp.marshal.localdb.DBObject;
import com.basmapp.marshal.localdb.annotations.ForeignKeyEntity;
import com.basmapp.marshal.localdb.annotations.ForeignKeyEntityArray;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FkData extends ColumnData {

    private Class<? extends DBObject> fkClass;
    private String valueColumn;

    public FkData(Field field, ForeignKeyEntity annotation, Method setter) {
        super(field, setter, annotation);
        this.fkClass = annotation.entityClass();
        this.valueColumn = annotation.valueColumnName();
    }

    public FkData(Field field, ForeignKeyEntityArray annotation, Method setter) {
        super(field, setter, annotation);
        this.fkClass = annotation.entityClass();
        this.valueColumn = annotation.valueColumnName();
    }

    public Class<? extends DBObject> getFkClass() {
        return fkClass;
    }

    public String getValueColumn() {
        return valueColumn;
    }
}
