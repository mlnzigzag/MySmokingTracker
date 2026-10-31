package com.gasperpintar.smokingtracker.utils

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.gasperpintar.smokingtracker.R

enum class HomeButtonColor(
    @param:ColorRes val colorRes: Int,
    @param:StringRes val labelRes: Int,
    val colorValue: Int
) {
    ORANGE(R.color.home_button_orange, R.string.home_button_color_orange, 0xFFB95000.toInt()),
    BLUE(R.color.home_button_blue, R.string.home_button_color_blue, 0xFF1565C0.toInt()),
    GREEN(R.color.home_button_green, R.string.home_button_color_green, 0xFF2E7D32.toInt()),
    PURPLE(R.color.home_button_purple, R.string.home_button_color_purple, 0xFF6A1B9A.toInt()),
    RED(R.color.home_button_red, R.string.home_button_color_red, 0xFFB71C1C.toInt()),
    GRAY(R.color.home_button_gray, R.string.home_button_color_gray, 0xFF455A64.toInt());

    companion object {
        val DEFAULT = ORANGE

        fun fromValue(value: Int): HomeButtonColor? = entries.firstOrNull { it.colorValue == value }
    }
}
