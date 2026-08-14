package com.buwin.smartfancooling.data.model

/**
 * Supported connectivity protocols.
 */
enum class ConnectionType {
    NONE,
    BLE,
    WIFI
}

/**
 * Status of the hardware connection.
 */
sealed interface ConnectionStatus {
    data object Disconnected : ConnectionStatus
    data object Scanning : ConnectionStatus
    data class Connecting(val target: String, val type: ConnectionType) : ConnectionStatus
    data class Connected(
        val type: ConnectionType,
        val targetName: String,
        val endpoint: String,
        val rssiOrPing: Int = 0
    ) : ConnectionStatus
    data class Error(val message: String) : ConnectionStatus
}

/**
 * Overall connection state of the application.
 */
data class ConnectionState(
    val status: ConnectionStatus = ConnectionStatus.Disconnected,
    val isBleAvailable: Boolean = true,
    val isWifiAvailable: Boolean = true,
    val staConnected: Boolean = false,
    val staSsid: String = "",
    val staIp: String = "",
    val apIp: String = "192.168.4.1"
) {
    val isConnected: Boolean
        get() = status is ConnectionStatus.Connected

    val activeType: ConnectionType
        get() = when (status) {
            is ConnectionStatus.Connected -> status.type
            is ConnectionStatus.Connecting -> status.type
            else -> ConnectionType.NONE
        }
}
