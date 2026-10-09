package com.sensoryorb.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sensoryorb.app.sensor.CompassData
import com.sensoryorb.app.sensor.LightData
import com.sensoryorb.app.sensor.MotionData
import com.sensoryorb.app.sensor.OrbThemeMode
import com.sensoryorb.app.sensor.SensorUiState
import com.sensoryorb.app.ui.components.GlassCard
import com.sensoryorb.app.ui.components.OrbArena
import com.sensoryorb.app.ui.components.SensorCardsSection
import com.sensoryorb.app.ui.theme.LocalSensoryColors
import com.sensoryorb.app.ui.theme.SensoryOrbTheme

/**
 * Main application screen composing:
 * 1. Top Header with App Title and Theme Status Chip
 * 2. Central Interactive Physics Arena with Orb & Compass
 * 3. Bottom Glassmorphic Sensor Telemetry Cards
 * 4. Interactive Simulation & Fallback controls for Emulator Demo
 */
@Composable
fun MainScreen(
    uiState: SensorUiState,
    onToggleSimulation: () -> Unit,
    onSimulateLuxChange: (Float) -> Unit,
    onTriggerShake: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSensoryColors.current
    var isControlSheetExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(colors.bgTop, colors.bgBottom)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header: App Name + Mode Status Chip
            TopHeaderSection(
                mode = uiState.light.mode,
                lux = uiState.light.lux,
                isSimulationActive = uiState.isSimulationActive,
                onControlToggle = { isControlSheetExpanded = !isControlSheetExpanded }
            )

            // 2. Central Arena (Orb, Trail, Compass Ring, Shake Shockwave)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                OrbArena(
                    motion = uiState.motion,
                    azimuthDegrees = uiState.compass.azimuthDegrees,
                    themeMode = uiState.light.mode,
                    lastShakeTimestamp = uiState.lastShakeTimestamp,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 3. Bottom Section: Sensor Info Cards + Simulation Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sensor Cards
                SensorCardsSection(
                    motion = uiState.motion,
                    isMotionAvailable = uiState.isMotionAvailable,
                    light = uiState.light,
                    isLightAvailable = uiState.isLightAvailable,
                    compass = uiState.compass,
                    isCompassAvailable = uiState.isCompassAvailable
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Emulator Simulation Panel (Expandable)
                AnimatedVisibility(
                    visible = isControlSheetExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SimulationControlsPanel(
                        uiState = uiState,
                        onToggleSimulation = onToggleSimulation,
                        onSimulateLuxChange = onSimulateLuxChange,
                        onTriggerShake = onTriggerShake,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                // Quick toggle pill for simulation controls
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.cardBg)
                        .clickable { isControlSheetExpanded = !isControlSheetExpanded }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Simulation Controls",
                        tint = colors.orbPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isControlSheetExpanded) "Tutup Kontrol Simulasi" else "Buka Kontrol Demo / Simulator",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun TopHeaderSection(
    mode: OrbThemeMode,
    lux: Float,
    isSimulationActive: Boolean,
    onControlToggle: () -> Unit
) {
    val colors = LocalSensoryColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Sensory Orb",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary
            )
            Text(
                text = if (isSimulationActive) "Mode Simulasi (${lux.toInt()} lx)" else "Real-Time Tri-Sensor Dynamic UI",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )
        }

        // Mode Status Chip
        val (modeIcon: ImageVector, modeColor) = when (mode) {
            OrbThemeMode.NIGHT -> Pair(Icons.Default.NightsStay, colors.orbPrimary)
            OrbThemeMode.TWILIGHT -> Pair(Icons.Default.WbTwilight, colors.orbSecondary)
            OrbThemeMode.DAY -> Pair(Icons.Default.WbSunny, colors.compassAccent)
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.cardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, modeColor.copy(alpha = 0.5f)),
            modifier = Modifier.clickable { onControlToggle() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = modeIcon,
                    contentDescription = mode.displayName,
                    tint = modeColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = mode.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = colors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun SimulationControlsPanel(
    uiState: SensorUiState,
    onToggleSimulation: () -> Unit,
    onSimulateLuxChange: (Float) -> Unit,
    onTriggerShake: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSensoryColors.current

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.isSimulationActive) "Mode Simulasi Aktif" else "Sensor Hardware Aktif",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
                    color = colors.textPrimary
                )
                Text(
                    text = if (uiState.isSimulationActive) "Gunakan Sensor Fisik" else "Override Slider",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = colors.orbSecondary,
                    modifier = Modifier.clickable { onToggleSimulation() }
                )
            }
            Text(
                text = "${uiState.light.lux.toInt()} Lux (${uiState.light.mode.displayName})",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = colors.orbPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Lux simulation slider
            Slider(
                value = uiState.light.lux,
                onValueChange = onSimulateLuxChange,
                valueRange = 0f..1500f,
                colors = SliderDefaults.colors(
                    thumbColor = colors.orbPrimary,
                    activeTrackColor = colors.orbSecondary,
                    inactiveTrackColor = colors.textSecondary.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0 lx (Night)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = colors.textSecondary
                )
                Text(
                    text = "500 lx (Twilight)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = colors.textSecondary
                )
                Text(
                    text = "1500 lx (Day)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Manual Shake Trigger Button
            Button(
                onClick = onTriggerShake,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.orbPrimary,
                    contentColor = colors.bgTop
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Simulate Shake",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "Simulasikan Shake / Guncangan Kuat",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 12.sp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Compose Previews (tampil di panel Preview Android Studio: Split / Design)
// ---------------------------------------------------------------------------

@Composable
private fun PreviewContent(lux: Float, mode: OrbThemeMode) {
    SensoryOrbTheme(mode = mode) {
        MainScreen(
            uiState = SensorUiState(
                motion = MotionData(x = 1.2f, y = 9.4f, z = 1.1f, shakeCount = 0),
                light = LightData(lux = lux, mode = mode, isAvailable = true),
                compass = CompassData(azimuthDegrees = 128f, cardinalDirection = "SE"),
                isSimulationActive = true,
                simulatedLux = lux
            ),
            onToggleSimulation = {},
            onSimulateLuxChange = {},
            onTriggerShake = {}
        )
    }
}

@Preview(name = "Day Mode", showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenDayPreview() {
    PreviewContent(lux = 1400f, mode = OrbThemeMode.DAY)
}
