package com.gasperpintar.smokingtracker.utils

import java.time.Duration
import java.time.LocalDateTime
import java.util.Locale

object ElapsedTime {
    const val DEFAULT_THRESHOLD_MINUTES = 60
    const val MAX_THRESHOLD_MINUTES = 10_080

    fun durationSince(lastSmoked: LocalDateTime?, now: LocalDateTime): Duration? {
        val duration = lastSmoked?.let { Duration.between(it, now) } ?: return null
        return if (duration.isNegative) Duration.ZERO else duration
    }

    fun isBelowThreshold(duration: Duration?, thresholdMinutes: Int): Boolean {
        return duration != null && duration < Duration.ofMinutes(thresholdMinutes.toLong())
    }

    fun format(duration: Duration): String {
        val safeDuration = if (duration.isNegative) Duration.ZERO else duration
        return String.format(
            Locale.ROOT,
            "%02d:%02d:%02d",
            safeDuration.toHours(),
            (safeDuration.toMinutes() % 60).toInt(),
            (safeDuration.seconds % 60).toInt()
        )
    }

    fun parseThresholdMinutes(input: CharSequence): Int? {
        val value = input.toString().trim().toLongOrNull() ?: return null
        return value.takeIf { it in 1..MAX_THRESHOLD_MINUTES.toLong() }?.toInt()
    }
}
