package com.basmach.marshal.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.basmach.marshal.BuildConfig;
import com.basmach.marshal.R;
import com.basmach.marshal.ui.utils.LocaleUtils;
import com.basmach.marshal.ui.utils.SuggestionProvider;
import com.basmach.marshal.ui.utils.ThemeUtils;

import java.util.Objects;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.updateTheme(this);
        super.onCreate(savedInstanceState);
        LocaleUtils.updateLocale(this);
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
        LocaleUtils.updateLocale(this);
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

            Preference prefClearHistory = findPreference("clear-history");
            prefClearHistory.setOnPreferenceClickListener(clearHistoryClickListener);

            Preference prefResetTutorials = findPreference("reset-tutorial");
            prefResetTutorials.setOnPreferenceClickListener(resetTutorialsClickListener);

            CheckBoxPreference prefCCT = (CheckBoxPreference) findPreference("chrome_custom_tabs");
            prefCCT.setOnPreferenceChangeListener(cctChangeListener);
        }

        Preference.OnPreferenceClickListener versionClickListener = new Preference.OnPreferenceClickListener() {
            int mTapCount;
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (mTapCount == 7) {
                    Toast.makeText(getActivity().getApplicationContext(), "Easter Egg!!! " + ("\ud83d\udc83"), Toast.LENGTH_LONG).show();
                    mTapCount = 0;
                }
                mTapCount++;
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
                            restartApp();
                        }
                        break;
                    case "en":
                        if (!Objects.equals(prefLanguage.getValue(), "en")) {
                            PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().putString("LANG", "en").apply();
                            restartApp();
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

        Preference.OnPreferenceClickListener clearHistoryClickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                        SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
                suggestions.clearHistory();
                Toast.makeText(getActivity().getApplicationContext(), R.string.pref_done, Toast.LENGTH_SHORT).show();
                return false;
            }
        };

        Preference.OnPreferenceClickListener resetTutorialsClickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MaterialShowcaseView.resetAll(getActivity());
                Toast.makeText(getActivity().getApplicationContext(), R.string.pref_done, Toast.LENGTH_SHORT).show();
                return false;
            }
        };

        Preference.OnPreferenceChangeListener cctChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((Boolean) newValue) {
                    PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().putBoolean("CCT", true).apply();
                } else {
                    PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().putBoolean("CCT", false).apply();
                }
                return true;
            }
        };

        private void restartApp() {
            getActivity().finishAffinity();
            getActivity().overridePendingTransition(0, 0);
            startActivity(new Intent(getActivity(), MainActivity.class));
            startActivity(getActivity().getIntent());
            getActivity().overridePendingTransition(0, 0);
        }
    }
}
