package com.gasperpintar.smokingtracker.utils

import android.content.Context
import androidx.core.content.edit

class ElapsedTimePreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun getThresholdMinutes(): Int {
        return preferences.getInt(THRESHOLD_KEY, ElapsedTime.DEFAULT_THRESHOLD_MINUTES)
            .takeIf { it in 1..ElapsedTime.MAX_THRESHOLD_MINUTES }
            ?: ElapsedTime.DEFAULT_THRESHOLD_MINUTES
    }

    fun setThresholdMinutes(value: Int) {
        require(value in 1..ElapsedTime.MAX_THRESHOLD_MINUTES) { "Invalid elapsed-time threshold" }
        preferences.edit { putInt(THRESHOLD_KEY, value) }
    }

    private companion object {
        const val FILE_NAME = "my_smoking_tracker_settings"
        const val THRESHOLD_KEY = "elapsed_time_warning_threshold_minutes"
    }
}
