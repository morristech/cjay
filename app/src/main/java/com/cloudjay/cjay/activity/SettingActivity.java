package com.cloudjay.cjay.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.util.Logger;

import org.androidannotations.annotations.EActivity;

/**
 * Created by thai on 07/11/2014.
 */
@EActivity
public class SettingActivity extends PreferenceActivity {

    static String PREF_KEY_AUTO_CHECK_UPDATE;
    static String PREF_KEY_RAINY_MODE;
    static String PREF_KEY_ENABLE_LOGGER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PREF_KEY_AUTO_CHECK_UPDATE = getString(R.string.pref_key_auto_check_update_checkbox);
        PREF_KEY_RAINY_MODE = getString(R.string.pref_key_enable_temporary_fragment_checkbox);
        PREF_KEY_ENABLE_LOGGER = getString(R.string.pref_key_enable_logger_checkbox);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupSimplePreferencesScreen();
    }

    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen() {

        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference(PREF_KEY_AUTO_CHECK_UPDATE));
        bindPreferenceSummaryToValue(findPreference(PREF_KEY_RAINY_MODE));
        bindPreferenceSummaryToValue(findPreference(PREF_KEY_ENABLE_LOGGER));

        Preference findPreference = findPreference("secret_log");


        findPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                //TODO: open Notification log
                return false;
            }
        });
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            String key = preference.getKey();
            if (key.equals(PREF_KEY_ENABLE_LOGGER)) {
                Logger.getInstance().setDebuggable((Boolean) value);
            }

            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {

        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        Object newValue = null;
        if (preference instanceof CheckBoxPreference) {

            newValue = PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                    .getBoolean(preference.getKey(), false);

        } else {
            newValue = PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), "");
        }

        // Trigger the listener immediately with the preference's current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, newValue);
    }
}
