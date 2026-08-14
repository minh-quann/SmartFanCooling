package com.buwin.smartfancooling.data.wifi

import android.util.Log
import com.buwin.smartfancooling.data.model.ConnectionStatus
import com.buwin.smartfancooling.data.model.ConnectionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Manages WebSocket communication with the ESP32 server over Wi-Fi (AP or STA mode).
 */
class WebSocketManager {

    companion object {
        private const val TAG = "WebSocketManager"
        const val DEFAULT_AP_IP = "192.168.4.1"
        const val DEFAULT_PORT = 81
        const val DEFAULT_MDNS_HOST = "llanofan.local"
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    private val _connectionStatus =
        MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _rpmFlow = MutableSharedFlow<Int>(replay = 1)
    val rpmFlow: SharedFlow<Int> = _rpmFlow.asSharedFlow()

    private val _statusJsonFlow = MutableSharedFlow<JSONObject>(replay = 1)
    val statusJsonFlow: SharedFlow<JSONObject> = _statusJsonFlow.asSharedFlow()

    private val _wifiConfigResultFlow = MutableSharedFlow<Pair<Boolean, String>>(replay = 1)
    val wifiConfigResultFlow: SharedFlow<Pair<Boolean, String>> = _wifiConfigResultFlow.asSharedFlow()

    fun connect(ipOrHost: String = DEFAULT_AP_IP, port: Int = DEFAULT_PORT) {
        disconnect()

        val cleanHost = ipOrHost.trim()
        val url = if (cleanHost.startsWith("ws://") || cleanHost.startsWith("http://")) {
            cleanHost.replace("http://", "ws://")
        } else {
            "ws://$cleanHost:$port"
        }

        _connectionStatus.value =
            ConnectionStatus.Connecting(target = "ESP32 Wi-Fi ($cleanHost)", type = ConnectionType.WIFI)

        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.i(TAG, "WebSocket connected to $url")
                _connectionStatus.value = ConnectionStatus.Connected(
                    type = ConnectionType.WIFI,
                    targetName = "Smart Fan (Wi-Fi)",
                    endpoint = url
                )
                // Request Wi-Fi status on connect
                sendWifiStatusRequest()
            }

            override fun onMessage(ws: WebSocket, text: String) {
                handleMessage(text)
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                Log.i(TAG, "WebSocket closing: $code / $reason")
                _connectionStatus.value = ConnectionStatus.Disconnected
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.i(TAG, "WebSocket closed")
                _connectionStatus.value = ConnectionStatus.Disconnected
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket error: ${t.message}")
                _connectionStatus.value = ConnectionStatus.Error("Wi-Fi connection failed: ${t.localizedMessage ?: "Unknown error"}")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionStatus.value = ConnectionStatus.Disconnected
    }

    private fun handleMessage(text: String) {
        try {
            val json = JSONObject(text)

            // Direct RPM broadcast: {"rpm": 1850}
            if (json.has("rpm") && !json.has("fan_pct")) {
                val rpm = json.optInt("rpm", 0)
                scope.launch { _rpmFlow.emit(rpm) }
            }

            // Full status broadcast: {"fan_pct":75,"fan_on":true,"led_mode":2,"led_on":true,"rpm":1850,"cpu":65.5,"gpu":70.2}
            if (json.has("fan_pct") || json.has("led_mode")) {
                scope.launch { _statusJsonFlow.emit(json) }
            }

            // Wi-Fi config response: {"cmd":"wifi_config","status":"ok"|"fail","ip":"..."}
            if (json.optString("cmd") == "wifi_config") {
                val isSuccess = json.optString("status") == "ok"
                val ip = json.optString("ip", "")
                scope.launch { _wifiConfigResultFlow.emit(Pair(isSuccess, ip)) }
            }

            // Wi-Fi status response: {"cmd":"wifi_status","sta_connected":true,"sta_ip":"...","sta_ssid":"...","ap_ip":"..."}
            if (json.optString("cmd") == "wifi_status") {
                scope.launch { _statusJsonFlow.emit(json) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "JSON parse error: ${e.message}")
        }
    }

    // ---- Outgoing Commands ----

    private fun sendJson(json: JSONObject) {
        val payload = json.toString()
        webSocket?.send(payload)
    }

    fun sendFanSpeed(percent: Int) {
        sendJson(JSONObject().apply {
            put("cmd", "fan_speed")
            put("value", percent.coerceIn(0, 100))
        })
    }

    fun sendFanState(isOn: Boolean) {
        sendJson(JSONObject().apply {
            put("cmd", "fan_state")
            put("value", isOn)
        })
    }

    fun sendLedMode(modeCode: Int) {
        sendJson(JSONObject().apply {
            put("cmd", "led_mode")
            put("value", modeCode)
        })
    }

    fun sendLedColor(r: Int, g: Int, b: Int) {
        sendJson(JSONObject().apply {
            put("cmd", "led_color")
            put("r", r.coerceIn(0, 255))
            put("g", g.coerceIn(0, 255))
            put("b", b.coerceIn(0, 255))
        })
    }

    fun sendLedBrightness(brightness: Int) {
        sendJson(JSONObject().apply {
            put("cmd", "led_brightness")
            put("value", brightness.coerceIn(0, 255))
        })
    }

    fun sendPcTemps(cpu: Float, gpu: Float, board: Float = 0.0f) {
        sendJson(JSONObject().apply {
            put("cmd", "temp")
            put("cpu", cpu)
            put("gpu", gpu)
            put("board", board)
        })
    }

    fun sendWifiConfig(ssid: String, pass: String) {
        sendJson(JSONObject().apply {
            put("cmd", "wifi_config")
            put("ssid", ssid)
            put("pass", pass)
        })
    }

    fun sendWifiStatusRequest() {
        sendJson(JSONObject().apply {
            put("cmd", "wifi_status")
        })
    }

    fun sendWifiReset() {
        sendJson(JSONObject().apply {
            put("cmd", "wifi_reset")
        })
    }
}
