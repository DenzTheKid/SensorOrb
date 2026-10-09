package com.sensoryorb.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sensoryorb.app.sensor.CompassData
import com.sensoryorb.app.sensor.LightData
import com.sensoryorb.app.sensor.MotionData
import com.sensoryorb.app.ui.theme.LocalSensoryColors
import java.util.Locale

/**
 * 3 Horizontal Glassmorphism cards displaying real-time readings from each sensor category.
 */
@Composable
fun SensorCardsSection(
    motion: MotionData,
    isMotionAvailable: Boolean,
    light: LightData,
    isLightAvailable: Boolean,
    compass: CompassData,
    isCompassAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Motion Card (Accelerometer)
        MotionSensorCard(
            motion = motion,
            isAvailable = isMotionAvailable,
            modifier = Modifier.weight(1f)
        )

        // 2. Light Card (Ambient Light)
        LightSensorCard(
            light = light,
            isAvailable = isLightAvailable,
            modifier = Modifier.weight(1f)
        )

        // 3. Position Card (Compass Azimuth)
        CompassSensorCard(
            compass = compass,
            isAvailable = isCompassAvailable,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MotionSensorCard(
    motion: MotionData,
    isAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalSensoryColors.current

    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            CardHeader(
                icon = Icons.Default.Speed,
                category = "MOTION",
                title = "Accel",
                accentColor = colors.orbPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (!isAvailable) {
                UnavailableBadge()
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "X",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                    Text(
                        text = String.format(Locale.US, "%+.1f", motion.x),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textPrimary
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Y",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                    Text(
                        text = String.format(Locale.US, "%+.1f", motion.y),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textPrimary
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Z",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                    Text(
                        text = String.format(Locale.US, "%+.1f", motion.z),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Shakes: ${motion.shakeCount}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = colors.orbSecondary
                )
            }
        }
    }
}

@Composable
fun LightSensorCard(
    light: LightData,
    isAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalSensoryColors.current
    val progressTarget = (light.lux / 1200f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progressTarget, label = "luxProgress")

    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            CardHeader(
                icon = Icons.Default.BrightnessMedium,
                category = "ENVIRON",
                title = "Light",
                accentColor = colors.orbSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (!isAvailable) {
                UnavailableBadge()
            } else {
                Text(
                    text = "${light.lux.toInt()} lx",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 15.sp),
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = colors.orbSecondary,
                    trackColor = colors.textSecondary.copy(alpha = 0.2f),
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = light.mode.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = colors.orbPrimary
                )
            }
        }
    }
}

@Composable
fun CompassSensorCard(
    compass: CompassData,
    isAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalSensoryColors.current

    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            CardHeader(
                icon = Icons.Default.Explore,
                category = "POSITION",
                title = "Compass",
                accentColor = colors.compassAccent
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (!isAvailable) {
                UnavailableBadge()
            } else {
                Text(
                    text = "${compass.azimuthDegrees.toInt()}°",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 15.sp),
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.compassAccent.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = compass.cardinalDirection,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.compassAccent
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Azimuth",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun CardHeader(
    icon: ImageVector,
    category: String,
    title: String,
    accentColor: androidx.compose.ui.graphics.Color
) {
    val colors = LocalSensoryColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )
        }
        Column {
            Text(
                text = category,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = colors.textSecondary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 12.sp),
                color = colors.textPrimary
            )
        }
    }
}

@Composable
private fun UnavailableBadge() {
    val colors = LocalSensoryColors.current
    Text(
        text = "Sensor\nN/A",
        style = MaterialTheme.typography.labelSmall,
        color = colors.textSecondary
    )
}
