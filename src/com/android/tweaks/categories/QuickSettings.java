/*
 * Copyright (C) 2023 The EverestOS Project
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

package com.android.tweaks.categories;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.os.UserHandle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import java.util.Arrays;
import java.util.List;

import lineageos.preference.LineageSecureSettingSwitchPreference;
import lineageos.providers.LineageSettings;
import com.android.settings.preferences.SystemSettingSwitchPreference;

@SearchIndexable
public class QuickSettings extends SettingsPreferenceFragment 
            implements Preference.OnPreferenceChangeListener {

    private static final String KEY_BRIGHTNESS_SLIDER_POSITION = "qs_brightness_slider_position";
    private static final String KEY_BRIGHTNESS_SLIDER_HAPTIC = "qs_brightness_slider_haptic";
    private static final String KEY_INTERFACE_CATEGORY = "quick_settings_interface_category";
    private static final String KEY_SHOW_BRIGHTNESS_SLIDER = "qs_show_brightness_slider";
    private static final String KEY_SHOW_AUTO_BRIGHTNESS = "qs_show_auto_brightness";

    private PreferenceCategory mInterfaceCategory;
    private ListPreference mShowBrightnessSlider;
    private ListPreference mBrightnessSliderPosition;
    private LineageSecureSettingSwitchPreference mShowAutoBrightness;
    private SystemSettingSwitchPreference mBrightnessSliderHaptic;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.everest_quicksettings);
        PreferenceScreen prefSet = getPreferenceScreen();
        final Resources res = getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        final Resources resources = mContext.getResources();
        mShowBrightnessSlider = findPreference(KEY_SHOW_BRIGHTNESS_SLIDER);
        mShowBrightnessSlider.setOnPreferenceChangeListener(this);
        boolean showSlider = LineageSettings.Secure.getIntForUser(resolver,
                LineageSettings.Secure.QS_SHOW_BRIGHTNESS_SLIDER, 1, UserHandle.USER_CURRENT) > 0;
        mBrightnessSliderPosition = findPreference(KEY_BRIGHTNESS_SLIDER_POSITION);
        mBrightnessSliderPosition.setEnabled(showSlider);
        mBrightnessSliderHaptic = findPreference(KEY_BRIGHTNESS_SLIDER_HAPTIC);
        mBrightnessSliderHaptic.setEnabled(showSlider);
        mShowAutoBrightness = findPreference(KEY_SHOW_AUTO_BRIGHTNESS);
        boolean automaticAvailable = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_automatic_brightness_available);
        if (automaticAvailable) {
            mShowAutoBrightness.setEnabled(showSlider);
        } else {
            prefScreen.removePreference(mShowAutoBrightness);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        if (preference == mShowBrightnessSlider) {
            int value = Integer.parseInt((String) newValue);
            mBrightnessSliderPosition.setEnabled(value > 0);
            mBrightnessSliderHaptic.setEnabled(value > 0);
            if (mShowAutoBrightness != null)
                mShowAutoBrightness.setEnabled(value > 0);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.EVEREST;
    }
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.everest_quicksettings;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);

                boolean autoBrightnessAvailable = resources.getBoolean(
                        com.android.internal.R.bool.config_automatic_brightness_available);
                if (!autoBrightnessAvailable) {
                    keys.add(KEY_SHOW_AUTO_BRIGHTNESS);
                }
                    return keys;
                }
            };
}
