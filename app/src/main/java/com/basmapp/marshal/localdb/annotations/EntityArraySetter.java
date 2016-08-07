package com.basmapp.marshal.localdb.annotations;

import com.basmapp.marshal.localdb.DBObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EntityArraySetter {
    String fkColumnName();
    Class<? extends DBObject> entityClass();
}
