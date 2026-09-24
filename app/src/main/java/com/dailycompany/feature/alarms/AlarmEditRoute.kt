package com.dailycompany.feature.alarms

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailycompany.ui.theme.AppStrings
import com.dailycompany.ui.theme.LocalAppLocale
import com.dailycompany.ui.theme.Spacing
import com.dailycompany.ui.theme.strings
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** Rota de criar/editar um alarme (`alarm_edit`). */
@Composable
fun AlarmEditRoute(
    onBack: () -> Unit,
    viewModel: AlarmEditViewModel = hiltViewModel(),
) {
    val s = strings()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbar) {
        val snackbar = uiState.snackbar ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = snackbar.toMessage(s),
            actionLabel = s.actionUndo,
            withDismissAction = true,
            duration = SnackbarDuration.Long,
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.undoDelete()
        } else {
            viewModel.closeAfterDelete()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { onBack() }
    }

    AlarmEditScreen(
        uiState = uiState,
        onBack = onBack,
        onTimeChange = viewModel::onTimeChange,
        onWeekdayToggle = viewModel::onWeekdayToggle,
        onLabelChange = viewModel::onLabelChange,
        onSoundChange = viewModel::onSoundChange,
        onVibrateChange = viewModel::onVibrateChange,
        onAutoSnoozeChange = viewModel::onAutoSnoozeChange,
        onSave = viewModel::save,
        onDelete = viewModel::delete,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AlarmEditScreen(
    uiState: AlarmEditUiState,
    onBack: () -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    onWeekdayToggle: (DayOfWeek) -> Unit,
    onLabelChange: (String) -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onVibrateChange: (Boolean) -> Unit,
    onAutoSnoozeChange: (Int) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    snackbarHost: @Composable () -> Unit,
) {
    val s = strings()
    val locale = LocalAppLocale.current
    var menuOpen by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = snackbarHost,
        topBar = {
            TopAppBar(
                title = { Text(text = if (uiState.isEditing) s.alarmsEditTitle else s.alarmsNewTitle) },
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
                    if (uiState.isEditing) {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = s.actionMore,
                            )
                        }
                        DropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = s.actionDelete) },
                                onClick = {
                                    menuOpen = false
                                    onDelete()
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenH),
        ) {
            Spacer(Modifier.height(Spacing.sm))

            Text(
                text = s.alarmsFieldTime,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.xs))
            OutlinedButton(onClick = { showTimePicker = true }) {
                Text(text = uiState.time.formatHour(locale))
            }

            Spacer(Modifier.height(Spacing.md))

            Text(
                text = s.alarmsRepeat,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.xs))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                DayOfWeek.entries.forEach { day ->
                    FilterChip(
                        selected = day in uiState.repeatDays,
                        onClick = { onWeekdayToggle(day) },
                        label = { Text(text = day.getDisplayName(TextStyle.NARROW, locale)) },
                    )
                }
            }

            Spacer(Modifier.height(Spacing.md))

            OutlinedTextField(
                value = uiState.label,
                onValueChange = onLabelChange,
                label = { Text(text = s.alarmsFieldLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.md))

            SwitchRow(
                label = s.alarmsFieldSound,
                checked = uiState.soundEnabled,
                onCheckedChange = onSoundChange,
            )
            Spacer(Modifier.height(Spacing.xs))
            SwitchRow(
                label = s.alarmsFieldVibrate,
                checked = uiState.vibrateEnabled,
                onCheckedChange = onVibrateChange,
            )

            Spacer(Modifier.height(Spacing.md))

            Text(
                text = s.alarmsFieldAutoSnooze,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.xs))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                AUTO_SNOOZE_OPTIONS.forEach { minutes ->
                    FilterChip(
                        selected = uiState.autoSnoozeMinutes == minutes,
                        onClick = { onAutoSnoozeChange(minutes) },
                        label = {
                            Text(
                                text = if (minutes == 0) s.alarmsAutoSnoozeOff else s.alarmsMinutes(minutes),
                            )
                        },
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            TextButton(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = s.actionSave)
            }

            Spacer(Modifier.height(Spacing.lg))
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = uiState.time.hour,
            initialMinute = uiState.time.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeChange(LocalTime.of(state.hour, state.minute))
                        showTimePicker = false
                    },
                ) {
                    Text(text = s.actionConfirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(text = s.actionCancel)
                }
            },
            text = { TimePicker(state = state) },
        )
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.xs))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.align(Alignment.Start),
        )
    }
}

/** Formata a hora do alarme como HH:mm no fuso do utilizador. */
private fun LocalTime.formatHour(locale: Locale): String =
    format(DateTimeFormatter.ofPattern("HH:mm", locale))

private fun AlarmEditSnackbar.toMessage(s: AppStrings): String = when (this) {
    AlarmEditSnackbar.DELETED -> s.alarmsDeleted
}

private val AUTO_SNOOZE_OPTIONS = listOf(0, 5, 10, 15, 30)