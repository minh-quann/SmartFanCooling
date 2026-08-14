package com.buwin.smartfancooling.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothSearching
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Toys
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.buwin.smartfancooling.ui.components.ConnectionModal
import com.buwin.smartfancooling.ui.components.FanControlSection
import com.buwin.smartfancooling.ui.components.LiquidGlassCard
import com.buwin.smartfancooling.ui.components.PcTelemetrySection
import com.buwin.smartfancooling.ui.components.RgbControlSection
import com.buwin.smartfancooling.ui.components.RpmGauge
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
import com.kyant.backdrop.catalog.MainContent
import com.kyant.backdrop.catalog.components.LiquidBottomTab
import com.kyant.backdrop.catalog.components.LiquidBottomTabs
import com.kyant.backdrop.catalog.components.LiquidButton

/**
 * Main dashboard screen with authentic Kyant0 AndroidLiquidGlass components on pure solid black.
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

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showConnectionModal by remember { mutableStateOf(false) }

    // BLE Permission Launcher
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
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Pure Pitch Black Background layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
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
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(NeonCyan.copy(alpha = 0.35f), NeonPurple.copy(alpha = 0.35f))
                                        )
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
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
                            text = "Liquid Glass Cooling Studio",
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
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.18f))
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

                // ---- Main Switchable Tabs Content ----
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = selectedTabIndex,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tab_content_transition"
                    ) { tabIndex ->
                        when (tabIndex) {
                            0 -> {
                                // Status / Dashboard Tab
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 90.dp)
                                ) {
                                    // 1. Tachometer RPM Hero Glass Card
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

                                    // 3. Quick Fan Switch
                                    item {
                                        FanControlSection(
                                            fanState = fanState,
                                            onSpeedChange = viewModel::onFanSpeedChange,
                                            onPowerToggle = viewModel::onFanPowerToggle,
                                            onPresetSelect = viewModel::onFanPresetSelect,
                                            backdrop = backdrop
                                        )
                                    }
                                }
                            }

                            1 -> {
                                // Cooling / Fan Control Tab
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 90.dp)
                                ) {
                                    item {
                                        FanControlSection(
                                            fanState = fanState,
                                            onSpeedChange = viewModel::onFanSpeedChange,
                                            onPowerToggle = viewModel::onFanPowerToggle,
                                            onPresetSelect = viewModel::onFanPresetSelect,
                                            backdrop = backdrop
                                        )
                                    }

                                    item {
                                        LiquidGlassCard(
                                            backdrop = backdrop,
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = 18.dp,
                                            shape = RoundedCornerShape(24.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    text = "PWM CLOSED-LOOP CONTROL",
                                                    color = TextPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.8.sp
                                                )
                                                Spacer(Modifier.height(8.dp))
                                                Text(
                                                    text = "Tachometer feedback is synchronized with the ESP32 closed-loop PID controller for maximum cooling efficiency.",
                                                    color = TextSecondary,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            2 -> {
                                // Lighting / RGB Studio Tab
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 90.dp)
                                ) {
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

                            3 -> {
                                // Device / Connection Tab
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 90.dp)
                                ) {
                                    item {
                                        LiquidGlassCard(
                                            backdrop = backdrop,
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = 20.dp,
                                            shape = RoundedCornerShape(26.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    text = "DEVICE CONNECTION",
                                                    color = TextPrimary,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.8.sp
                                                )
                                                Spacer(Modifier.height(6.dp))
                                                Text(
                                                    text = "Connect directly to your ESP32-S3 Smart Fan Hub via Bluetooth Low Energy (BLE) or high-speed Wi-Fi WebSocket.",
                                                    color = TextSecondary,
                                                    fontSize = 12.sp
                                                )
                                                Spacer(Modifier.height(16.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    LiquidButton(
                                                        onClick = {
                                                            showConnectionModal = true
                                                            requestBleAndScan()
                                                        },
                                                        backdrop = backdrop,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.QrCodeScanner,
                                                            contentDescription = "Scan",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(
                                                            text = if (isScanning) "Scanning..." else "Scan BLE",
                                                            color = Color.White,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }

                                                    LiquidButton(
                                                        onClick = { viewModel.toggleDemoSimulation() },
                                                        backdrop = backdrop,
                                                        tint = NeonPurple,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Sensors,
                                                            contentDescription = "Demo",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(
                                                            text = "Demo Mode",
                                                            color = Color.White,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            4 -> {
                                // Full Kyant0 Backdrop Catalog Showcase!
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 80.dp)
                                ) {
                                    MainContent()
                                }
                            }
                        }
                    }
                }
            }

            // 3. Refined Kyant0 LiquidBottomTabs (5 Tabs)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                LiquidBottomTabs(
                    selectedTabIndex = { selectedTabIndex },
                    onTabSelected = { selectedTabIndex = it },
                    backdrop = backdrop,
                    tabsCount = 5
                ) {
                    LiquidBottomTab(onClick = { selectedTabIndex = 0 }) {
                        Icon(
                            imageVector = Icons.Rounded.Dashboard,
                            contentDescription = "Status",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Status",
                            fontSize = 9.sp,
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    LiquidBottomTab(onClick = { selectedTabIndex = 1 }) {
                        Icon(
                            imageVector = Icons.Rounded.Toys,
                            contentDescription = "Cooling",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Cooling",
                            fontSize = 9.sp,
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    LiquidBottomTab(onClick = { selectedTabIndex = 2 }) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = "Lighting",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Lighting",
                            fontSize = 9.sp,
                            fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    LiquidBottomTab(onClick = { selectedTabIndex = 3 }) {
                        Icon(
                            imageVector = Icons.Rounded.Bluetooth,
                            contentDescription = "Device",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Device",
                            fontSize = 9.sp,
                            fontWeight = if (selectedTabIndex == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    LiquidBottomTab(onClick = { selectedTabIndex = 4 }) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "Catalog",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Catalog",
                            fontSize = 9.sp,
                            fontWeight = if (selectedTabIndex == 4) FontWeight.Bold else FontWeight.Normal
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
