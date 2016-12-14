package com.basmapp.marshal.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class SettingsActivity extends BaseActivity {

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

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment()).commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            if (LocaleUtils.isRtl(getResources())) {
                overridePendingTransition(R.anim.activity_close_enter,
                        R.anim.activity_close_exit_rtl);
            } else {
                overridePendingTransition(R.anim.activity_close_enter,
                        R.anim.activity_close_exit);
            }
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
        private FcmRegistrationReceiver fcmRegistrationReceiver;
        private MultiSelectListPreference prefFcmChannels;
        private int[] PRIMARY_COLORS;
        private int[] ACCENT_COLORS;

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

            Preference prefAccentColor = findPreference(Constants.PREF_ACCENT_COLOR);
            prefAccentColor.setOnPreferenceClickListener(this);

            Preference prefRevertTheme = findPreference(Constants.PREF_REVERT_THEME);
            prefRevertTheme.setOnPreferenceClickListener(this);

            prefFcmChannels = (MultiSelectListPreference) findPreference(Constants.PREF_FCM_CHANNELS);
            prefFcmChannels.setOnPreferenceChangeListener(this);

            Set<String> categories = prefFcmChannels.getSharedPreferences()
                    .getStringSet(Constants.PREF_CATEGORIES, new HashSet<String>());
            ArrayList<String> channelsNames = new ArrayList<>();
            ArrayList<String> channelsValues = new ArrayList<>();
            for (String categoryValues : categories) {
                String[] values = categoryValues.split(";");
                if (Locale.getDefault().toString().toLowerCase().equals("en")) {
                    channelsNames.add(values[1]);
                } else if (Locale.getDefault().toString().toLowerCase().equals("iw")) {
                    channelsNames.add(values[2]);
                }
                channelsValues.add(values[0]);
            }
            CharSequence[] channelsNamesSequences = channelsNames.toArray(new CharSequence[channelsNames.size()]);
            CharSequence[] channelsValuesSequences = channelsValues.toArray(new CharSequence[channelsValues.size()]);

            prefFcmChannels.setEntryValues(channelsValuesSequences);
            prefFcmChannels.setEntries(channelsNamesSequences);
            prefFcmChannels.setDefaultValue(channelsValuesSequences);
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
                                    .getStringSet(Constants.PREF_FCM_CHANNELS, prefFcmChannels.getValues()));
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
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            PRIMARY_COLORS = new int[]{
                    ContextCompat.getColor(getActivity(), R.color.red_primary),
                    ContextCompat.getColor(getActivity(), R.color.pink_primary),
                    ContextCompat.getColor(getActivity(), R.color.purple_primary),
                    ContextCompat.getColor(getActivity(), R.color.deep_purple_primary),
                    ContextCompat.getColor(getActivity(), R.color.indigo_primary),
                    ContextCompat.getColor(getActivity(), R.color.blue_primary),
                    ContextCompat.getColor(getActivity(), R.color.light_blue_primary),
                    ContextCompat.getColor(getActivity(), R.color.cyan_primary),
                    ContextCompat.getColor(getActivity(), R.color.teal_primary),
                    ContextCompat.getColor(getActivity(), R.color.green_primary),
                    ContextCompat.getColor(getActivity(), R.color.light_green_primary),
                    ContextCompat.getColor(getActivity(), R.color.lime_primary),
                    ContextCompat.getColor(getActivity(), R.color.yellow_primary),
                    ContextCompat.getColor(getActivity(), R.color.amber_primary),
                    ContextCompat.getColor(getActivity(), R.color.orange_primary),
                    ContextCompat.getColor(getActivity(), R.color.deep_orange_primary),
                    ContextCompat.getColor(getActivity(), R.color.brown_primary),
                    ContextCompat.getColor(getActivity(), R.color.grey_primary),
                    ContextCompat.getColor(getActivity(), R.color.blue_grey_primary),
                    ContextCompat.getColor(getActivity(), R.color.black_primary)
            };

            ACCENT_COLORS = new int[]{
                    ContextCompat.getColor(getActivity(), R.color.red_accent),
                    ContextCompat.getColor(getActivity(), R.color.pink_accent),
                    ContextCompat.getColor(getActivity(), R.color.purple_accent),
                    ContextCompat.getColor(getActivity(), R.color.deep_purple_accent),
                    ContextCompat.getColor(getActivity(), R.color.indigo_accent),
                    ContextCompat.getColor(getActivity(), R.color.blue_accent),
                    ContextCompat.getColor(getActivity(), R.color.light_blue_accent),
                    ContextCompat.getColor(getActivity(), R.color.cyan_accent),
                    ContextCompat.getColor(getActivity(), R.color.teal_accent),
                    ContextCompat.getColor(getActivity(), R.color.green_accent),
                    ContextCompat.getColor(getActivity(), R.color.light_green_accent),
                    ContextCompat.getColor(getActivity(), R.color.lime_accent),
                    ContextCompat.getColor(getActivity(), R.color.yellow_accent),
                    ContextCompat.getColor(getActivity(), R.color.amber_accent),
                    ContextCompat.getColor(getActivity(), R.color.orange_accent),
                    ContextCompat.getColor(getActivity(), R.color.deep_orange_accent),
            };

            findPreference(Constants.PREF_PRIMARY_COLOR).setSummary(primaryColorName());
            findPreference(Constants.PREF_ACCENT_COLOR).setSummary(accentColorName());
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
                new RevertThemeConfirmationDialog().show(getChildFragmentManager(), Constants.DIALOG_FRAGMENT_REVERT_THEME);
            } else if (preference.getKey().equals(Constants.PREF_CLEAR_TAP_TARGETS)) {
                PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit()
                        .putBoolean(Constants.SHOW_CYCLE_FAB_TAP_TARGET, true)
                        .putBoolean(Constants.SHOW_FILTER_TAP_TARGET, true)
                        .apply();
                Snackbar.make(getActivity().findViewById(R.id.coordinatorLayout), R.string.pref_tap_targets_cleared, Snackbar.LENGTH_SHORT).show();
            } else if (preference.getKey().equals(Constants.PREF_CLEAR_HISTORY)) {
                SuggestionProvider.clear(getActivity());
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
            } else if (preference.getKey().equals(Constants.PREF_FCM_CHANNELS)) {
                preference.setEnabled(false);
                Toast.makeText(getActivity(), getResources().getString(R.string.fcm_settings_change), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), FcmRegistrationService.class);
                intent.setAction(FcmRegistrationService.ACTION_UPDATE_CHANNELS);
                intent.putExtra(Constants.EXTRA_FCM_CHANNELS, ((HashSet<String>) newValue));
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
            if (MainActivity.getPrimaryColorCode(getActivity()) == PRIMARY_COLORS[0]) {
                primaryColorName = getString(R.string.red);
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == PRIMARY_COLORS[1]) {
                primaryColorName = (getString(R.string.pink));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == PRIMARY_COLORS[2]) {
                primaryColorName = (getString(R.string.purple));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == PRIMARY_COLORS[3]) {
                primaryColorName = (getString(R.string.deep_purple));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == PRIMARY_COLORS[4]) {
                primaryColorName = (getString(R.string.indigo));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == PRIMARY_COLORS[5]) {
                primaryColorName = (getString(R.string.blue));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == PRIMARY_COLORS[6]) {
                primaryColorName = (getString(R.string.light_blue));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == PRIMARY_COLORS[7]) {
                primaryColorName = (getString(R.string.cyan));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == PRIMARY_COLORS[8]) {
                primaryColorName = (getString(R.string.teal));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == PRIMARY_COLORS[9]) {
                primaryColorName = (getString(R.string.green));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == PRIMARY_COLORS[10]) {
                primaryColorName = (getString(R.string.light_green));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == PRIMARY_COLORS[11]) {
                primaryColorName = (getString(R.string.lime));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == PRIMARY_COLORS[12]) {
                primaryColorName = (getString(R.string.yellow));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == PRIMARY_COLORS[13]) {
                primaryColorName = (getString(R.string.amber));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == PRIMARY_COLORS[14]) {
                primaryColorName = (getString(R.string.orange));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == PRIMARY_COLORS[15]) {
                primaryColorName = (getString(R.string.deep_orange));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == PRIMARY_COLORS[16]) {
                primaryColorName = (getString(R.string.brown));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == PRIMARY_COLORS[17]) {
                primaryColorName = (getString(R.string.grey));
            } else if (MainActivity.getPrimaryColorCode(getActivity()) == PRIMARY_COLORS[18]) {
                primaryColorName = (getString(R.string.blue_grey));
            } else {
                primaryColorName = (getString(R.string.black));
            }
            return primaryColorName;
        }

        public String accentColorName() {
            String accentColorName;
            if (MainActivity.getAccentColorCode(getActivity()) == ACCENT_COLORS[0]) {
                accentColorName = getString(R.string.red);
            } else if (MainActivity.getAccentColorCode(getActivity()) == ACCENT_COLORS[1]) {
                accentColorName = (getString(R.string.pink));
            } else if (MainActivity.getAccentColorCode(getActivity()) == ACCENT_COLORS[2]) {
                accentColorName = (getString(R.string.purple));
            } else if (MainActivity.getAccentColorCode(getActivity()) == ACCENT_COLORS[3]) {
                accentColorName = (getString(R.string.deep_purple));
            } else if (MainActivity.getAccentColorCode(getActivity()) == ACCENT_COLORS[4]) {
                accentColorName = (getString(R.string.indigo));
            } else if (MainActivity.getAccentColorCode(getActivity()) == ACCENT_COLORS[5]) {
                accentColorName = (getString(R.string.blue));
            } else if (MainActivity.getAccentColorCode(getActivity()) == ACCENT_COLORS[6]) {
                accentColorName = (getString(R.string.light_blue));
            } else if (MainActivity.getAccentColorCode(getActivity()) == ACCENT_COLORS[7]) {
                accentColorName = (getString(R.string.cyan));
            } else if (MainActivity.getAccentColorCode(getActivity()) == ACCENT_COLORS[8]) {
                accentColorName = (getString(R.string.teal));
            } else if (MainActivity.getAccentColorCode(getActivity()) == ACCENT_COLORS[9]) {
                accentColorName = (getString(R.string.green));
            } else if (MainActivity.getAccentColorCode(getActivity()) == ACCENT_COLORS[10]) {
                accentColorName = (getString(R.string.light_green));
            } else if (MainActivity.getAccentColorCode(getActivity()) == ACCENT_COLORS[11]) {
                accentColorName = (getString(R.string.lime));
            } else if (MainActivity.getAccentColorCode(getActivity()) == ACCENT_COLORS[12]) {
                accentColorName = (getString(R.string.yellow));
            } else if (MainActivity.getAccentColorCode(getActivity()) == ACCENT_COLORS[13]) {
                accentColorName = (getString(R.string.amber));
            } else if (MainActivity.getAccentColorCode(getActivity()) == ACCENT_COLORS[14]) {
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

    public static class RevertThemeConfirmationDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.are_you_sure)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit()
                                    .putInt(Constants.PREF_PRIMARY_COLOR_CODE, ContextCompat.getColor(getActivity()
                                            .getApplicationContext(), R.color.blue_primary))
                                    .putInt(Constants.PREF_ACCENT_COLOR_CODE, ContextCompat.getColor(getActivity()
                                            .getApplicationContext(), R.color.red_accent))
                                    .putString(Constants.PREF_THEME, "light")
                                    .apply();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
        }
    }

}