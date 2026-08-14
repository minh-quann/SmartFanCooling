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
 * Pure neutral grey / monochrome Liquid Glass Container:
 * - Refractive blur & vibrancy
 * - Specular edge highlight
 * - Neutral grey surface (zero blue/cyan tint)
 */
@Composable
fun LiquidGlassCard(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    contentPadding: Dp = 16.dp,
    tint: Color = Color.Unspecified,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable BoxScope.() -> Unit
) {
    val isLightTheme = !isDarkTheme
    val defaultSurface = if (isLightTheme) Color.White.copy(0.85f) else Color(0xFF1E1E22).copy(0.65f)

    Box(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(16f.dp.toPx())
                    lens(
                        10f.dp.toPx(),
                        20f.dp.toPx(),
                        chromaticAberration = false
                    )
                },
                highlight = {
                    Highlight.Default.copy(alpha = if (isLightTheme) 0.4f else 0.5f)
                },
                innerShadow = {
                    InnerShadow(
                        radius = 4f.dp,
                        alpha = if (isLightTheme) 0.15f else 0.25f
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
