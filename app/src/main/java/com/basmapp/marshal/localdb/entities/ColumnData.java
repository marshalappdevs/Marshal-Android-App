package com.basmapp.marshal.localdb.entities;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ColumnData {

    private String name;
    private Field field;
    private Method setter;

    public ColumnData(String name, Field field, Method setter) {
        this.name = name;
        this.field = field;
        this.setter = setter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Method getSetter() {
        return setter;
    }
}
