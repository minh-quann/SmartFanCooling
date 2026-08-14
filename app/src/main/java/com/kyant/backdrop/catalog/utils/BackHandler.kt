package com.kyant.backdrop.catalog.utils

import androidx.activity.compose.BackHandler as AndroidBackHandler
import androidx.compose.runtime.Composable

@Composable
fun BackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
) {
    AndroidBackHandler(enabled = enabled, onBack = onBack)
}
