package com.dailycompany.feature.tasks

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dailycompany.ui.components.EmptyState
import com.dailycompany.ui.components.TopAppBarWithSettings
import com.dailycompany.ui.theme.strings

@Composable
fun TasksRoute(onNavigateToSettings: () -> Unit) {
    TasksScreen(onNavigateToSettings = onNavigateToSettings)
}

@Composable
fun TasksScreen(onNavigateToSettings: () -> Unit) {
    val s = strings()
    Scaffold(
        topBar = {
            TopAppBarWithSettings(
                title = s.tasksTitle,
                onNavigateToSettings = onNavigateToSettings,
            )
        },
    ) { innerPadding ->
        EmptyState(
            icon = Icons.Outlined.CheckCircleOutline,
            message = s.tasksEmpty,
            modifier = Modifier.padding(innerPadding),
        )
    }
}