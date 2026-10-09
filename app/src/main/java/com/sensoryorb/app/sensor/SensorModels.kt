package com.sensoryorb.app.sensor

/**
 * Three environmental modes triggered by ambient light level (lux)
 * - NIGHT: < 20 lux
 * - TWILIGHT: 20 to 1000 lux
 * - DAY: > 1000 lux
 */
enum class OrbThemeMode(val displayName: String, val thresholdRange: String) {
    NIGHT("Night Mode", "< 20 lux"),
    TWILIGHT("Twilight", "20 – 1000 lux"),
    DAY("Day Mode", "> 1000 lux")
}

/**
 * Data model for accelerometer motion readings.
 */
data class MotionData(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 9.8f,
    val magnitude: Float = 9.8f,
    val shakeCount: Int = 0
)

/**
 * Data model for light sensor readings.
 */
data class LightData(
    val lux: Float = 50f,
    val mode: OrbThemeMode = OrbThemeMode.TWILIGHT,
    val isAvailable: Boolean = true
)

/**
 * Data model for orientation / compass readings.
 */
data class CompassData(
    val azimuthDegrees: Float = 0f,
    val cardinalDirection: String = "N",
    val isAvailable: Boolean = true
)

/**
 * Consolidated UI state observed by Jetpack Compose.
 */
data class SensorUiState(
    val motion: MotionData = MotionData(),
    val light: LightData = LightData(),
    val compass: CompassData = CompassData(),
    val isMotionAvailable: Boolean = true,
    val isLightAvailable: Boolean = true,
    val isCompassAvailable: Boolean = true,
    val isSimulationActive: Boolean = false,
    val simulatedLux: Float = 50f,
    val lastShakeTimestamp: Long = 0L
)
