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
import com.kyant.backdrop.shadow.Shadow

/**
 * Authentic Liquid Glass Container using Kyant0's Backdrop library:
 * - Refractive blur & vibrancy
 * - Chromatic aberration lens
 * - Specular edge highlight
 * - Outer and inner shadows
 */
@Composable
fun LiquidGlassCard(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    contentPadding: Dp = 18.dp,
    tint: Color = Color.Unspecified,
    content: @Composable BoxScope.() -> Unit
) {
    val isLightTheme = !isSystemInDarkTheme()
    val defaultSurface = if (isLightTheme) Color.Black.copy(0.04f) else Color.White.copy(0.06f)

    Box(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(16f.dp.toPx())
                    lens(
                        20f.dp.toPx(),
                        40f.dp.toPx(),
                        chromaticAberration = true
                    )
                },
                highlight = {
                    Highlight.Default.copy(alpha = if (isLightTheme) 0.6f else 0.85f)
                },
                shadow = {
                    Shadow(
                        radius = 16f.dp,
                        color = Color.Black.copy(alpha = 0.55f)
                    )
                },
                innerShadow = {
                    InnerShadow(
                        radius = 8f.dp,
                        alpha = if (isLightTheme) 0.35f else 0.55f
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
