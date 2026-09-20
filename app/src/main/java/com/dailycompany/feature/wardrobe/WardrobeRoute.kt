package com.dailycompany.feature.wardrobe

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dailycompany.ui.components.EmptyState
import com.dailycompany.ui.components.TopAppBarWithSettings
import com.dailycompany.ui.theme.strings

@Composable
fun WardrobeRoute(onNavigateToSettings: () -> Unit) {
    WardrobeScreen(onNavigateToSettings = onNavigateToSettings)
}

@Composable
fun WardrobeScreen(onNavigateToSettings: () -> Unit) {
    val s = strings()
    Scaffold(
        topBar = {
            TopAppBarWithSettings(
                title = s.wardrobeTitle,
                onNavigateToSettings = onNavigateToSettings,
            )
        },
    ) { innerPadding ->
        EmptyState(
            icon = Icons.Outlined.Checkroom,
            message = s.wardrobeEmpty,
            modifier = Modifier.padding(innerPadding),
        )
    }
}