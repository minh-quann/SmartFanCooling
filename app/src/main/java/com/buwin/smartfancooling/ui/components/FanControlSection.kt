package com.buwin.smartfancooling.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.ModeFanOff
import androidx.compose.material.icons.rounded.Toys
import androidx.compose.material3.Icon
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
import com.buwin.smartfancooling.ui.theme.NeonCyan
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.catalog.components.LiquidSlider
import com.kyant.backdrop.catalog.components.LiquidToggle

@Composable
fun FanControlSection(
    fanState: FanState,
    onSpeedChange: (Int) -> Unit,
    onPowerToggle: (Boolean) -> Unit,
    onPresetSelect: (Int) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val textPrimary = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)

    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        contentPadding = 18.dp,
        shape = RoundedCornerShape(26.dp),
        isDarkTheme = isDarkTheme
    ) {
        Column {
            // Header: Title & Authentic Liquid Glass Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (fanState.isPoweredOn) NeonCyan.copy(alpha = 0.22f)
                                else if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (fanState.isPoweredOn) Icons.Rounded.Toys else Icons.Rounded.ModeFanOff,
                            contentDescription = "Fan Power",
                            tint = if (fanState.isPoweredOn) NeonCyan else textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "FAN SPEED CONTROL",
                            color = textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = if (fanState.isPoweredOn) "${fanState.speedPercent}% Duty Cycle" else "Motor Off",
                            color = if (fanState.isPoweredOn) NeonCyan else textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Kyant0 Liquid Toggle Switch
                LiquidToggle(
                    selected = { fanState.isPoweredOn },
                    onSelect = { onPowerToggle(it) },
                    backdrop = backdrop
                )
            }

            Spacer(Modifier.height(18.dp))

            // Kyant0 Liquid Slider
            LiquidSlider(
                value = { fanState.speedPercent.toFloat() },
                onValueChange = { onSpeedChange(it.toInt()) },
                valueRange = 0f..100f,
                visibilityThreshold = 1f,
                backdrop = backdrop
            )

            Spacer(Modifier.height(18.dp))

            // Quick Preset Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Quiet" to 30,
                    "Bal" to 60,
                    "Game" to 85,
                    "Max" to 100
                ).forEach { (label, presetVal) ->
                    val isSelected = fanState.speedPercent == presetVal && fanState.isPoweredOn

                    val presetBg by animateColorAsState(
                        targetValue = if (isSelected) NeonCyan.copy(alpha = 0.28f)
                        else if (isDarkTheme) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f),
                        label = "preset_bg"
                    )
                    val presetTextColor by animateColorAsState(
                        targetValue = if (isSelected) NeonCyan else textSecondary,
                        label = "preset_text"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(CircleShape)
                            .background(presetBg)
                            .clickable { onPresetSelect(presetVal) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$label (${presetVal}%)",
                            color = presetTextColor,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
