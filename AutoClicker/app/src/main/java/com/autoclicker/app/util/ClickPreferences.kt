package com.autoclicker.app.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Helper class for managing app preferences using SharedPreferences
 */
class ClickPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var clickInterval: Long
        get() = prefs.getLong(KEY_CLICK_INTERVAL, 1000L)
        set(value) = prefs.edit { putLong(KEY_CLICK_INTERVAL, value) }

    var targetX: Int
        get() = prefs.getInt(KEY_TARGET_X, 0)
        set(value) = prefs.edit { putInt(KEY_TARGET_X, value) }

    var targetY: Int
        get() = prefs.getInt(KEY_TARGET_Y, 0)
        set(value) = prefs.edit { putInt(KEY_TARGET_Y, value) }

    var randomOffset: Int
        get() = prefs.getInt(KEY_RANDOM_OFFSET, 0)
        set(value) = prefs.edit { putInt(KEY_RANDOM_OFFSET, value) }

    var maxClicks: Int
        get() = prefs.getInt(KEY_MAX_CLICKS, -1)
        set(value) = prefs.edit { putInt(KEY_MAX_CLICKS, value) }

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_VIBRATION_ENABLED, value) }

    var showClickIndicator: Boolean
        get() = prefs.getBoolean(KEY_SHOW_CLICK_INDICATOR, true)
        set(value) = prefs.edit { putBoolean(KEY_SHOW_CLICK_INDICATOR, value) }

    var overlayPermissionGranted: Boolean
        get() = prefs.getBoolean(KEY_OVERLAY_PERMISSION, false)
        set(value) = prefs.edit { putBoolean(KEY_OVERLAY_PERMISSION, value) }

    fun clear() {
        prefs.edit { clear() }
    }

    companion object {
        private const val PREFS_NAME = "autoclicker_prefs"
        
        private const val KEY_CLICK_INTERVAL = "click_interval"
        private const val KEY_TARGET_X = "target_x"
        private const val KEY_TARGET_Y = "target_y"
        private const val KEY_RANDOM_OFFSET = "random_offset"
        private const val KEY_MAX_CLICKS = "max_clicks"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_SHOW_CLICK_INDICATOR = "show_click_indicator"
        private const val KEY_OVERLAY_PERMISSION = "overlay_permission"
    }
}
