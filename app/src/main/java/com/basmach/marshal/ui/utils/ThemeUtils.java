package com.basmach.marshal.ui.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import com.basmach.marshal.R;

public class ThemeUtils {

    public static void updateTheme(AppCompatActivity appCompatActivity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appCompatActivity);
        String theme = sharedPreferences.getString("THEME", "light");
        if (theme.equals("light")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        if (theme.equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        if (theme.equals("auto")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
        }
        appCompatActivity.getDelegate().applyDayNight();
        appCompatActivity.setTheme(R.style.AppTheme);
    }
}
