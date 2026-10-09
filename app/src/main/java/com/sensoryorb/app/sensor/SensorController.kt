package com.sensoryorb.app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * SensorController encapsulates Android SensorManager operations and acts as the Single Source
 * of Truth for real-time sensor streams across 3 distinct sensor categories:
 * 1. MOTION: TYPE_ACCELEROMETER
 * 2. POSITION: TYPE_ACCELEROMETER + TYPE_MAGNETIC_FIELD (Compass azimuth)
 * 3. ENVIRONMENT: TYPE_LIGHT
 */
class SensorController(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Hardware Sensor references
    val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    // Availability flags
    val isMotionAvailable: Boolean = accelerometer != null
    val isCompassAvailable: Boolean = accelerometer != null && magnetometer != null
    val isLightAvailable: Boolean = lightSensor != null

    // StateFlow streams exposed to ViewModel
    private val _motionState = MutableStateFlow(MotionData())
    val motionState: StateFlow<MotionData> = _motionState.asStateFlow()

    private val _lightState = MutableStateFlow(
        LightData(
            lux = 50f,
            mode = OrbThemeMode.TWILIGHT,
            isAvailable = isLightAvailable
        )
    )
    val lightState: StateFlow<LightData> = _lightState.asStateFlow()

    private val _compassState = MutableStateFlow(
        CompassData(
            azimuthDegrees = 0f,
            cardinalDirection = "N",
            isAvailable = isCompassAvailable
        )
    )
    val compassState: StateFlow<CompassData> = _compassState.asStateFlow()

    private val _shakeEvent = MutableStateFlow(0L)
    val shakeEvent: StateFlow<Long> = _shakeEvent.asStateFlow()

    // Internal sensor calculation buffers
    private val gravityValues = FloatArray(3)
    private val geomagneticValues = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Low-pass filter smoothing coefficient (0.0 = freezes, 1.0 = raw unbuffered)
    private val lowPassAlpha = 0.15f

    // Shake detection thresholds
    private var lastShakeTime = 0L
    private val shakeCooldownMs = 800L
    private val shakeThreshold = 13.5f // Delta beyond Earth 9.8 m/s²
    private var shakeCounter = 0

    private var isRunning = false

    /**
     * =========================================================================
     * [LIFECYCLE PRESENTATION POINT - SENSOR REGISTRATION]
     * Dipanggil pada MainActivity onResume().
     *
     * 1. Accelerometer & Magnetometer diregistrasikan dengan SENSOR_DELAY_GAME (~20ms / 50Hz)
     *    agar pergerakan bola (orb physics) dan rotasi kompas terasa sangat mulus, responsif,
     *    serta bebas stutter pada layar 60Hz/120Hz.
     * 2. Light Sensor diregistrasikan dengan SENSOR_DELAY_NORMAL (~200ms / 5Hz)
     *    karena perubahan intensitas cahaya ambien bersifat lambat sehingga tidak memerlukan
     *    frekuensi sampling tinggi, yang bertujuan untuk efisiensi baterai.
     * =========================================================================
     */
    fun start() {
        if (isRunning) return
        isRunning = true

        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }

        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }

        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    /**
     * =========================================================================
     * [LIFECYCLE PRESENTATION POINT - SENSOR UNREGISTRATION]
     * Dipanggil pada MainActivity onPause().
     *
     * Wajib melepaskan (unregister) listener saat activity tidak aktif/masuk background.
     * Jika tidak di-unregister, hardware chip sensor dan driver HAL Android akan terus
     * berjalan di latar belakang, memicu CPU interrupts secara kontinyu dan menyebabkan
     * battery drain (baterai cepat habis) yang signifikan serta panas pada perangkat.
     * =========================================================================
     */
    fun stop() {
        if (!isRunning) return
        isRunning = false
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                handleAccelerometer(event.values)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                handleMagnetometer(event.values)
            }
            Sensor.TYPE_LIGHT -> {
                handleLight(event.values[0])
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used for direct UI rendering
    }

    /**
     * Memproses data accelerometer untuk pergerakan orb dan deteksi shake.
     */
    private fun handleAccelerometer(values: FloatArray) {
        val x = values[0]
        val y = values[1]
        val z = values[2]

        // Low-pass filter untuk memisahkan gravitasi statis (berguna juga untuk kompas)
        gravityValues[0] = gravityValues[0] + lowPassAlpha * (x - gravityValues[0])
        gravityValues[1] = gravityValues[1] + lowPassAlpha * (y - gravityValues[1])
        gravityValues[2] = gravityValues[2] + lowPassAlpha * (z - gravityValues[2])
        hasGravity = true

        val magnitude = sqrt(x * x + y * y + z * z)
        val netAcceleration = abs(magnitude - SensorManager.GRAVITY_EARTH)

        // Deteksi guncangan kuat (Shake detection)
        val currentTime = System.currentTimeMillis()
        if (netAcceleration > shakeThreshold && (currentTime - lastShakeTime > shakeCooldownMs)) {
            lastShakeTime = currentTime
            shakeCounter++
            _shakeEvent.value = currentTime
        }

        _motionState.value = MotionData(
            x = x,
            y = y,
            z = z,
            magnitude = magnitude,
            shakeCount = shakeCounter
        )

        updateCompassOrientation()
    }

    /**
     * Memproses data magnetometer dan menerapkan low-pass filter.
     */
    private fun handleMagnetometer(values: FloatArray) {
        geomagneticValues[0] = geomagneticValues[0] + lowPassAlpha * (values[0] - geomagneticValues[0])
        geomagneticValues[1] = geomagneticValues[1] + lowPassAlpha * (values[1] - geomagneticValues[1])
        geomagneticValues[2] = geomagneticValues[2] + lowPassAlpha * (values[2] - geomagneticValues[2])
        hasGeomagnetic = true

        updateCompassOrientation()
    }

    /**
     * Menghitung orientasi kompas (Azimuth) dari matriks rotasi gravitasi & medan magnet.
     */
    private fun updateCompassOrientation() {
        if (!hasGravity || !hasGeomagnetic) return

        val success = SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            gravityValues,
            geomagneticValues
        )

        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val azimuthRad = orientationAngles[0]
            val azimuthDeg = ((Math.toDegrees(azimuthRad.toDouble()) + 360) % 360).toFloat()
            val cardinal = getCardinalDirection(azimuthDeg)

            _compassState.value = CompassData(
                azimuthDegrees = azimuthDeg,
                cardinalDirection = cardinal,
                isAvailable = isCompassAvailable
            )
        }
    }

    /**
     * Memproses data intensitas cahaya dalam satuan lux dan menentukan mode tema.
     */
    private fun handleLight(lux: Float) {
        val mode = when {
            lux < 20f -> OrbThemeMode.NIGHT
            lux <= 1000f -> OrbThemeMode.TWILIGHT
            else -> OrbThemeMode.DAY
        }

        _lightState.value = LightData(
            lux = lux,
            mode = mode,
            isAvailable = true
        )
    }

    /**
     * Menghasilkan teks arah mata angin 8-poin (N, NE, E, SE, S, SW, W, NW)
     */
    private fun getCardinalDirection(degrees: Float): String {
        val cardinals = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = (((degrees + 22.5f) % 360) / 45f).toInt().coerceIn(0, 7)
        return cardinals[index]
    }
}
