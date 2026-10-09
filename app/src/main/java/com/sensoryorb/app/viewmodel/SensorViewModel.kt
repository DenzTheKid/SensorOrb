package com.sensoryorb.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sensoryorb.app.sensor.CompassData
import com.sensoryorb.app.sensor.LightData
import com.sensoryorb.app.sensor.MotionData
import com.sensoryorb.app.sensor.OrbThemeMode
import com.sensoryorb.app.sensor.SensorController
import com.sensoryorb.app.sensor.SensorUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Internal simulation settings for fallback and emulator testing.
 */
data class SimulationSettings(
    val isActive: Boolean = false,
    val lux: Float = 50f,
    val manualShakeTime: Long = 0L,
    val manualShakeCount: Int = 0
)

class SensorViewModel(application: Application) : AndroidViewModel(application) {

    val sensorController = SensorController(application)

    private val _simulationSettings = MutableStateFlow(
        SimulationSettings(
            // Auto-activate simulation mode if hardware light sensor is missing
            isActive = !sensorController.isLightAvailable,
            lux = 50f
        )
    )

    /**
     * Unified UI state stream combining raw sensor readings and simulation overrides.
     */
    val uiState: StateFlow<SensorUiState> = combine(
        sensorController.motionState,
        sensorController.lightState,
        sensorController.compassState,
        sensorController.shakeEvent,
        _simulationSettings
    ) { motion, light, compass, shakeTime, sim ->
        val effectiveLux = if (sim.isActive) sim.lux else light.lux
        val effectiveMode = when {
            effectiveLux < 20f -> OrbThemeMode.NIGHT
            effectiveLux <= 1000f -> OrbThemeMode.TWILIGHT
            else -> OrbThemeMode.DAY
        }
        val effectiveLight = if (sim.isActive) {
            LightData(lux = effectiveLux, mode = effectiveMode, isAvailable = true)
        } else {
            light.copy(mode = effectiveMode)
        }

        val effectiveMotion = motion.copy(
            shakeCount = motion.shakeCount + sim.manualShakeCount
        )

        SensorUiState(
            motion = effectiveMotion,
            light = effectiveLight,
            compass = compass,
            isMotionAvailable = sensorController.isMotionAvailable,
            isLightAvailable = sensorController.isLightAvailable,
            isCompassAvailable = sensorController.isCompassAvailable,
            isSimulationActive = sim.isActive,
            simulatedLux = sim.lux,
            lastShakeTimestamp = maxOf(shakeTime, sim.manualShakeTime)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SensorUiState(
            isMotionAvailable = sensorController.isMotionAvailable,
            isLightAvailable = sensorController.isLightAvailable,
            isCompassAvailable = sensorController.isCompassAvailable,
            isSimulationActive = !sensorController.isLightAvailable
        )
    )

    /**
     * Start sensor listeners (delegated from MainActivity onResume)
     */
    fun startSensors() {
        sensorController.start()
    }

    /**
     * Stop sensor listeners (delegated from MainActivity onPause)
     */
    fun stopSensors() {
        sensorController.stop()
    }

    /**
     * Toggle emulator simulation mode.
     */
    fun toggleSimulation() {
        _simulationSettings.value = _simulationSettings.value.copy(
            isActive = !_simulationSettings.value.isActive
        )
    }

    /**
     * Adjust simulated ambient light level (0..2000 lux).
     */
    fun setSimulatedLux(lux: Float) {
        _simulationSettings.value = _simulationSettings.value.copy(
            lux = lux,
            isActive = true
        )
    }

    /**
     * Trigger simulated shake explosion.
     */
    fun triggerManualShake() {
        _simulationSettings.value = _simulationSettings.value.copy(
            manualShakeTime = System.currentTimeMillis(),
            manualShakeCount = _simulationSettings.value.manualShakeCount + 1
        )
    }
}
