package com.buwin.smartfancooling.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.VideogameAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.buwin.smartfancooling.ui.theme.NeonCyan
import com.buwin.smartfancooling.ui.theme.TextPrimary
import com.buwin.smartfancooling.ui.theme.TextSecondary
import com.kyant.backdrop.Backdrop

/**
 * Telemetry row showing CPU and GPU temperatures and loads in glass cards.
 */
@Composable
fun PcTelemetrySection(
    pcStats: PcStats,
    backdrop: Backdrop? = null,
    modifier: Modifier = Modifier
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
            modifier = Modifier.weight(1f)
        )

        // GPU Tile
        HardwareStatCard(
            title = "GPU CORE",
            temp = pcStats.gpuTemp,
            loadPercent = pcStats.gpuUsage,
            icon = Icons.Rounded.VideogameAsset,
            backdrop = backdrop,
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
    backdrop: Backdrop? = null,
    modifier: Modifier = Modifier
) {
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
        shape = RoundedCornerShape(20.dp)
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
                        color = TextSecondary,
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
                    color = TextPrimary,
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
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = if (loadPercent > 0f) "${loadPercent.toInt()}%" else "--",
                    color = TextPrimary,
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
                trackColor = Color.White.copy(alpha = 0.08f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
