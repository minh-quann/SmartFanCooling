package com.buwin.smartfancooling.ui.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.buwin.smartfancooling.data.model.BleDeviceItem
import com.buwin.smartfancooling.data.model.ConnectionState
import com.buwin.smartfancooling.data.model.FanState
import com.buwin.smartfancooling.data.model.PcStats
import com.buwin.smartfancooling.data.model.RgbMode
import com.buwin.smartfancooling.data.model.RgbState
import com.buwin.smartfancooling.data.repository.SmartFanRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel managing the smart fan application state and actions.
 */
class SmartFanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SmartFanRepository(application.applicationContext)

    val fanState: StateFlow<FanState> = repository.fanState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FanState())

    val rgbState: StateFlow<RgbState> = repository.rgbState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RgbState())

    val pcStats: StateFlow<PcStats> = repository.pcStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PcStats())

    val connectionState: StateFlow<ConnectionState> = repository.connectionState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectionState())

    val discoveredBleDevices: StateFlow<List<BleDeviceItem>> = repository.discoveredBleDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isBleScanning: StateFlow<Boolean> = repository.isBleScanning
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onFanSpeedChange(percent: Int) {
        repository.setFanSpeed(percent)
    }

    fun onFanPowerToggle(isOn: Boolean) {
        repository.toggleFanPower(isOn)
    }

    fun onFanPresetSelect(percent: Int) {
        repository.setFanSpeed(percent)
    }

    fun onRgbModeSelect(mode: RgbMode) {
        repository.setRgbMode(mode)
    }

    fun onRgbColorChange(color: Color) {
        val r = (color.red * 255).toInt().coerceIn(0, 255)
        val g = (color.green * 255).toInt().coerceIn(0, 255)
        val b = (color.blue * 255).toInt().coerceIn(0, 255)
        repository.setRgbColor(r, g, b)
    }

    fun onRgbBrightnessChange(brightness: Int) {
        repository.setRgbBrightness(brightness)
    }

    fun onRgbPowerToggle(isOn: Boolean) {
        repository.toggleRgbPower(isOn)
    }

    fun startBleScan() {
        repository.startBleScan()
    }

    fun stopBleScan() {
        repository.stopBleScan()
    }

    fun connectBle(address: String) {
        repository.connectBle(address)
    }

    fun connectWifi(ip: String, port: Int = 81) {
        repository.connectWifi(ip, port)
    }

    fun disconnect() {
        repository.disconnect()
    }

    fun provisionWifi(ssid: String, pass: String) {
        repository.provisionWifi(ssid, pass)
    }

    fun toggleDemoSimulation() {
        repository.toggleDemoSimulation()
    }
}
