package com.gasperpintar.smokingtracker.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime

class ElapsedTimeTest {
    @Test fun durationIsClampedWhenClockMovesBehindLastEntry() {
        val now = LocalDateTime.of(2026, 1, 1, 12, 0)
        assertEquals(Duration.ZERO, ElapsedTime.durationSince(now.plusMinutes(2), now))
        assertNull(ElapsedTime.durationSince(null, now))
    }
    @Test fun formattingSupportsDurationsOverOneDay() {
        assertEquals("00:42:18", ElapsedTime.format(Duration.ofSeconds(2_538)))
        assertEquals("25:00:01", ElapsedTime.format(Duration.ofHours(25).plusSeconds(1)))
        assertEquals("00:00:00", ElapsedTime.format(Duration.ofSeconds(-1)))
    }
    @Test fun warningEndsExactlyAtThreshold() {
        assertTrue(ElapsedTime.isBelowThreshold(Duration.ofSeconds(3_599), 60))
        assertFalse(ElapsedTime.isBelowThreshold(Duration.ofHours(1), 60))
        assertFalse(ElapsedTime.isBelowThreshold(Duration.ofHours(2), 120))
        assertFalse(ElapsedTime.isBelowThreshold(null, 60))
    }
    @Test fun thresholdValidationRejectsInvalidAndOverflowingValues() {
        assertEquals(1, ElapsedTime.parseThresholdMinutes(" 1 "))
        assertEquals(10_080, ElapsedTime.parseThresholdMinutes("10080"))
        listOf("", "0", "-1", "abc", "1.5", "10081", "999999999999999999999").forEach {
            assertNull(it, ElapsedTime.parseThresholdMinutes(it))
        }
    }
}
