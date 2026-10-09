package com.sensoryorb.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import com.sensoryorb.app.sensor.OrbThemeMode

val LocalSensoryColors = compositionLocalOf {
    SensoryOrbColors(
        bgTop = NightBgTop,
        bgBottom = NightBgBottom,
        orbPrimary = NightOrbPrimary,
        orbSecondary = NightOrbSecondary,
        cardBg = NightCardBg,
        cardBorder = NightCardBorder,
        textPrimary = NightTextPrimary,
        textSecondary = NightTextSecondary,
        compassAccent = NeonCyan
    )
}

@Composable
fun rememberAnimatedSensoryColors(mode: OrbThemeMode): SensoryOrbColors {
    val targetColors = when (mode) {
        OrbThemeMode.NIGHT -> SensoryOrbColors(
            bgTop = NightBgTop,
            bgBottom = NightBgBottom,
            orbPrimary = NightOrbPrimary,
            orbSecondary = NightOrbSecondary,
            cardBg = NightCardBg,
            cardBorder = NightCardBorder,
            textPrimary = NightTextPrimary,
            textSecondary = NightTextSecondary,
            compassAccent = NeonCyan
        )
        OrbThemeMode.TWILIGHT -> SensoryOrbColors(
            bgTop = TwilightBgTop,
            bgBottom = TwilightBgBottom,
            orbPrimary = TwilightOrbPrimary,
            orbSecondary = TwilightOrbSecondary,
            cardBg = TwilightCardBg,
            cardBorder = TwilightCardBorder,
            textPrimary = TwilightTextPrimary,
            textSecondary = TwilightTextSecondary,
            compassAccent = TwilightOrbSecondary
        )
        OrbThemeMode.DAY -> SensoryOrbColors(
            bgTop = DayBgTop,
            bgBottom = DayBgBottom,
            orbPrimary = DayOrbPrimary,
            orbSecondary = DayOrbSecondary,
            cardBg = DayCardBg,
            cardBorder = DayCardBorder,
            textPrimary = DayTextPrimary,
            textSecondary = DayTextSecondary,
            compassAccent = DayOrbPrimary
        )
    }

    val animSpec = tween<androidx.compose.ui.graphics.Color>(
        durationMillis = 700,
        easing = FastOutSlowInEasing
    )

    val bgTop = animateColorAsState(targetColors.bgTop, animSpec, label = "bgTop").value
    val bgBottom = animateColorAsState(targetColors.bgBottom, animSpec, label = "bgBottom").value
    val orbPrimary = animateColorAsState(targetColors.orbPrimary, animSpec, label = "orbPrimary").value
    val orbSecondary = animateColorAsState(targetColors.orbSecondary, animSpec, label = "orbSecondary").value
    val cardBg = animateColorAsState(targetColors.cardBg, animSpec, label = "cardBg").value
    val cardBorder = animateColorAsState(targetColors.cardBorder, animSpec, label = "cardBorder").value
    val textPrimary = animateColorAsState(targetColors.textPrimary, animSpec, label = "textPrimary").value
    val textSecondary = animateColorAsState(targetColors.textSecondary, animSpec, label = "textSecondary").value
    val compassAccent = animateColorAsState(targetColors.compassAccent, animSpec, label = "compassAccent").value

    return SensoryOrbColors(
        bgTop = bgTop,
        bgBottom = bgBottom,
        orbPrimary = orbPrimary,
        orbSecondary = orbSecondary,
        cardBg = cardBg,
        cardBorder = cardBorder,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        compassAccent = compassAccent
    )
}

@Composable
fun SensoryOrbTheme(
    mode: OrbThemeMode,
    content: @Composable () -> Unit
) {
    val animatedColors = rememberAnimatedSensoryColors(mode)

    val colorScheme = if (mode == OrbThemeMode.DAY) {
        lightColorScheme(
            primary = animatedColors.orbPrimary,
            surface = animatedColors.cardBg,
            onSurface = animatedColors.textPrimary,
            onBackground = animatedColors.textPrimary
        )
    } else {
        darkColorScheme(
            primary = animatedColors.orbPrimary,
            surface = animatedColors.cardBg,
            onSurface = animatedColors.textPrimary,
            onBackground = animatedColors.textPrimary
        )
    }

    CompositionLocalProvider(LocalSensoryColors provides animatedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
