package com.sensoryorb.app.ui.theme

import androidx.compose.ui.graphics.Color

// Shared / Accent Colors
val NeonCyan = Color(0xFF00F5FF)
val NeonMagenta = Color(0xFFFF2BD6)
val ElectricBlue = Color(0xFF00D2FF)
val DeepViolet = Color(0xFF8A2BE2)
val CoralPink = Color(0xFFFF6B6B)
val SunburstPeach = Color(0xFFFF9E79)

// Night Theme Palette (< 20 lux)
val NightBgTop = Color(0xFF0A0E27)
val NightBgBottom = Color(0xFF1A1040)
val NightOrbPrimary = Color(0xFF00F5FF)
val NightOrbSecondary = Color(0xFFFF2BD6)
val NightCardBg = Color(0x9912173E)
val NightCardBorder = Color(0x6600F5FF)
val NightTextPrimary = Color(0xFFFFFFFF)
val NightTextSecondary = Color(0xFF8E9CBF)

// Twilight Theme Palette (20 – 1000 lux)
val TwilightBgTop = Color(0xFF2D1B69)
val TwilightBgBottom = Color(0xFF11998E)
val TwilightOrbPrimary = Color(0xFFA855F7)
val TwilightOrbSecondary = Color(0xFF38BDF8)
val TwilightCardBg = Color(0x991F184B)
val TwilightCardBorder = Color(0x66A855F7)
val TwilightTextPrimary = Color(0xFFFFFFFF)
val TwilightTextSecondary = Color(0xFFC7D2FE)

// Day Theme Palette (> 1000 lux)
val DayBgTop = Color(0xFFFFF4E0)
val DayBgBottom = Color(0xFFBFE3FF)
val DayOrbPrimary = Color(0xFFFF6B6B)
val DayOrbSecondary = Color(0xFFFF9E79)
val DayCardBg = Color(0xCCFFFFFF)
val DayCardBorder = Color(0x66FF6B6B)
val DayTextPrimary = Color(0xFF0F172A) // High contrast slate dark
val DayTextSecondary = Color(0xFF475569)

data class SensoryOrbColors(
    val bgTop: Color,
    val bgBottom: Color,
    val orbPrimary: Color,
    val orbSecondary: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val compassAccent: Color
)
