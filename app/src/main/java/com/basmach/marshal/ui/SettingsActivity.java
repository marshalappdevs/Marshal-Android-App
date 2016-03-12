package com.basmach.marshal.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.basmach.marshal.BuildConfig;
import com.basmach.marshal.R;

import java.util.Locale;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        updateTheme();
        super.onCreate(savedInstanceState);
        updateLocale();
        setContentView(R.layout.activity_container);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.navigation_drawer_settings);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment()).commit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLocale();
    }

    private void updateTheme() {
        String theme = mSharedPreferences.getString("THEME", "light");
        if (theme.equals("light")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        if (theme.equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        if (theme.equals("auto")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
        }
        getDelegate().applyDayNight();
        setTheme(R.style.AppTheme);
    }

    private void updateLocale() {
        Configuration config = getBaseContext().getResources().getConfiguration();
        String lang = mSharedPreferences.getString("LANG", "iw");
        if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
            Locale locale = new Locale(lang);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.setLocale(locale);
            Locale.setDefault(locale);
            res.updateConfiguration(conf, dm);
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
            prefVersion.setOnPreferenceClickListener(versionClickListener);
            prefVersion.setSummary(versionName);

            ListPreference prefLanguage = (ListPreference) findPreference("language");
            prefLanguage.setOnPreferenceChangeListener(languageChangeListener);

            ListPreference prefNightMode = (ListPreference) findPreference("night_mode");
            prefNightMode.setOnPreferenceChangeListener(themeChangeListener);
        }

        Preference.OnPreferenceClickListener versionClickListener = new Preference.OnPreferenceClickListener() {
            int clickCount = 0;
            @Override
            public boolean onPreferenceClick(Preference preference) {
                clickCount = clickCount + 1;
                if (clickCount == 7) {
                    Toast.makeText(getActivity().getApplicationContext(), "Easter Egg!!! " + ("\ud83d\udc83"), Toast.LENGTH_LONG).show();
                    clickCount = 0;
                }
                return false;
            }
        };

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

        Preference.OnPreferenceChangeListener themeChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ListPreference prefLanguage = (ListPreference) findPreference("night_mode");
                switch (newValue.toString()) {
                    case "light":
                        if (!Objects.equals(prefLanguage.getValue(), "light")) {
                            PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().putString("THEME", "light").apply();
                            restartApp();
                        }
                        break;
                    case "dark":
                        if (!Objects.equals(prefLanguage.getValue(), "dark")) {
                            PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().putString("THEME", "dark").apply();
                            restartApp();
                        }
                        break;
                    case "auto":
                        if (!Objects.equals(prefLanguage.getValue(), "auto")) {
                            PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().putString("THEME", "auto").apply();
                            restartApp();
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
                restartApp();
        }

        private void restartApp() {
            getActivity().finishAffinity();
            getActivity().overridePendingTransition(0, 0);
            startActivity(new Intent(getActivity(), MainActivity.class));
            startActivity(getActivity().getIntent());
            getActivity().overridePendingTransition(0, 0);
        }
    }
}
