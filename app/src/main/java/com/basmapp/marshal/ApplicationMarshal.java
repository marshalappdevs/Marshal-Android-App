package com.basmapp.marshal;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.basmapp.marshal.services.UpdateIntentService;
import com.basmapp.marshal.util.LocaleUtils;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ApplicationMarshal extends Application {
    private static final String SIGNATURE = "ndtzzSJ5ftzT6vSiJH20aMp8/m8=";

    @Override
    public void onCreate() {
        super.onCreate();
        LocaleUtils.updateLocale(this);
        LocaleUtils.updateConfig(this, getBaseContext().getResources().getConfiguration());
        UpdateIntentService.startCheckForUpdate(getApplicationContext());
//        ContentProvider.getInstance().initAllData(getApplicationContext());
        if (!isValidSignature(getApplicationContext())) {
            // application signed with wrong key
            throw new RuntimeException();
        }
        if (isEmulator()) { /* enable on live builds */
            // application running on emulator
//            throw new RuntimeException();
        }
        if (isDebuggable(getApplicationContext())) { /* enable on live builds */
            // application is debuggable
//            throw new RuntimeException();
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

    public static boolean isRooted() {
        if (new File("/system/app/Superuser.apk").exists()) {
            // root management app detected
            return true;
        } else if (new File("/system/framework/XposedBridge.jar").exists()) {
            // xposed framework detected
            return true;
        } else {
            // searchPaths is a list of all PATH environment variables
            List<String> searchPaths = Arrays.asList(System.getenv("PATH").split(":"));
            for (String path : searchPaths) {
                if (!path.endsWith("/")) {
                    path += "/";
                }
                String filePath = path + "su";
                if (new File(filePath).exists()) {
                    // su binary detected
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isEmulator() {
        return Build.HARDWARE.toLowerCase(Locale.US).contains("goldfish")
                || Build.PRODUCT.toLowerCase(Locale.US).contains("sdk")
                || Build.MODEL.toLowerCase(Locale.US).contains("sdk")
                || Build.MODEL.toLowerCase(Locale.US).contains("emulator")
                || Build.MANUFACTURER.toLowerCase(Locale.US).contains("sdk")
                || Build.MANUFACTURER.toLowerCase(Locale.US).contains("genymotion")
                || Build.FINGERPRINT.startsWith("generic")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"));
    }
}