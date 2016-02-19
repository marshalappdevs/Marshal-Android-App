package com.basmach.marshal.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;

import com.basmach.marshal.BuildConfig;
import com.basmach.marshal.R;

import java.util.Locale;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isNightMode = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isNightMode", false);
        if (isNightMode) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme_Light);
        }
        super.onCreate(savedInstanceState);
        updateLocale();
        setContentView(R.layout.activity_container);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment()).commit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLocale();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mToolbar.setTitle(R.string.navigation_drawer_settings);
    }

    private void updateLocale() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration config = getBaseContext().getResources().getConfiguration();
        String lang = sharedPreferences.getString("LANG", "iw");
        if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
            Locale locale;
            locale = new Locale(lang);
            config.setLocale(locale);
            Locale.setDefault(locale);
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else if (preference instanceof RingtonePreference) {
                if (TextUtils.isEmpty(stringValue)) {
                    preference.setSummary(R.string.pref_ringtone_silent);
                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(stringValue));
                    if (ringtone == null) {
                        preference.setSummary(null);
                    } else {
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            String versionName = BuildConfig.VERSION_NAME;
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
            Preference prefVersion = findPreference("version");
            prefVersion.setSummary(versionName);

            final SwitchPreference prefNightMode = (SwitchPreference) findPreference("night_mode");
            prefNightMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (prefNightMode.isChecked()) {
                        getActivity().getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isNightMode", false).apply();
                    } else {
                        getActivity().getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isNightMode", true).apply();
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.pref_theme_changed);
                    builder.setPositiveButton(R.string.undo_string, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = getActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            getActivity().finishAffinity();
                            startActivity(intent);
                        }
                    }).show();
                    return true;
                }
            });

            ListPreference prefLanguage = (ListPreference) findPreference("language");
            //langPref.setSummary(langPref.getEntry());

            prefLanguage.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    preference.setSummary(o.toString());
                    return true;
                }
            });
            prefLanguage.setOnPreferenceChangeListener(languageChangeListener);
        }

        Preference.OnPreferenceChangeListener languageChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ListPreference prefLanguage = (ListPreference) findPreference("language");
                switch (newValue.toString()) {
                    case "iw":
                        if (!Objects.equals(prefLanguage.getValue(), "iw")) {
                            PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().putString("LANG", "iw").apply();
                            setLocale("iw");
                        }
                        break;
                    case "en":
                        if (!Objects.equals(prefLanguage.getValue(), "en")) {
                            PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().putString("LANG", "en").apply();
                            setLocale("en");
                        }
                        break;
                }
                return true;
            }
        };

        public void setLocale(String lang) {
            Locale locale = new Locale(lang);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.setLocale(locale);
            Locale.setDefault(locale);
            res.updateConfiguration(conf, dm);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.pref_language_changed);
            builder.setPositiveButton(R.string.undo_string, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = getActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getActivity().finishAffinity();
                    startActivity(intent);
                }
            }).show();
        }
    }
}
