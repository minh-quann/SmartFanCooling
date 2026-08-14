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
import androidx.compose.material.icons.rounded.Memory
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
import com.buwin.smartfancooling.ui.theme.EmeraldGreen
import com.kyant.backdrop.Backdrop

/**
 * Telemetry row showing CPU and GPU temperatures and loads in liquid glass cards.
 */
@Composable
fun PcTelemetrySection(
    pcStats: PcStats,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // CPU Tile
        HardwareStatCard(
            title = "CPU PACKAGE",
            temp = pcStats.cpuTemp,
            loadPercent = pcStats.cpuUsage,
            icon = Icons.Rounded.Memory,
            backdrop = backdrop,
            isDarkTheme = isDarkTheme,
            modifier = Modifier.weight(1f)
        )

        // GPU Tile
        HardwareStatCard(
            title = "GPU CORE",
            temp = pcStats.gpuTemp,
            loadPercent = pcStats.gpuUsage,
            icon = Icons.Rounded.VideogameAsset,
            backdrop = backdrop,
            isDarkTheme = isDarkTheme,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun HardwareStatCard(
    title: String,
    temp: Float,
    loadPercent: Float,
    icon: ImageVector,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)

    val heatColor = when {
        temp <= 0f -> Color(0xFF64748B)
        temp < 55f -> EmeraldGreen
        temp < 72f -> AmberWarning
        else -> CrimsonAlert
    }

    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier,
        contentPadding = 14.dp,
        shape = RoundedCornerShape(20.dp),
        isDarkTheme = isDarkTheme
    ) {
        Column {
            // Header: Icon + Title
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
                            .background(heatColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = heatColor,
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

                // Heat status dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(heatColor)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Main Temperature Display
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = if (temp > 0f) String.format("%.1f", temp) else "--",
                    color = textPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "°C",
                    color = heatColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Load progress bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Load",
                    color = textSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = if (loadPercent > 0f) "${loadPercent.toInt()}%" else "--",
                    color = textPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { (loadPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = heatColor,
                trackColor = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
