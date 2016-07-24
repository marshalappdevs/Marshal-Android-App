package com.basmach.marshal;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.basmach.marshal.services.UpdateIntentService;
import java.util.Date;

public class ApplicationMarshal extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("LIFE_CYCLE", "Application - onCreate");
        UpdateIntentService.startCheckForUpdate(getApplicationContext());
    }

    public static void setLastUpdatedNow(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putLong(Constants.PREF_LAST_UPDATE_TIMESTAMP, new Date().getTime()).apply();
    }

    public static void switchNewUpdatesToShowSetting(Context context, boolean state){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(Constants.PREF_IS_THERE_UPDATES_TO_SHOW, state).apply();
    }
}
