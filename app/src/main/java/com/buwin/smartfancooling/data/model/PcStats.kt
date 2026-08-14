package com.buwin.smartfancooling.data.model

/**
 * Real-time PC and system telemetry metrics.
 */
data class PcStats(
    val cpuTemp: Float = 48.5f,
    val gpuTemp: Float = 52.0f,
    val boardTemp: Float = 36.5f,
    val cpuUsage: Float = 24.0f,
    val gpuUsage: Float = 18.0f,
    val ramUsage: Float = 42.0f,
    val ramUsedGb: Float = 13.4f,
    val ramTotalGb: Float = 32.0f,
    val cpuPowerWatts: Float = 65.0f,
    val gpuPowerWatts: Float = 120.0f,
    val lastUpdated: Long = 0L
) {
    val maxTemp: Float
        get() = maxOf(cpuTemp, gpuTemp, boardTemp)

    val totalPowerWatts: Float
        get() = cpuPowerWatts + gpuPowerWatts

    val isHot: Boolean
        get() = maxTemp >= 75.0f

    val isWarning: Boolean
        get() = maxTemp >= 60.0f && maxTemp < 75.0f
}
