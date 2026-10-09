package com.sensoryorb.app.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.sensoryorb.app.ui.theme.LocalSensoryColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Floating futuristic compass ring surrounding the orb arena.
 * Rotates smoothly according to the device's magnetic azimuth heading.
 */
@Composable
fun CompassRing(
    azimuthDegrees: Float,
    modifier: Modifier = Modifier
) {
    val colors = LocalSensoryColors.current

    // Smooth shortest-arc angle animation to avoid erratic 360° -> 0° spin
    val animatedRotation = remember { Animatable(0f) }

    LaunchedEffect(azimuthDegrees) {
        val current = animatedRotation.value
        var diff = (azimuthDegrees - current) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        animatedRotation.animateTo(
            targetValue = current + diff,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = minOf(size.width, size.height) / 2f - 14f
        val innerRadius = outerRadius - 18f

        // Draw static arena outer containment glow
        drawCircle(
            color = colors.cardBorder.copy(alpha = 0.20f),
            radius = outerRadius,
            center = center,
            style = Stroke(width = 2f)
        )

        // Draw rotating compass dial
        rotate(degrees = -animatedRotation.value, pivot = center) {
            // Draw compass track ring
            drawCircle(
                color = colors.cardBorder.copy(alpha = 0.35f),
                radius = innerRadius,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Degree tick marks (every 15 degrees, with longer ticks every 45 degrees)
            for (angleDeg in 0 until 360 step 15) {
                val angleRad = (angleDeg - 90) * (PI / 180.0)
                val isMajor = angleDeg % 45 == 0
                val tickLength = if (isMajor) 12f else 6f
                val strokeWidth = if (isMajor) 2f else 1f
                val tickColor = if (isMajor) colors.compassAccent else colors.textSecondary.copy(alpha = 0.4f)

                val startX = (center.x + (innerRadius - tickLength) * cos(angleRad)).toFloat()
                val startY = (center.y + (innerRadius - tickLength) * sin(angleRad)).toFloat()
                val endX = (center.x + innerRadius * cos(angleRad)).toFloat()
                val endY = (center.y + innerRadius * sin(angleRad)).toFloat()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeWidth
                )
            }

            // Cardinal Letters (N, E, S, W)
            val cardinals = listOf(
                Pair("N", 0f),
                Pair("E", 90f),
                Pair("S", 180f),
                Pair("W", 270f)
            )

            val textPaint = Paint().apply {
                isAntiAlias = true
                textSize = 34f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }

            for ((label, angleDeg) in cardinals) {
                val angleRad = (angleDeg - 90) * (PI / 180.0)
                val textRadius = innerRadius - 26f
                val x = (center.x + textRadius * cos(angleRad)).toFloat()
                val y = (center.y + textRadius * sin(angleRad)).toFloat() + 12f

                if (label == "N") {
                    textPaint.color = colors.compassAccent.toArgb()
                } else {
                    textPaint.color = colors.textSecondary.toArgb()
                }

                drawContext.canvas.nativeCanvas.drawText(label, x, y, textPaint)
            }
        }

        // Fixed Top Heading Marker (Needle at 12 o'clock)
        val markerPath = Path().apply {
            val needleTop = center.y - outerRadius - 4f
            moveTo(center.x, needleTop)
            lineTo(center.x - 7f, needleTop + 14f)
            lineTo(center.x + 7f, needleTop + 14f)
            close()
        }
        drawPath(
            path = markerPath,
            color = colors.compassAccent
        )
    }
}
