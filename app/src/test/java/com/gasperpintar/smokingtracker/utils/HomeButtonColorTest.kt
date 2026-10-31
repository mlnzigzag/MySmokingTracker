package com.gasperpintar.smokingtracker.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeButtonColorTest {
    @Test
    fun defaultColorIsOriginalOrange() {
        assertEquals(HomeButtonColor.ORANGE, HomeButtonColor.DEFAULT)
        assertEquals(0xFFB95000.toInt(), HomeButtonColor.DEFAULT.colorValue)
    }

    @Test
    fun onlySupportedColorsResolve() {
        HomeButtonColor.entries.forEach { color ->
            assertEquals(color, HomeButtonColor.fromValue(color.colorValue))
        }
        assertNull(HomeButtonColor.fromValue(0))
    }
}
