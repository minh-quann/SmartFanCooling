package com.buwin.smartfancooling.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * Authentic Liquid Glass organic fluid mesh background.
 * Creates rich, colorful, flowing refractive pools inspired by Apple iOS 18 wallpaper.
 */
@Composable
fun LiquidOrganicBackground(
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF00E5FF)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_ambient")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060D18))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Base gradient canvas (Deep Midnight Sky to Oceanic Teal)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF07111E),
                        Color(0xFF0A1E32),
                        Color(0xFF081424),
                        Color(0xFF030A12)
                    )
                )
            )

            // 2. Large Mint / Emerald Organic Swirl (Top-Right flowing into Center)
            val c1X = w * (0.68f + 0.16f * cos(phase * 0.9f))
            val c1Y = h * (0.24f + 0.14f * sin(phase * 1.1f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00E676).copy(alpha = 0.38f),
                        Color(0xFF00B0FF).copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = Offset(c1X, c1Y),
                    radius = w * 0.85f
                ),
                center = Offset(c1X, c1Y),
                radius = w * 0.85f
            )

            // 3. Cyan / Electric Blue Swirl (Left-Center to Upper-Left)
            val c2X = w * (0.22f + 0.18f * sin(phase * 1.2f))
            val c2Y = h * (0.42f + 0.15f * cos(phase * 0.8f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.45f),
                        Color(0xFF0091EA).copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(c2X, c2Y),
                    radius = w * 0.95f
                ),
                center = Offset(c2X, c2Y),
                radius = w * 0.95f
            )

            // 4. Vibrant Indigo / Violet Swirl (Bottom-Right / Floating base)
            val c3X = w * (0.78f + 0.14f * cos(phase * 1.3f))
            val c3Y = h * (0.78f + 0.12f * sin(phase * 0.95f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF7C4DFF).copy(alpha = 0.35f),
                        Color(0xFF00E5FF).copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(c3X, c3Y),
                    radius = w * 0.80f
                ),
                center = Offset(c3X, c3Y),
                radius = w * 0.80f
            )

            // 5. White Specular Highlight Core (Center refraction anchor)
            val c4X = w * (0.45f + 0.10f * sin(phase * 0.7f))
            val c4Y = h * (0.55f + 0.12f * cos(phase * 1.0f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color(0xFF80D8FF).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(c4X, c4Y),
                    radius = w * 0.55f
                ),
                center = Offset(c4X, c4Y),
                radius = w * 0.55f
            )
        }
    }
}
