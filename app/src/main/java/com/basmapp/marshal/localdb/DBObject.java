package com.basmapp.marshal.localdb;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.basmapp.marshal.R;
import com.basmapp.marshal.localdb.annotations.Column;
import com.basmapp.marshal.localdb.annotations.ColumnGetter;
import com.basmapp.marshal.localdb.annotations.ColumnSetter;
import com.basmapp.marshal.localdb.annotations.EntityArraySetter;
import com.basmapp.marshal.localdb.annotations.EntitySetter;
import com.basmapp.marshal.localdb.annotations.ForeignKeyEntity;
import com.basmapp.marshal.localdb.annotations.ForeignKeyEntityArray;
import com.basmapp.marshal.localdb.annotations.PrimaryKey;
import com.basmapp.marshal.localdb.annotations.PrimaryKeySetter;
import com.basmapp.marshal.localdb.annotations.TableName;
import com.basmapp.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmapp.marshal.util.DateHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class DBObject {
    // Database fields
    public static final String TYPE_INT = "int";
    public static final String TYPE_LONG = "long";
    public static final String TYPE_STRING = "string";
    public static final String TYPE_BOOLEAN = "boolean";
    public static final String TYPE_DATE = "date";
    public static final String TYPE_DOUBLE = "double";
    public static final String DATE_FORMAT = "dd-MM-yyyy HH:mm";
    private static final String SUCCESS_FLAG = "Done";
    private static final String ERROR_FLAG = "Error";

    private Context mContext;
    private ArrayList<String> allColumnsList;
    private ArrayList<Method> allGetters;
    private ArrayList<Method> allSetters;
    private String tableName;
    private PrimaryKey primaryKey;

    public DBObject() {}

    public DBObject(Context context) {
        this.mContext = context;
        getAnnotations();
        getGettersAndSetters();
    }

    public void Ctor(Context context) {
        this.mContext = context;
        getAnnotations();
        getGettersAndSetters();
    }

    protected String prepareStringForSql(String value) {
        if (value != null && !value.equals("")) {
            return "'" + value.replace("'","''") + "'";
        } else {
            return "''";
        }
    }

    private static SQLiteDatabase getDatabase(Context context) {
        return LocalDBHelper.getHelperInstance(context).getWritableDatabase();
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
        allColumnsList = new ArrayList<>();
        allSetters = new ArrayList<>();
        allGetters = new ArrayList<>();

        if (this.getClass().isAnnotationPresent(TableName.class)) {
            tableName = this.getClass().getAnnotation(TableName.class).name();
        }

        Field[] fields = this.getClass().getDeclaredFields();
        Annotation[] currFieldAnnotations;
        for (Field field:fields){
            currFieldAnnotations = field.getAnnotations();
            for (Annotation annotation:currFieldAnnotations) {
                if (annotation instanceof Column){
                    allColumnsList.add(((Column) annotation).name());
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
                    e.printStackTrace();
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
                    else if (value instanceof Double){
                        values.put(column.name(),(Double) value);
                    }
                    else if (value instanceof String){
                        values.put(column.name(),(String)value);
                    }
                    else if (value instanceof Boolean){
                        values.put(column.name(),(Boolean)value);
                    }
                    else if (value instanceof Date) {
                        values.put(column.name(),(((Date)value)).getTime());
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
        Validator.setAttribute(setterOwner, getSetterByColumnName(columnName), columnName,
                isNullable, value, validationType);
    }

    public Date stringToDate(String string) throws Exception{
        if (string != null) {
            return DateHelper.stringToDate(string);
        } else {
            return null;
        }
    }

    public String dateToString(@NonNull Date date) {
        return DateHelper.dateToString(date);
    }

    public Object cursorToObject(Cursor cursor, Context context) throws Exception{
        if (cursor.getCount() > 0) {
//            for (String column:allColumnsList) {
                for (Method setter:allSetters) {
                    if (setter.isAnnotationPresent(ColumnSetter.class)) {
                        ColumnSetter columnSetter = setter.getAnnotation(ColumnSetter.class);
//                        if (column.equals(columnSetter.columnName())) {
                            try {
                                setter.setAccessible(true);
                                if (columnSetter.type().equals(TYPE_INT)) {
                                    setter.invoke(this, cursor.getInt(cursor.getColumnIndex(columnSetter.columnName())));
                                } else if (columnSetter.type().equals(TYPE_LONG)) {
                                    setter.invoke(this, cursor.getLong(cursor.getColumnIndex(columnSetter.columnName())));
                                } else if (columnSetter.type().equals(TYPE_DOUBLE)) {
                                    setter.invoke(this, (cursor.getDouble(cursor.getColumnIndex(columnSetter.columnName()))));
                                } else if (columnSetter.type().equals(TYPE_BOOLEAN)) {
                                    setter.invoke(this, (cursor.getInt(cursor.getColumnIndex(columnSetter.columnName()))) != 0);
                                } else if (columnSetter.type().equals(TYPE_STRING)) {
                                    setter.invoke(this, cursor.getString(cursor.getColumnIndex(columnSetter.columnName())));
                                } else if (columnSetter.type().equals(TYPE_DATE)) {
                                    setter.invoke(this,
                                            new Date(cursor.getLong(cursor.getColumnIndex(columnSetter.columnName()))));

                                }
                            } catch (Exception e) {
                                throw e;
                            }

//                            break;
//                        }
                    }
                    else if (setter.isAnnotationPresent(EntitySetter.class)) {
                        EntitySetter entitySetter = setter.getAnnotation(EntitySetter.class);
//                        if (column.equals(entitySetter.fkColumnName())) {
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

//                            break;
//                        }
                    }
                    else if (setter.isAnnotationPresent(EntityArraySetter.class)) {
                        EntityArraySetter entityArraySetter =
                                setter.getAnnotation(EntityArraySetter.class);
//                        if (column.equals(entityArraySetter.fkColumnName())) {
                            try {
                                ArrayList<Object> objectArray = new ArrayList<>();
                                Class<? extends DBObject> entityClass = entityArraySetter.entityClass();

                                String idsArray = cursor
                                        .getString(cursor
                                                .getColumnIndex(entityArraySetter.fkColumnName()));

                                if (idsArray != null && !idsArray.equals("")) {

                                    String[] ids = idsArray.split(",");

                                    for(String id : ids) {
                                        Object entityInstance = entityClass
                                                .getConstructor(Context.class)
                                                .newInstance(context);

                                        long idLong = Long.valueOf(id);

                                        entityClass.cast(entityInstance).getById(idLong, context);

                                        objectArray.add(entityInstance);
                                    }

                                    setter.setAccessible(true);
                                    setter.invoke(this, objectArray);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                throw e;
                            }

//                            break;
//                        }
                    }
                }
//            }
        }

        return this.getClass();
    }

    public void create() throws Exception {
        try {
            ContentValues values = getContentValues();
            long objectId = LocalDBHelper.getDatabaseWritableInstance(mContext).insertOrThrow(tableName, null, values);
            setId(objectId);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage() != null) {
                throw e;
            }
        }
    }

    public void save() throws Exception {
        try {
            ContentValues values = getContentValues();
            LocalDBHelper.getDatabaseWritableInstance(mContext).update(tableName, values, primaryKey.columnName() + " = " + getId(), null);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage() != null) {
                throw e;
            }
        }
    }

    public void getById(long id, Context context) throws Exception {
        Cursor cursor = LocalDBHelper.getDatabaseWritableInstance(context).query(tableName,
                null, primaryKey.columnName() + " = " + id, null,
                null, null, null);
        cursor.moveToFirst();
        try {
            cursorToObject(cursor, context);
        } catch (Exception e) {
            throw e;
        } finally {
            cursor.close();
        }

    }

    public void delete() throws Exception{
        LocalDBHelper.getDatabaseWritableInstance(mContext).delete(tableName, primaryKey.columnName() + " = " + getId(), null);
    }

    public static int count(Context context, Class<? extends DBObject> targetClass) throws Exception {
        String query = "SELECT COUNT(*) FROM " + getTableName(targetClass);
        Cursor cursor = LocalDBHelper.getDatabaseWritableInstance(context).rawQuery(query,null);
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public static int countByColumn(Context context, Class<? extends DBObject> targetClass,
                            String filterColumn, Object filterValue) throws Exception {
        if (filterValue instanceof String) filterValue = "'" + filterValue + "'";
        String query = "SELECT COUNT(*) FROM " + getTableName(targetClass) +
                " WHERE " + filterColumn + "=" + filterValue;
        Cursor cursor = LocalDBHelper.getDatabaseWritableInstance(context).rawQuery(query,null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public static float getAverageByColumn(Context context, Class<? extends DBObject> targetClass,
                                 String avgColumn, String filterColumn, Object filterValue) throws Exception {
        if (filterValue instanceof String) filterValue = "'" + filterValue + "'";
        String query = "SELECT AVG(" + avgColumn + ") FROM " + getTableName(targetClass) +
                " WHERE " + filterColumn + "=" + filterValue;
        Cursor cursor = LocalDBHelper.getDatabaseWritableInstance(context).rawQuery(query, null);
        cursor.moveToFirst();
        float average = cursor.getFloat(0);
        cursor.close();
        return average;
    }

    public static List<Object> getAll(String orderByColumnName,
                                      Context context,
                                      Class<? extends DBObject> targetClass) throws Exception{

        List<Object> allObjects = new ArrayList<>();

        Cursor cursor = LocalDBHelper.getDatabaseWritableInstance(context).query(getTableName(targetClass),
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
                return allObjects;
            }
            catch (Exception e){
                cursor.close();
                throw e;
            }
        } else {
            cursor.close();
            return allObjects;
        }
    }

    public static List<Object> getAllByColumn(String columnName,
                                              Object value,
                                              String orderByColumnName,
                                              Context context,
                                              Class<? extends DBObject> targetClass) throws Exception {
        List<Object> allObjects = new ArrayList<>();

        if(value instanceof Boolean)
            value = (boolean)value ? 1 : 0;
        else if (value instanceof String)
            value = "'" + value + "'";
        Cursor cursor = LocalDBHelper.getDatabaseWritableInstance(context).query(getTableName(targetClass),
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
    }

    public static List<Object> query(Context context,
                        Class<? extends DBObject> targetClass,
                             String[] whereColumns,
                             String[] whereArgs) throws Exception {

        List<Object> allObjects = new ArrayList<>();
        String whereColumnsWithQuestionMark = null;

        if (whereColumns != null) {
            whereColumnsWithQuestionMark = "";
            for (int position = 0; position < whereColumns.length; position++) {
                if (position < whereColumns.length - 1) {
                    whereColumnsWithQuestionMark += (whereColumns[position] + "=? AND ");
                } else {
                    whereColumnsWithQuestionMark += (whereColumns[position] + "=?");
                }
            }
        }

        Cursor cursor = LocalDBHelper.getDatabaseWritableInstance(context).query(getTableName(targetClass), null,
                whereColumnsWithQuestionMark, whereArgs, null, null, null);

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
                    progressDialog.setMessage(context.getResources().getString(R.string.loading));
                    progressDialog.show();
                }
            }


            @Override
            protected String doInBackground(Void... voids) {
                try {
                    create();
                    return SUCCESS_FLAG;
                } catch (Exception e) {
                    if (e.getMessage() != null) {
                        return e.getMessage();
                    } else {
                        return ERROR_FLAG;
                    }
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
                    progressDialog.setMessage(context.getResources().getString(R.string.loading));
                    progressDialog.show();
                }
            }


            @Override
            protected String doInBackground(Void... voids) {
                try {
                    save();
                    return SUCCESS_FLAG;
                } catch (Exception e) {
                    if (e.getMessage() != null) {
                        return e.getMessage();
                    } else {
                        return ERROR_FLAG;
                    }
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
                    progressDialog.setMessage(context.getResources().getString(R.string.loading));
                    progressDialog.show();
                }
            }


            @Override
            protected String doInBackground(Void... voids) {
                try {
                    delete();
                    return SUCCESS_FLAG;
                } catch (Exception e) {
                    if (e.getMessage() != null) {
                        return e.getMessage();
                    } else {
                        return ERROR_FLAG;
                    }
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
                    progressDialog.setMessage(context.getResources().getString(R.string.loading));
                    progressDialog.show();
                }
            }


            @Override
            protected String doInBackground(Void... voids) {
                try {
                    getById(objectId, context);
                    return SUCCESS_FLAG;
                } catch (Exception e) {
                    if (e.getMessage() != null) {
                        return e.getMessage();
                    } else {
                        return ERROR_FLAG;
                    }
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
                    progressDialog.setMessage(context.getResources().getString(R.string.loading));
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
                    if (e.getMessage() != null) {
                        return e.getMessage();
                    } else {
                        return ERROR_FLAG;
                    }
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
                    progressDialog = getProgressDialog(context);
                    progressDialog.show();
                }
            }


            @Override
            protected String doInBackground(Void... voids) {
                try {
                    data = getAll(orderByColumn, context, targetClass);
                    return SUCCESS_FLAG;
                } catch (Exception e) {
                    if (e.getMessage() != null) {
                        return e.getMessage();
                    } else {
                        return ERROR_FLAG;
                    }
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

    public static void getByColumnInBackground(final boolean showProgressBar,
                                               final String columnName,
                                               final Object value,
                                               final String orderByColumnName,
                                               final Context context,
                                               final Class<? extends DBObject> targetClass,
                                               final BackgroundTaskCallBack callBack) {

        new AsyncTask<Void, Void, String>() {

            ProgressDialog progressDialog;
            List<Object> data;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                if (showProgressBar) {
                    progressDialog = getProgressDialog(context);
                    progressDialog.show();
                }
            }


            @Override
            protected String doInBackground(Void... voids) {
                try {
                    data = getAllByColumn(columnName, value, orderByColumnName, context, targetClass);
                    return SUCCESS_FLAG;
                } catch (Exception e) {
                    if (e.getMessage() != null) {
                        return e.getMessage();
                    } else {
                        return ERROR_FLAG;
                    }
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

    public static void queryInBackground(final Class<? extends DBObject> targetClass,
                                         final Context context,
                                         final boolean showProgressBar,
                                         final String[] whereColumns,
                                         final String[] whereArgs,
                                         final BackgroundTaskCallBack callBack) {

        new AsyncTask<Void, Void, String>() {

            ProgressDialog progressDialog;
            List<Object> data;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                if (showProgressBar) {
                    progressDialog = getProgressDialog(context);
                    progressDialog.show();
                }
            }


            @Override
            protected String doInBackground(Void... voids) {
                try {
                    data = query(context, targetClass, whereColumns, whereArgs);
                    return SUCCESS_FLAG;
                } catch (Exception e) {
                    if (e.getMessage() != null) {
                        return e.getMessage();
                    } else {
                        return ERROR_FLAG;
                    }
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

    public static void countByColumnInBackground(final Class<? extends DBObject> targetClass,
                                         final Context context,
                                         final boolean showProgressBar,
                                         final String filterColumn,
                                         final String filterValue,
                                         final BackgroundTaskCallBack callBack) {

        new AsyncTask<Void, Void, String>() {

            ProgressDialog progressDialog;
            List<Object> data;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                if (showProgressBar) {
                    progressDialog = getProgressDialog(context);
                    progressDialog.show();
                }
            }


            @Override
            protected String doInBackground(Void... voids) {
                try {
                    data = new ArrayList<>();
                    data.add(countByColumn(context, targetClass, filterColumn, filterValue));
                    return SUCCESS_FLAG;
                } catch (Exception e) {
//                    database.close();
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

    public static void getAverageByColumnInBackground(final Class<? extends DBObject> targetClass,
                                                 final Context context,
                                                 final boolean showProgressBar,
                                                 final String avgColumn,
                                                 final String filterColumn,
                                                 final String filterValue,
                                                 final BackgroundTaskCallBack callBack) {

        new AsyncTask<Void, Void, String>() {

            ProgressDialog progressDialog;
            List<Object> data;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                if (showProgressBar) {
                    progressDialog = getProgressDialog(context);
                    progressDialog.show();
                }
            }


            @Override
            protected String doInBackground(Void... voids) {
                try {

                    data = new ArrayList<>();
                    data.add(getAverageByColumn(context, targetClass,avgColumn, filterColumn, filterValue));
                    return SUCCESS_FLAG;
                } catch (Exception e) {
                    if (e.getMessage() != null) {
                        return e.getMessage();
                    } else {
                        return ERROR_FLAG;
                    }
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

    public static void rawQueryInBackground(final String query, final Context context, final Class<? extends DBObject> targetClass,
                                     final boolean showProgressBar, final BackgroundTaskCallBack callBack) {
        new AsyncTask<Void, Void, String>() {

            ProgressDialog progressDialog;
            List<Object> data;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                if (showProgressBar) {
                    progressDialog = getProgressDialog(context);
                    progressDialog.show();
                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    data = rawQuery(context, query, targetClass);
                    return SUCCESS_FLAG;
                } catch (Exception e) {
                    if (e.getMessage() != null) {
                        return e.getMessage();
                    } else {
                        return ERROR_FLAG;
                    }
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

    public static List<Object> rawQuery(Context context, String query, Class<? extends DBObject> targetClass) throws Exception {
        List<Object> allObjects = new ArrayList<>();

        Cursor cursor = LocalDBHelper.getDatabaseWritableInstance(context).rawQuery(query, null);

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
                return allObjects;
            }
            catch (Exception e){
                cursor.close();
                throw e;
            }
        } else {
            cursor.close();
            return allObjects;
        }
    }

    private static ProgressDialog getProgressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }
}
