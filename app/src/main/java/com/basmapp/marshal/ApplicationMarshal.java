package com.basmapp.marshal;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.basmapp.marshal.services.UpdateIntentService;
import com.basmapp.marshal.util.LocaleUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class ApplicationMarshal extends Application {
    private static final String SIGNATURE = "ndtzzSJ5ftzT6vSiJH20aMp8/m8=";

    @Override
    public void onCreate() {
        super.onCreate();
        // Normal app init code...
        LocaleUtils.updateLocale(this);
        LocaleUtils.updateConfig(this, getBaseContext().getResources().getConfiguration());
        UpdateIntentService.startCheckForUpdate(getApplicationContext());
//        ContentProvider.getInstance().initAllData(getApplicationContext());
        if (!isValidSignature(getApplicationContext())) {
            System.exit(0);
        }
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

    // Check if package manager signature equals to basmapp_keystore_release signature,
    // protect app from being signed and installed with different key
    public static boolean isValidSignature(Context context) {
        try {
            Signature[] signatures = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES).signatures;
            for (Signature signature : signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String currentSignature = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                // Compare signatures
                if (SIGNATURE.equals(currentSignature)) {
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            // Issue in checking signature
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isDebuggable(Context context) {
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }
}