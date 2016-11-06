package com.basmapp.marshal.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.ui.MainActivity;

public class ThemeUtils {

    public static void updateTheme(AppCompatActivity appCompatActivity) {
        int[] PRIMARY_COLORS = new int[]{
                ContextCompat.getColor(appCompatActivity, R.color.red_primary),
                ContextCompat.getColor(appCompatActivity, R.color.pink_primary),
                ContextCompat.getColor(appCompatActivity, R.color.purple_primary),
                ContextCompat.getColor(appCompatActivity, R.color.deep_purple_primary),
                ContextCompat.getColor(appCompatActivity, R.color.indigo_primary),
                ContextCompat.getColor(appCompatActivity, R.color.blue_primary),
                ContextCompat.getColor(appCompatActivity, R.color.light_blue_primary),
                ContextCompat.getColor(appCompatActivity, R.color.cyan_primary),
                ContextCompat.getColor(appCompatActivity, R.color.teal_primary),
                ContextCompat.getColor(appCompatActivity, R.color.green_primary),
                ContextCompat.getColor(appCompatActivity, R.color.light_green_primary),
                ContextCompat.getColor(appCompatActivity, R.color.lime_primary),
                ContextCompat.getColor(appCompatActivity, R.color.yellow_primary),
                ContextCompat.getColor(appCompatActivity, R.color.amber_primary),
                ContextCompat.getColor(appCompatActivity, R.color.orange_primary),
                ContextCompat.getColor(appCompatActivity, R.color.deep_orange_primary),
                ContextCompat.getColor(appCompatActivity, R.color.brown_primary),
                ContextCompat.getColor(appCompatActivity, R.color.grey_primary),
                ContextCompat.getColor(appCompatActivity, R.color.blue_grey_primary),
                ContextCompat.getColor(appCompatActivity, R.color.black_primary)
        };

        int[] ACCENT_COLORS = new int[]{
                ContextCompat.getColor(appCompatActivity, R.color.red_accent),
                ContextCompat.getColor(appCompatActivity, R.color.pink_accent),
                ContextCompat.getColor(appCompatActivity, R.color.purple_accent),
                ContextCompat.getColor(appCompatActivity, R.color.deep_purple_accent),
                ContextCompat.getColor(appCompatActivity, R.color.indigo_accent),
                ContextCompat.getColor(appCompatActivity, R.color.blue_accent),
                ContextCompat.getColor(appCompatActivity, R.color.light_blue_accent),
                ContextCompat.getColor(appCompatActivity, R.color.cyan_accent),
                ContextCompat.getColor(appCompatActivity, R.color.teal_accent),
                ContextCompat.getColor(appCompatActivity, R.color.green_accent),
                ContextCompat.getColor(appCompatActivity, R.color.light_green_accent),
                ContextCompat.getColor(appCompatActivity, R.color.lime_accent),
                ContextCompat.getColor(appCompatActivity, R.color.yellow_accent),
                ContextCompat.getColor(appCompatActivity, R.color.amber_accent),
                ContextCompat.getColor(appCompatActivity, R.color.orange_accent),
                ContextCompat.getColor(appCompatActivity, R.color.deep_orange_accent),
        };

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
        if (primaryColorCode == PRIMARY_COLORS[0]) {
            appCompatActivity.setTheme(R.style.AppTheme_Red);
        } else if (primaryColorCode == PRIMARY_COLORS[1]) {
            appCompatActivity.setTheme(R.style.AppTheme_Pink);
        } else if (primaryColorCode == PRIMARY_COLORS[2]) {
            appCompatActivity.setTheme(R.style.AppTheme_Purple);
        } else if (primaryColorCode == PRIMARY_COLORS[3]) {
            appCompatActivity.setTheme(R.style.AppTheme_Deep_Purple);
        } else if (primaryColorCode == PRIMARY_COLORS[4]) {
            appCompatActivity.setTheme(R.style.AppTheme_Indigo);
        } else if (primaryColorCode == PRIMARY_COLORS[5]) {
            appCompatActivity.setTheme(R.style.AppTheme_Blue);
        } else if (primaryColorCode == PRIMARY_COLORS[6]) {
            appCompatActivity.setTheme(R.style.AppTheme_Light_Blue);
        } else if (primaryColorCode == PRIMARY_COLORS[7]) {
            appCompatActivity.setTheme(R.style.AppTheme_Cyan);
        } else if (primaryColorCode == PRIMARY_COLORS[8]) {
            appCompatActivity.setTheme(R.style.AppTheme_Teal);
        } else if (primaryColorCode == PRIMARY_COLORS[9]) {
            appCompatActivity.setTheme(R.style.AppTheme_Green);
        } else if (primaryColorCode == PRIMARY_COLORS[10]) {
            appCompatActivity.setTheme(R.style.AppTheme_Light_Green);
        } else if (primaryColorCode == PRIMARY_COLORS[11]) {
            appCompatActivity.setTheme(R.style.AppTheme_Lime);
        } else if (primaryColorCode == PRIMARY_COLORS[12]) {
            appCompatActivity.setTheme(R.style.AppTheme_Yellow);
        } else if (primaryColorCode == PRIMARY_COLORS[13]) {
            appCompatActivity.setTheme(R.style.AppTheme_Amber);
        } else if (primaryColorCode == PRIMARY_COLORS[14]) {
            appCompatActivity.setTheme(R.style.AppTheme_Orange);
        } else if (primaryColorCode == PRIMARY_COLORS[15]) {
            appCompatActivity.setTheme(R.style.AppTheme_Deep_Orange);
        } else if (primaryColorCode == PRIMARY_COLORS[16]) {
            appCompatActivity.setTheme(R.style.AppTheme_Brown);
        } else if (primaryColorCode == PRIMARY_COLORS[17]) {
            appCompatActivity.setTheme(R.style.AppTheme_Grey);
        } else if (primaryColorCode == PRIMARY_COLORS[18]) {
            appCompatActivity.setTheme(R.style.AppTheme_Blue_Grey);
        } else if (primaryColorCode == PRIMARY_COLORS[19]) {
            appCompatActivity.setTheme(R.style.AppTheme_Black);
        }
        int accentColorCode = MainActivity.getAccentColorCode(appCompatActivity);
        if (accentColorCode == ACCENT_COLORS[0]) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorRed, true);
        } else if (accentColorCode == ACCENT_COLORS[1]) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorPink, true);
        } else if (accentColorCode == ACCENT_COLORS[2]) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorPurple, true);
        } else if (accentColorCode == ACCENT_COLORS[3]) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorDeepPurple, true);
        } else if (accentColorCode == ACCENT_COLORS[4]) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorIndigo, true);
        } else if (accentColorCode == ACCENT_COLORS[5]) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorBlue, true);
        } else if (accentColorCode == ACCENT_COLORS[6]) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorLightBlue, true);
        } else if (accentColorCode == ACCENT_COLORS[7]) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorCyan, true);
        } else if (accentColorCode == ACCENT_COLORS[8]) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorTeal, true);
        } else if (accentColorCode == ACCENT_COLORS[9]) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorGreen, true);
        } else if (accentColorCode == ACCENT_COLORS[10]) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorLightGreen, true);
        } else if (accentColorCode == ACCENT_COLORS[11]) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorLime, true);
        } else if (accentColorCode == ACCENT_COLORS[12]) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorYellow, true);
        } else if (accentColorCode == ACCENT_COLORS[13]) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorAmber, true);
        } else if (accentColorCode == ACCENT_COLORS[14]) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorOrange, true);
        } else if (accentColorCode == ACCENT_COLORS[15]) {
            appCompatActivity.getTheme().applyStyle(R.style.OverrideAccentColorDeepOrange, true);
        }
    }

    public static int getThemeColor(Context context, int id) {
        Resources.Theme theme = context.getTheme();
        TypedArray a = theme.obtainStyledAttributes(new int[]{id});
        int result = a.getColor(0, 0);
        a.recycle();
        return result;
    }
}
