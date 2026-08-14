package com.buwin.smartfancooling.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.VideogameAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buwin.smartfancooling.data.model.PcStats
import com.buwin.smartfancooling.ui.theme.AmberWarning
import com.buwin.smartfancooling.ui.theme.CrimsonAlert
import com.buwin.smartfancooling.ui.theme.ElectricBlue
import com.buwin.smartfancooling.ui.theme.EmeraldGreen
import com.buwin.smartfancooling.ui.theme.NeonCyan
import com.kyant.backdrop.Backdrop

/**
 * Telemetry section showing CPU, GPU, RAM, and Board/Power in liquid glass cards.
 */
@Composable
fun PcTelemetrySection(
    pcStats: PcStats,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: CPU Package & GPU Core
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HardwareStatCard(
                title = "CPU PACKAGE",
                primaryValue = if (pcStats.cpuTemp > 0f) String.format("%.1f", pcStats.cpuTemp) else "--",
                unit = "°C",
                subLabel = "Load",
                subValue = if (pcStats.cpuUsage > 0f) "${pcStats.cpuUsage.toInt()}%" else "--",
                progress = (pcStats.cpuUsage / 100f).coerceIn(0f, 1f),
                icon = Icons.Rounded.Memory,
                statusColor = when {
                    pcStats.cpuTemp <= 0f -> Color(0xFF64748B)
                    pcStats.cpuTemp < 55f -> EmeraldGreen
                    pcStats.cpuTemp < 72f -> AmberWarning
                    else -> CrimsonAlert
                },
                backdrop = backdrop,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )

            HardwareStatCard(
                title = "GPU CORE",
                primaryValue = if (pcStats.gpuTemp > 0f) String.format("%.1f", pcStats.gpuTemp) else "--",
                unit = "°C",
                subLabel = "Load",
                subValue = if (pcStats.gpuUsage > 0f) "${pcStats.gpuUsage.toInt()}%" else "--",
                progress = (pcStats.gpuUsage / 100f).coerceIn(0f, 1f),
                icon = Icons.Rounded.VideogameAsset,
                statusColor = when {
                    pcStats.gpuTemp <= 0f -> Color(0xFF64748B)
                    pcStats.gpuTemp < 55f -> EmeraldGreen
                    pcStats.gpuTemp < 72f -> AmberWarning
                    else -> CrimsonAlert
                },
                backdrop = backdrop,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: RAM Memory & Power / Board Telemetry
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HardwareStatCard(
                title = "MEMORY RAM",
                primaryValue = if (pcStats.ramUsage > 0f) "${pcStats.ramUsage.toInt()}" else "--",
                unit = "%",
                subLabel = "Used",
                subValue = "${String.format("%.1f", pcStats.ramUsedGb)} / ${pcStats.ramTotalGb.toInt()} GB",
                progress = (pcStats.ramUsage / 100f).coerceIn(0f, 1f),
                icon = Icons.Rounded.Storage,
                statusColor = when {
                    pcStats.ramUsage < 60f -> ElectricBlue
                    pcStats.ramUsage < 85f -> AmberWarning
                    else -> CrimsonAlert
                },
                backdrop = backdrop,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )

            HardwareStatCard(
                title = "SYSTEM POWER",
                primaryValue = if (pcStats.totalPowerWatts > 0f) "${pcStats.totalPowerWatts.toInt()}" else "--",
                unit = "W",
                subLabel = "VRM Board",
                subValue = "${String.format("%.1f", pcStats.boardTemp)}°C",
                progress = (pcStats.totalPowerWatts / 300f).coerceIn(0f, 1f),
                icon = Icons.Rounded.Bolt,
                statusColor = NeonCyan,
                backdrop = backdrop,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun HardwareStatCard(
    title: String,
    primaryValue: String,
    unit: String,
    subLabel: String,
    subValue: String,
    progress: Float,
    icon: ImageVector,
    statusColor: Color,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)

    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier,
        contentPadding = 14.dp,
        shape = RoundedCornerShape(20.dp),
        isDarkTheme = isDarkTheme
    ) {
        Column {
            // Header: Icon + Title + Status Dot
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = title,
                        color = textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                // Heat/Activity status dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Main Telemetry Display
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = primaryValue,
                    color = textPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = unit,
                    color = statusColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Sub Info (e.g. Load 24% or 13.4 / 32 GB)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = subLabel,
                    color = textSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = subValue,
                    color = textPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
