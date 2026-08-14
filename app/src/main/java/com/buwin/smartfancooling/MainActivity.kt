package com.buwin.smartfancooling

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.buwin.smartfancooling.ui.screens.MainDashboardScreen
import com.buwin.smartfancooling.ui.viewmodel.SmartFanViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SmartFanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isLightTheme = !isSystemInDarkTheme()
            CompositionLocalProvider(
                LocalIndication provides ripple(color = if (isLightTheme) Color.Black else Color.White)
            ) {
                MainDashboardScreen(viewModel = viewModel)
            }
        }
    }
}