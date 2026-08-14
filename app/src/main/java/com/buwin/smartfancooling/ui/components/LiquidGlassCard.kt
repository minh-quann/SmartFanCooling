package com.buwin.smartfancooling.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow

/**
 * Authentic Liquid Glass Container using Kyant0's Backdrop library:
 * - Refractive blur & vibrancy
 * - Specular edge highlight
 * - Clean border, no heavy black drop shadows
 */
@Composable
fun LiquidGlassCard(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    contentPadding: Dp = 18.dp,
    tint: Color = Color.Unspecified,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable BoxScope.() -> Unit
) {
    val isLightTheme = !isDarkTheme
    val defaultSurface = if (isLightTheme) Color.White.copy(0.75f) else Color(0xFF141721).copy(0.70f)

    Box(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(16f.dp.toPx())
                    lens(
                        12f.dp.toPx(),
                        24f.dp.toPx(),
                        chromaticAberration = false
                    )
                },
                highlight = {
                    Highlight.Default.copy(alpha = if (isLightTheme) 0.5f else 0.8f)
                },
                innerShadow = {
                    InnerShadow(
                        radius = 6f.dp,
                        alpha = if (isLightTheme) 0.2f else 0.4f
                    )
                },
                onDrawSurface = {
                    drawRect(if (tint != Color.Unspecified) tint else defaultSurface)
                }
            )
            .padding(contentPadding),
        content = content
    )
}
