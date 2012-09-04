/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.cmparts.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;

import com.cyanogenmod.cmparts.R;
import com.cyanogenmod.cmparts.utils.ShortcutPickHelper;

public class LockscreenButtonActivity extends PreferenceActivity implements
        ShortcutPickHelper.OnPickListener, Preference.OnPreferenceChangeListener {
    private static final String LONG_PRESS_HOME = "pref_lockscreen_long_press_home";
    private static final String LONG_PRESS_MENU = "pref_lockscreen_long_press_menu";
    private static final String LONG_PRESS_SEARCH = "pref_lockscreen_long_press_search";

    private static final String CUSTOM_ENTRY = "custom";

    private ListPreference mLongHomeAction;
    private ListPreference mLongMenuAction;
    private ListPreference mLongSearchAction;
    private ListPreference[] mActions;
    private ListPreference mPickingAction;

    private ShortcutPickHelper mPicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.lockscreen_settings_title_subhead);
        addPreferencesFromResource(R.xml.lockscreen_button_settings);

        mPicker = new ShortcutPickHelper(this, this);

        PreferenceScreen prefSet = getPreferenceScreen();

        mLongHomeAction = (ListPreference) prefSet.findPreference(LONG_PRESS_HOME);
        mLongHomeAction.setKey(Settings.System.LOCKSCREEN_LONG_HOME_ACTION);

        mLongMenuAction = (ListPreference) prefSet.findPreference(LONG_PRESS_MENU);
        mLongMenuAction.setKey(Settings.System.LOCKSCREEN_LONG_MENU_ACTION);

        mLongSearchAction = (ListPreference) prefSet.findPreference(LONG_PRESS_SEARCH);
        mLongSearchAction.setKey(Settings.System.LOCKSCREEN_LONG_SEARCH_ACTION);

        mActions = new ListPreference[] {
            mLongHomeAction, mLongMenuAction, mLongSearchAction
        };
        for (ListPreference pref : mActions) {
            pref.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        for (ListPreference pref : mActions) {
            updateEntry(pref);
        }
    }

    private void updateEntry(ListPreference pref) {
        String value = Settings.System.getString(getContentResolver(), pref.getKey());
        if (value == null) {
            value = "";
        }

        CharSequence entry = findEntryForValue(pref, value);
        if (entry != null) {
            pref.setValue(value);
            pref.setSummary(entry);
            return;
        }

        pref.setValue(CUSTOM_ENTRY);
        pref.setSummary(mPicker.getFriendlyNameForUri(value));
    }

    private CharSequence findEntryForValue(ListPreference pref, CharSequence value) {
        CharSequence[] entries = pref.getEntryValues();
        for (int i = 0; i < entries.length; i++) {
            if (TextUtils.equals(entries[i], value)) {
                return pref.getEntries()[i];
            }
        }
        return null;
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        /* we only have ListPreferences, so know newValue is a string */
        ListPreference list = (ListPreference) pref;
        String value = (String) newValue;

        if (TextUtils.equals(value, CUSTOM_ENTRY)) {
            mPickingAction = list;
            mPicker.pickShortcut();
            return false;
        }

        if (Settings.System.putString(getContentResolver(), list.getKey(), value)) {
            pref.setSummary(findEntryForValue(list, value));
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPicker.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
        if (Settings.System.putString(getContentResolver(), mPickingAction.getKey(), uri)) {
            mPickingAction.setSummary(friendlyName);
        }
    }
}
