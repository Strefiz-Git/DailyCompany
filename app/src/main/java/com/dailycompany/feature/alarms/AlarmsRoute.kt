package com.dailycompany.feature.alarms

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailycompany.domain.model.Alarm
import com.dailycompany.ui.components.EmptyState
import com.dailycompany.ui.theme.LocalAppLocale
import com.dailycompany.ui.theme.Spacing
import com.dailycompany.ui.theme.strings
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** Rota da lista de alarmes (`alarms`). Só aqui o `AlarmsViewModel` é criado. */
@Composable
fun AlarmsRoute(
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onOpenAlarm: (Long) -> Unit,
    onOpenHelp: () -> Unit,
    viewModel: AlarmsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AlarmsScreen(
        uiState = uiState,
        onBack = onBack,
        onCreate = onCreate,
        onOpenAlarm = onOpenAlarm,
        onOpenHelp = onOpenHelp,
        onSetEnabled = viewModel::setEnabled,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    uiState: AlarmsUiState,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onOpenAlarm: (Long) -> Unit,
    onOpenHelp: () -> Unit,
    onSetEnabled: (Alarm, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings()
    val locale = LocalAppLocale.current

    // Permissão de notificações (Android 13+): aviso no topo + pedido direto.
    val context = LocalContext.current
    var notifGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> notifGranted = granted }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = s.alarmsTitle) },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = s.actionClose,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenHelp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = s.actionHelp,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                icon = { Icon(imageVector = Icons.Outlined.Add, contentDescription = null) },
                text = { Text(text = s.alarmsNewTitle) },
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.alarms.isEmpty() -> {
                EmptyState(
                    icon = Icons.Outlined.Alarm,
                    message = s.alarmsEmpty,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.padding(innerPadding),
                    contentPadding = PaddingValues(vertical = Spacing.sm),
                ) {
                    if (!notifGranted) {
                        item {
                            NotificationPermissionCard(
                                onGrant = {
                                    if (Build.VERSION.SDK_INT >= 33) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                onOpenHelp = onOpenHelp,
                            )
                        }
                    }
                    items(uiState.alarms, key = { it.id }) { alarm ->
                        AlarmRow(
                            alarm = alarm,
                            locale = locale,
                            onOpen = { onOpenAlarm(alarm.id) },
                            onToggle = { enabled -> onSetEnabled(alarm, enabled) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlarmRow(
    alarm: Alarm,
    locale: Locale,
    onOpen: () -> Unit,
    onToggle: (Boolean) -> Unit,
) {
    val s = strings()
    val timeText = alarm.time.format(DateTimeFormatter.ofPattern("HH:mm", locale))
    val repeatText = if (alarm.repeatDays.isEmpty()) {
        s.alarmsFiresOnce
    } else {
        alarm.repeatDays.sortedBy { it.value }
            .joinToString(" · ") { it.getDisplayName(TextStyle.SHORT, locale) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(horizontal = Spacing.screenH, vertical = Spacing.sm)
            .alpha(if (alarm.enabled) 1f else 0.45f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = repeatText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (alarm.label.isNotBlank()) {
                Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(
            checked = alarm.enabled,
            onCheckedChange = onToggle,
        )
    }
}

/** Aviso no topo da lista quando faltam as notificações (Android 13+). */
@Composable
private fun NotificationPermissionCard(
    onGrant: () -> Unit,
    onOpenHelp: () -> Unit,
) {
    val s = strings()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenH, vertical = Spacing.sm),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = s.alarmsPermissionMissing,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(Spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                TextButton(onClick = onGrant) { Text(text = s.alarmsPermissionGrant) }
                TextButton(onClick = onOpenHelp) { Text(text = s.actionHelp) }
            }
        }
    }
}