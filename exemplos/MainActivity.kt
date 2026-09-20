package com.dailycompany

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.dailycompany.data.settings.AppSettings
import com.dailycompany.data.settings.SettingsRepository
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
     * É o valor que o splash usa para saber quanto tempo se deve manter no ecrã —
     * ver `onCreate` abaixo. Sem isto, a app arrancava sempre em claro por uma
     * fração de segundo antes de aplicar "Escuro", o que pisca visivelmente.
     */
    val settings: StateFlow<AppSettings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // TEM de ser chamado antes de super.onCreate / setContent, e antes de
        // qualquer outro código que desenhe algo — é o que troca o tema do
        // Window de Theme.App.Starting para Theme.App no momento certo.
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // AppSettingsViewModel só existe depois do Hilt montar o grafo; para conseguir
        // o mesmo ViewModel dentro e fora do Compose (para a condição do splash e para
        // o tema), criamos aqui um placeholder mínimo — a origem de verdade é sempre
        // a instância que o hiltViewModel() dentro do setContent devolve.
        var settingsSnapshot: AppSettings? = null

        setContent {
            val viewModel: AppSettingsViewModel = hiltViewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            settingsSnapshot = settings

            // Mantém o splash visível até a primeira leitura do DataStore chegar,
            // para o tema (claro/escuro) já estar decidido quando o primeiro frame
            // da app aparecer — sem isto haveria um flash breve do tema errado.
            splashScreen.setKeepOnScreenCondition { settingsSnapshot == null }

            val resolved = settings ?: AppSettings()   // valores por omissão enquanto carrega
            DailyCompanyTheme(
                themeMode = resolved.themeMode,
                language = resolved.language,
                dynamicColor = resolved.dynamicColor,
            ) {
                DailyCompanyApp()   // NavHost com os 5 destinos
            }
        }
    }
}