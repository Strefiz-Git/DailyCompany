package com.dailycompany

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailycompany.data.settings.AppSettings
import com.dailycompany.data.settings.SettingsRepository
import com.dailycompany.ui.navigation.DailyCompanyApp
import com.dailycompany.ui.theme.DailyCompanyTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {
    /**
     * `null` enquanto o DataStore ainda não devolveu a primeira leitura.
     * É o valor que o splash usa para saber quanto tempo se deve manter no ecrã.
     */
    val settings: StateFlow<AppSettings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var settingsSnapshot: AppSettings? = null

        setContent {
            val viewModel: AppSettingsViewModel = hiltViewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            settingsSnapshot = settings

            splashScreen.setKeepOnScreenCondition { settingsSnapshot == null }

            val resolved = settings ?: AppSettings()
            DailyCompanyTheme(
                themeMode = resolved.themeMode,
                language = resolved.language,
                dynamicColor = resolved.dynamicColor,
            ) {
                DailyCompanyApp()
            }
        }
    }
}