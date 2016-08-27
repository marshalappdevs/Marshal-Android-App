package com.basmapp.marshal.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
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

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.basmapp.marshal.BuildConfig;
import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.interfaces.GcmReceiverListener;
import com.basmapp.marshal.receivers.GcmRegistrationReceiver;
import com.basmapp.marshal.services.GcmRegistrationService;
import com.basmapp.marshal.ui.utils.LocaleUtils;
import com.basmapp.marshal.ui.utils.SuggestionProvider;
import com.basmapp.marshal.ui.utils.ThemeUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    public final static int[] PRIMARY_COLORS = new int[]{
            Color.parseColor("#F44336"),
            Color.parseColor("#E91E63"),
            Color.parseColor("#9C27B0"),
            Color.parseColor("#673AB7"),
            Color.parseColor("#3F51B5"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#03A9F4"),
            Color.parseColor("#00BCD4"),
            Color.parseColor("#009688"),
            Color.parseColor("#4CAF50"),
            Color.parseColor("#8BC34A"),
            Color.parseColor("#CDDC39"),
            Color.parseColor("#FFEB3B"),
            Color.parseColor("#FFC107"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#FF5722"),
            Color.parseColor("#795548"),
            Color.parseColor("#9E9E9E"),
            Color.parseColor("#607D8B"),
            Color.parseColor("#000000")
    };

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
                overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
            }
        });

        getFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
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
        GcmRegistrationReceiver gcmRegistrationReceiver;
        MultiSelectListPreference prefGcmChannels;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            bindPreferenceSummaryToValue(findPreference(Constants.PREF_NOTIFICATIONS_RINGTONE));

            Preference prefVersion = findPreference(Constants.PREF_VERSION);
            prefVersion.setOnPreferenceClickListener(versionClickListener);
            prefVersion.setSummary(BuildConfig.VERSION_NAME);

            ListPreference prefLanguage = (ListPreference) findPreference(Constants.PREF_LANGUAGE);
            prefLanguage.setOnPreferenceChangeListener(languageChangeListener);

            ListPreference prefTheme = (ListPreference) findPreference(Constants.PREF_THEME);
            prefTheme.setOnPreferenceChangeListener(themeChangeListener);

            Preference prefColor = findPreference(Constants.PREF_COLOR);
            prefColor.setOnPreferenceClickListener(colorChangeListener);
            prefColor.setSummary(String.format("#%06X", (0xFFFFFF & MainActivity.getColorCode(getActivity()))));

            prefGcmChannels = (MultiSelectListPreference) findPreference(Constants.PREF_GCM_CHANNELS);
            prefGcmChannels.setOnPreferenceChangeListener(gcmChannelsChangeListener);
            updateGcmChannelsPrefSummary();

//            Preference prefClearCache = findPreference(Constants.PREF_CLEAR_CACHE);
//            prefClearCache.setOnPreferenceClickListener(clearCacheClickListener);
//            prefClearCache.setSummary(String.format(getString(R.string.clear_local_cache_summary),
//                    getCacheFolderSize() / 1048576L));

            Preference prefClearHistory = findPreference(Constants.PREF_CLEAR_HISTORY);
            prefClearHistory.setOnPreferenceClickListener(clearHistoryClickListener);

            CheckBoxPreference prefCCT = (CheckBoxPreference) findPreference(Constants.PREF_CCT);
            prefCCT.setOnPreferenceChangeListener(cctChangeListener);

            gcmRegistrationReceiver = new GcmRegistrationReceiver(new GcmReceiverListener() {
                @Override
                public void onFinish(boolean result) {
                    if (result) {
                        restartApp();
                    }

                    updateGcmChannelsPrefSummary();
                    prefGcmChannels.setEnabled(true);
                }
            });
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
                PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                        .edit().putString(Constants.PREF_LANGUAGE, newValue.toString()).apply();
                restartApp();
                return false;
            }
        };

        Preference.OnPreferenceClickListener colorChangeListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SettingsActivity act = (SettingsActivity) getActivity();
                if (act == null)
                    return false;
                ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
                colorPickerDialog.initialize(
                        R.string.color_picker_default_title, PRIMARY_COLORS, MainActivity.getColorCode(act), 4, PRIMARY_COLORS.length);
                colorPickerDialog.show(getFragmentManager(), Constants.FRAGMENT_COLOR_PICKER);
                colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        if (color != MainActivity.getColorCode(getActivity())) {
                            PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                                    .edit().putInt(Constants.PREF_COLOR_CODE, color).apply();
                            restartApp();
                        }
                    }
                });
                return false;
            }
        };

        Preference.OnPreferenceChangeListener themeChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                        .edit().putString(Constants.PREF_THEME, newValue.toString()).apply();
                restartApp();
                return false;
            }
        };

        Preference.OnPreferenceClickListener clearCacheClickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    deleteDir(getActivity().getCacheDir());
                    findPreference(Constants.PREF_CLEAR_CACHE).setSummary(String.format(getString(R.string.clear_local_cache_summary),
                            getCacheFolderSize() / 1048576L));
                    Toast.makeText(getActivity().getApplicationContext(), R.string.pref_cache_cleared, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        };

        Preference.OnPreferenceClickListener clearHistoryClickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                        SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
                suggestions.clearHistory();
                Toast.makeText(getActivity().getApplicationContext(), R.string.pref_search_history_cleared, Toast.LENGTH_SHORT).show();
                return false;
            }
        };

        Preference.OnPreferenceChangeListener cctChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return true;
            }
        };

        Preference.OnPreferenceChangeListener gcmChannelsChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setEnabled(false);
                Toast.makeText(getActivity(), getResources().getString(R.string.gcm_settings_change), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), GcmRegistrationService.class);
                intent.setAction(GcmRegistrationService.ACTION_UPDATE_CHANNELS);
                intent.putExtra(Constants.EXTRA_GCM_CHANNELS, ((HashSet<String>)newValue));
                getActivity().startService(intent);
                return false;
            }
        };

        private void restartApp() {
            getActivity().finishAffinity();
            getActivity().overridePendingTransition(0, 0);
            startActivity(new Intent(getActivity(), MainActivity.class));
            startActivity(getActivity().getIntent());
            getActivity().overridePendingTransition(0, 0);
        }

        private void updateGcmChannelsPrefSummary() {
            Set<String> values = prefGcmChannels.getValues();
            Set<CharSequence> labels = new HashSet<>();
            for(String value: values) {
                int index = prefGcmChannels.findIndexOfValue(value);
                labels.add(prefGcmChannels.getEntries()[index]);
            }
            if (!values.isEmpty()) {
                prefGcmChannels.setSummary(labels.toString().replaceAll("\\[", "").replaceAll("\\]", ""));
            } else {
                prefGcmChannels.setSummary(getString(R.string.pref_gcm_channels_summary));
            }
        }

        private long getCacheFolderSize() {
            long size = 0;
            File[] files = getActivity().getCacheDir().listFiles();
            for (File f:files) {
                size = size+f.length();
            }
            return size;
        }

        private boolean deleteDir(File dir) {
            if (dir != null && dir.isDirectory()) {
                String[] children = dir.list();
                for (String aChildren : children) {
                    boolean success = deleteDir(new File(dir, aChildren));
                    if (!success) {
                        return false;
                    }
                }
                return dir.delete();
            } else
                return dir != null && dir.isFile() && dir.delete();
        }

        @Override
        public void onResume() {
            super.onResume();
            getActivity().registerReceiver(gcmRegistrationReceiver, new IntentFilter(GcmRegistrationReceiver.ACTION_RESULT));
        }

        @Override
        public void onPause() {
            super.onStop();
            getActivity().unregisterReceiver(gcmRegistrationReceiver);
        }
    }
}