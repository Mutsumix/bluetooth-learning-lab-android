package com.example.btlearninglab.data.ble

import android.content.Context
import android.content.SharedPreferences

class ScaleDeviceRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "scale_device_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_SAVED_DEVICE_ADDRESS = "saved_device_address"
    }

    fun saveSelectedDevice(address: String) {
        prefs.edit().putString(KEY_SAVED_DEVICE_ADDRESS, address).apply()
    }

    fun getSavedDeviceAddress(): String? {
        return prefs.getString(KEY_SAVED_DEVICE_ADDRESS, null)
    }

    fun clearSavedDevice() {
        prefs.edit().remove(KEY_SAVED_DEVICE_ADDRESS).apply()
    }
}
