package com.dailycompany.feature.today

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dailycompany.ui.components.EmptyState
import com.dailycompany.ui.components.TopAppBarWithSettings
import com.dailycompany.ui.theme.strings

@Composable
fun TodayRoute(
    onNavigateToSettings: () -> Unit,
    onOpenAlarms: () -> Unit,
) {
    TodayScreen(
        onNavigateToSettings = onNavigateToSettings,
        onOpenAlarms = onOpenAlarms,
    )
}

@Composable
fun TodayScreen(
    onNavigateToSettings: () -> Unit,
    onOpenAlarms: () -> Unit,
) {
    val s = strings()
    Scaffold(
        topBar = {
            TopAppBarWithSettings(
                title = s.navToday,
                onNavigateToSettings = onNavigateToSettings,
                onOpenAlarms = onOpenAlarms,
            )
        },
    ) { innerPadding ->
        EmptyState(
            icon = Icons.Outlined.WbSunny,
            message = s.todayEmpty,
            modifier = Modifier.padding(innerPadding),
        )
    }
}