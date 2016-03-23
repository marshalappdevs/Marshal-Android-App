package com.basmach.marshal.localdb;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import org.jetbrains.annotations.NotNull;
import com.basmach.marshal.localdb.annotations.Column;
import com.basmach.marshal.localdb.annotations.ColumnGetter;
import com.basmach.marshal.localdb.annotations.ColumnSetter;
import com.basmach.marshal.localdb.annotations.EntityArraySetter;
import com.basmach.marshal.localdb.annotations.EntitySetter;
import com.basmach.marshal.localdb.annotations.ForeignKeyEntity;
import com.basmach.marshal.localdb.annotations.ForeignKeyEntityArray;
import com.basmach.marshal.localdb.annotations.PrimaryKey;
import com.basmach.marshal.localdb.annotations.PrimaryKeySetter;
import com.basmach.marshal.localdb.annotations.TableName;
import com.basmach.marshal.localdb.interfaces.BackgroundTaskCallBack;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Ido on 9/19/2015.
 */
public abstract class DBObject {
    // Database fields
    public static final String TYPE_INT = "int";
    public static final String TYPE_LONG = "long";
    public static final String TYPE_STRING = "string";
    public static final String TYPE_BOOLEAN = "boolean";
    public static final String TYPE_DATE = "date";
    public static final String DATE_FORMAT = "dd-MM-yyyy HH:mm";
    private static final String SUCCESS_FLAG = "Done";

    private static SQLiteDatabase database;
    private LocalDBHelper dbHelper;
    private ArrayList<String> allColumnsList;
    private ArrayList<Method> allGetters;
    private ArrayList<Method> allSetters;
    private String tableName;
    private PrimaryKey primaryKey;

    public DBObject(Context context) {
        dbHelper = new LocalDBHelper(context);
        open();
        getAnnotations();
        getGettersAndSetters();
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getDatabase(Context context) {
        LocalDBHelper dbHelper = new LocalDBHelper(context);
        return dbHelper.getWritableDatabase();
    }

    private static <T> String getTableName (Class<T> targetClass) {
        if (targetClass.isAnnotationPresent(TableName.class)) {
            return targetClass.getAnnotation(TableName.class).name();
        }
        else {
            return null;
        }
    }

    private void getAnnotations(){
//        allColumns = new String[]{};
        allColumnsList = new ArrayList<>();
        allSetters = new ArrayList<>();
        allGetters = new ArrayList<>();

        if (this.getClass().isAnnotationPresent(TableName.class)) {
            tableName = this.getClass().getAnnotation(TableName.class).name();
        }

        Field[] fields = this.getClass().getDeclaredFields();
//        int columnsCount = 0;
        Annotation[] currFieldAnnotations;
        for (Field field:fields){
            currFieldAnnotations = field.getAnnotations();
            for (Annotation annotation:currFieldAnnotations) {
                if (annotation instanceof Column){
                    allColumnsList.add(((Column) annotation).name());
//                    allColumns[columnsCount] = ((Column) annotation).name();
//                    columnsCount ++;
                }
                else if (annotation instanceof PrimaryKey) {
                    allColumnsList.add(((PrimaryKey) annotation).columnName());
                    primaryKey = (PrimaryKey) annotation;
                }
                else if (annotation instanceof ForeignKeyEntity) {
                    allColumnsList.add(((ForeignKeyEntity) annotation).fkColumnName());
                }
                else if (annotation instanceof ForeignKeyEntityArray) {
                    allColumnsList.add(((ForeignKeyEntityArray) annotation).fkColumnName());
                }
            }
        }
    }

    private void getGettersAndSetters(){
        for (Method method:this.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(ColumnGetter.class)){
                allGetters.add(method);
            }
            else if (method.isAnnotationPresent(ColumnSetter.class) ||
                    method.isAnnotationPresent(EntitySetter.class) ||
                    method.isAnnotationPresent(EntityArraySetter.class)) {
                allSetters.add(method);
            }
        }
    }

    private void setId(Object id) throws Exception{
        try {
            for (Method setter:allSetters) {
                if (setter.isAnnotationPresent(PrimaryKeySetter.class)) {
                    setter.setAccessible(true);
                    setter.invoke(this,id);
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private Object getId(){
        for (Field field:this.getClass().getDeclaredFields()){
            if(field.isAnnotationPresent(PrimaryKey.class)){
                try {
                    field.setAccessible(true);
                    return field.get(this);
                }
                catch (Exception e){

                }
            }
        }

        return null;
    }

    private ContentValues getContentValues() throws Exception{
        // Get all values into a ContentValues object
        ContentValues values = new ContentValues();
        for(Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                // Field has the annotation "@Column"
                Column column = field.getAnnotation(Column.class);
                try{
                    field.setAccessible(true);
                    Object value = field.get(this);
                    if (value instanceof Integer) {
                        values.put(column.name(),(Integer)value);
                    }
                    else if (value instanceof Long){
                        values.put(column.name(),(Long)value);
                    }
                    else if (value instanceof String){
                        values.put(column.name(),(String)value);
                    }
                    else if (value instanceof Boolean){
                        values.put(column.name(),(Boolean)value);
                    }
                    else if (value instanceof Date) {
                        values.put(column.name(),(dateToString((Date)value)));
                    }
                }
                catch (Exception e){
                    throw e;
                }
            } else if (field.isAnnotationPresent(ForeignKeyEntity.class)) {
                ForeignKeyEntity foreignKey = field.getAnnotation(ForeignKeyEntity.class);
                try {
                    field.setAccessible(true);
                    Class<?> entityClass = field.getType();
                    Object entity = entityClass.cast(field.get(this));
                    if (entity != null) {
                        Object fkValue = ((DBObject) entity).getId();

                        if(fkValue instanceof Integer){
                            values.put(foreignKey.fkColumnName(),(Integer)fkValue);
                        }
                        else if(fkValue instanceof Long){
                            values.put(foreignKey.fkColumnName(),(Long)fkValue);
                        }
                        else if(fkValue instanceof String){
                            values.put(foreignKey.fkColumnName(),(String)fkValue);
                        }
                    }
                }
                catch (Exception e) {
                    throw e;
                }
            } else if (field.isAnnotationPresent(ForeignKeyEntityArray.class)) {
                ForeignKeyEntityArray foreignKey = field.getAnnotation(ForeignKeyEntityArray.class);
                try {
                    field.setAccessible(true);
                    ArrayList<? extends DBObject> objectsArrayList =
                            (ArrayList<? extends DBObject>)field.get(this);
//                    Class<? extends DBObject> entityClass = foreignKey.entityClass();
                    if (objectsArrayList != null) {
                        String fkValue = "";

                        for (Object currObject:objectsArrayList) {

                            Object entityId = ((DBObject)currObject).getId();

                            if (entityId != null) {
                                if(!fkValue.equals("")) {
                                    fkValue = fkValue + "," + entityId.toString();
                                }
                                else fkValue = entityId.toString();
                            }
                        }

                        values.put(foreignKey.fkColumnName(),fkValue);
                    }
                }
                catch (Exception e) {
                    throw e;
                }
            }
        }

        return values;
    }

    public Method getSetterByColumnName(String columnName) throws Exception {
        for (Method setter:allSetters) {
            String currSetterColumnName = "";
            if(setter.isAnnotationPresent(ColumnSetter.class)) {
                ColumnSetter columnSetter = setter.getAnnotation(ColumnSetter.class);
                currSetterColumnName = columnSetter.columnName();
            } else if (setter.isAnnotationPresent(EntitySetter.class)) {
                EntitySetter entitySetter = setter.getAnnotation(EntitySetter.class);
                currSetterColumnName = entitySetter.fkColumnName();
            }

            if (columnName.equals(currSetterColumnName)) {
                return setter;
            }
        }
        throw new Exception("didn't found a setter for this column. Please contact the programmer");
    }
    
    public void setAttribute(Object setterOwner, String columnName, boolean isNullable, Object value, int validationType)
            throws Exception {
        try {
            Validator.setAttribute(setterOwner, getSetterByColumnName(columnName), columnName,
                    isNullable, value, validationType);
        } catch (Exception e) {
            throw e;
        }
    }

    public Date stringToDate(String string) throws Exception{
        if (string != null) {
            DateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
            return format.parse(string);
        } else {
            return null;
        }
    }

    public String dateToString(@NotNull Date date) {
        if (date != null) {
            DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            return dateFormat.format(date);
        } else return "";
    }

    public Object cursorToObject(Cursor cursor, Context context) throws Exception{
        if (cursor.getCount() > 0) {
            for (String column:allColumnsList) {
                for (Method setter:allSetters) {
                    if (setter.isAnnotationPresent(ColumnSetter.class)) {
                        ColumnSetter columnSetter = setter.getAnnotation(ColumnSetter.class);
                        if (column.equals(columnSetter.columnName())) {
                            try {
                                setter.setAccessible(true);
                                if (columnSetter.type().equals(TYPE_INT)) {
                                    setter.invoke(this, cursor.getInt(cursor.getColumnIndex(column)));
                                } else if (columnSetter.type().equals(TYPE_LONG)) {
                                    setter.invoke(this, cursor.getLong(cursor.getColumnIndex(column)));
                                } else if (columnSetter.type().equals(TYPE_BOOLEAN)) {
                                    setter.invoke(this, (cursor.getInt(cursor.getColumnIndex(column))) != 1);
                                } else if (columnSetter.type().equals(TYPE_STRING)) {
                                    setter.invoke(this, cursor.getString(cursor.getColumnIndex(column)));
                                } else if (columnSetter.type().equals(TYPE_DATE)) {
                                    setter.invoke(this,
                                            stringToDate(cursor.
                                                    getString(cursor.getColumnIndex(column))));

                                }
                            } catch (Exception e) {
                                throw e;
                            }
                        }
                    }
                    if (setter.isAnnotationPresent(EntitySetter.class)) {
                        EntitySetter entitySetter = setter.getAnnotation(EntitySetter.class);
                        if (column.equals(entitySetter.fkColumnName())) {
                            try {
                                Class<? extends DBObject> entityClass = entitySetter.entityClass();

                                Object entityInstance = entityClass
                                        .getConstructor(Context.class)
                                        .newInstance(context);

                                entityClass.cast(entityInstance)
                                        .getById(cursor.getLong(cursor
                                                        .getColumnIndex(entitySetter.fkColumnName())),
                                                context);

                                setter.setAccessible(true);
                                setter.invoke(this, entityInstance);
                            } catch (Exception e) {
                                throw e;
                            }
                        }
                    }
                    if (setter.isAnnotationPresent(EntityArraySetter.class)) {
                        EntityArraySetter entityArraySetter =
                                setter.getAnnotation(EntityArraySetter.class);
                        if (column.equals(entityArraySetter.fkColumnName())) {
                            try {
                                ArrayList<Object> objectArray = new ArrayList<>();
                                Class<? extends DBObject> entityClass = entityArraySetter.entityClass();

                                String idsArray = cursor
                                        .getString(cursor
                                                .getColumnIndex(entityArraySetter.fkColumnName()));

                                if (idsArray != null) {
                                    int startIndex = 0;

                                    while (startIndex <= (idsArray.length() - 1)) {

                                        Object entityInstance = entityClass
                                                .getConstructor(Context.class)
                                                .newInstance(context);

                                        long entityId = Long.valueOf(idsArray
                                                .substring(startIndex, startIndex + 1));

                                        entityClass.cast(entityInstance).getById(entityId, context);

                                        objectArray.add(entityInstance);

                                        startIndex += 2;
                                    }

                                    setter.setAccessible(true);
                                    setter.invoke(this, objectArray);
                                }

                            } catch (Exception e) {
                                throw e;
                            }
                        }
                    }
                }
            }
        }

        return this.getClass();
    }

    public void close() {
        dbHelper.close();
    }

    public void create() throws Exception {
        open();
        try {
            ContentValues values = getContentValues();
            long objectId = database.insertOrThrow(tableName, null, values);
            setId(objectId);
        } catch (Exception e) {
            if (e.getMessage() != null) {
                throw e;
            }
        } finally {
            close();
        }
    }

    public void save() throws Exception {
        try {
            ContentValues values = getContentValues();
            open();
            database.update(tableName, values, primaryKey.columnName() + " = " + getId(), null);
        } catch (Exception e) {
            if (e.getMessage() != null) {
                throw e;
            }
        } finally {
            close();
        }
    }

    public void getById(long id, Context context) throws Exception {
        open();
        Cursor cursor = database.query(tableName,
                null, primaryKey.columnName() + " = " + id, null,
                null, null, null);
        cursor.moveToFirst();
        try {
            cursorToObject(cursor, context);
        } catch (Exception e) {
            throw e;
        } finally {
            cursor.close();
            close();
        }

    }

    public void delete() throws Exception{
        open();
        database.delete(tableName, primaryKey.columnName() + " = " + getId(), null);
        close();
    }

    public static int count(Context context, Class<? extends DBObject> targetClass) {
        database = getDatabase(context);

        Cursor cursor = database.query(getTableName(targetClass),
                null, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        database.close();
        return count;
    }

    public static List<Object> getAll(String orderByColumnName,
                                      Context context,
                                      Class<? extends DBObject> targetClass) throws Exception{

        List<Object> allObjects = new ArrayList<>();
        database = getDatabase(context);

        Cursor cursor = database.query(getTableName(targetClass),
                null, null, null, null, null, orderByColumnName + " ASC");

        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            try{
                while (!cursor.isAfterLast()) {
                    Object currObject = targetClass.getConstructor(Context.class)
                            .newInstance(context);
                    (targetClass.cast(currObject)).cursorToObject(cursor, context);
                    allObjects.add(currObject);
                    cursor.moveToNext();
                }

                cursor.close();
                database.close();
                return allObjects;
            }
            catch (Exception e){
                cursor.close();
                database.close();
                throw e;
            }
        } else return allObjects;
    }

    public static List<Object> getAllByColumn(String columnName,
                                              Object value,
                                              String orderByColumnName,
                                              Context context,
                                              Class<? extends DBObject> targetClass) throws Exception {
        List<Object> allObjects = new ArrayList<>();

        Cursor cursor = database.query(getTableName(targetClass),
                null, columnName + " = " + value, null, null, null, orderByColumnName + " ASC");

        cursor.moveToFirst();

        try{
            while (!cursor.isAfterLast()) {
                Object currObject = targetClass.getConstructor(Context.class).newInstance(context);
                (targetClass.cast(currObject)).cursorToObject(cursor, context);
                allObjects.add(currObject);
                cursor.moveToNext();
            }

            cursor.close();
            return allObjects;
        }
        catch (Exception e) {
            cursor.close();
            throw e;
        }

        // Make sure to close the cursor

    }



    public void createInBackground(final Context context,
                                   final boolean showProgressBar,
                                   final BackgroundTaskCallBack callBack) {

        new AsyncTask<Void, Void, String>() {

            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                if (showProgressBar) {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.show();
                }
            }


            @Override
            protected String doInBackground(Void... voids) {
                try {
                    create();
                    return SUCCESS_FLAG;
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String strResult) {
                super.onPostExecute(strResult);

                if (showProgressBar) {
                    progressDialog.dismiss();
                }

                if (strResult.equals(SUCCESS_FLAG)) {
                    callBack.onSuccess(strResult, null);
                } else {
                    callBack.onError(strResult);
                }
            }
        }.execute();
    }

    public void saveInBackground(final Context context,
                                   final boolean showProgressBar,
                                   final BackgroundTaskCallBack callBack) {

        new AsyncTask<Void, Void, String>() {

            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                if (showProgressBar) {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.show();
                }
            }


            @Override
            protected String doInBackground(Void... voids) {
                try {
                    save();
                    return SUCCESS_FLAG;
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String strResult) {
                super.onPostExecute(strResult);

                if (showProgressBar) {
                    progressDialog.dismiss();
                }

                if (strResult.equals(SUCCESS_FLAG)) {
                    callBack.onSuccess(strResult, null);
                } else {
                    callBack.onError(strResult);
                }
            }
        }.execute();
    }

    public void deleteInBackground(final Context context,
                                   final boolean showProgressBar,
                                   final BackgroundTaskCallBack callBack) {

        new AsyncTask<Void, Void, String>() {

            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                if (showProgressBar) {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.show();
                }
            }


            @Override
            protected String doInBackground(Void... voids) {
                try {
                    delete();
                    return SUCCESS_FLAG;
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String strResult) {
                super.onPostExecute(strResult);

                if (showProgressBar) {
                    progressDialog.dismiss();
                }

                if (strResult.equals(SUCCESS_FLAG)) {
                    callBack.onSuccess(strResult, null);
                } else {
                    callBack.onError(strResult);
                }
            }
        }.execute();
    }

    public void getByIdInBackground(final Context context,
                                    final boolean showProgressBar,
                                    final long objectId,
                                    final BackgroundTaskCallBack callBack) {

        new AsyncTask<Void, Void, String>() {

            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                if (showProgressBar) {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.show();
                }
            }


            @Override
            protected String doInBackground(Void... voids) {
                try {
                    getById(objectId, context);
                    return SUCCESS_FLAG;
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String strResult) {
                super.onPostExecute(strResult);

                if (showProgressBar) {
                    progressDialog.dismiss();
                }

                if (strResult.equals(SUCCESS_FLAG)) {
                    callBack.onSuccess(strResult, null);
                } else {
                    callBack.onError(strResult);
                }
            }
        }.execute();
    }

    public void countInBackground(final Context context,
                                  final boolean showProgressBar,
                                  final Class<? extends DBObject> targetClass,
                                  final BackgroundTaskCallBack callBack) {

        new AsyncTask<Void, Void, String>() {

            ProgressDialog progressDialog;
            List<Object> data;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                if (showProgressBar) {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.show();
                }
            }


            @Override
            protected String doInBackground(Void... voids) {
                try {
                    int count = count(context, targetClass);
                    data = new ArrayList<>();
                    data.add(count);
                    return SUCCESS_FLAG;
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String strResult) {
                super.onPostExecute(strResult);

                if (showProgressBar) {
                    progressDialog.dismiss();
                }

                if (strResult.equals(SUCCESS_FLAG)) {
                    callBack.onSuccess(strResult, data);
                } else {
                    callBack.onError(strResult);
                }
            }
        }.execute();
    }

    public static void getAllInBackground(final String orderByColumn,
                                   final Class<? extends DBObject> targetClass,
                                   final Context context,
                                   final boolean showProgressBar,
                                   final BackgroundTaskCallBack callBack) {

        new AsyncTask<Void, Void, String>() {

            ProgressDialog progressDialog;
            List<Object> data;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                if (showProgressBar) {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.show();
                }
            }


            @Override
            protected String doInBackground(Void... voids) {
                try {
                    data = getAll(orderByColumn, context, targetClass);
                    return SUCCESS_FLAG;
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String strResult) {
                super.onPostExecute(strResult);

                if (showProgressBar) {
                    progressDialog.dismiss();
                }

                if (strResult.equals(SUCCESS_FLAG)) {
                    callBack.onSuccess(strResult, data);
                } else {
                    callBack.onError(strResult);
                }
            }
        }.execute();
    }

}
