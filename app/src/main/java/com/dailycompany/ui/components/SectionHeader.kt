package com.dailycompany.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dailycompany.ui.theme.Spacing

/** Cabeçalho de secção usado dentro dos ecrãs (Hoje, Definições, etc.). */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.fillMaxWidth().padding(horizontal = Spacing.screenH, vertical = Spacing.sm),
    )
}