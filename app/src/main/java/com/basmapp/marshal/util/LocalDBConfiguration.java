package com.basmapp.marshal.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.basmapp.marshal.Constants;
import com.simplite.orm.ManifestProvider;
import com.simplite.orm.interfaces.SimpLiteConfiguration;

public class LocalDBConfiguration implements SimpLiteConfiguration {
    @Override
    public void beforeOnCreate(Context context) {
        Log.i("SimpLite","beforeOnCreate");
    }

    @Override
    public void afterOnCreate(Context context) {
        Log.i("SimpLite","AfterOnCreate");
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().putBoolean(Constants.PREF_IS_FIRST_RUN, true).apply();
            sharedPreferences.edit().putBoolean(Constants.PREF_IS_UPDATE_SERVICE_SUCCESS_ONCE, false).apply();
            sharedPreferences.edit().putLong(Constants.PREF_LAST_UPDATE_TIMESTAMP, 0).apply();
            sharedPreferences.edit().putInt(Constants.PREF_DATABASE_VERSION, ManifestProvider.getDatabaseVersion(context)).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beforeOnUpgrade(Context context) {
        Log.i("SimpLite","beforeOnUpgrade");
    }

    @Override
    public void afterOnUpgrade(Context context) {
        Log.i("SimpLite","AfterOnUpgrade");
    }
}
