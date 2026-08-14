package com.buwin.smartfancooling.data.model

/**
 * Represents a discovered BLE device.
 */
data class BleDeviceItem(
    val name: String,
    val address: String,
    val rssi: Int = 0,
    val isSmartFan: Boolean = false
)
