package com.buwin.smartfancooling.data.model

/**
 * Real-time PC and system telemetry metrics.
 */
data class PcStats(
    val cpuTemp: Float = 0.0f,
    val gpuTemp: Float = 0.0f,
    val boardTemp: Float = 0.0f,
    val cpuUsage: Float = 0.0f,
    val gpuUsage: Float = 0.0f,
    val ramUsage: Float = 0.0f,
    val lastUpdated: Long = 0L
) {
    val maxTemp: Float
        get() = maxOf(cpuTemp, gpuTemp, boardTemp)

    val isHot: Boolean
        get() = maxTemp >= 75.0f

    val isWarning: Boolean
        get() = maxTemp >= 60.0f && maxTemp < 75.0f
}
