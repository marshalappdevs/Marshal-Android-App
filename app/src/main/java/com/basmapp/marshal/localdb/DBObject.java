package com.basmapp.marshal.localdb;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import com.basmapp.marshal.R;
import com.basmapp.marshal.localdb.annotations.Column;
import com.basmapp.marshal.localdb.annotations.ForeignKeyEntity;
import com.basmapp.marshal.localdb.annotations.ForeignKeyEntityArray;
import com.basmapp.marshal.localdb.annotations.PrimaryKey;
import com.basmapp.marshal.localdb.annotations.TableName;
import com.basmapp.marshal.localdb.entities.ColumnData;
import com.basmapp.marshal.localdb.entities.FkData;
import com.basmapp.marshal.localdb.entities.PkData;
import com.basmapp.marshal.localdb.interfaces.BackgroundTaskCallBack;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class DBObject {

    private static final String SUCCESS_FLAG = "Done";
    private static final String ERROR_FLAG = "Error";

    private Context mContext;

    private String tableName;
    private PkData primaryKey;
    private ArrayList<ColumnData> mColumns;
    private ArrayList<FkData> mForeignKeys;
    private ArrayList<FkData> mArrayForeignKeys;

    protected DBObject() {
    }

    public DBObject(Context context) {
        this.mContext = context;
        initialize();
    }

    public void Ctor(Context context) {
        this.mContext = context;
        initialize();
    }

    private void initialize() {
        getFields();
        tableName = getTableName(getClass());
    }

    protected abstract boolean isPrimaryKeyAutoIncrement();

    protected String prepareStringForSql(String value) {
        if (value != null && !value.equals("")) {
            return "'" + value.replace("'", "''") + "'";
        } else {
            return "''";
        }
    }

    private static String getTableName(Class<? extends DBObject> targetClass) {
        if (targetClass.isAnnotationPresent(TableName.class)) {
            return targetClass.getAnnotation(TableName.class).name();
        } else {
            return null;
        }
    }

    private void getFields() {
        Field[] declaredFields = getClass().getDeclaredFields();

        mColumns = new ArrayList<>();
        mForeignKeys = new ArrayList<>();
        mArrayForeignKeys = new ArrayList<>();

        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                primaryKey = new PkData(field, field.getAnnotation(PrimaryKey.class), getSetter(field));
            } else if (field.isAnnotationPresent(Column.class)) {
                mColumns.add(new ColumnData(field.getAnnotation(Column.class).name(), field, getSetter(field)));
            } else if (field.isAnnotationPresent(ForeignKeyEntity.class)) {
                ForeignKeyEntity annotation = field.getAnnotation(ForeignKeyEntity.class);
                mForeignKeys.add(new FkData(field, annotation.valueColumnName(),
                        annotation.fkColumnName(), annotation.entityClass(), getSetter(field)));
            } else if (field.isAnnotationPresent(ForeignKeyEntityArray.class)) {
                ForeignKeyEntityArray annotation = field.getAnnotation(ForeignKeyEntityArray.class);
                mArrayForeignKeys.add(new FkData(field, annotation.valueColumnName()
                        , annotation.fkColumnName(), annotation.entityClass(), getSetter(field)));
            }
        }
    }

    private Field getFieldByColumn(String valueColumn) {
        for (ColumnData column : mColumns) {
            if (column.getName().equals(valueColumn))
                return column.getField();
        }
        return null;
    }

    private Method getSetter(Field field) {
        String fieldName = field.getName();
        String methodName = "set";

        if (fieldName.length() > 1) {
            methodName = methodName + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        } else {
            methodName = methodName + fieldName.toUpperCase();
        }

        try {
            return getClass().getDeclaredMethod(methodName, field.getType());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setObjectId(Object id) throws Exception {
        try {
            Method setter = primaryKey.getSetter();
            setter.setAccessible(true);
            setter.invoke(this, id);
        } catch (Exception e) {
            throw e;
        }
    }

    public Object getObjectId() {
        try {
            Field field = getClass().getDeclaredField(primaryKey.getField().getName());
            field.setAccessible(true);
            return field.get(this);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ContentValues getContentValues() throws Exception {
        // Get all values into a ContentValues object
        ContentValues values = new ContentValues();

        if (!primaryKey.isAutoIncrement()) {
            Object id = getObjectId();
            if (id instanceof String) {
                values.put(primaryKey.getName(), (String) id);
            }
            if (id instanceof Integer) {
                values.put(primaryKey.getName(), (Integer) id);
            } else if (id instanceof Long) {
                values.put(primaryKey.getName(), (Long) id);
            }
        }

        for (ColumnData column : mColumns) {
            Field field = getClass().getDeclaredField(column.getField().getName());
            try {
                field.setAccessible(true);
                Object value = field.get(this);
                if (value instanceof Integer) {
                    values.put(column.getName(), (Integer) value);
                } else if (value instanceof Long) {
                    values.put(column.getName(), (Long) value);
                } else if (value instanceof Double) {
                    values.put(column.getName(), (Double) value);
                } else if (value instanceof String) {
                    values.put(column.getName(), (String) value);
                } else if (value instanceof Boolean) {
                    values.put(column.getName(), (Boolean) value);
                } else if (value instanceof Date) {
                    values.put(column.getName(), (((Date) value)).getTime());
                }
            } catch (Exception e) {
                throw e;
            }
        }

        return values;
    }

    protected Object cursorToObject(Cursor cursor) throws Exception {
        if (cursor.getCount() > 0) {
            if (primaryKey != null) {
                Method setter = primaryKey.getSetter();
                Class type = primaryKey.getField().getType();
                setter.setAccessible(true);

                if (type == Integer.TYPE) {
                    setter.invoke(this, cursor.getInt(cursor.getColumnIndex(primaryKey.getName())));
                } else if (type == Long.TYPE) {
                    setter.invoke(this, cursor.getLong(cursor.getColumnIndex(primaryKey.getName())));
                } else if (type.getName().equals("java.lang.String")) {
                    setter.invoke(this, cursor.getString(cursor.getColumnIndex(primaryKey.getName())));
                }
            }

            for (ColumnData column : mColumns) {
                Method setter = column.getSetter();
                Class type = column.getField().getType();
                setter.setAccessible(true);

                setter.setAccessible(true);
                if (type == Integer.TYPE) {
                    setter.invoke(this, cursor.getInt(cursor.getColumnIndex(column.getName())));
                } else if (type == Long.TYPE) {
                    setter.invoke(this, cursor.getLong(cursor.getColumnIndex(column.getName())));
                } else if (type == Double.TYPE) {
                    setter.invoke(this, (cursor.getDouble(cursor.getColumnIndex(column.getName()))));
                } else if (type == Boolean.TYPE) {
                    setter.invoke(this, (cursor.getInt(cursor.getColumnIndex(column.getName()))) != 0);
                } else if (type.getName().equals("java.lang.String")) {
                    setter.invoke(this, cursor.getString(cursor.getColumnIndex(column.getName())));
                } else if (type.getName().equals("java.util.Date")) {
                    setter.invoke(this,
                            new Date(cursor.getLong(cursor.getColumnIndex(column.getName()))));

                }
            }

            for (FkData foreignKey : mForeignKeys) {
                Object entityInstance;

                Field filterValueField = getFieldByColumn(foreignKey.getValueColumn());
                filterValueField.setAccessible(true);

                entityInstance = findOne(foreignKey.getValueColumn(), filterValueField.get(this),
                        mContext, foreignKey.getFkClass());

                Method setter = foreignKey.getSetter();
                setter.setAccessible(true);
                setter.invoke(this, entityInstance);
            }

            for (FkData foreignKey : mArrayForeignKeys) {
                ArrayList<Object> entityInstance;

                Field filterValueField = getFieldByColumn(foreignKey.getValueColumn());
                filterValueField.setAccessible(true);

                entityInstance = (ArrayList<Object>) findAllByColumn(foreignKey.getValueColumn(), filterValueField.get(this),
                        foreignKey.getName(), mContext, foreignKey.getFkClass());

                Method setter = foreignKey.getSetter();
                setter.setAccessible(true);
                setter.invoke(this, entityInstance);
            }
        }

        return this.getClass();
    }

    public void create() throws Exception {
        try {
            ContentValues values = getContentValues();
            long objectId = LocalDBHelper.getDatabaseWritableInstance(mContext).insertOrThrow(tableName, null, values);
            setObjectId(objectId);
            createOrUpdateForeignKeys();
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
            Object id = getObjectId();
            if (id != null && id instanceof String) id = "'" + id + "'";
            LocalDBHelper.getDatabaseWritableInstance(mContext).update(tableName, values, primaryKey.getName() + " = " + id, null);
            createOrUpdateForeignKeys();
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage() != null) {
                throw e;
            }
        }
    }

    private void createOrUpdateForeignKeys() throws Exception {
        for (FkData foreignKey : mForeignKeys) {
            Field field = getClass().getDeclaredField(foreignKey.getField().getName());
            field.setAccessible(true);
            Object fieldValue = field.get(this);

            foreignKey.getFkClass().cast(fieldValue).Ctor(mContext);
            Object objectId = foreignKey.getFkClass().cast(fieldValue).getObjectId();

            if (isNullOrZero(objectId)) {
                foreignKey.getFkClass().cast(fieldValue).create();
            } else {
                foreignKey.getFkClass().cast(fieldValue).save();
            }
        }

        for (FkData foreignKey : mArrayForeignKeys) {
            Field field = getClass().getDeclaredField(foreignKey.getField().getName());
            field.setAccessible(true);
            ArrayList<? extends DBObject> objectsArrayList =
                    (ArrayList<? extends DBObject>) field.get(this);

            for (Object object : objectsArrayList) {
                foreignKey.getFkClass().cast(object).Ctor(mContext);
                Object objectId = foreignKey.getFkClass().cast(object).getObjectId();

                if (isNullOrZero(objectId)) {
                    foreignKey.getFkClass().cast(object).create();
                } else {
                    foreignKey.getFkClass().cast(object).save();
                }
            }
        }
    }

    private boolean isNullOrZero(Object object) {
        return object == null ||
                object instanceof Integer && (int)object == 0 ||
                object instanceof Long && (long)object == 0 ||
                object instanceof String && object.equals("");
    }

    public void getById(long id, Context context) throws Exception {
        Cursor cursor = LocalDBHelper.getDatabaseWritableInstance(context).query(tableName,
                null, primaryKey.getName() + " = " + id, null,
                null, null, null);
        cursor.moveToFirst();
        try {
            cursorToObject(cursor);
        } catch (Exception e) {
            throw e;
        } finally {
            cursor.close();
        }

    }

    public void delete() throws Exception {
        Object id = getObjectId();
        if (id != null && id instanceof String) id = "'" + id + "'";
        LocalDBHelper.getDatabaseWritableInstance(mContext).delete(tableName, primaryKey.getName() + " = " + id, null);
    }

    private static int count(Context context, Class<? extends DBObject> targetClass) throws Exception {
        String query = "SELECT COUNT(*) FROM " + getTableName(targetClass);
        Cursor cursor = LocalDBHelper.getDatabaseWritableInstance(context).rawQuery(query, null);
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    private static int countByColumn(Context context, Class<? extends DBObject> targetClass,
                                     String filterColumn, Object filterValue) throws Exception {
        if (filterValue instanceof String) filterValue = "'" + filterValue + "'";
        String query = "SELECT COUNT(*) FROM " + getTableName(targetClass) +
                " WHERE " + filterColumn + "=" + filterValue;
        Cursor cursor = LocalDBHelper.getDatabaseWritableInstance(context).rawQuery(query, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    private static float getAverageByColumn(Context context, Class<? extends DBObject> targetClass,
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

    public static List<Object> findAll(String orderByColumnName,
                                      Context context,
                                      Class<? extends DBObject> targetClass) throws Exception {

        List<Object> allObjects = new ArrayList<>();

        Cursor cursor = LocalDBHelper.getDatabaseWritableInstance(context).query(getTableName(targetClass),
                null, null, null, null, null, orderByColumnName + " ASC");

        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            try {
                while (!cursor.isAfterLast()) {
                    Object currObject = targetClass.getConstructor(Context.class)
                            .newInstance(context);
                    (targetClass.cast(currObject)).cursorToObject(cursor);
                    allObjects.add(currObject);
                    cursor.moveToNext();
                }

                cursor.close();
                return allObjects;
            } catch (Exception e) {
                cursor.close();
                throw e;
            }
        } else {
            cursor.close();
            return allObjects;
        }
    }

    public static Object findOne(String filterColumnName,
                                 Object filterValue,
                                 Context context,
                                 Class<? extends DBObject> targetClass) throws Exception {
        if (filterValue instanceof Boolean)
            filterValue = (boolean) filterValue ? 1 : 0;
        else if (filterValue instanceof String)
            filterValue = "'" + filterValue + "'";

        Cursor cursor = LocalDBHelper.getDatabaseWritableInstance(context).query(getTableName(targetClass),
                null, filterColumnName + " = " + filterValue, null, null, null, null);

        cursor.moveToFirst();

        try {
            Object currObject = targetClass.getConstructor(Context.class).newInstance(context);
            (targetClass.cast(currObject)).cursorToObject(cursor);

            cursor.close();
            return currObject;
        } catch (Exception e) {
            cursor.close();
            throw e;
        }
    }

    public static List<Object> findAllByColumn(String columnName,
                                              Object value,
                                              String orderByColumnName,
                                              Context context,
                                              Class<? extends DBObject> targetClass) throws Exception {
        List<Object> allObjects = new ArrayList<>();

        if (value instanceof Boolean)
            value = (boolean) value ? 1 : 0;
        else if (value instanceof String)
            value = "'" + value + "'";
        Cursor cursor = LocalDBHelper.getDatabaseWritableInstance(context).query(getTableName(targetClass),
                null, columnName + " = " + value, null, null, null, orderByColumnName + " ASC");

        cursor.moveToFirst();

        try {
            while (!cursor.isAfterLast()) {
                Object currObject = targetClass.getConstructor(Context.class).newInstance(context);
                (targetClass.cast(currObject)).cursorToObject(cursor);
                allObjects.add(currObject);
                cursor.moveToNext();
            }

            cursor.close();
            return allObjects;
        } catch (Exception e) {
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

        try {
            while (!cursor.isAfterLast()) {
                Object currObject = targetClass.getConstructor(Context.class).newInstance(context);
                (targetClass.cast(currObject)).cursorToObject(cursor);
                allObjects.add(currObject);
                cursor.moveToNext();
            }

            cursor.close();
            return allObjects;
        } catch (Exception e) {
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

    public static void findAllInBackground(final String orderByColumn,
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
                    data = findAll(orderByColumn, context, targetClass);
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
                    data = findAllByColumn(columnName, value, orderByColumnName, context, targetClass);
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
                                                 final Object filterValue,
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
                                                      final Object filterValue,
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
                    data.add(getAverageByColumn(context, targetClass, avgColumn, filterColumn, filterValue));
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
            try {
                while (!cursor.isAfterLast()) {
                    Object currObject = targetClass.getConstructor(Context.class)
                            .newInstance(context);
                    (targetClass.cast(currObject)).cursorToObject(cursor);
                    allObjects.add(currObject);
                    cursor.moveToNext();
                }

                cursor.close();
                return allObjects;
            } catch (Exception e) {
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
