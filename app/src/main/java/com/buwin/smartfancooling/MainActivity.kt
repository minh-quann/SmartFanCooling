package com.buwin.smartfancooling

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.buwin.smartfancooling.ui.screens.MainDashboardScreen
import com.buwin.smartfancooling.ui.theme.SmartFanCoolingTheme
import com.buwin.smartfancooling.ui.viewmodel.SmartFanViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SmartFanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartFanCoolingTheme {
                MainDashboardScreen(viewModel = viewModel)
            }
        }
    }
}