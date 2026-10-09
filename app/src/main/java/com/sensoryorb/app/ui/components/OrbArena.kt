package com.sensoryorb.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.sensoryorb.app.sensor.MotionData
import com.sensoryorb.app.sensor.OrbThemeMode
import com.sensoryorb.app.ui.theme.LocalSensoryColors
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class TrailPoint(
    val offset: Offset,
    var alpha: Float,
    val size: Float
)

private data class ExplosionParticle(
    var pos: Offset,
    val vel: Offset,
    val color: Color,
    val radius: Float,
    var alpha: Float = 1f
)

/**
 * Main Interactive Canvas Arena containing:
 * 1. Compass Ring HUD
 * 2. Real-time physics glowing Orb moving via accelerometer tilt / drag
 * 3. Neon comet trail behind fast movements
 * 4. Shockwave ripple & particle explosion upon shake detection
 */
@Composable
fun OrbArena(
    motion: MotionData,
    azimuthDegrees: Float,
    themeMode: OrbThemeMode,
    lastShakeTimestamp: Long,
    modifier: Modifier = Modifier
) {
    val colors = LocalSensoryColors.current
    val density = LocalDensity.current

    val orbBaseRadiusPx = with(density) { 34.dp.toPx() }

    // Pulsing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "orbPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Physics simulation state
    var orbPosition by remember { mutableStateOf(Offset.Zero) }
    var orbVelocity by remember { mutableStateOf(Offset.Zero) }
    var arenaRadiusPx by remember { mutableFloatStateOf(300f) }

    // Visual trails and particle explosion states
    val trailPoints = remember { mutableStateListOf<TrailPoint>() }
    val explosionParticles = remember { mutableStateListOf<ExplosionParticle>() }
    val rippleProgress = remember { Animatable(1f) }

    // Shake explosion trigger
    LaunchedEffect(lastShakeTimestamp) {
        if (lastShakeTimestamp > 0L) {
            // Trigger shockwave ripple
            launch {
                rippleProgress.snapTo(0f)
                rippleProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(750, easing = LinearEasing)
                )
            }

            // Burst particles outward
            explosionParticles.clear()
            val rnd = Random(lastShakeTimestamp)
            val currentPos = orbPosition
            for (i in 0 until 28) {
                val angle = (i * (360f / 28f) + rnd.nextFloat() * 10f) * (PI / 180f)
                val speed = 350f + rnd.nextFloat() * 700f
                val vel = Offset((cos(angle) * speed).toFloat(), (sin(angle) * speed).toFloat())
                val particleColor = if (i % 2 == 0) colors.orbPrimary else colors.orbSecondary
                explosionParticles.add(
                    ExplosionParticle(
                        pos = currentPos,
                        vel = vel,
                        color = particleColor,
                        radius = 4f + rnd.nextFloat() * 8f
                    )
                )
            }

            // Spring return orb smoothly to center
            val bounceSpring = spring<Float>(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
            orbVelocity = Offset.Zero
            val startX = orbPosition.x
            val startY = orbPosition.y
            val animX = Animatable(startX)
            val animY = Animatable(startY)
            launch {
                animX.animateTo(0f, bounceSpring) {
                    orbPosition = Offset(this.value, orbPosition.y)
                }
            }
            launch {
                animY.animateTo(0f, bounceSpring) {
                    orbPosition = Offset(orbPosition.x, this.value)
                }
            }
        }
    }

    // Frame-rate physics loop for natural tilt dynamics
    LaunchedEffect(motion) {
        var lastNano = withFrameNanos { it }
        while (isActive) {
            val nowNano = withFrameNanos { it }
            val dt = ((nowNano - lastNano) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
            lastNano = nowNano

            // Tilt force mapping:
            // Tilting phone right (negative x) accelerates orb right (+x)
            // Tilting phone forward/top-down (negative y) accelerates orb up (-y)
            val tiltForceX = -motion.x * 2400f
            val tiltForceY = motion.y * 2400f

            orbVelocity = (orbVelocity + Offset(tiltForceX, tiltForceY) * dt) * 0.94f
            var nextPos = orbPosition + orbVelocity * dt

            // Circular arena boundary clamp with bouncy wall restitution
            val maxRadius = (arenaRadiusPx - orbBaseRadiusPx).coerceAtLeast(10f)
            val distance = nextPos.getDistance()
            if (distance > maxRadius) {
                val normal = nextPos / distance
                nextPos = normal * maxRadius
                // Invert and dampen velocity vector along wall normal
                val dot = orbVelocity.x * normal.x + orbVelocity.y * normal.y
                orbVelocity = (orbVelocity - normal * (1.6f * dot)) * 0.65f
            }
            orbPosition = nextPos

            // Record glowing trail points when moving with noticeable velocity
            val speed = orbVelocity.getDistance()
            if (speed > 40f) {
                trailPoints.add(
                    TrailPoint(
                        offset = orbPosition,
                        alpha = 0.85f,
                        size = orbBaseRadiusPx * (0.8f.coerceAtLeast(speed / 1000f).coerceAtMost(1f))
                    )
                )
            }

            // Update trail decay
            val trailIterator = trailPoints.iterator()
            while (trailIterator.hasNext()) {
                val pt = trailIterator.next()
                pt.alpha -= dt * 2.2f
                if (pt.alpha <= 0.02f) {
                    trailIterator.remove()
                }
            }

            // Update explosion particles
            if (explosionParticles.isNotEmpty()) {
                val pIterator = explosionParticles.iterator()
                while (pIterator.hasNext()) {
                    val p = pIterator.next()
                    p.pos = p.pos + p.vel * dt
                    p.alpha -= dt * 1.5f
                    if (p.alpha <= 0.02f) {
                        pIterator.remove()
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .padding(16.dp)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        // Rotating Compass Ring surrounding the arena
        CompassRing(
            azimuthDegrees = azimuthDegrees,
            modifier = Modifier.fillMaxSize()
        )

        // Interactive Canvas for Orb, Trails, Ripple & Particles
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    // Touch/drag fallback support for desktop emulator testing
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            var dragPos = orbPosition + dragAmount
                            val maxRadius = (arenaRadiusPx - orbBaseRadiusPx).coerceAtLeast(10f)
                            if (dragPos.getDistance() > maxRadius) {
                                dragPos = (dragPos / dragPos.getDistance()) * maxRadius
                            }
                            orbPosition = dragPos
                            orbVelocity = dragAmount * 15f
                        }
                    )
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = minOf(size.width, size.height) / 2f - 24f
            arenaRadiusPx = outerRadius

            val orbCenter = center + orbPosition

            // 1. Draw Shockwave Ripple on Shake
            if (rippleProgress.value < 1f) {
                val currentRippleRadius = rippleProgress.value * outerRadius * 1.1f
                val rippleAlpha = (1f - rippleProgress.value).coerceIn(0f, 1f)
                drawCircle(
                    color = colors.orbPrimary.copy(alpha = rippleAlpha * 0.75f),
                    radius = currentRippleRadius,
                    center = center + orbPosition,
                    style = Stroke(width = 6f * (1f - rippleProgress.value) + 2f)
                )
            }

            // 2. Draw Burst Particles
            for (p in explosionParticles) {
                drawCircle(
                    color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                    radius = p.radius,
                    center = center + p.pos
                )
            }

            // 3. Draw Luminous Comet Trail
            for (pt in trailPoints) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colors.orbSecondary.copy(alpha = pt.alpha * 0.6f),
                            colors.orbPrimary.copy(alpha = pt.alpha * 0.2f),
                            Color.Transparent
                        ),
                        center = center + pt.offset,
                        radius = pt.size
                    ),
                    radius = pt.size,
                    center = center + pt.offset,
                    blendMode = BlendMode.Plus
                )
            }

            // 4. Glow Multipliers based on Light sensor theme
            val glowIntensity = when (themeMode) {
                OrbThemeMode.NIGHT -> 1.4f   // Maximum neon glow in darkness
                OrbThemeMode.TWILIGHT -> 1.0f
                OrbThemeMode.DAY -> 0.75f    // Cleaner pastel contrast in bright day
            }

            val outerGlowRadius = orbBaseRadiusPx * 2.6f * pulseScale * glowIntensity
            val midGlowRadius = orbBaseRadiusPx * 1.6f * pulseScale

            // Outermost soft neon glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.orbPrimary.copy(alpha = 0.45f * glowIntensity),
                        colors.orbSecondary.copy(alpha = 0.20f * glowIntensity),
                        Color.Transparent
                    ),
                    center = orbCenter,
                    radius = outerGlowRadius
                ),
                radius = outerGlowRadius,
                center = orbCenter,
                blendMode = BlendMode.Plus
            )

            // Medium radiant glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.orbSecondary.copy(alpha = 0.65f),
                        colors.orbPrimary.copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    center = orbCenter,
                    radius = midGlowRadius
                ),
                radius = midGlowRadius,
                center = orbCenter,
                blendMode = BlendMode.Plus
            )

            // Core Orb body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        colors.orbPrimary,
                        colors.orbSecondary
                    ),
                    center = orbCenter - Offset(orbBaseRadiusPx * 0.25f, orbBaseRadiusPx * 0.25f),
                    radius = orbBaseRadiusPx
                ),
                radius = orbBaseRadiusPx,
                center = orbCenter
            )

            // Specular Highlight reflection dot
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = orbBaseRadiusPx * 0.22f,
                center = orbCenter - Offset(orbBaseRadiusPx * 0.32f, orbBaseRadiusPx * 0.32f)
            )
        }
    }
}
