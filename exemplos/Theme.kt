package com.dailycompany.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

enum class ThemeMode { LIGHT, DARK, SYSTEM }

private val LightScheme = lightColorScheme(
    primary = md_light_primary, onPrimary = md_light_onPrimary,
    primaryContainer = md_light_primaryContainer, onPrimaryContainer = md_light_onPrimaryContainer,
    secondary = md_light_secondary, onSecondary = md_light_onSecondary,
    secondaryContainer = md_light_secondaryContainer, onSecondaryContainer = md_light_onSecondaryContainer,
    tertiary = md_light_tertiary, onTertiary = md_light_onTertiary,
    tertiaryContainer = md_light_tertiaryContainer, onTertiaryContainer = md_light_onTertiaryContainer,
    error = md_light_error, onError = md_light_onError,
    errorContainer = md_light_errorContainer, onErrorContainer = md_light_onErrorContainer,
    background = md_light_background, onBackground = md_light_onBackground,
    surface = md_light_surface, onSurface = md_light_onSurface,
    surfaceVariant = md_light_surfaceVariant, onSurfaceVariant = md_light_onSurfaceVariant,
    outline = md_light_outline, outlineVariant = md_light_outlineVariant,
    surfaceContainer = md_light_surfaceContainer, surfaceContainerHigh = md_light_surfaceContainerHigh,
    inverseSurface = md_light_inverseSurface, inverseOnSurface = md_light_inverseOnSurface,
    scrim = md_light_scrim,
)

private val DarkScheme = darkColorScheme(
    primary = md_dark_primary, onPrimary = md_dark_onPrimary,
    primaryContainer = md_dark_primaryContainer, onPrimaryContainer = md_dark_onPrimaryContainer,
    secondary = md_dark_secondary, onSecondary = md_dark_onSecondary,
    secondaryContainer = md_dark_secondaryContainer, onSecondaryContainer = md_dark_onSecondaryContainer,
    tertiary = md_dark_tertiary, onTertiary = md_dark_onTertiary,
    tertiaryContainer = md_dark_tertiaryContainer, onTertiaryContainer = md_dark_onTertiaryContainer,
    error = md_dark_error, onError = md_dark_onError,
    errorContainer = md_dark_errorContainer, onErrorContainer = md_dark_onErrorContainer,
    background = md_dark_background, onBackground = md_dark_onBackground,
    surface = md_dark_surface, onSurface = md_dark_onSurface,
    surfaceVariant = md_dark_surfaceVariant, onSurfaceVariant = md_dark_onSurfaceVariant,
    outline = md_dark_outline, outlineVariant = md_dark_outlineVariant,
    surfaceContainer = md_dark_surfaceContainer, surfaceContainerHigh = md_dark_surfaceContainerHigh,
    inverseSurface = md_dark_inverseSurface, inverseOnSurface = md_dark_inverseOnSurface,
    scrim = md_dark_scrim,
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
val LocalSpacing = staticCompositionLocalOf { Spacing }
val LocalAppLocale = staticCompositionLocalOf { Locale.forLanguageTag(AppLanguage.DEFAULT.tag) }

/**
 * Tema da app: cor, tipografia, formas, língua e locale, tudo num sítio.
 *
 * `themeMode` e `language` vêm do DataStore (SettingsRepository) e são passados pela MainActivity.
 * Além do MaterialTheme, este composable:
 *  - escolhe o objeto de strings certo (LocalStrings),
 *  - força o Locale na Configuration, para que DatePicker, TimePicker e formatadores
 *    de data sigam a língua escolhida na app e não a do sistema.
 */
@Composable
fun DailyCompanyTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    language: AppLanguage = AppLanguage.DEFAULT,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkScheme
        else -> LightScheme
    }
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    val locale = remember(language) { Locale.forLanguageTag(language.tag) }
    val configuration = LocalConfiguration.current
    val localizedConfiguration = remember(configuration, locale) {
        android.content.res.Configuration(configuration).apply { setLocale(locale) }
    }
    val localizedContext = remember(localizedConfiguration) {
        context.createConfigurationContext(localizedConfiguration)
    }

    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalSpacing provides Spacing,
        LocalStrings provides stringsFor(language),
        LocalAppLanguage provides language,
        LocalAppLocale provides locale,
        LocalConfiguration provides localizedConfiguration,
        LocalContext provides localizedContext,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
