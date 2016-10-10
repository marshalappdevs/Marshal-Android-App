package com.basmapp.marshal.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.view.ContextThemeWrapper;
import android.view.View;

import java.util.Locale;

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
}
