package com.sensoryorb.app

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.sensoryorb.app.ui.screens.MainScreen
import com.sensoryorb.app.ui.theme.SensoryOrbTheme
import com.sensoryorb.app.viewmodel.SensorViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SensorViewModel by viewModels()

    /**
     * =========================================================================
     * [LIFECYCLE PRESENTATION POINT - onResume]
     * Dipanggil setiap kali Activity masuk ke foreground dan siap berinteraksi.
     * Mendaftarkan SensorEventListener (startSensors) agar sensor hardware aktif:
     * - Accelerometer & Magnetometer dengan SENSOR_DELAY_GAME
     * - Light Sensor dengan SENSOR_DELAY_NORMAL
     * Menjamin UI langsung responsif saat aplikasi dibuka atau kembali dari background.
     * =========================================================================
     */
    override fun onResume() {
        super.onResume()
        viewModel.startSensors()
    }

    /**
     * =========================================================================
     * [LIFECYCLE PRESENTATION POINT - onPause]
     * Dipanggil segera saat Activity kehilangan fokus (misal user menekan Home,
     * menerima panggilan telepon, atau beralih ke aplikasi lain).
     * Wajib menghentikan (stopSensors) pendaftaran SensorEventListener.
     * Mencegah pemborosan daya baterai (battery drain) dan penggunaan CPU di background.
     * =========================================================================
     */
    override fun onPause() {
        super.onPause()
        viewModel.stopSensors()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Collect StateFlow using modern Lifecycle-aware API
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // Trigger physical haptic vibration and audio sound upon shake explosion
            LaunchedEffect(uiState.lastShakeTimestamp) {
                if (uiState.lastShakeTimestamp > 0L) {
                    performShakeHaptic()
                    playShakeExplosionSound()
                }
            }

            // Play futuristic chime when ambient light theme changes
            LaunchedEffect(uiState.light.mode) {
                playThemeChimeSound()
            }

            // Outer Theme Wrapper dynamically responding to Ambient Light Mode
            SensoryOrbTheme(mode = uiState.light.mode) {
                MainScreen(
                    uiState = uiState,
                    onToggleSimulation = { viewModel.toggleSimulation() },
                    onSimulateLuxChange = { lux -> viewModel.setSimulatedLux(lux) },
                    onTriggerShake = { viewModel.triggerManualShake() }
                )
            }
        }
    }

    /**
     * Memainkan efek suara ledakan sci-fi sintetis saat orb meledak karena shake.
     */
    private fun playShakeExplosionSound() {
        Thread {
            try {
                val sampleRate = 44100
                val duration = 0.28
                val numSamples = (duration * sampleRate).toInt()
                val samples = ShortArray(numSamples)
                // Frekuensi sweep menurun dari 850Hz ke 120Hz (efek ledakan laser sci-fi)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = 850.0 * (1.0 - t / duration) + 120.0
                    val angle = 2.0 * Math.PI * freq * t
                    val envelope = (1.0 - t / duration)
                    samples[i] = (Math.sin(angle) * envelope * Short.MAX_VALUE * 0.75).toInt().toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(numSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, numSamples)
                audioTrack.play()
            } catch (_: Exception) {
                // Ignore gracefully if audio output is unavailable
            }
        }.start()
    }

    /**
     * Memainkan efek suara nada melodi (chime) futuristik saat tema berganti (Night/Twilight/Day).
     */
    private fun playThemeChimeSound() {
        Thread {
            try {
                val sampleRate = 44100
                val duration = 0.22
                val numSamples = (duration * sampleRate).toInt()
                val samples = ShortArray(numSamples)
                // Frekuensi nada ascending lembut dari C5 (523Hz) ke E5 (659Hz)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = 523.25 + (136.0 * (t / duration))
                    val angle = 2.0 * Math.PI * freq * t
                    val envelope = Math.sin(Math.PI * (t / duration))
                    samples[i] = (Math.sin(angle) * envelope * Short.MAX_VALUE * 0.55).toInt().toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(numSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, numSamples)
                audioTrack.play()
            } catch (_: Exception) {
                // Ignore gracefully if audio output is unavailable
            }
        }.start()
    }

    /**
     * Memberikan umpan balik getaran taktil (haptic feedback) saat orb meledak karena shake.
     */
    private fun performShakeHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(120)
                }
            }
        } catch (_: Exception) {
            // Gracefully ignore if device lacks vibration hardware
        }
    }
}
