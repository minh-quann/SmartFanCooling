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
import com.buwin.smartfancooling.ui.theme.BackgroundDark
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated mesh ambient background with floating glowing orbs for backdrop sampling.
 */
@Composable
fun AnimatedMeshBackground(
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF00E5FF)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh_ambient")

    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_oscillation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Base deep dark gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070A10),
                        Color(0xFF0D1322),
                        Color(0xFF06090F)
                    )
                )
            )

            // Orb 1: Primary Cyan / Accent floating top-left to center
            val orb1X = width * (0.28f + 0.18f * cos(animTime))
            val orb1Y = height * (0.22f + 0.12f * sin(animTime * 1.2f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.35f),
                        accentColor.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(orb1X, orb1Y),
                    radius = width * 0.75f
                ),
                center = Offset(orb1X, orb1Y),
                radius = width * 0.75f
            )

            // Orb 2: Deep Blue floating right-center
            val orb2X = width * (0.75f + 0.15f * sin(animTime * 0.9f))
            val orb2Y = height * (0.52f + 0.16f * cos(animTime * 1.1f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2979FF).copy(alpha = 0.28f),
                        Color(0xFF1565C0).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(orb2X, orb2Y),
                    radius = width * 0.85f
                ),
                center = Offset(orb2X, orb2Y),
                radius = width * 0.85f
            )

            // Orb 3: Violet / Magenta floating bottom-left
            val orb3X = width * (0.35f + 0.20f * cos(animTime * 1.3f))
            val orb3Y = height * (0.80f + 0.12f * sin(animTime * 0.8f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF7C4DFF).copy(alpha = 0.25f),
                        Color(0xFFFF4081).copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(orb3X, orb3Y),
                    radius = width * 0.70f
                ),
                center = Offset(orb3X, orb3Y),
                radius = width * 0.70f
            )
        }
    }
}
