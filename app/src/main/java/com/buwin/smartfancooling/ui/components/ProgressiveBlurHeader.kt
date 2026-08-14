package com.buwin.smartfancooling.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawPlainBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.runtimeShaderEffect

/**
 * Exact Alpha-Masked Progressive Blur Header from Kyant0's library:
 * Directly extracted from ProgressiveBlurContent.kt
 */
@Composable
fun ProgressiveBlurHeader(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    height: Dp = 90.dp
) {
    val isLightTheme = !isSystemInDarkTheme()
    val tintColor = if (isLightTheme) Color(0xFFFAFAF9) else Color(0xFF000000)

    Box(
        modifier = modifier
            .drawPlainBackdrop(
                backdrop = backdrop,
                shape = { RectangleShape },
                effects = {
                    blur(8f.dp.toPx())
                    runtimeShaderEffect(
                        "AlphaMask",
                        """
    uniform shader content;
    
    uniform float2 size;
    layout(color) uniform half4 tint;
    uniform float tintIntensity;
    
    half4 main(float2 coord) {
        float blurAlpha = smoothstep(size.y, size.y * 0.4, coord.y);
        float tintAlpha = smoothstep(size.y, size.y * 0.4, coord.y);
        return mix(content.eval(coord) * blurAlpha, tint * tintAlpha, tintIntensity);
    }""",
                        "content"
                    ) {
                        setFloatUniform("size", size.width, size.height)
                        setColorUniform("tint", tintColor)
                        setFloatUniform("tintIntensity", if (isLightTheme) 0.75f else 0.85f)
                    }
                }
            )
            .height(height)
            .fillMaxWidth()
    )
}
