package com.buwin.smartfancooling.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

/**
 * Liquid Glass container using Kyant0's Backdrop library with refractive blur,
 * specular highlight border, and subtle inner glow.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 1.dp,
    tintColor: Color = Color(0xFF131C2E).copy(alpha = 0.55f),
    highlightColor: Color = Color.White.copy(alpha = 0.25f),
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val glassBorderBrush = Brush.verticalGradient(
        colors = listOf(
            highlightColor,
            Color.White.copy(alpha = 0.04f),
            Color.White.copy(alpha = 0.08f)
        )
    )

    val baseModifier = if (backdrop != null) {
        modifier
            .shadow(
                elevation = 12.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(16.dp.toPx())
                    lens(16.dp.toPx(), 32.dp.toPx())
                }
            )
            .clip(shape)
            .background(tintColor)
            .border(borderWidth, glassBorderBrush, shape)
    } else {
        modifier
            .shadow(
                elevation = 12.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .clip(shape)
            .background(tintColor)
            .border(borderWidth, glassBorderBrush, shape)
    }

    Box(
        modifier = baseModifier.padding(contentPadding),
        content = content
    )
}
