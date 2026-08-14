package com.buwin.smartfancooling.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buwin.smartfancooling.data.model.RgbMode
import com.buwin.smartfancooling.data.model.RgbState
import com.buwin.smartfancooling.ui.theme.EmeraldGreen
import com.buwin.smartfancooling.ui.theme.NeonCyan
import com.buwin.smartfancooling.ui.theme.NeonMagenta
import com.buwin.smartfancooling.ui.theme.NeonPurple
import com.buwin.smartfancooling.ui.theme.TextMuted
import com.buwin.smartfancooling.ui.theme.TextPrimary
import com.buwin.smartfancooling.ui.theme.TextSecondary
import com.kyant.backdrop.Backdrop

/**
 * RGB Lighting Studio section for mode selection, color presets, and brightness.
 */
@Composable
fun RgbControlSection(
    rgbState: RgbState,
    onModeSelect: (RgbMode) -> Unit,
    onColorSelect: (Color) -> Unit,
    onBrightnessChange: (Int) -> Unit,
    onPowerToggle: (Boolean) -> Unit,
    backdrop: Backdrop? = null,
    modifier: Modifier = Modifier
) {
    val colorPresets = listOf(
        NeonCyan,
        Color(0xFF2979FF),
        NeonPurple,
        NeonMagenta,
        Color(0xFFFF1744),
        Color(0xFFFF9100),
        EmeraldGreen,
        Color.White
    )

    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        contentPadding = 18.dp,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column {
            // Header: Title & Active color indicator
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
                                if (rgbState.isPoweredOn) NeonPurple.copy(alpha = 0.2f)
                                else Color.White.copy(alpha = 0.08f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "RGB Studio",
                            tint = if (rgbState.isPoweredOn) NeonPurple else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "RGB LIGHTING STUDIO",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = if (rgbState.isPoweredOn) rgbState.mode.title else "Disabled",
                            color = if (rgbState.isPoweredOn) NeonPurple else TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Power Toggle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (rgbState.isPoweredOn) EmeraldGreen.copy(alpha = 0.2f)
                            else Color.White.copy(alpha = 0.08f)
                        )
                        .clickable { onPowerToggle(!rgbState.isPoweredOn) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PowerSettingsNew,
                        contentDescription = "Toggle RGB",
                        tint = if (rgbState.isPoweredOn) EmeraldGreen else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // LED Effect Modes Scrollable Row
            Text(
                text = "EFFECT MODES",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            Spacer(Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(RgbMode.entries) { mode ->
                    val isSelected = rgbState.mode == mode && rgbState.isPoweredOn
                    val bg by animateColorAsState(
                        targetValue = if (isSelected) NeonPurple.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.06f),
                        label = "mode_chip_bg"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else TextSecondary,
                        label = "mode_chip_text"
                    )

                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) NeonPurple.copy(alpha = 0.6f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = rgbState.isPoweredOn) { onModeSelect(mode) }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.title,
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Color Palette Presets (Active when mode supports color like Static, Breathing, Wave, Comet)
            Text(
                text = "COLOR PALETTE",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                colorPresets.forEach { color ->
                    val isSelected = (rgbState.composeColor == color) && rgbState.isPoweredOn
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .clickable(enabled = rgbState.isPoweredOn) { onColorSelect(color) }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Brightness Slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.LightMode,
                        contentDescription = "Brightness",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "BRIGHTNESS",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
                Text(
                    text = "${rgbState.brightnessPercent}%",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Slider(
                value = rgbState.brightness.toFloat(),
                onValueChange = { onBrightnessChange(it.toInt()) },
                valueRange = 0f..255f,
                enabled = rgbState.isPoweredOn,
                colors = SliderDefaults.colors(
                    thumbColor = NeonPurple,
                    activeTrackColor = NeonPurple,
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f),
                    disabledThumbColor = TextMuted,
                    disabledActiveTrackColor = TextMuted.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
