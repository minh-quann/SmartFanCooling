package com.buwin.smartfancooling.data.repository

import android.content.Context
import com.buwin.smartfancooling.data.ble.BleManager
import com.buwin.smartfancooling.data.model.BleDeviceItem
import com.buwin.smartfancooling.data.model.ConnectionState
import com.buwin.smartfancooling.data.model.ConnectionStatus
import com.buwin.smartfancooling.data.model.ConnectionType
import com.buwin.smartfancooling.data.model.FanState
import com.buwin.smartfancooling.data.model.PcStats
import com.buwin.smartfancooling.data.model.RgbMode
import com.buwin.smartfancooling.data.model.RgbState
import com.buwin.smartfancooling.data.wifi.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.random.Random

/**
 * Unified repository managing both BLE and Wi-Fi communication and state streams.
 */
class SmartFanRepository(context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val bleManager = BleManager(context)
    private val webSocketManager = WebSocketManager()

    private val _fanState = MutableStateFlow(FanState())
    val fanState: StateFlow<FanState> = _fanState.asStateFlow()

    private val _rgbState = MutableStateFlow(RgbState())
    val rgbState: StateFlow<RgbState> = _rgbState.asStateFlow()

    private val _pcStats = MutableStateFlow(
        PcStats(cpuTemp = 48.5f, gpuTemp = 52.0f, boardTemp = 36.0f, cpuUsage = 24.0f, gpuUsage = 18.0f, ramUsage = 42.0f)
    )
    val pcStats: StateFlow<PcStats> = _pcStats.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState())
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    val discoveredBleDevices: StateFlow<List<BleDeviceItem>> = bleManager.discoveredDevices
    val isBleScanning: StateFlow<Boolean> = bleManager.isScanning

    private var simulationJob: Job? = null
    private var isSimulating = false

    init {
        // Combine connection states
        scope.launch {
            combine(bleManager.connectionStatus, webSocketManager.connectionStatus) { bleStatus, wsStatus ->
                when {
                    wsStatus is ConnectionStatus.Connected -> wsStatus
                    bleStatus is ConnectionStatus.Connected -> bleStatus
                    wsStatus is ConnectionStatus.Connecting -> wsStatus
                    bleStatus is ConnectionStatus.Connecting -> bleStatus
                    wsStatus is ConnectionStatus.Error -> wsStatus
                    bleStatus is ConnectionStatus.Error -> bleStatus
                    else -> ConnectionStatus.Disconnected
                }
            }.collect { mergedStatus ->
                _connectionState.value = _connectionState.value.copy(status = mergedStatus)
            }
        }

        // Collect BLE RPM notifications
        scope.launch {
            bleManager.rpmFlow.collect { rpm ->
                _fanState.value = _fanState.value.copy(currentRpm = rpm)
            }
        }

        // Collect BLE Status updates
        scope.launch {
            bleManager.statusJsonFlow.collect { json ->
                parseStatusJson(json)
            }
        }

        // Collect WebSocket RPM notifications
        scope.launch {
            webSocketManager.rpmFlow.collect { rpm ->
                _fanState.value = _fanState.value.copy(currentRpm = rpm)
            }
        }

        // Collect WebSocket Status updates
        scope.launch {
            webSocketManager.statusJsonFlow.collect { json ->
                parseStatusJson(json)
            }
        }
    }

    private fun parseStatusJson(json: JSONObject) {
        if (json.has("fan_pct")) {
            val fanPct = json.optInt("fan_pct", _fanState.value.speedPercent)
            val fanOn = json.optBoolean("fan_on", _fanState.value.isPoweredOn)
            val rpm = json.optInt("rpm", _fanState.value.currentRpm)
            _fanState.value = _fanState.value.copy(
                speedPercent = fanPct,
                isPoweredOn = fanOn,
                currentRpm = rpm
            )
        }

        if (json.has("led_mode")) {
            val modeCode = json.optInt("led_mode", _rgbState.value.mode.code)
            val ledOn = json.optBoolean("led_on", _rgbState.value.isPoweredOn)
            _rgbState.value = _rgbState.value.copy(
                mode = RgbMode.fromCode(modeCode),
                isPoweredOn = ledOn
            )
        }

        if (json.has("cpu") || json.has("gpu")) {
            val cpu = json.optDouble("cpu", _pcStats.value.cpuTemp.toDouble()).toFloat()
            val gpu = json.optDouble("gpu", _pcStats.value.gpuTemp.toDouble()).toFloat()
            _pcStats.value = _pcStats.value.copy(
                cpuTemp = cpu,
                gpuTemp = gpu,
                lastUpdated = System.currentTimeMillis()
            )
        }

        if (json.has("sta_connected")) {
            val staConnected = json.optBoolean("sta_connected", false)
            val staIp = json.optString("sta_ip", "")
            val staSsid = json.optString("sta_ssid", "")
            val apIp = json.optString("ap_ip", "192.168.4.1")
            _connectionState.value = _connectionState.value.copy(
                staConnected = staConnected,
                staIp = staIp,
                staSsid = staSsid,
                apIp = apIp
            )
        }
    }

    // ---- Connection Controls ----

    fun startBleScan() {
        bleManager.startScan()
    }

    fun stopBleScan() {
        bleManager.stopScan()
    }

    fun connectBle(address: String) {
        stopSimulation()
        webSocketManager.disconnect()
        bleManager.connect(address)
    }

    fun connectWifi(ip: String = WebSocketManager.DEFAULT_AP_IP, port: Int = WebSocketManager.DEFAULT_PORT) {
        stopSimulation()
        bleManager.disconnect()
        webSocketManager.connect(ip, port)
    }

    fun disconnect() {
        bleManager.disconnect()
        webSocketManager.disconnect()
        stopSimulation()
    }

    // ---- Hardware Commands ----

    fun setFanSpeed(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        _fanState.value = _fanState.value.copy(speedPercent = clamped)

        when (_connectionState.value.activeType) {
            ConnectionType.BLE -> bleManager.writeFanSpeed(clamped)
            ConnectionType.WIFI -> webSocketManager.sendFanSpeed(clamped)
            ConnectionType.NONE -> {
                if (isSimulating) {
                    val approxRpm = (clamped * 2800) / 100
                    _fanState.value = _fanState.value.copy(currentRpm = approxRpm)
                }
            }
        }
    }

    fun toggleFanPower(isOn: Boolean) {
        _fanState.value = _fanState.value.copy(isPoweredOn = isOn)

        when (_connectionState.value.activeType) {
            ConnectionType.BLE -> bleManager.writeFanState(isOn)
            ConnectionType.WIFI -> webSocketManager.sendFanState(isOn)
            ConnectionType.NONE -> {}
        }
    }

    fun setRgbMode(mode: RgbMode) {
        _rgbState.value = _rgbState.value.copy(mode = mode)

        when (_connectionState.value.activeType) {
            ConnectionType.BLE -> bleManager.writeLedMode(mode.code)
            ConnectionType.WIFI -> webSocketManager.sendLedMode(mode.code)
            ConnectionType.NONE -> {}
        }
    }

    fun setRgbColor(r: Int, g: Int, b: Int) {
        _rgbState.value = _rgbState.value.copy(red = r, green = g, blue = b)

        when (_connectionState.value.activeType) {
            ConnectionType.BLE -> bleManager.writeLedColor(r, g, b)
            ConnectionType.WIFI -> webSocketManager.sendLedColor(r, g, b)
            ConnectionType.NONE -> {}
        }
    }

    fun setRgbBrightness(brightness: Int) {
        val clamped = brightness.coerceIn(0, 255)
        _rgbState.value = _rgbState.value.copy(brightness = clamped)

        when (_connectionState.value.activeType) {
            ConnectionType.BLE -> bleManager.writeLedBrightness(clamped)
            ConnectionType.WIFI -> webSocketManager.sendLedBrightness(clamped)
            ConnectionType.NONE -> {}
        }
    }

    fun toggleRgbPower(isOn: Boolean) {
        _rgbState.value = _rgbState.value.copy(isPoweredOn = isOn)
        if (!isOn) {
            setRgbMode(RgbMode.OFF)
        } else {
            setRgbMode(RgbMode.RAINBOW)
        }
    }

    fun provisionWifi(ssid: String, pass: String) {
        when (_connectionState.value.activeType) {
            ConnectionType.BLE -> bleManager.writeWifiConfig(ssid, pass)
            ConnectionType.WIFI -> webSocketManager.sendWifiConfig(ssid, pass)
            ConnectionType.NONE -> {}
        }
    }

    fun sendPcTelemetry(cpu: Float, gpu: Float, board: Float = 0f) {
        _pcStats.value = _pcStats.value.copy(
            cpuTemp = cpu,
            gpuTemp = gpu,
            boardTemp = board,
            lastUpdated = System.currentTimeMillis()
        )

        when (_connectionState.value.activeType) {
            ConnectionType.BLE -> bleManager.writePcTemps(cpu, gpu, board)
            ConnectionType.WIFI -> webSocketManager.sendPcTemps(cpu, gpu, board)
            ConnectionType.NONE -> {}
        }
    }

    // ---- Demo / Simulation Mode ----

    fun toggleDemoSimulation() {
        if (isSimulating) {
            stopSimulation()
        } else {
            startSimulation()
        }
    }

    private fun startSimulation() {
        disconnect()
        isSimulating = true
        _connectionState.value = _connectionState.value.copy(
            status = ConnectionStatus.Connected(
                type = ConnectionType.BLE,
                targetName = "Virtual Llano S3 (Demo)",
                endpoint = "AA:BB:CC:DD:EE:FF",
                rssiOrPing = -58
            )
        )

        simulationJob?.cancel()
        simulationJob = scope.launch {
            var simSpeed = _fanState.value.speedPercent
            while (isActive && isSimulating) {
                // Fluctuate RPM slightly around the target speed percentage
                val targetRpm = (simSpeed * 2800) / 100
                val jitter = Random.nextInt(-35, 40)
                val currentRpm = if (_fanState.value.isPoweredOn) (targetRpm + jitter).coerceAtLeast(0) else 0

                // Fluctuate temperatures slightly
                val currentCpu = (_pcStats.value.cpuTemp + Random.nextDouble(-0.8, 0.9).toFloat()).coerceIn(40f, 92f)
                val currentGpu = (_pcStats.value.gpuTemp + Random.nextDouble(-0.7, 0.8).toFloat()).coerceIn(42f, 88f)
                val roundedCpu = (kotlin.math.round(currentCpu * 10f) / 10f).toFloat()
                val roundedGpu = (kotlin.math.round(currentGpu * 10f) / 10f).toFloat()

                _fanState.value = _fanState.value.copy(currentRpm = currentRpm)
                _pcStats.value = _pcStats.value.copy(
                    cpuTemp = roundedCpu,
                    gpuTemp = roundedGpu,
                    cpuUsage = (currentCpu * 0.9f).coerceIn(10f, 98f),
                    gpuUsage = (currentGpu * 0.85f).coerceIn(5f, 95f),
                    lastUpdated = System.currentTimeMillis()
                )

                delay(500)
            }
        }
    }

    private fun stopSimulation() {
        isSimulating = false
        simulationJob?.cancel()
        simulationJob = null
        if (_connectionState.value.status is ConnectionStatus.Connected &&
            (_connectionState.value.status as ConnectionStatus.Connected).targetName.contains("Demo")
        ) {
            _connectionState.value = _connectionState.value.copy(status = ConnectionStatus.Disconnected)
        }
    }
}
