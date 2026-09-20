package com.dailycompany.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dailycompany.ui.theme.strings

/** Barra superior de um ecrã de topo, com menu ⋮ do qual se chega às Definições. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithSettings(
    title: String,
    onNavigateToSettings: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val s = strings()

    TopAppBar(
        title = { Text(text = title) },
        actions = {
            IconButton(onClick = { menuOpen = true }) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = s.actionMore,
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(text = s.settingsTitle) },
                    onClick = {
                        menuOpen = false
                        onNavigateToSettings()
                    },
                )
            }
        },
    )
}