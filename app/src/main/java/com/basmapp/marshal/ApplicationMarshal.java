package com.basmapp.marshal;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Log;

import com.basmapp.marshal.services.UpdateIntentService;
import com.basmapp.marshal.util.ContentProvider;
import com.basmapp.marshal.util.LocaleUtils;

import java.util.Date;

public class ApplicationMarshal extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("LIFE_CYCLE", "Application - onCreate");
        LocaleUtils.updateLocale(this);
        LocaleUtils.updateConfig(this, getBaseContext().getResources().getConfiguration());
        UpdateIntentService.startCheckForUpdate(getApplicationContext());
//        ContentProvider.getInstance().initAllData(getApplicationContext());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleUtils.updateConfig(this, newConfig);
    }

    public static void setLastUpdatedNow(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putLong(Constants.PREF_LAST_UPDATE_TIMESTAMP, new Date().getTime()).apply();
    }

    public static void switchNewUpdatesToShowSetting(Context context, boolean state) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(Constants.PREF_IS_THERE_UPDATES_TO_SHOW, state).apply();
    }
}
