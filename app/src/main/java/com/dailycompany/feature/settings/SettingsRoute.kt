package com.dailycompany.feature.settings

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailycompany.data.settings.AppSettings
import com.dailycompany.ui.components.SectionHeader
import com.dailycompany.ui.theme.AppLanguage
import com.dailycompany.ui.theme.DailyCompanyTheme
import com.dailycompany.ui.theme.Spacing
import com.dailycompany.ui.theme.ThemeMode
import com.dailycompany.ui.theme.strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    SettingsScreen(
        settings = settings ?: AppSettings(),
        onBack = onBack,
        onThemeModeChange = viewModel::setThemeMode,
        onDynamicColorChange = viewModel::setDynamicColor,
        onFollowSystemLanguage = viewModel::setFollowSystemLanguage,
        onLanguageChange = viewModel::setLanguage,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onFollowSystemLanguage: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings()
    val themeOptions = listOf(
        ThemeMode.LIGHT to s.settingsThemeLight,
        ThemeMode.DARK to s.settingsThemeDark,
        ThemeMode.SYSTEM to s.settingsThemeSystem,
    )
    val languageOptions = listOf(
        AppLanguage.PT_PT to s.settingsLanguagePtPt,
        AppLanguage.PT_BR to s.settingsLanguagePtBr,
        AppLanguage.EN to s.settingsLanguageEn,
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = s.settingsTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = s.actionClose,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(vertical = Spacing.sm),
        ) {
            item { SectionHeader(title = s.settingsAppearance) }

            themeOptions.forEach { (mode, label) ->
                item {
                    ThemeOption(
                        label = label,
                        selected = settings.themeMode == mode,
                        onSelect = { onThemeModeChange(mode) },
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screenH, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = s.settingsDynamicColor,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Switch(
                        checked = settings.dynamicColor,
                        onCheckedChange = onDynamicColorChange,
                    )
                }
            }

            item { SectionHeader(title = s.settingsLanguage) }

            item {
                ThemeOption(
                    label = s.settingsLanguageSystem,
                    selected = settings.followSystemLanguage,
                    onSelect = onFollowSystemLanguage,
                )
            }
            languageOptions.forEach { (language, label) ->
                item {
                    ThemeOption(
                        label = label,
                        selected = !settings.followSystemLanguage && settings.language == language,
                        onSelect = { onLanguageChange(language) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = Spacing.xs),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    DailyCompanyTheme(themeMode = ThemeMode.SYSTEM, language = AppLanguage.PT_PT) {
        SettingsScreen(
            settings = AppSettings(),
            onBack = {},
            onThemeModeChange = {},
            onDynamicColorChange = {},
            onFollowSystemLanguage = {},
            onLanguageChange = {},
        )
    }
}

@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenDarkPreview() {
    DailyCompanyTheme(themeMode = ThemeMode.DARK, language = AppLanguage.PT_PT) {
        SettingsScreen(
            settings = AppSettings(),
            onBack = {},
            onThemeModeChange = {},
            onDynamicColorChange = {},
            onFollowSystemLanguage = {},
            onLanguageChange = {},
        )
    }
}