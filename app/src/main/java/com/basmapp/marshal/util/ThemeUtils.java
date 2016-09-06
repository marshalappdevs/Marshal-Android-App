package com.basmapp.marshal.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.ui.MainActivity;

public class ThemeUtils {

    public static void updateTheme(AppCompatActivity appCompatActivity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appCompatActivity);
        String theme = sharedPreferences.getString(Constants.PREF_THEME, "light");
        switch (theme) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "auto":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                break;
        }
        appCompatActivity.getDelegate().applyDayNight();
        int primaryColorCode = MainActivity.getPrimaryColorCode(appCompatActivity);
        if (primaryColorCode == Color.parseColor("#F44336")) {
            appCompatActivity.setTheme(R.style.AppTheme_Red);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Red").apply();
        } else if (primaryColorCode == Color.parseColor("#E91E63")) {
            appCompatActivity.setTheme(R.style.AppTheme_Pink);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Pink").apply();
        } else if (primaryColorCode == Color.parseColor("#9C27B0")) {
            appCompatActivity.setTheme(R.style.AppTheme_Purple);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Purple").apply();
        } else if (primaryColorCode == Color.parseColor("#673AB7")) {
            appCompatActivity.setTheme(R.style.AppTheme_Deep_Purple);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Deep Purple").apply();
        } else if (primaryColorCode == Color.parseColor("#3F51B5")) {
            appCompatActivity.setTheme(R.style.AppTheme_Indigo);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Indigo").apply();
        } else if (primaryColorCode == Color.parseColor("#2196F3")) {
            appCompatActivity.setTheme(R.style.AppTheme_Blue);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Blue").apply();
        } else if (primaryColorCode == Color.parseColor("#03A9F4")) {
            appCompatActivity.setTheme(R.style.AppTheme_Light_Blue);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Light Blue").apply();
        } else if (primaryColorCode == Color.parseColor("#00BCD4")) {
            appCompatActivity.setTheme(R.style.AppTheme_Cyan);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Cyan").apply();
        } else if (primaryColorCode == Color.parseColor("#009688")) {
            appCompatActivity.setTheme(R.style.AppTheme_Teal);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Teal").apply();
        } else if (primaryColorCode == Color.parseColor("#4CAF50")) {
            appCompatActivity.setTheme(R.style.AppTheme_Green);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Green").apply();
        } else if (primaryColorCode == Color.parseColor("#8BC34A")) {
            appCompatActivity.setTheme(R.style.AppTheme_Light_Green);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Light Green").apply();
        } else if (primaryColorCode == Color.parseColor("#CDDC39")) {
            appCompatActivity.setTheme(R.style.AppTheme_Lime);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Lime").apply();
        } else if (primaryColorCode == Color.parseColor("#FFEB3B")) {
            appCompatActivity.setTheme(R.style.AppTheme_Yellow);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Yellow").apply();
        } else if (primaryColorCode == Color.parseColor("#FFC107")) {
            appCompatActivity.setTheme(R.style.AppTheme_Amber);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Amber").apply();
        } else if (primaryColorCode == Color.parseColor("#FF9800")) {
            appCompatActivity.setTheme(R.style.AppTheme_Orange);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Orange").apply();
        } else if (primaryColorCode == Color.parseColor("#FF5722")) {
            appCompatActivity.setTheme(R.style.AppTheme_Deep_Orange);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Deep Orange").apply();
        } else if (primaryColorCode == Color.parseColor("#795548")) {
            appCompatActivity.setTheme(R.style.AppTheme_Brown);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Brown").apply();
        } else if (primaryColorCode == Color.parseColor("#9E9E9E")) {
            appCompatActivity.setTheme(R.style.AppTheme_Grey);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Grey").apply();
        } else if (primaryColorCode == Color.parseColor("#607D8B")) {
            appCompatActivity.setTheme(R.style.AppTheme_Blue_Grey);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Blue Grey").apply();
        } else if (primaryColorCode == Color.parseColor("#000000")) {
            appCompatActivity.setTheme(R.style.AppTheme_Black);
            sharedPreferences.edit().putString(Constants.PREF_PRIMARY_COLOR_NAME, "Black").apply();
        }
        int accentColorCode = MainActivity.getAccentColorCode(appCompatActivity);
        if (accentColorCode == Color.parseColor("#FF5252")) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorRed, true);
            sharedPreferences.edit().putString(Constants.PREF_ACCENT_COLOR_NAME, "Red").apply();
        } else if (accentColorCode == Color.parseColor("#FF4081")) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorPink, true);
            sharedPreferences.edit().putString(Constants.PREF_ACCENT_COLOR_NAME, "Pink").apply();
        } else if (accentColorCode == Color.parseColor("#E040FB")) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorPurple, true);
            sharedPreferences.edit().putString(Constants.PREF_ACCENT_COLOR_NAME, "Purple").apply();
        } else if (accentColorCode == Color.parseColor("#7C4DFF")) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorDeepPurple, true);
            sharedPreferences.edit().putString(Constants.PREF_ACCENT_COLOR_NAME, "Deep Purple").apply();
        } else if (accentColorCode == Color.parseColor("#536DFE")) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorIndigo, true);
            sharedPreferences.edit().putString(Constants.PREF_ACCENT_COLOR_NAME, "Indigo").apply();
        } else if (accentColorCode == Color.parseColor("#448AFF")) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorBlue, true);
            sharedPreferences.edit().putString(Constants.PREF_ACCENT_COLOR_NAME, "Blue").apply();
        } else if (accentColorCode == Color.parseColor("#40C4FF")) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorLightBlue, true);
            sharedPreferences.edit().putString(Constants.PREF_ACCENT_COLOR_NAME, "Light Blue").apply();
        } else if (accentColorCode == Color.parseColor("#18FFFF")) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorCyan, true);
            sharedPreferences.edit().putString(Constants.PREF_ACCENT_COLOR_NAME, "Cyan").apply();
        } else if (accentColorCode == Color.parseColor("#64FFDA")) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorTeal, true);
            sharedPreferences.edit().putString(Constants.PREF_ACCENT_COLOR_NAME, "Teal").apply();
        } else if (accentColorCode == Color.parseColor("#69F0AE")) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorGreen, true);
            sharedPreferences.edit().putString(Constants.PREF_ACCENT_COLOR_NAME, "Green").apply();
        } else if (accentColorCode == Color.parseColor("#B2FF59")) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorLightGreen, true);
            sharedPreferences.edit().putString(Constants.PREF_ACCENT_COLOR_NAME, "Light Green").apply();
        } else if (accentColorCode == Color.parseColor("#EEFF41")) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorLime, true);
            sharedPreferences.edit().putString(Constants.PREF_ACCENT_COLOR_NAME, "Lime").apply();
        } else if (accentColorCode == Color.parseColor("#FFFF00")) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorYellow, true);
            sharedPreferences.edit().putString(Constants.PREF_ACCENT_COLOR_NAME, "Yellow").apply();
        } else if (accentColorCode == Color.parseColor("#FFD740")) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorAmber, true);
            sharedPreferences.edit().putString(Constants.PREF_ACCENT_COLOR_NAME, "Amber").apply();
        } else if (accentColorCode == Color.parseColor("#FFAB40")) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorOrange, true);
            sharedPreferences.edit().putString(Constants.PREF_ACCENT_COLOR_NAME, "Orange").apply();
        } else if (accentColorCode == Color.parseColor("#FF6E40")) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorDeepOrange, true);
            sharedPreferences.edit().putString(Constants.PREF_ACCENT_COLOR_NAME, "Deep Orange").apply();
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
