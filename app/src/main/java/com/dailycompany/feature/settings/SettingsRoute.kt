package com.dailycompany.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailycompany.data.settings.AppSettings
import com.dailycompany.ui.components.SectionHeader
import com.dailycompany.ui.theme.AppLanguage
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
    val current = settings ?: AppSettings()
    val s = strings()

    Scaffold(
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

            item {
                ThemeOption(
                    label = s.settingsThemeLight,
                    selected = current.themeMode == ThemeMode.LIGHT,
                    onSelect = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                )
            }
            item {
                ThemeOption(
                    label = s.settingsThemeDark,
                    selected = current.themeMode == ThemeMode.DARK,
                    onSelect = { viewModel.setThemeMode(ThemeMode.DARK) },
                )
            }
            item {
                ThemeOption(
                    label = s.settingsThemeSystem,
                    selected = current.themeMode == ThemeMode.SYSTEM,
                    onSelect = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                )
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
                        checked = current.dynamicColor,
                        onCheckedChange = viewModel::setDynamicColor,
                    )
                }
            }

            item { SectionHeader(title = s.settingsLanguage) }

            item {
                ThemeOption(
                    label = s.settingsLanguageSystem,
                    selected = current.followSystemLanguage,
                    onSelect = { viewModel.setFollowSystemLanguage() },
                )
            }
            AppLanguage.entries.forEach { language ->
                item {
                    ThemeOption(
                        label = language.label,
                        selected = !current.followSystemLanguage && current.language == language,
                        onSelect = { viewModel.setLanguage(language) },
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