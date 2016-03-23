package com.basmach.marshal.localdb;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocalDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "marshal_local_db";
    private static final int DATABASE_VERSION = 1;

    public LocalDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
        onCreate(db);
    }
}
