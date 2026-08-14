package com.buwin.smartfancooling.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import com.buwin.smartfancooling.data.model.BleDeviceItem
import com.buwin.smartfancooling.data.model.ConnectionStatus
import com.buwin.smartfancooling.data.model.ConnectionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

/**
 * Manages Bluetooth Low Energy (BLE) scanning, connection, and data exchange.
 */
class BleManager(private val context: Context) {

    companion object {
        private const val TAG = "BleManager"
        private const val SCAN_PERIOD_MS = 10000L
    }

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var bluetoothGatt: BluetoothGatt? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var scanJob: Job? = null

    private val _discoveredDevices = MutableStateFlow<List<BleDeviceItem>>(emptyList())
    val discoveredDevices: StateFlow<List<BleDeviceItem>> = _discoveredDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    // Real-time telemetry events from BLE notifications
    private val _rpmFlow = MutableSharedFlow<Int>(replay = 1)
    val rpmFlow: SharedFlow<Int> = _rpmFlow.asSharedFlow()

    private val _statusJsonFlow = MutableSharedFlow<JSONObject>(replay = 1)
    val statusJsonFlow: SharedFlow<JSONObject> = _statusJsonFlow.asSharedFlow()

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device ?: return
            val name = device.name ?: result.scanRecord?.deviceName ?: "Unknown BLE Device"
            val isSmartFan = name.contains("Llano", ignoreCase = true) ||
                    name.contains("Smart Fan", ignoreCase = true) ||
                    name.contains("Fan", ignoreCase = true)

            val item = BleDeviceItem(
                name = name,
                address = device.address,
                rssi = result.rssi,
                isSmartFan = isSmartFan
            )

            val current = _discoveredDevices.value.toMutableList()
            val existingIndex = current.indexOfFirst { it.address == item.address }
            if (existingIndex >= 0) {
                current[existingIndex] = item
            } else {
                current.add(item)
            }
            // Sort Smart Fan devices to the top
            _discoveredDevices.value = current.sortedWith(
                compareByDescending<BleDeviceItem> { it.isSmartFan }.thenByDescending { it.rssi }
            )
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed with error code: $errorCode")
            _isScanning.value = false
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceName = gatt.device.name ?: "Smart Fan"
            val address = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "GATT connected to $address, discovering services...")
                    _connectionStatus.value = ConnectionStatus.Connected(
                        type = ConnectionType.BLE,
                        targetName = deviceName,
                        endpoint = address
                    )
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "GATT disconnected from $address")
                    _connectionStatus.value = ConnectionStatus.Disconnected
                    cleanupGatt()
                }
            } else {
                Log.e(TAG, "GATT error: status=$status, newState=$newState")
                _connectionStatus.value = ConnectionStatus.Error("BLE connection failed (code: $status)")
                cleanupGatt()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Services discovered successfully")
                val service = gatt.getService(BleConstants.SERVICE_UUID)
                if (service != null) {
                    // Enable notifications on RPM and Status characteristics
                    enableNotifications(gatt, service.getCharacteristic(BleConstants.CHAR_RPM_UUID))
                    scope.launch {
                        delay(300)
                        enableNotifications(gatt, service.getCharacteristic(BleConstants.CHAR_STATUS_UUID))
                    }
                } else {
                    Log.w(TAG, "Smart Fan service not found on device")
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            handleCharacteristicUpdate(characteristic)
        }

        // Android 13+ callback
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handleCharacteristicUpdate(characteristic, value)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                handleCharacteristicUpdate(characteristic)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableNotifications(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic?
    ) {
        if (characteristic == null) return
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(BleConstants.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR)
        if (descriptor != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            } else {
                @Suppress("DEPRECATION")
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                @Suppress("DEPRECATION")
                gatt.writeDescriptor(descriptor)
            }
        }
    }

    private fun handleCharacteristicUpdate(
        characteristic: BluetoothGattCharacteristic,
        directBytes: ByteArray? = null
    ) {
        @Suppress("DEPRECATION")
        val data = directBytes ?: characteristic.value ?: return

        when (characteristic.uuid) {
            BleConstants.CHAR_RPM_UUID -> {
                if (data.size >= 2) {
                    val rpm = (data[0].toInt() and 0xFF) or ((data[1].toInt() and 0xFF) shl 8)
                    scope.launch { _rpmFlow.emit(rpm) }
                } else if (data.isNotEmpty()) {
                    val rpm = data[0].toInt() and 0xFF
                    scope.launch { _rpmFlow.emit(rpm) }
                }
            }
            BleConstants.CHAR_STATUS_UUID -> {
                try {
                    val jsonStr = String(data, Charsets.UTF_8)
                    val json = JSONObject(jsonStr)
                    scope.launch { _statusJsonFlow.emit(json) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing status JSON: ${e.message}")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionStatus.value = ConnectionStatus.Error("Bluetooth is disabled")
            return
        }

        val scanner = bluetoothAdapter.bluetoothLeScanner ?: return
        _discoveredDevices.value = emptyList()
        _isScanning.value = true

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            scanner.startScan(null, settings, scanCallback)
            scanJob?.cancel()
            scanJob = scope.launch {
                delay(SCAN_PERIOD_MS)
                stopScan()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting scan: ${e.message}")
            _isScanning.value = false
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (_isScanning.value) {
            try {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping scan: ${e.message}")
            }
            _isScanning.value = false
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(address: String) {
        stopScan()
        val device = bluetoothAdapter?.getRemoteDevice(address) ?: run {
            _connectionStatus.value = ConnectionStatus.Error("Device not found")
            return
        }

        cleanupGatt()
        _connectionStatus.value = ConnectionStatus.Connecting(device.name ?: address, ConnectionType.BLE)

        bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(context, false, gattCallback)
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
        cleanupGatt()
        _connectionStatus.value = ConnectionStatus.Disconnected
    }

    @SuppressLint("MissingPermission")
    private fun cleanupGatt() {
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    // ---- Write Commands ----

    @SuppressLint("MissingPermission")
    fun writeFanSpeed(speedPercent: Int) {
        val bytes = byteArrayOf(speedPercent.coerceIn(0, 100).toByte())
        writeCharacteristic(BleConstants.CHAR_FAN_SPEED_UUID, bytes)
    }

    @SuppressLint("MissingPermission")
    fun writeFanState(isOn: Boolean) {
        val bytes = byteArrayOf(if (isOn) 1.toByte() else 0.toByte())
        writeCharacteristic(BleConstants.CHAR_FAN_STATE_UUID, bytes)
    }

    @SuppressLint("MissingPermission")
    fun writeLedMode(modeCode: Int) {
        val bytes = byteArrayOf(modeCode.toByte())
        writeCharacteristic(BleConstants.CHAR_LED_MODE_UUID, bytes)
    }

    @SuppressLint("MissingPermission")
    fun writeLedColor(r: Int, g: Int, b: Int) {
        val bytes = byteArrayOf(r.toByte(), g.toByte(), b.toByte())
        writeCharacteristic(BleConstants.CHAR_LED_COLOR_UUID, bytes)
    }

    @SuppressLint("MissingPermission")
    fun writeLedBrightness(brightness: Int) {
        val bytes = byteArrayOf(brightness.coerceIn(0, 255).toByte())
        writeCharacteristic(BleConstants.CHAR_LED_BRIGHT_UUID, bytes)
    }

    @SuppressLint("MissingPermission")
    fun writeWifiConfig(ssid: String, pass: String) {
        val json = JSONObject().apply {
            put("ssid", ssid)
            put("pass", pass)
        }
        val bytes = json.toString().toByteArray(Charsets.UTF_8)
        writeCharacteristic(BleConstants.CHAR_WIFI_CONFIG_UUID, bytes)
    }

    @SuppressLint("MissingPermission")
    fun writePcTemps(cpu: Float, gpu: Float, board: Float = 0f) {
        val json = JSONObject().apply {
            put("cpu", cpu)
            put("gpu", gpu)
            put("board", board)
        }
        val bytes = json.toString().toByteArray(Charsets.UTF_8)
        writeCharacteristic(BleConstants.CHAR_TEMP_UUID, bytes)
    }

    @SuppressLint("MissingPermission")
    private fun writeCharacteristic(uuid: UUID, value: ByteArray) {
        val gatt = bluetoothGatt ?: return
        val service = gatt.getService(BleConstants.SERVICE_UUID) ?: return
        val characteristic = service.getCharacteristic(uuid) ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(
                characteristic,
                value,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
        } else {
            @Suppress("DEPRECATION")
            characteristic.value = value
            @Suppress("DEPRECATION")
            gatt.writeCharacteristic(characteristic)
        }
    }
}
