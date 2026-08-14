package com.kyant.backdrop.catalog.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kyant.shapes.Capsule
import kotlin.math.abs

internal val LocalLiquidBottomTabScale =
    staticCompositionLocalOf { { 1f } }

internal val LocalLiquidBottomTabPillPosition =
    staticCompositionLocalOf { { -1f } }

internal val LocalLiquidBottomTabIsBackdrop =
    staticCompositionLocalOf { false }

@Composable
fun RowScope.LiquidBottomTab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val scale = LocalLiquidBottomTabScale.current
    val isBackdrop = LocalLiquidBottomTabIsBackdrop.current
    val pillPosition = LocalLiquidBottomTabPillPosition.current

    var tabIndexInParent by remember { mutableFloatStateOf(-1f) }

    Column(
        modifier
            .clip(Capsule())
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .fillMaxHeight()
            .weight(1f)
            .onGloballyPositioned { coordinates ->
                val parentWidth = coordinates.parentLayoutCoordinates?.size?.width?.toFloat() ?: 0f
                if (parentWidth > 0f) {
                    val myX = coordinates.positionInParent().x
                    val myWidth = coordinates.size.width.toFloat()
                    if (myWidth > 0f) {
                        tabIndexInParent = myX / myWidth
                    }
                }
            }
            .graphicsLayer {
                if (isBackdrop) {
                    val s = scale()
                    scaleX = s
                    scaleY = s
                } else {
                    val currentPill = pillPosition()
                    if (currentPill >= 0f && tabIndexInParent >= 0f) {
                        val distance = abs(currentPill - tabIndexInParent)
                        val overlap = (1f - distance).coerceIn(0f, 1f)
                        alpha = 1f - overlap
                    }
                }
            },
        verticalArrangement = Arrangement.spacedBy(2f.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}