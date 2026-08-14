package com.buwin.smartfancooling.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buwin.smartfancooling.data.model.FanState
import com.buwin.smartfancooling.ui.theme.AmberWarning
import com.buwin.smartfancooling.ui.theme.CrimsonAlert
import com.buwin.smartfancooling.ui.theme.ElectricBlue
import com.buwin.smartfancooling.ui.theme.EmeraldGreen
import com.buwin.smartfancooling.ui.theme.NeonCyan
import com.buwin.smartfancooling.ui.theme.TextMuted
import com.buwin.smartfancooling.ui.theme.TextPrimary
import com.buwin.smartfancooling.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated circular speedometer gauge for fan RPM and duty cycle telemetry.
 */
@Composable
fun RpmGauge(
    fanState: FanState,
    modifier: Modifier = Modifier,
    size: Dp = 230.dp
) {
    val targetRatio = if (fanState.isPoweredOn) fanState.rpmRatio else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetRatio,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "rpm_ratio_anim"
    )

    val animatedRpm by animateIntAsState(
        targetValue = if (fanState.isPoweredOn) fanState.currentRpm else 0,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "rpm_value_anim"
    )

    // Spin animation when fan is active
    val infiniteTransition = rememberInfiniteTransition(label = "gauge_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1800 - (animatedProgress * 1200)).toInt().coerceAtLeast(300),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "fan_rotation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 14.dp.toPx()
            val trackRadius = (this.size.minDimension - strokeWidth * 2) / 2
            val centerOffset = Offset(this.size.width / 2, this.size.height / 2)
            val arcSize = Size(trackRadius * 2, trackRadius * 2)
            val arcTopLeft = Offset(centerOffset.x - trackRadius, centerOffset.y - trackRadius)

            val startAngle = 135f
            val totalSweep = 270f
            val currentSweep = totalSweep * animatedProgress

            // 1. Background Track
            drawArc(
                color = Color(0xFF1E293B).copy(alpha = 0.6f),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Tick marks
            val tickCount = 9
            for (i in 0 until tickCount) {
                val tickFraction = i.toFloat() / (tickCount - 1)
                val angleDeg = startAngle + tickFraction * totalSweep
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val innerR = trackRadius - 16.dp.toPx()
                val outerR = trackRadius - 8.dp.toPx()

                val p1 = Offset(
                    (centerOffset.x + innerR * cos(angleRad)).toFloat(),
                    (centerOffset.y + innerR * sin(angleRad)).toFloat()
                )
                val p2 = Offset(
                    (centerOffset.x + outerR * cos(angleRad)).toFloat(),
                    (centerOffset.y + outerR * sin(angleRad)).toFloat()
                )

                val isPassed = tickFraction <= animatedProgress
                val tickColor = if (isPassed) NeonCyan.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.15f)
                drawLine(
                    color = tickColor,
                    start = p1,
                    end = p2,
                    strokeWidth = if (i % 2 == 0) 3.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 3. Active Progress Arc with Dynamic Gradient
            if (currentSweep > 0f) {
                val gradient = Brush.sweepGradient(
                    colors = listOf(
                        NeonCyan,
                        ElectricBlue,
                        AmberWarning,
                        CrimsonAlert,
                        NeonCyan
                    ),
                    center = centerOffset
                )

                drawArc(
                    brush = gradient,
                    startAngle = startAngle,
                    sweepAngle = currentSweep,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Glowing pointer cap at current angle
                val headAngleRad = Math.toRadians((startAngle + currentSweep).toDouble())
                val headPos = Offset(
                    (centerOffset.x + trackRadius * cos(headAngleRad)).toFloat(),
                    (centerOffset.y + trackRadius * sin(headAngleRad)).toFloat()
                )

                drawCircle(
                    color = Color.White,
                    radius = strokeWidth * 0.45f,
                    center = headPos
                )
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.45f),
                    radius = strokeWidth * 0.9f,
                    center = headPos
                )
            }

            // 4. Subtle center fan rotating blade icon
            if (fanState.isPoweredOn && animatedProgress > 0.05f) {
                rotate(spinAngle, centerOffset) {
                    val bladeRadius = 28.dp.toPx()
                    for (b in 0 until 3) {
                        val bladeAngle = b * 120.0
                        val bRad = Math.toRadians(bladeAngle)
                        val tip = Offset(
                            (centerOffset.x + bladeRadius * cos(bRad)).toFloat(),
                            (centerOffset.y + bladeRadius * sin(bRad)).toFloat()
                        )
                        drawLine(
                            color = NeonCyan.copy(alpha = 0.25f),
                            start = centerOffset,
                            end = tip,
                            strokeWidth = 4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        // Center readout text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Speed percentage pill
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                ) {
                    Canvas(Modifier.matchParentSize()) {
                        drawCircle(
                            color = if (fanState.isPoweredOn) EmeraldGreen else CrimsonAlert
                        )
                    }
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (fanState.isPoweredOn) "${fanState.speedPercent}% PWM" else "OFFLINE",
                    color = if (fanState.isPoweredOn) NeonCyan else TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(4.dp))

            // Main RPM number
            Text(
                text = String.format("%,d", animatedRpm),
                color = TextPrimary,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-1).sp
            )

            Text(
                text = "RPM",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
        }
    }
}
