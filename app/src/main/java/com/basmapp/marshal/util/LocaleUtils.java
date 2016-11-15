package com.basmapp.marshal.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.view.ContextThemeWrapper;
import android.view.View;

import com.basmapp.marshal.Constants;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class LocaleUtils {
    private static Locale sLocale;

    public static void updateLocale(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lang = sharedPreferences.getString("LANG", "iw");
        if (!"".equals(lang) && !Locale.getDefault().toString().toLowerCase().equals(lang)) {
            sLocale = new Locale(lang);
            Locale.setDefault(sLocale);
        }
    }

    public static void updateConfig(ContextThemeWrapper wrapper) {
        if (sLocale != null) {
            Configuration configuration = new Configuration();
            configuration.setLocale(sLocale);
            wrapper.applyOverrideConfiguration(configuration);
        }
    }

    public static void updateConfig(Context context, Configuration configuration) {
        if (sLocale != null) {
            // Wrapping the configuration to avoid Activity endless loop
            Configuration config = new Configuration(configuration);
            config.setLocale(sLocale);
            context.getApplicationContext().getResources().updateConfiguration(config,
                    context.getApplicationContext().getResources().getDisplayMetrics());
        }
    }

    public static boolean isRtl(Resources resources) {
        return resources.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    public static String getCategoryLocaleTitle(String category, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> categories = sharedPreferences.getStringSet(Constants.PREF_CATEGORIES, new HashSet<String>());
        for (String categoryValues : categories) {
            String[] values = categoryValues.split(";");
            if (values[0].equals(category)) {
                if (Locale.getDefault().toString().toLowerCase().equals("en")) {
                    return values[1];
                } else if (Locale.getDefault().toString().toLowerCase().equals("iw")) {
                    return values[2];
                }
            }
        }
        return category;
    }
}
