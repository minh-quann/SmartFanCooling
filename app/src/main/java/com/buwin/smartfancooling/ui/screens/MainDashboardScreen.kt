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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.buwin.smartfancooling.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothSearching
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
import com.buwin.smartfancooling.ui.viewmodel.SmartFanViewModel
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.catalog.FlightIcon
import com.kyant.backdrop.catalog.components.LiquidBottomTab
import com.kyant.backdrop.catalog.components.LiquidBottomTabs
import com.kyant.backdrop.catalog.components.LiquidButton
import com.kyant.backdrop.catalog.components.LiquidSlider
import com.kyant.backdrop.catalog.components.LiquidToggle

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

@Composable
fun MainDashboardScreen(
    viewModel: SmartFanViewModel
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()

    var themeMode by rememberSaveable { mutableStateOf(AppThemeMode.SYSTEM) }
    var autoPidControl by rememberSaveable { mutableStateOf(true) }
    var overheatAlarmTemp by rememberSaveable { mutableFloatStateOf(80f) }
    var refreshRateHz by rememberSaveable { mutableFloatStateOf(20f) }

    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val bgColor = if (isDark) Color(0xFF000000) else Color(0xFFEFF2F6)
    val textPrimaryColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val textSecondaryColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val backgroundBackdrop = rememberLayerBackdrop()

    val fanState by viewModel.fanState.collectAsState()
    val rgbState by viewModel.rgbState.collectAsState()
    val pcStats by viewModel.pcStats.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val discoveredDevices by viewModel.discoveredBleDevices.collectAsState()
    val isScanning by viewModel.isBleScanning.collectAsState()

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showConnectionModal by remember { mutableStateOf(false) }

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
        containerColor = bgColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Background Backdrop source layer (captures background for glass refraction)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor)
                    .layerBackdrop(backgroundBackdrop)
            )

            // Foreground Content lives as a sibling to the backdrop source
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(14.dp))

                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SMART FAN",
                                color = textPrimaryColor,
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
                            color = textSecondaryColor,
                            fontSize = 11.sp
                        )
                    }

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
                            .padding(horizontal = 10.dp, vertical = 6.dp),
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

                // Content View for 4 tabs
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
                                // 1. Dashboard Tab
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 96.dp)
                                ) {
                                    item {
                                        LiquidGlassCard(
                                            backdrop = backgroundBackdrop,
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = 20.dp,
                                            shape = RoundedCornerShape(28.dp),
                                            isDarkTheme = isDark
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                RpmGauge(
                                                    fanState = fanState,
                                                    size = 230.dp,
                                                    isDarkTheme = isDark
                                                )
                                            }
                                        }
                                    }

                                    item {
                                        PcTelemetrySection(
                                            pcStats = pcStats,
                                            backdrop = backgroundBackdrop,
                                            isDarkTheme = isDark
                                        )
                                    }

                                    item {
                                        FanControlSection(
                                            fanState = fanState,
                                            onSpeedChange = viewModel::onFanSpeedChange,
                                            onPowerToggle = viewModel::onFanPowerToggle,
                                            onPresetSelect = viewModel::onFanPresetSelect,
                                            backdrop = backgroundBackdrop,
                                            isDarkTheme = isDark
                                        )
                                    }

                                    item {
                                        LiquidGlassCard(
                                            backdrop = backgroundBackdrop,
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = 18.dp,
                                            shape = RoundedCornerShape(24.dp),
                                            isDarkTheme = isDark
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Tune,
                                                            contentDescription = "PID",
                                                            tint = NeonCyan,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(
                                                            text = "CLOSED-LOOP PID SYNC",
                                                            color = textPrimaryColor,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            letterSpacing = 0.8.sp
                                                        )
                                                    }
                                                    Spacer(Modifier.height(4.dp))
                                                    Text(
                                                        text = "Tachometer feedback dynamically stabilizes fan RPM according to CPU/GPU thermal demand.",
                                                        color = textSecondaryColor,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                                Spacer(Modifier.width(12.dp))
                                                LiquidToggle(
                                                    selected = { autoPidControl },
                                                    onSelect = { autoPidControl = it },
                                                    backdrop = backgroundBackdrop
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            1 -> {
                                // 2. Lighting Tab
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 96.dp)
                                ) {
                                    item {
                                        RgbControlSection(
                                            rgbState = rgbState,
                                            onModeSelect = viewModel::onRgbModeSelect,
                                            onColorSelect = viewModel::onRgbColorChange,
                                            onBrightnessChange = viewModel::onRgbBrightnessChange,
                                            onPowerToggle = viewModel::onRgbPowerToggle,
                                            backdrop = backgroundBackdrop
                                        )
                                    }
                                }
                            }

                            2 -> {
                                // 3. Device Tab
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 96.dp)
                                ) {
                                    item {
                                        LiquidGlassCard(
                                            backdrop = backgroundBackdrop,
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = 20.dp,
                                            shape = RoundedCornerShape(26.dp),
                                            isDarkTheme = isDark
                                        ) {
                                            Column {
                                                Text(
                                                    text = "DEVICE CONNECTION",
                                                    color = textPrimaryColor,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.8.sp
                                                )
                                                Spacer(Modifier.height(6.dp))
                                                Text(
                                                    text = "Connect directly to your ESP32-S3 Smart Fan Hub via Bluetooth Low Energy (BLE) or high-speed Wi-Fi WebSocket.",
                                                    color = textSecondaryColor,
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
                                                        backdrop = backgroundBackdrop,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.QrCodeScanner,
                                                            contentDescription = "Scan",
                                                            tint = textPrimaryColor,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(
                                                            text = if (isScanning) "Scanning..." else "Scan BLE",
                                                            color = textPrimaryColor,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }

                                                    LiquidButton(
                                                        onClick = { viewModel.toggleDemoSimulation() },
                                                        backdrop = backgroundBackdrop,
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

                            3 -> {
                                // 4. Settings Tab
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 96.dp)
                                ) {
                                    // 1. Theme Setting
                                    item {
                                        LiquidGlassCard(
                                            backdrop = backgroundBackdrop,
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = 20.dp,
                                            shape = RoundedCornerShape(26.dp),
                                            isDarkTheme = isDark
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.DarkMode,
                                                        contentDescription = "Theme",
                                                        tint = NeonCyan,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(Modifier.width(10.dp))
                                                    Text(
                                                        text = "GIAO DIỆN & MÀU NỀN",
                                                        color = textPrimaryColor,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 0.8.sp
                                                    )
                                                }

                                                Spacer(Modifier.height(14.dp))

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = "Theo hệ thống (System Default)",
                                                            color = textPrimaryColor,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        Text(
                                                            text = if (themeMode == AppThemeMode.SYSTEM) "Đang bật: tự đổi theo cài đặt Android" else "Đang tắt",
                                                            color = textSecondaryColor,
                                                            fontSize = 11.sp
                                                        )
                                                    }

                                                    LiquidToggle(
                                                        selected = { themeMode == AppThemeMode.SYSTEM },
                                                        onSelect = { isSystem ->
                                                            themeMode = if (isSystem) AppThemeMode.SYSTEM else if (isDark) AppThemeMode.DARK else AppThemeMode.LIGHT
                                                        },
                                                        backdrop = backgroundBackdrop
                                                    )
                                                }

                                                Spacer(Modifier.height(14.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(38.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (themeMode == AppThemeMode.SYSTEM) NeonCyan.copy(alpha = 0.25f)
                                                                else if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                                                            )
                                                            .border(
                                                                width = 1.dp,
                                                                color = if (themeMode == AppThemeMode.SYSTEM) NeonCyan else Color.Transparent,
                                                                shape = CircleShape
                                                            )
                                                            .clickable { themeMode = AppThemeMode.SYSTEM },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                imageVector = Icons.Rounded.PhoneAndroid,
                                                                contentDescription = "System",
                                                                tint = if (themeMode == AppThemeMode.SYSTEM) NeonCyan else textSecondaryColor,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Spacer(Modifier.width(6.dp))
                                                            Text(
                                                                text = "Hệ thống",
                                                                color = if (themeMode == AppThemeMode.SYSTEM) NeonCyan else textSecondaryColor,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(38.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (themeMode == AppThemeMode.LIGHT) NeonCyan.copy(alpha = 0.25f)
                                                                else if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                                                            )
                                                            .border(
                                                                width = 1.dp,
                                                                color = if (themeMode == AppThemeMode.LIGHT) NeonCyan else Color.Transparent,
                                                                shape = CircleShape
                                                            )
                                                            .clickable { themeMode = AppThemeMode.LIGHT },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                imageVector = Icons.Rounded.LightMode,
                                                                contentDescription = "Light",
                                                                tint = if (themeMode == AppThemeMode.LIGHT) NeonCyan else textSecondaryColor,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Spacer(Modifier.width(6.dp))
                                                            Text(
                                                                text = "Sáng",
                                                                color = if (themeMode == AppThemeMode.LIGHT) NeonCyan else textSecondaryColor,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(38.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (themeMode == AppThemeMode.DARK) NeonCyan.copy(alpha = 0.25f)
                                                                else if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                                                            )
                                                            .border(
                                                                width = 1.dp,
                                                                color = if (themeMode == AppThemeMode.DARK) NeonCyan else Color.Transparent,
                                                                shape = CircleShape
                                                            )
                                                            .clickable { themeMode = AppThemeMode.DARK },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                imageVector = Icons.Rounded.DarkMode,
                                                                contentDescription = "Dark",
                                                                tint = if (themeMode == AppThemeMode.DARK) NeonCyan else textSecondaryColor,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Spacer(Modifier.width(6.dp))
                                                            Text(
                                                                text = "Tối",
                                                                color = if (themeMode == AppThemeMode.DARK) NeonCyan else textSecondaryColor,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 2. Hardware Alerts Setting
                                    item {
                                        LiquidGlassCard(
                                            backdrop = backgroundBackdrop,
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = 20.dp,
                                            shape = RoundedCornerShape(26.dp),
                                            isDarkTheme = isDark
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.NotificationsActive,
                                                        contentDescription = "Alerts",
                                                        tint = CrimsonAlert,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(Modifier.width(10.dp))
                                                    Text(
                                                        text = "CẢNH BÁO NHIỆT ĐỘ PC",
                                                        color = textPrimaryColor,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 0.8.sp
                                                    )
                                                }

                                                Spacer(Modifier.height(14.dp))

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "Ngưỡng báo động:",
                                                        color = textSecondaryColor,
                                                        fontSize = 12.sp
                                                    )
                                                    Text(
                                                        text = "${overheatAlarmTemp.toInt()}°C",
                                                        color = CrimsonAlert,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Spacer(Modifier.height(8.dp))

                                                LiquidSlider(
                                                    value = { overheatAlarmTemp },
                                                    onValueChange = { overheatAlarmTemp = it },
                                                    valueRange = 60f..100f,
                                                    visibilityThreshold = 1f,
                                                    backdrop = backgroundBackdrop
                                                )

                                                Spacer(Modifier.height(18.dp))

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Speed,
                                                        contentDescription = "Telemetry Rate",
                                                        tint = ElectricBlue,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(
                                                        text = "Tần số cập nhật Telemetry:",
                                                        color = textSecondaryColor,
                                                        fontSize = 12.sp
                                                    )
                                                    Spacer(Modifier.weight(1f))
                                                    Text(
                                                        text = "${refreshRateHz.toInt()} Hz",
                                                        color = ElectricBlue,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Spacer(Modifier.height(8.dp))

                                                LiquidSlider(
                                                    value = { refreshRateHz },
                                                    onValueChange = { refreshRateHz = it },
                                                    valueRange = 5f..60f,
                                                    visibilityThreshold = 1f,
                                                    backdrop = backgroundBackdrop
                                                )
                                            }
                                        }
                                    }

                                    // 3. Info Card
                                    item {
                                        LiquidGlassCard(
                                            backdrop = backgroundBackdrop,
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = 18.dp,
                                            shape = RoundedCornerShape(22.dp),
                                            isDarkTheme = isDark
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Info,
                                                        contentDescription = "Info",
                                                        tint = textSecondaryColor,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = "Smart Fan Cooling Studio",
                                                            color = textPrimaryColor,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        Text(
                                                            text = "v2.0.0 • Liquid Glass AGSL Engine",
                                                            color = textSecondaryColor,
                                                            fontSize = 10.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Exact 4-Tab LiquidBottomTabs with Liquid Glass Material
            val isLightTheme = !isDark
            val contentColor = if (isLightTheme) Color.Black else Color.White
            val airplaneModeIcon = rememberVectorPainter(FlightIcon)
            val iconColorFilter = ColorFilter.tint(contentColor)

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 18.dp)
            ) {
                LiquidBottomTabs(
                    selectedTabIndex = { selectedTabIndex },
                    onTabSelected = { selectedTabIndex = it },
                    backdrop = backgroundBackdrop,
                    tabsCount = 4,
                    modifier = Modifier.padding(horizontal = 36.dp)
                ) {
                    repeat(4) { index ->
                        LiquidBottomTab(onClick = { selectedTabIndex = index }) {
                            Box(
                                Modifier
                                    .size(28.dp)
                                    .paint(airplaneModeIcon, colorFilter = iconColorFilter)
                            )
                            BasicText(
                                when (index) {
                                    0 -> "Dashboard"
                                    1 -> "Lighting"
                                    2 -> "Device"
                                    else -> "Settings"
                                },
                                style = TextStyle(contentColor, 12.sp)
                            )
                        }
                    }
                }
            }
        }

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
