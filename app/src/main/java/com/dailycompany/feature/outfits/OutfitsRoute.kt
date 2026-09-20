package com.dailycompany.feature.outfits

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dailycompany.ui.components.EmptyState
import com.dailycompany.ui.components.TopAppBarWithSettings
import com.dailycompany.ui.theme.strings

@Composable
fun OutfitsRoute(onNavigateToSettings: () -> Unit) {
    OutfitsScreen(onNavigateToSettings = onNavigateToSettings)
}

@Composable
fun OutfitsScreen(onNavigateToSettings: () -> Unit) {
    val s = strings()
    Scaffold(
        topBar = {
            TopAppBarWithSettings(
                title = s.outfitsTitle,
                onNavigateToSettings = onNavigateToSettings,
            )
        },
    ) { innerPadding ->
        EmptyState(
            icon = Icons.Outlined.AutoAwesome,
            message = s.outfitsEmpty,
            modifier = Modifier.padding(innerPadding),
        )
    }
}