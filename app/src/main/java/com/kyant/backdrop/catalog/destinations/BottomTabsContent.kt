package com.kyant.backdrop.catalog.destinations

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.catalog.BackdropDemoScaffold
import com.kyant.backdrop.catalog.FlightIcon
import com.kyant.backdrop.catalog.components.LiquidBottomTab
import com.kyant.backdrop.catalog.components.LiquidBottomTabs

@Composable
fun BottomTabsContent() {
    val isLightTheme = !isSystemInDarkTheme()
    val contentColor = if (isLightTheme) Color.Black else Color.White

    val airplaneModeIcon = rememberVectorPainter(FlightIcon)
    val iconColorFilter = ColorFilter.tint(contentColor)

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    BackdropDemoScaffold { backdrop ->
        Box(
            Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            LiquidBottomTabs(
                selectedTabIndex = { selectedTabIndex },
                onTabSelected = { selectedTabIndex = it },
                backdrop = backdrop,
                tabsCount = 4,
                modifier = Modifier.padding(horizontal = 36.dp)
            ) {
                repeat(4) { index ->
                    LiquidBottomTab({ selectedTabIndex = index }) {
                        Box(
                            Modifier
                                .size(28.dp)
                                .paint(airplaneModeIcon, colorFilter = iconColorFilter)
                        )
                        BasicText(
                            when (index) {
                                0 -> "Dashboard"
                                1 -> "Lighting"
                                2 -> "Device"
                                else -> "Settings"
                            },
                            style = TextStyle(contentColor, 12.sp)
                        )
                    }
                }
            }
        }
    }
}
