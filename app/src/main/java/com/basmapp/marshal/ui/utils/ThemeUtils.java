package com.basmapp.marshal.ui.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.ui.MainActivity;

public class ThemeUtils {

    public static void updateTheme(AppCompatActivity appCompatActivity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appCompatActivity);
        String theme = sharedPreferences.getString(Constants.PREF_THEME, "light");
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
        int colorCode = MainActivity.getColorCode(appCompatActivity);
        if (colorCode == Color.parseColor("#F44336")) {
            appCompatActivity.setTheme(R.style.AppTheme_Red);
        } else if (colorCode == Color.parseColor("#E91E63")) {
            appCompatActivity.setTheme(R.style.AppTheme_Pink);
        } else if (colorCode == Color.parseColor("#9C27B0")) {
            appCompatActivity.setTheme(R.style.AppTheme_Purple);
        } else if (colorCode == Color.parseColor("#673AB7")) {
            appCompatActivity.setTheme(R.style.AppTheme_Deep_Purple);
        } else if (colorCode == Color.parseColor("#3F51B5")) {
            appCompatActivity.setTheme(R.style.AppTheme_Indigo);
        } else if (colorCode == Color.parseColor("#2196F3")) {
            appCompatActivity.setTheme(R.style.AppTheme_Blue);
        } else if (colorCode == Color.parseColor("#03A9F4")) {
            appCompatActivity.setTheme(R.style.AppTheme_Light_Blue);
        } else if (colorCode == Color.parseColor("#00BCD4")) {
            appCompatActivity.setTheme(R.style.AppTheme_Cyan);
        } else if (colorCode == Color.parseColor("#009688")) {
            appCompatActivity.setTheme(R.style.AppTheme_Teal);
        } else if (colorCode == Color.parseColor("#4CAF50")) {
            appCompatActivity.setTheme(R.style.AppTheme_Green);
        } else if (colorCode == Color.parseColor("#8BC34A")) {
            appCompatActivity.setTheme(R.style.AppTheme_Light_Green);
        } else if (colorCode == Color.parseColor("#CDDC39")) {
            appCompatActivity.setTheme(R.style.AppTheme_Lime);
        } else if (colorCode == Color.parseColor("#FFEB3B")) {
            appCompatActivity.setTheme(R.style.AppTheme_Yellow);
        } else if (colorCode == Color.parseColor("#FFC107")) {
            appCompatActivity.setTheme(R.style.AppTheme_Amber);
        } else if (colorCode == Color.parseColor("#FF9800")) {
            appCompatActivity.setTheme(R.style.AppTheme_Orange);
        } else if (colorCode == Color.parseColor("#FF5722")) {
            appCompatActivity.setTheme(R.style.AppTheme_Deep_Orange);
        } else if (colorCode == Color.parseColor("#795548")) {
            appCompatActivity.setTheme(R.style.AppTheme_Brown);
        } else if (colorCode == Color.parseColor("#9E9E9E")) {
            appCompatActivity.setTheme(R.style.AppTheme_Grey);
        } else if (colorCode == Color.parseColor("#607D8B")) {
            appCompatActivity.setTheme(R.style.AppTheme_Blue_Grey);
        } else if (colorCode == Color.parseColor("#000000")) {
            appCompatActivity.setTheme(R.style.AppTheme_Black);
        }
    }
    public static int getThemeColor(Context context, int id) {
        Resources.Theme theme = context.getTheme();
        TypedArray a = theme.obtainStyledAttributes(new int[] { id });
        int result = a.getColor(0, 0);
        a.recycle();
        return result;
    }
}
