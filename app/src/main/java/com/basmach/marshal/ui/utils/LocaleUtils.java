package com.basmach.marshal.ui.utils;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import java.util.Locale;

public class LocaleUtils {

    public static void updateLocale(AppCompatActivity appCompatActivity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appCompatActivity);
        Configuration config = appCompatActivity.getBaseContext().getResources().getConfiguration();
        String lang = sharedPreferences.getString("LANG", "iw");
        if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
            Locale locale = new Locale(lang);
            Resources res = appCompatActivity.getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.setLocale(locale);
            Locale.setDefault(locale);
            res.updateConfiguration(conf, dm);
        }
    }
}
