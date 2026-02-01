package com.example.btlearninglab.data.scale

import android.content.Context
import android.content.SharedPreferences

/**
 * Repository for storing the latest weight data.
 */
class WeightRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("weight_data", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_WEIGHT = "latest_weight"
        private const val KEY_TIMESTAMP = "latest_timestamp"
    }

    /**
     * Save the latest weight.
     */
    fun saveWeight(weight: Double) {
        prefs.edit()
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    /**
     * Get the latest weight.
     */
    fun getLatestWeight(): Double {
        return prefs.getFloat(KEY_WEIGHT, 0f).toDouble()
    }

    /**
     * Get the timestamp of the latest weight.
     */
    fun getLatestTimestamp(): Long {
        return prefs.getLong(KEY_TIMESTAMP, 0L)
    }
}
