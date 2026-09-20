package com.dailycompany.ui.theme

import androidx.compose.ui.graphics.Color

// Light
val md_light_primary = Color(0xFF8F4B2A)
val md_light_onPrimary = Color(0xFFFFFFFF)
val md_light_primaryContainer = Color(0xFFFFDBCB)
val md_light_onPrimaryContainer = Color(0xFF360E00)
val md_light_secondary = Color(0xFF52634F)
val md_light_onSecondary = Color(0xFFFFFFFF)
val md_light_secondaryContainer = Color(0xFFD5E8CF)
val md_light_onSecondaryContainer = Color(0xFF101F10)
val md_light_tertiary = Color(0xFF7A5900)
val md_light_onTertiary = Color(0xFFFFFFFF)
val md_light_tertiaryContainer = Color(0xFFFFDF9B)
val md_light_onTertiaryContainer = Color(0xFF261A00)
val md_light_error = Color(0xFFBA1A1A)
val md_light_onError = Color(0xFFFFFFFF)
val md_light_errorContainer = Color(0xFFFFDAD6)
val md_light_onErrorContainer = Color(0xFF410002)
val md_light_background = Color(0xFFFFF8F5)
val md_light_onBackground = Color(0xFF221A16)
val md_light_surface = Color(0xFFFFF8F5)
val md_light_onSurface = Color(0xFF221A16)
val md_light_surfaceVariant = Color(0xFFF4DED4)
val md_light_onSurfaceVariant = Color(0xFF53433C)
val md_light_outline = Color(0xFF85736B)
val md_light_outlineVariant = Color(0xFFD7C2B9)
val md_light_surfaceContainer = Color(0xFFFCECE5)
val md_light_surfaceContainerHigh = Color(0xFFF7E6DF)
val md_light_inverseSurface = Color(0xFF382E2A)
val md_light_inverseOnSurface = Color(0xFFFFEDE7)
val md_light_scrim = Color(0xFF000000)

// Dark
val md_dark_primary = Color(0xFFFFB693)
val md_dark_onPrimary = Color(0xFF552100)
val md_dark_primaryContainer = Color(0xFF733314)
val md_dark_onPrimaryContainer = Color(0xFFFFDBCB)
val md_dark_secondary = Color(0xFFB9CCB4)
val md_dark_onSecondary = Color(0xFF253423)
val md_dark_secondaryContainer = Color(0xFF3B4B38)
val md_dark_onSecondaryContainer = Color(0xFFD5E8CF)
val md_dark_tertiary = Color(0xFFEFC04E)
val md_dark_onTertiary = Color(0xFF402D00)
val md_dark_tertiaryContainer = Color(0xFF5C4300)
val md_dark_onTertiaryContainer = Color(0xFFFFDF9B)
val md_dark_error = Color(0xFFFFB4AB)
val md_dark_onError = Color(0xFF690005)
val md_dark_errorContainer = Color(0xFF93000A)
val md_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_dark_background = Color(0xFF1A120E)
val md_dark_onBackground = Color(0xFFF1DFD8)
val md_dark_surface = Color(0xFF1A120E)
val md_dark_onSurface = Color(0xFFF1DFD8)
val md_dark_surfaceVariant = Color(0xFF53433C)
val md_dark_onSurfaceVariant = Color(0xFFD7C2B9)
val md_dark_outline = Color(0xFF9F8D85)
val md_dark_outlineVariant = Color(0xFF53433C)
val md_dark_surfaceContainer = Color(0xFF271E19)
val md_dark_surfaceContainerHigh = Color(0xFF322823)
val md_dark_inverseSurface = Color(0xFFF1DFD8)
val md_dark_inverseOnSurface = Color(0xFF382E2A)
val md_dark_scrim = Color(0xFF000000)

/** Cores semânticas da app (fora do colorScheme do M3). */
data class AppColors(
    val overdue: Color,
    val done: Color,
    val eventDot: Color,
    val weatherWarm: Color,
    val weatherCold: Color,
)

val LightAppColors = AppColors(
    overdue = Color(0xFFB3261E),
    done = Color(0xFF3E6B46),
    eventDot = md_light_tertiary,
    weatherWarm = Color(0xFFD97706),
    weatherCold = Color(0xFF3E6B8A),
)

val DarkAppColors = AppColors(
    overdue = Color(0xFFFFB4AB),
    done = Color(0xFF9FCFA6),
    eventDot = md_dark_tertiary,
    weatherWarm = Color(0xFFF0B24A),
    weatherCold = Color(0xFF8FBEDB),
)