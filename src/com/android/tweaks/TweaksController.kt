/*
* Copyright (C) 2023-2024 the RisingOS Android Project
* Copyright (C) 2024-2025 the EverestOS Project
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
package com.android.tweaks

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.widget.LayoutPreference

class TweaksController(context: Context) : AbstractPreferenceController(context) {

   override fun displayPreference(screen: PreferenceScreen) {
       super.displayPreference(screen)
       screen.findPreference<LayoutPreference>(KEY_TWEAKS)?.let { tweaksPref ->
           setupTweaksClickListeners(tweaksPref)
       }
   }

   private fun setupTweaksClickListeners(preference: LayoutPreference) {
       // Handle wallpaper card separately since it uses a different launch method
       preference.findViewById<View>(R.id.wallpaper_card)?.setOnClickListener {
           launchWallpaperPickerActivity()
       }

       val TweaksClickMap = mapOf(
           R.id.fonts_card to "com.android.settings.Settings\$TweaksFontsActivity",
           R.id.theme_card to "com.android.settings.Settings\$TweaksThemesActivity",
           R.id.profile_card to "com.android.settings.Settings\$TweaksProfileActivity",
           R.id.about_card to "com.android.settings.Settings\$TweaksAboutActivity",
           R.id.lockscreen_card to "com.android.settings.Settings\$TweaksLockscreenActivity",
           R.id.volume_card to "com.android.settings.Settings\$TweaksVolumeActivity",
           R.id.notifications_card to "com.android.settings.Settings\$TweaksNotificationsActivity",
           R.id.qs_card to "com.android.settings.Settings\$TweaksQuickSettingsActivity",
           R.id.statusbar_card to "com.android.settings.Settings\$TweaksStatusBarActivity",
           R.id.gestures_card to "com.android.settings.Settings\$TweaksGesturesActivity",
           R.id.clockface_card to "com.android.settings.Settings\$TweaksClockFacesActivity",
           R.id.power_card to "com.android.settings.Settings\$TweaksPowerMenuActivity",
           R.id.battery_card to "com.android.settings.Settings\$TweaksBatteryActivity",
           R.id.system_card to "com.android.settings.Settings\$TweaksSystemActivity",
           R.id.misc_card to "com.android.settings.Settings\$TweaksMiscActivity"
       )

       TweaksClickMap.forEach { (viewId, activityName) ->
           preference.findViewById<View>(viewId)?.setOnClickListener {
               mContext.startActivity(createIntent(activityName))
           }
       }
   }

   private fun launchWallpaperPickerActivity() {
       val intent = Intent().apply {
           setClassName(
               "com.google.android.apps.wallpaper",
               "com.google.android.apps.wallpaper.picker.CategoryPickerActivity"
           )
       }
       mContext.startActivity(intent)
   }

   private fun createIntent(activityName: String): Intent {
       return Intent().setComponent(ComponentName("com.android.settings", activityName))
   }

   override fun isAvailable(): Boolean = true

   override fun getPreferenceKey(): String = KEY_TWEAKS

   companion object {
       private const val KEY_TWEAKS = "tweaks_dashboard_quick_access"
   }
}
