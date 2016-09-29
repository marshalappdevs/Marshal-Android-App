package com.basmapp.marshal.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.basmapp.marshal.BaseActivity;
import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.interfaces.FcmReceiverListener;
import com.basmapp.marshal.receivers.FcmRegistrationReceiver;
import com.basmapp.marshal.services.FcmRegistrationService;
import com.basmapp.marshal.util.LocaleUtils;
import com.basmapp.marshal.util.SuggestionProvider;
import com.basmapp.marshal.ui.widget.colorpicker.ColorPickerDialog;
import com.basmapp.marshal.ui.widget.colorpicker.ColorPickerSwatch;

import java.util.HashSet;
import java.util.Set;

public class SettingsActivity extends BaseActivity {
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
    public final static int[] ACCENT_COLORS = new int[]{
            Color.parseColor("#FF5252"),
            Color.parseColor("#FF4081"),
            Color.parseColor("#E040FB"),
            Color.parseColor("#7C4DFF"),
            Color.parseColor("#536DFE"),
            Color.parseColor("#448AFF"),
            Color.parseColor("#40C4FF"),
            Color.parseColor("#18FFFF"),
            Color.parseColor("#64FFDA"),
            Color.parseColor("#69F0AE"),
            Color.parseColor("#B2FF59"),
            Color.parseColor("#EEFF41"),
            Color.parseColor("#FFFF00"),
            Color.parseColor("#FFD740"),
            Color.parseColor("#FFAB40"),
            Color.parseColor("#FF6E40")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.navigation_drawer_settings);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment()).commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
        }
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener,
            Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
        FcmRegistrationReceiver fcmRegistrationReceiver;
        MultiSelectListPreference prefFcmChannels;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(Constants.PREF_NOTIFICATIONS_RINGTONE));

            ListPreference prefLanguage = (ListPreference) findPreference(Constants.PREF_LANGUAGE);
            prefLanguage.setOnPreferenceChangeListener(this);

            ListPreference prefTheme = (ListPreference) findPreference(Constants.PREF_THEME);
            prefTheme.setOnPreferenceChangeListener(this);

            Preference prefColor = findPreference(Constants.PREF_PRIMARY_COLOR);
            prefColor.setOnPreferenceClickListener(this);
            prefColor.setSummary(primaryColorName());

            Preference prefAccentColor = findPreference(Constants.PREF_ACCENT_COLOR);
            prefAccentColor.setOnPreferenceClickListener(this);
            prefAccentColor.setSummary(accentColorName());

            Preference prefRevertTheme = findPreference(Constants.PREF_REVERT_THEME);
            prefRevertTheme.setOnPreferenceClickListener(this);

            prefFcmChannels = (MultiSelectListPreference) findPreference(Constants.PREF_GCM_CHANNELS);
            prefFcmChannels.setOnPreferenceChangeListener(this);
            updateFcmChannelsPrefSummary();

            Preference prefClearTapTargets = findPreference(Constants.PREF_CLEAR_TAP_TARGETS);
            prefClearTapTargets.setOnPreferenceClickListener(this);

            Preference prefClearHistory = findPreference(Constants.PREF_CLEAR_HISTORY);
            prefClearHistory.setOnPreferenceClickListener(this);

            ListPreference prefNotificationColor = (ListPreference) findPreference(Constants.PREF_NOTIFICATIONS_COLOR);
            prefNotificationColor.setOnPreferenceChangeListener(this);

            CheckBoxPreference prefCCT = (CheckBoxPreference) findPreference(Constants.PREF_CCT);
            prefCCT.setOnPreferenceChangeListener(this);

            fcmRegistrationReceiver = new FcmRegistrationReceiver(new FcmReceiverListener() {
                @Override
                public void onFinish(boolean result) {
                    if (result) {
                        try {
                            prefFcmChannels.setValues(prefFcmChannels.getSharedPreferences()
                                    .getStringSet(Constants.PREF_GCM_CHANNELS, prefFcmChannels.getValues()));
                            updateFcmChannelsPrefSummary();
                        } catch (Exception e) {
                            e.printStackTrace();
                            restartApp();
                        }
                    }
                    prefFcmChannels.setEnabled(true);
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            getActivity().registerReceiver(fcmRegistrationReceiver, new IntentFilter(FcmRegistrationReceiver.ACTION_RESULT));
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getActivity().unregisterReceiver(fcmRegistrationReceiver);
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(Constants.PREF_LANGUAGE) || key.equals(Constants.PREF_THEME)
                    || key.equals(Constants.PREF_PRIMARY_COLOR_CODE) || key.equals(Constants.PREF_ACCENT_COLOR_CODE)) {
                MainActivity.needRecreate = true;
                getActivity().recreate();
            }
        }

        @Override
        public boolean onPreferenceClick(final Preference preference) {
            if (preference.getKey().equals(Constants.PREF_PRIMARY_COLOR)) {
                SettingsActivity act = (SettingsActivity) getActivity();
                if (act == null)
                    return false;
                ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
                colorPickerDialog.initialize(
                        R.string.color_picker_default_title, PRIMARY_COLORS, MainActivity.getPrimaryColorCode(act), 4, PRIMARY_COLORS.length);
                colorPickerDialog.show(getFragmentManager(), Constants.FRAGMENT_PRIMARY_COLOR_PICKER);
                colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        if (color != MainActivity.getPrimaryColorCode(getActivity())) {
                            PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                                    .edit().putInt(Constants.PREF_PRIMARY_COLOR_CODE, color).apply();
                        }
                    }
                });
            } else if (preference.getKey().equals(Constants.PREF_ACCENT_COLOR)) {
                SettingsActivity act = (SettingsActivity) getActivity();
                if (act == null)
                    return false;
                ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
                colorPickerDialog.initialize(
                        R.string.color_picker_default_title, ACCENT_COLORS, MainActivity.getAccentColorCode(act), 4, ACCENT_COLORS.length);
                colorPickerDialog.show(getFragmentManager(), Constants.FRAGMENT_ACCENT_COLOR_PICKER);
                colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        if (color != MainActivity.getAccentColorCode(getActivity())) {
                            PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                                    .edit().putInt(Constants.PREF_ACCENT_COLOR_CODE, color).apply();
                        }
                    }
                });
            } else if (preference.getKey().equals(Constants.PREF_REVERT_THEME)) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.are_you_sure)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit()
                                        .putInt(Constants.PREF_PRIMARY_COLOR_CODE, ContextCompat.getColor(getActivity()
                                                .getApplicationContext(), R.color.blue_primary_color))
                                        .putInt(Constants.PREF_ACCENT_COLOR_CODE, ContextCompat.getColor(getActivity()
                                                .getApplicationContext(), R.color.red_accent_color))
                                        .putString(Constants.PREF_THEME, "light")
                                        .apply();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            } else if (preference.getKey().equals(Constants.PREF_CLEAR_TAP_TARGETS)) {
                PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit()
                        .putBoolean(Constants.SHOW_CYCLE_FAB_TAP_TARGET, true)
                        .putBoolean(Constants.SHOW_FILTER_TAP_TARGET, true)
                        .apply();
                Snackbar.make(getActivity().findViewById(R.id.coordinatorLayout), R.string.pref_tap_targets_cleared, Snackbar.LENGTH_SHORT).show();
            } else if (preference.getKey().equals(Constants.PREF_CLEAR_HISTORY)) {
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                        SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
                suggestions.clearHistory();
                Snackbar.make(getActivity().findViewById(R.id.coordinatorLayout), R.string.pref_search_history_cleared, Snackbar.LENGTH_SHORT).show();
            }
            return false;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference.getKey().equals(Constants.PREF_LANGUAGE)) {
                PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                        .edit().putString(Constants.PREF_LANGUAGE, newValue.toString()).apply();
                LocaleUtils.updateLocale(getActivity());
            } else if (preference.getKey().equals(Constants.PREF_THEME)) {
                PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                        .edit().putString(Constants.PREF_THEME, newValue.toString()).apply();
            } else if (preference.getKey().equals(Constants.PREF_GCM_CHANNELS)) {
                preference.setEnabled(false);
                Toast.makeText(getActivity(), getResources().getString(R.string.fcm_settings_change), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), FcmRegistrationService.class);
                intent.setAction(FcmRegistrationService.ACTION_UPDATE_CHANNELS);
                intent.putExtra(Constants.EXTRA_GCM_CHANNELS, ((HashSet<String>) newValue));
                getActivity().startService(intent);
            } else if (preference.getKey().equals(Constants.PREF_NOTIFICATIONS_COLOR)) {
                PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                        .edit().putString(Constants.PREF_NOTIFICATIONS_COLOR, newValue.toString()).apply();
                int i = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                CharSequence[] entries = ((ListPreference) preference).getEntries();
                preference.setSummary(entries[i]);
                return true;
            } else if (preference.getKey().equals(Constants.PREF_CCT)) {
                return true;
            }
            return false;
        }

        public String primaryColorName() {
            String primaryColorName;
            if (MainActivity.getPrimaryColorCode(getActivity()) == Color.parseColor("#F44336")) {
                primaryColorName = getString(R.string.red);
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == Color.parseColor("#E91E63")) {
                primaryColorName = (getString(R.string.pink));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == Color.parseColor("#9C27B0")) {
                primaryColorName = (getString(R.string.purple));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == Color.parseColor("#673AB7")) {
                primaryColorName = (getString(R.string.deep_purple));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == Color.parseColor("#3F51B5")) {
                primaryColorName = (getString(R.string.indigo));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == Color.parseColor("#2196F3")) {
                primaryColorName = (getString(R.string.blue));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == Color.parseColor("#03A9F4")) {
                primaryColorName = (getString(R.string.light_blue));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == Color.parseColor("#00BCD4")) {
                primaryColorName = (getString(R.string.cyan));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == Color.parseColor("#009688")) {
                primaryColorName = (getString(R.string.teal));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == Color.parseColor("#4CAF50")) {
                primaryColorName = (getString(R.string.green));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == Color.parseColor("#8BC34A")) {
                primaryColorName = (getString(R.string.light_green));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == Color.parseColor("#CDDC39")) {
                primaryColorName = (getString(R.string.lime));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == Color.parseColor("#FFEB3B")) {
                primaryColorName = (getString(R.string.yellow));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == Color.parseColor("#FFC107")) {
                primaryColorName = (getString(R.string.amber));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == Color.parseColor("#FF9800")) {
                primaryColorName = (getString(R.string.orange));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == Color.parseColor("#FF5722")) {
                primaryColorName = (getString(R.string.deep_orange));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == Color.parseColor("#795548")) {
                primaryColorName = (getString(R.string.brown));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == Color.parseColor("#9E9E9E")) {
                primaryColorName = (getString(R.string.grey));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == Color.parseColor("#607D8B")) {
                primaryColorName = (getString(R.string.blue_grey));
            } else {
                primaryColorName = (getString(R.string.black));
            }
            return primaryColorName;
        }

        public String accentColorName() {
            String accentColorName;
            if (MainActivity.getAccentColorCode(getActivity()) == Color.parseColor("#FF5252")) {
                accentColorName = getString(R.string.red);
            } else if (MainActivity.getAccentColorCode(getActivity()) == Color.parseColor("#FF4081")) {
                accentColorName = (getString(R.string.pink));
            } else if (MainActivity.getAccentColorCode(getActivity()) == Color.parseColor("#E040FB")) {
                accentColorName = (getString(R.string.purple));
            } else if (MainActivity.getAccentColorCode(getActivity()) == Color.parseColor("#7C4DFF")) {
                accentColorName = (getString(R.string.deep_purple));
            } else if (MainActivity.getAccentColorCode(getActivity()) == Color.parseColor("#536DFE")) {
                accentColorName = (getString(R.string.indigo));
            } else if (MainActivity.getAccentColorCode(getActivity()) == Color.parseColor("#448AFF")) {
                accentColorName = (getString(R.string.blue));
            } else if (MainActivity.getAccentColorCode(getActivity()) == Color.parseColor("#40C4FF")) {
                accentColorName = (getString(R.string.light_blue));
            } else if (MainActivity.getAccentColorCode(getActivity()) == Color.parseColor("#18FFFF")) {
                accentColorName = (getString(R.string.cyan));
            } else if (MainActivity.getAccentColorCode(getActivity()) == Color.parseColor("#64FFDA")) {
                accentColorName = (getString(R.string.teal));
            } else if (MainActivity.getAccentColorCode(getActivity()) == Color.parseColor("#69F0AE")) {
                accentColorName = (getString(R.string.green));
            } else if (MainActivity.getAccentColorCode(getActivity()) == Color.parseColor("#B2FF59")) {
                accentColorName = (getString(R.string.light_green));
            } else if (MainActivity.getAccentColorCode(getActivity()) == Color.parseColor("#EEFF41")) {
                accentColorName = (getString(R.string.lime));
            } else if (MainActivity.getAccentColorCode(getActivity()) == Color.parseColor("#FFFF00")) {
                accentColorName = (getString(R.string.yellow));
            } else if (MainActivity.getAccentColorCode(getActivity()) == Color.parseColor("#FFD740")) {
                accentColorName = (getString(R.string.amber));
            } else if (MainActivity.getAccentColorCode(getActivity()) == Color.parseColor("#FFAB40")) {
                accentColorName = (getString(R.string.orange));
            } else {
                accentColorName = (getString(R.string.deep_orange));
            }
            return accentColorName;
        }

        private void restartApp() {
            getActivity().finishAffinity();
            getActivity().overridePendingTransition(0, 0);
            startActivity(new Intent(getActivity(), MainActivity.class));
            startActivity(getActivity().getIntent());
            getActivity().overridePendingTransition(0, 0);
//            TaskStackBuilder.create(getActivity().getApplicationContext())
//                    .addNextIntentWithParentStack(getActivity().getIntent()).startActivities(); 
        }

        private void updateFcmChannelsPrefSummary() {
            Set<String> values = prefFcmChannels.getValues();
            Set<CharSequence> labels = new HashSet<>();
            for (String value : values) {
                int index = prefFcmChannels.findIndexOfValue(value);
                labels.add(prefFcmChannels.getEntries()[index]);
            }
            if (!values.isEmpty()) {
                prefFcmChannels.setSummary(labels.toString().replaceAll("\\[", "").replaceAll("\\]", ""));
            } else {
                prefFcmChannels.setSummary(getString(R.string.pref_fcm_channels_summary));
            }
        }
    }
}