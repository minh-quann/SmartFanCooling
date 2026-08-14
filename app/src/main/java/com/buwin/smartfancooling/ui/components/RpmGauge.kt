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
import androidx.compose.foundation.isSystemInDarkTheme
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
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated circular speedometer gauge for fan RPM and duty cycle telemetry.
 */
@Composable
fun RpmGauge(
    fanState: FanState,
    modifier: Modifier = Modifier,
    size: Dp = 230.dp,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF1C1C1E)
    val textSecondary = if (isDarkTheme) Color(0xFF8E8E93) else Color(0xFF8E8E93)
    val trackBgColor = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

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
            val sweepAngleTotal = 270f

            // 1. Background Arc Track
            drawArc(
                color = trackBgColor,
                startAngle = startAngle,
                sweepAngle = sweepAngleTotal,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Active Progress Arc
            if (animatedProgress > 0f) {
                val progressSweep = sweepAngleTotal * animatedProgress.coerceIn(0.01f, 1f)

                val gradientBrush = Brush.sweepGradient(
                    listOf(
                        ElectricBlue,
                        NeonCyan,
                        EmeraldGreen,
                        AmberWarning,
                        CrimsonAlert
                    ),
                    center = centerOffset
                )

                rotate(degrees = 0f, pivot = centerOffset) {
                    drawArc(
                        brush = gradientBrush,
                        startAngle = startAngle,
                        sweepAngle = progressSweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            // 3. Tick Marks
            val numTicks = 19
            val tickSweepStep = sweepAngleTotal / (numTicks - 1)
            for (i in 0 until numTicks) {
                val tickAngle = Math.toRadians((startAngle + i * tickSweepStep).toDouble())
                val isMajor = i % 3 == 0
                val tickLen = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                val tickInnerRadius = trackRadius - strokeWidth / 2 - 4.dp.toPx()
                val tickOuterRadius = tickInnerRadius - tickLen

                val p1 = Offset(
                    (centerOffset.x + tickInnerRadius * cos(tickAngle)).toFloat(),
                    (centerOffset.y + tickInnerRadius * sin(tickAngle)).toFloat()
                )
                val p2 = Offset(
                    (centerOffset.x + tickOuterRadius * cos(tickAngle)).toFloat(),
                    (centerOffset.y + tickOuterRadius * sin(tickAngle)).toFloat()
                )

                val isActive = (i.toFloat() / (numTicks - 1)) <= animatedProgress
                val tickColor = when {
                    !fanState.isPoweredOn -> if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1)
                    isActive -> NeonCyan.copy(alpha = if (isMajor) 0.95f else 0.6f)
                    else -> if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1)
                }

                drawLine(
                    color = tickColor,
                    start = p1,
                    end = p2,
                    strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 4. Rotating Fan Blades
            if (fanState.isPoweredOn && animatedProgress > 0f) {
                rotate(degrees = spinAngle, pivot = centerOffset) {
                    val bladeRadius = 26.dp.toPx()
                    val bladeCount = 5
                    for (b in 0 until bladeCount) {
                        val angle = Math.toRadians((b * (360f / bladeCount)).toDouble())
                        val tip = Offset(
                            (centerOffset.x + bladeRadius * cos(angle)).toFloat(),
                            (centerOffset.y + bladeRadius * sin(angle)).toFloat()
                        )
                        drawLine(
                            color = NeonCyan.copy(alpha = 0.35f),
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
                color = textPrimary,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-1).sp
            )

            Text(
                text = "RPM",
                color = textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
        }
    }
}
