package com.sensoryorb.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sensoryorb.app.ui.theme.LocalSensoryColors

/**
 * Premium glassmorphic container with semi-transparent frosted fill,
 * smooth rounded corners, and a glowing thin gradient border.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LocalSensoryColors.current
    val shape = RoundedCornerShape(cornerRadius)

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            colors.cardBorder.copy(alpha = 0.55f),
            colors.cardBorder.copy(alpha = 0.15f),
            colors.cardBorder.copy(alpha = 0.40f)
        )
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.cardBg)
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = shape
            )
            .padding(14.dp),
        content = content
    )
}
