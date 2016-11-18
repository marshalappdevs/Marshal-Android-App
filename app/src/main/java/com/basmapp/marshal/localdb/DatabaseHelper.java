package com.basmapp.marshal.localdb;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.Cycle;
import com.basmapp.marshal.entities.MalshabItem;
import com.basmapp.marshal.entities.MaterialItem;
import com.basmapp.marshal.entities.Rating;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "marshal_local_db";
    public static final int DATABASE_VERSION = 5;

    private static DatabaseHelper helperInstance;
    private static SQLiteDatabase databaseInstance;

    private Context context;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    static DatabaseHelper getHelperInstance(Context context) {
        if (helperInstance == null)
            helperInstance = new DatabaseHelper(context.getApplicationContext());

        return helperInstance;
    }

    public static SQLiteDatabase getWritableDatabaseInstance(Context context) {
        if (databaseInstance == null || (!databaseInstance.isOpen())) {
            databaseInstance = getHelperInstance(context).getWritableDatabase();
        }

        return databaseInstance;
    }

    public static void closeIfExist() {
        if (helperInstance != null)
            helperInstance.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(new MaterialItem(context).getCreateTableCommand());
            db.execSQL(new Cycle(context).getCreateTableCommand());
            db.execSQL(new Course(context).getCreateTableCommand());
            db.execSQL(new Rating(context).getCreateTableCommand());
            db.execSQL(new MalshabItem(context).getCreateTableCommand());
            initializePreferences();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializePreferences() {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().putBoolean(Constants.PREF_IS_FIRST_RUN, true).apply();
            sharedPreferences.edit().putBoolean(Constants.PREF_IS_UPDATE_SERVICE_SUCCESS_ONCE, false).apply();
            sharedPreferences.edit().putLong(Constants.PREF_LAST_UPDATE_TIMESTAMP, 0).apply();
            sharedPreferences.edit().putInt(Constants.PREF_DATABASE_VERSION, DATABASE_VERSION).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(MaterialItem.getDropTableIfExistCommand(MaterialItem.class));
        db.execSQL(Course.getDropTableIfExistCommand(Course.class));
        db.execSQL(Cycle.getDropTableIfExistCommand(Cycle.class));
        db.execSQL(Rating.getDropTableIfExistCommand(Rating.class));
        db.execSQL(MalshabItem.getDropTableIfExistCommand(MalshabItem.class));
        onCreate(db);
    }
}
