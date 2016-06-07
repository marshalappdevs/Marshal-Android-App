package com.basmach.marshal.localdb;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocalDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "marshal_local_db";
    private static final int DATABASE_VERSION = 1;

    public static LocalDBHelper helperInstance;
    public static SQLiteDatabase databaseInstance;

    public LocalDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static LocalDBHelper getHelperInstance(Context context) {
        if (helperInstance == null)
            helperInstance = new LocalDBHelper(context);

        return helperInstance;
    }

    public static SQLiteDatabase getDatabaseWritableInstance(Context context) {
        if (databaseInstance == null)
            databaseInstance = getHelperInstance(context).getWritableDatabase();

        return databaseInstance;
    }

    public static void closeIfExist(){
        if (helperInstance != null)
            helperInstance.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(DBConstants.CREATE_T_MATERIAL_ITEM);
            Log.i(DATABASE_NAME, "t_material_item created");
            db.execSQL(DBConstants.CREATE_T_CYCLE);
            Log.i(DATABASE_NAME, "t_cycle created");
            db.execSQL(DBConstants.CREATE_T_COURSE);
            Log.i(DATABASE_NAME, "t_course created");
            db.execSQL(DBConstants.CREATE_T_RATING);
            Log.i(DATABASE_NAME, "t_rating created");

            Log.i(DATABASE_NAME, "database created");
        }
        catch (SQLException e){
            Log.e(DATABASE_NAME, e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(LocalDBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL(DBConstants.DROP_T_MATERIAL_ITEM);
        db.execSQL(DBConstants.DROP_T_COURSE);
        db.execSQL(DBConstants.DROP_T_CYCLE);
        db.execSQL(DBConstants.DROP_T_RATING);

        onCreate(db);
    }
}
