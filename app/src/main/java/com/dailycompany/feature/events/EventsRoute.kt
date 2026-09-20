package com.dailycompany.feature.events

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dailycompany.ui.components.EmptyState
import com.dailycompany.ui.components.TopAppBarWithSettings
import com.dailycompany.ui.theme.strings

@Composable
fun EventsRoute(onNavigateToSettings: () -> Unit) {
    EventsScreen(onNavigateToSettings = onNavigateToSettings)
}

@Composable
fun EventsScreen(onNavigateToSettings: () -> Unit) {
    val s = strings()
    Scaffold(
        topBar = {
            TopAppBarWithSettings(
                title = s.eventsTitle,
                onNavigateToSettings = onNavigateToSettings,
            )
        },
    ) { innerPadding ->
        EmptyState(
            icon = Icons.Outlined.CalendarMonth,
            message = s.eventsEmpty,
            modifier = Modifier.padding(innerPadding),
        )
    }
}