package com.basmapp.marshal.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Locale;

public class LocaleUtils {

    public static void updateLocale(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lang = sharedPreferences.getString("LANG", "iw");
        if (!"".equals(lang) && !Locale.getDefault().toString().toLowerCase().equals(lang)) {
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            context.getApplicationContext().getResources().getConfiguration().setLocale(locale);
            context.getApplicationContext().getResources().updateConfiguration(
                    context.getApplicationContext().getResources().getConfiguration(),
                    context.getApplicationContext().getResources().getDisplayMetrics());
        }
    }
}
