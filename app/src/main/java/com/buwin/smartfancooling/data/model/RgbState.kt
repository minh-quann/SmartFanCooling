package com.buwin.smartfancooling.data.model

import androidx.compose.ui.graphics.Color

/**
 * Lighting modes supported by the ESP32 firmware.
 */
enum class RgbMode(val code: Int, val title: String, val description: String) {
    OFF(0, "Off", "Disable all lighting"),
    STATIC(1, "Static", "Solid single color"),
    RAINBOW(2, "Rainbow", "Smooth spectrum flow"),
    BREATHING(3, "Breathing", "Pulsing glow animation"),
    SPEED_SYNC(4, "Speed Sync", "Color shifts with fan RPM"),
    WAVE(5, "Wave", "Flowing wave across ring"),
    FIRE(6, "Fire", "Flickering fiery embers"),
    COMET(7, "Comet", "Orbiting light trail"),
    PULSE(8, "Pulse", "Heartbeat rhythm"),
    DUAL_SPIN(9, "Dual Spin", "Dual chasing tracers");

    companion object {
        fun fromCode(code: Int): RgbMode = entries.firstOrNull { it.code == code } ?: RAINBOW
    }
}

/**
 * RGB lighting configuration state.
 */
data class RgbState(
    val isPoweredOn: Boolean = true,
    val mode: RgbMode = RgbMode.RAINBOW,
    val red: Int = 0,
    val green: Int = 220,
    val blue: Int = 255,
    val brightness: Int = 200 // 0 - 255
) {
    val composeColor: Color
        get() = Color(red, green, blue)

    val brightnessPercent: Int
        get() = ((brightness.coerceIn(0, 255) * 100) / 255)
}
