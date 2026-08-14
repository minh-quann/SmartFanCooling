package com.buwin.smartfancooling.data.model

/**
 * State representing the cooling fan telemetry and control.
 */
data class FanState(
    val speedPercent: Int = 50,
    val isPoweredOn: Boolean = true,
    val currentRpm: Int = 0,
    val maxRpm: Int = 2800,
    val minRpm: Int = 300,
    val isAutoCurve: Boolean = false,
    val targetRpm: Int = 1400
) {
    val speedRatio: Float
        get() = (speedPercent.coerceIn(0, 100)) / 100f

    val rpmRatio: Float
        get() = if (maxRpm > 0) (currentRpm.coerceIn(0, maxRpm).toFloat() / maxRpm) else 0f
}
