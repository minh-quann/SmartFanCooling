package com.buwin.smartfancooling.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothSearching
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.buwin.smartfancooling.data.model.ConnectionStatus
import com.buwin.smartfancooling.data.model.ConnectionType
import com.buwin.smartfancooling.ui.components.AnimatedMeshBackground
import com.buwin.smartfancooling.ui.components.ConnectionModal
import com.buwin.smartfancooling.ui.components.FanControlSection
import com.buwin.smartfancooling.ui.components.LiquidGlassCard
import com.buwin.smartfancooling.ui.components.PcTelemetrySection
import com.buwin.smartfancooling.ui.components.RgbControlSection
import com.buwin.smartfancooling.ui.components.RpmGauge
import com.buwin.smartfancooling.ui.theme.BackgroundDark
import com.buwin.smartfancooling.ui.theme.CrimsonAlert
import com.buwin.smartfancooling.ui.theme.ElectricBlue
import com.buwin.smartfancooling.ui.theme.EmeraldGreen
import com.buwin.smartfancooling.ui.theme.NeonCyan
import com.buwin.smartfancooling.ui.theme.NeonPurple
import com.buwin.smartfancooling.ui.theme.TextPrimary
import com.buwin.smartfancooling.ui.theme.TextSecondary
import com.buwin.smartfancooling.ui.viewmodel.SmartFanViewModel
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/**
 * Main dashboard screen displaying hardware telemetry, fan speed gauge, and controls.
 */
@Composable
fun MainDashboardScreen(
    viewModel: SmartFanViewModel
) {
    val context = LocalContext.current
    val backdrop = rememberLayerBackdrop()

    val fanState by viewModel.fanState.collectAsState()
    val rgbState by viewModel.rgbState.collectAsState()
    val pcStats by viewModel.pcStats.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val discoveredDevices by viewModel.discoveredBleDevices.collectAsState()
    val isScanning by viewModel.isBleScanning.collectAsState()

    var showConnectionModal by remember { mutableStateOf(false) }

    // BLE Permission Launcher for Android 12+ (API 31+) and below
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModel.startBleScan()
        }
    }

    fun requestBleAndScan() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isEmpty()) {
            viewModel.startBleScan()
        } else {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    Scaffold(
        containerColor = BackgroundDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Dedicated background layer captured by backdrop
            AnimatedMeshBackground(
                accentColor = if (rgbState.isPoweredOn) rgbState.composeColor else NeonCyan,
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(backdrop)
            )

            // 2. Foreground Glass UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // ---- Top Header Bar ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SMART FAN",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(NeonCyan.copy(alpha = 0.25f), NeonPurple.copy(alpha = 0.25f))
                                        )
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ESP32-S3",
                                    color = NeonCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        Text(
                            text = "Next-Gen Cooling Ecosystem",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    // Connection Badge Pill
                    val statusText: String
                    val statusColor: Color
                    val statusIcon: androidx.compose.ui.graphics.vector.ImageVector

                    when (val status = connectionState.status) {
                        is ConnectionStatus.Connected -> {
                            statusText = when (status.type) {
                                ConnectionType.BLE -> "BLE"
                                ConnectionType.WIFI -> "Wi-Fi"
                                else -> "Online"
                            }
                            statusColor = EmeraldGreen
                            statusIcon = if (status.type == ConnectionType.BLE) Icons.Rounded.Bluetooth else Icons.Rounded.Wifi
                        }
                        is ConnectionStatus.Connecting -> {
                            statusText = "Syncing..."
                            statusColor = ElectricBlue
                            statusIcon = Icons.Rounded.BluetoothSearching
                        }
                        else -> {
                            statusText = "Disconnected"
                            statusColor = CrimsonAlert
                            statusIcon = Icons.Rounded.WifiOff
                        }
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .clickable { showConnectionModal = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = "Status",
                            tint = statusColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ---- Main Scrollable Content ----
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // 1. Tachometer RPM Hero Card
                    item {
                        LiquidGlassCard(
                            backdrop = backdrop,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = 20.dp,
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                RpmGauge(
                                    fanState = fanState,
                                    size = 230.dp
                                )
                            }
                        }
                    }

                    // 2. PC Telemetry Section
                    item {
                        PcTelemetrySection(
                            pcStats = pcStats,
                            backdrop = backdrop
                        )
                    }

                    // 3. Fan Speed Controller Section
                    item {
                        FanControlSection(
                            fanState = fanState,
                            onSpeedChange = viewModel::onFanSpeedChange,
                            onPowerToggle = viewModel::onFanPowerToggle,
                            onPresetSelect = viewModel::onFanPresetSelect,
                            backdrop = backdrop
                        )
                    }

                    // 4. RGB Lighting Studio Section
                    item {
                        RgbControlSection(
                            rgbState = rgbState,
                            onModeSelect = viewModel::onRgbModeSelect,
                            onColorSelect = viewModel::onRgbColorChange,
                            onBrightnessChange = viewModel::onRgbBrightnessChange,
                            onPowerToggle = viewModel::onRgbPowerToggle,
                            backdrop = backdrop
                        )
                    }
                }
            }
        }

        // Connection Bottom Sheet Modal
        ConnectionModal(
            isOpen = showConnectionModal,
            onDismiss = { showConnectionModal = false },
            connectionState = connectionState,
            discoveredDevices = discoveredDevices,
            isScanning = isScanning,
            onStartScan = { requestBleAndScan() },
            onStopScan = viewModel::stopBleScan,
            onConnectBle = { addr ->
                viewModel.connectBle(addr)
                showConnectionModal = false
            },
            onConnectWifi = { ip, port ->
                viewModel.connectWifi(ip, port)
                showConnectionModal = false
            },
            onDisconnect = {
                viewModel.disconnect()
            },
            onProvisionWifi = { ssid, pass ->
                viewModel.provisionWifi(ssid, pass)
            },
            onToggleDemo = {
                viewModel.toggleDemoSimulation()
                showConnectionModal = false
            }
        )
    }
}
