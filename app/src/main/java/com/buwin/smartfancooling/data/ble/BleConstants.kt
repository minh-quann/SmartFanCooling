package com.buwin.smartfancooling.data.ble

import java.util.UUID

/**
 * BLE Service and Characteristic UUIDs matching ESP32 firmware.
 */
object BleConstants {
    val SERVICE_UUID: UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
    val CHAR_FAN_SPEED_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
    val CHAR_FAN_STATE_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a9")
    val CHAR_LED_MODE_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26aa")
    val CHAR_LED_COLOR_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26ab")
    val CHAR_LED_BRIGHT_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26ac")
    val CHAR_RPM_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26ad")
    val CHAR_STATUS_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26ae")
    val CHAR_TEMP_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26af")
    val CHAR_WIFI_CONFIG_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26b0")

    val CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR: UUID =
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    const val DEVICE_NAME_PREFIX = "Llano Smart Fan"
}
