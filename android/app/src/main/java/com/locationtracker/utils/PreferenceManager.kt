package com.locationtracker.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * 偏好设置管理器
 */
object PreferenceManager {

    private const val PREF_NAME = "location_tracker_prefs"
    private const val KEY_PRIVACY_ACCEPTED = "privacy_accepted"
    private const val KEY_DEVICE_ID = "device_id"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 隐私协议是否已同意
     */
    fun isPrivacyAccepted(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_PRIVACY_ACCEPTED, false)
    }

    fun setPrivacyAccepted(context: Context, accepted: Boolean) {
        getPreferences(context)
            .edit()
            .putBoolean(KEY_PRIVACY_ACCEPTED, accepted)
            .apply()
    }

    /**
     * 设备ID
     */
    fun getDeviceId(context: Context): String? {
        return getPreferences(context).getString(KEY_DEVICE_ID, null)
    }

    fun setDeviceId(context: Context, deviceId: String) {
        getPreferences(context)
            .edit()
            .putString(KEY_DEVICE_ID, deviceId)
            .apply()
    }
}
