package com.kyant.shapes

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp

/**
 * Capsule stadium shape (50% rounded corners).
 */
fun Capsule(): RoundedCornerShape = RoundedCornerShape(percent = 50)

/**
 * RoundedRectangle shape.
 */
fun RoundedRectangle(radius: Dp): RoundedCornerShape = RoundedCornerShape(radius)
fun RoundedRectangle(radius: Float): RoundedCornerShape = RoundedCornerShape(radius)
