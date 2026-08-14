package com.buwin.smartfancooling.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buwin.smartfancooling.ui.components.FanControlSection
import com.buwin.smartfancooling.ui.components.FanHealthAndPidSection
import com.buwin.smartfancooling.ui.components.LiquidGlassCard
import com.buwin.smartfancooling.ui.components.PcTelemetrySection
import com.buwin.smartfancooling.ui.components.ProgressiveBlurHeader
import com.buwin.smartfancooling.ui.components.RgbControlSection
import com.buwin.smartfancooling.ui.components.RpmGauge
import com.buwin.smartfancooling.ui.viewmodel.SmartFanViewModel
import com.kyant.backdrop.catalog.BackdropDemoScaffold
import com.kyant.backdrop.catalog.components.LiquidBottomTab
import com.kyant.backdrop.catalog.components.LiquidBottomTabs

@Composable
fun MainDashboardScreen(
    viewModel: SmartFanViewModel,
    modifier: Modifier = Modifier
) {
    val fanState by viewModel.fanState.collectAsState()
    val pcStats by viewModel.pcStats.collectAsState()
    val rgbState by viewModel.rgbState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val isLightTheme = !isSystemInDarkTheme()
    val contentColor = if (isLightTheme) Color.Black else Color.White

    val dashboardIcon = rememberVectorPainter(Icons.Rounded.Dashboard)
    val lightingIcon = rememberVectorPainter(Icons.Rounded.Lightbulb)
    val deviceIcon = rememberVectorPainter(Icons.Rounded.Devices)
    val settingsIcon = rememberVectorPainter(Icons.Rounded.Settings)

    val iconColorFilter = ColorFilter.tint(contentColor)

    BackdropDemoScaffold { backdrop ->
        Box(modifier = modifier.fillMaxSize()) {
            // Main Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        // Dashboard Tab: Speedometer & PC Telemetry & Fan Control & Health
                        LiquidGlassCard(
                            backdrop = backdrop,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = 16.dp
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                RpmGauge(
                                    fanState = fanState,
                                    isDarkTheme = !isLightTheme
                                )
                            }
                        }

                        PcTelemetrySection(
                            pcStats = pcStats,
                            backdrop = backdrop,
                            isDarkTheme = !isLightTheme
                        )

                        FanControlSection(
                            fanState = fanState,
                            onSpeedChange = { viewModel.onFanSpeedChange(it) },
                            onPowerToggle = { viewModel.onFanPowerToggle(it) },
                            onPresetSelect = { viewModel.onFanPresetSelect(it) },
                            backdrop = backdrop,
                            isDarkTheme = !isLightTheme
                        )

                        FanHealthAndPidSection(
                            backdrop = backdrop,
                            isDarkTheme = !isLightTheme
                        )
                    }
                    1 -> {
                        // Lighting Tab
                        RgbControlSection(
                            rgbState = rgbState,
                            onModeSelect = { viewModel.onRgbModeSelect(it) },
                            onColorSelect = { viewModel.onRgbColorChange(it) },
                            onBrightnessChange = { viewModel.onRgbBrightnessChange(it) },
                            onPowerToggle = { viewModel.onRgbPowerToggle(it) },
                            backdrop = backdrop
                        )
                    }
                    2 -> {
                        // Device Tab: Connection details & ESP32 stats
                        LiquidGlassCard(
                            backdrop = backdrop,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = 20.dp
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                BasicText(
                                    "ESP32-S3 Hardware Controller",
                                    style = TextStyle(contentColor, 18.sp, fontWeight = FontWeight.Bold)
                                )
                                BasicText(
                                    "Status: ${if (connectionState.isConnected) "Connected via BLE / Serial" else "Disconnected"}",
                                    style = TextStyle(if (connectionState.isConnected) Color(0xFF34C759) else Color(0xFFFF3B30), 14.sp)
                                )
                                BasicText(
                                    "Protocol: ESP-NOW / BLE GATT v4.2\nTachometer: GPIO 18 (Closed-Loop)\nPWM Output: GPIO 19 (25kHz)",
                                    style = TextStyle(contentColor.copy(alpha = 0.7f), 13.sp)
                                )
                            }
                        }
                    }
                    else -> {
                        // Settings Tab
                        LiquidGlassCard(
                            backdrop = backdrop,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = 20.dp
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                BasicText(
                                    "System & Hardware Calibration",
                                    style = TextStyle(contentColor, 18.sp, fontWeight = FontWeight.Bold)
                                )
                                BasicText(
                                    "Liquid Glass Rendering Engine: Kyant0 Backdrop 2.0\nShapes Engine: Kyant0 Shapes 1.2.0\nTheme: Adaptive Dynamic System",
                                    style = TextStyle(contentColor.copy(alpha = 0.7f), 13.sp)
                                )
                            }
                        }
                    }
                }

                // Bottom spacing to ensure content scrolls completely clear of Bottom Navigation Bar
                Spacer(Modifier.height(110.dp))
            }

            // Top Header: Pure Alpha-Masked Progressive Blur Overlay from Kyant0's library
            ProgressiveBlurHeader(
                backdrop = backdrop,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Fixed Liquid Glass Bottom Navigation Bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            ) {
                LiquidBottomTabs(
                    selectedTabIndex = { selectedTabIndex },
                    onTabSelected = { selectedTabIndex = it },
                    backdrop = backdrop,
                    tabsCount = 4,
                    modifier = Modifier.padding(horizontal = 36.dp)
                ) {
                    repeat(4) { index ->
                        val (painter, label) = when (index) {
                            0 -> dashboardIcon to "Dashboard"
                            1 -> lightingIcon to "Lighting"
                            2 -> deviceIcon to "Device"
                            else -> settingsIcon to "Settings"
                        }

                        LiquidBottomTab(onClick = { selectedTabIndex = index }) {
                            Box(
                                Modifier
                                    .size(26.dp)
                                    .paint(painter, colorFilter = iconColorFilter)
                            )
                            BasicText(
                                label,
                                style = TextStyle(contentColor, 12.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}
