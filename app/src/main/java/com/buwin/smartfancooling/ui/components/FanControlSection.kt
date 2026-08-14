package com.buwin.smartfancooling.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.ModeFanOff
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Toys
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buwin.smartfancooling.data.model.FanState
import com.buwin.smartfancooling.ui.theme.CrimsonAlert
import com.buwin.smartfancooling.ui.theme.ElectricBlue
import com.buwin.smartfancooling.ui.theme.EmeraldGreen
import com.buwin.smartfancooling.ui.theme.NeonCyan
import com.buwin.smartfancooling.ui.theme.TextMuted
import com.buwin.smartfancooling.ui.theme.TextPrimary
import com.buwin.smartfancooling.ui.theme.TextSecondary
import com.kyant.backdrop.Backdrop

/**
 * Fan control section with power toggle, PWM slider, and quick presets.
 */
@Composable
fun FanControlSection(
    fanState: FanState,
    onSpeedChange: (Int) -> Unit,
    onPowerToggle: (Boolean) -> Unit,
    onPresetSelect: (Int) -> Unit,
    backdrop: Backdrop? = null,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        contentPadding = 18.dp,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column {
            // Header: Title & Power Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (fanState.isPoweredOn) NeonCyan.copy(alpha = 0.18f)
                                else Color.White.copy(alpha = 0.08f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (fanState.isPoweredOn) Icons.Rounded.Toys else Icons.Rounded.ModeFanOff,
                            contentDescription = "Fan Power",
                            tint = if (fanState.isPoweredOn) NeonCyan else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "FAN SPEED CONTROL",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = if (fanState.isPoweredOn) "${fanState.speedPercent}% Duty Cycle" else "Motor Off",
                            color = if (fanState.isPoweredOn) NeonCyan else TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Power toggle button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (fanState.isPoweredOn) EmeraldGreen.copy(alpha = 0.2f)
                            else CrimsonAlert.copy(alpha = 0.15f)
                        )
                        .clickable { onPowerToggle(!fanState.isPoweredOn) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PowerSettingsNew,
                        contentDescription = "Toggle Power",
                        tint = if (fanState.isPoweredOn) EmeraldGreen else CrimsonAlert,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Speed Slider
            Slider(
                value = fanState.speedPercent.toFloat(),
                onValueChange = { onSpeedChange(it.toInt()) },
                valueRange = 0f..100f,
                enabled = fanState.isPoweredOn,
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = NeonCyan,
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f),
                    disabledThumbColor = TextMuted,
                    disabledActiveTrackColor = TextMuted.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Presets row: Quiet (30%), Balanced (60%), Gaming (85%), Max (100%)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetPill(
                    label = "Quiet",
                    percent = 30,
                    isSelected = fanState.speedPercent == 30,
                    enabled = fanState.isPoweredOn,
                    onClick = { onPresetSelect(30) },
                    modifier = Modifier.weight(1f)
                )
                PresetPill(
                    label = "Bal",
                    percent = 60,
                    isSelected = fanState.speedPercent == 60,
                    enabled = fanState.isPoweredOn,
                    onClick = { onPresetSelect(60) },
                    modifier = Modifier.weight(1f)
                )
                PresetPill(
                    label = "Game",
                    percent = 85,
                    isSelected = fanState.speedPercent == 85,
                    enabled = fanState.isPoweredOn,
                    onClick = { onPresetSelect(85) },
                    modifier = Modifier.weight(1f)
                )
                PresetPill(
                    label = "Max",
                    percent = 100,
                    isSelected = fanState.speedPercent == 100,
                    enabled = fanState.isPoweredOn,
                    onClick = { onPresetSelect(100) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PresetPill(
    label: String,
    percent: Int,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            !enabled -> Color.White.copy(alpha = 0.04f)
            isSelected -> NeonCyan.copy(alpha = 0.25f)
            else -> Color.White.copy(alpha = 0.06f)
        },
        label = "preset_bg"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            !enabled -> TextMuted
            isSelected -> NeonCyan
            else -> TextSecondary
        },
        label = "preset_text"
    )

    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$label ($percent%)",
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
