package com.dailycompany.feature.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailycompany.domain.model.RecurrenceEnd
import com.dailycompany.ui.theme.AppStrings
import com.dailycompany.ui.theme.LocalAppLocale
import com.dailycompany.ui.theme.Spacing
import com.dailycompany.ui.theme.strings
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle

/** Rota de criar/editar um evento (`event_edit`). */
@Composable
fun EventEditRoute(
    onBack: () -> Unit,
    viewModel: EventEditViewModel = hiltViewModel(),
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

    EventEditScreen(
        uiState = uiState,
        onBack = onBack,
        onTitleChange = viewModel::onTitleChange,
        onDateChange = viewModel::onDateChange,
        onAllDayChange = viewModel::onAllDayChange,
        onTimeChange = viewModel::onTimeChange,
        onLocationChange = viewModel::onLocationChange,
        onNotesChange = viewModel::onNotesChange,
        onRecurrenceOptionChange = viewModel::onRecurrenceOptionChange,
        onWeekdayToggle = viewModel::onWeekdayToggle,
        onEndChange = viewModel::onEndChange,
        onEndDateChange = viewModel::onEndDateChange,
        onEndCountChange = viewModel::onEndCountChange,
        onSave = viewModel::save,
        onDelete = viewModel::delete,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventEditScreen(
    uiState: EventEditUiState,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onAllDayChange: (Boolean) -> Unit,
    onTimeChange: (LocalTime?) -> Unit,
    onLocationChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onRecurrenceOptionChange: (RecurrenceOption) -> Unit,
    onWeekdayToggle: (DayOfWeek) -> Unit,
    onEndChange: (RecurrenceEnd) -> Unit,
    onEndDateChange: (LocalDate) -> Unit,
    onEndCountChange: (Int) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    snackbarHost: @Composable () -> Unit,
) {
    val s = strings()
    val locale = LocalAppLocale.current
    val titleFocus = remember { FocusRequester() }
    var menuOpen by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val option = RecurrenceOption.from(uiState.recurrence)
    val end = uiState.recurrence.end
    val endDate = (end as? RecurrenceEnd.OnDate)?.date
    val initialEndDate = endDate ?: uiState.date.plusYears(1)

    LaunchedEffect(Unit) {
        if (uiState.title.isBlank()) titleFocus.requestFocus()
    }

    Scaffold(
        snackbarHost = snackbarHost,
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) s.eventsEditTitle else s.eventsNewTitle) },
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
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text(s.actionDelete) },
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
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.lg),
            )
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.screenH),
            ) {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = onTitleChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(titleFocus),
                    label = { Text(s.eventsFieldTitle) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                Spacer(Modifier.height(Spacing.sm))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(uiState.date.humanDate(locale))
                    }
                    Spacer(Modifier.width(Spacing.xs))
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isAllDay,
                    ) {
                        Text(uiState.time?.let { it.formatHour(locale) } ?: s.eventsFieldTime)
                    }
                }
                Spacer(Modifier.height(Spacing.xs))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = s.eventsFieldAllDay,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = uiState.isAllDay,
                        onCheckedChange = onAllDayChange,
                    )
                }
                Spacer(Modifier.height(Spacing.md))

                OutlinedTextField(
                    value = uiState.location,
                    onValueChange = onLocationChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(s.eventsFieldLocation) },
                    singleLine = true,
                )
                Spacer(Modifier.height(Spacing.sm))
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = onNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(s.eventsFieldNotes) },
                    minLines = 3,
                )
                Spacer(Modifier.height(Spacing.md))

                RecurrencePicker(
                    option = option,
                    onOptionChange = onRecurrenceOptionChange,
                    weekDays = uiState.recurrence.weekDays,
                    onWeekdayToggle = onWeekdayToggle,
                    end = end,
                    onEndChange = onEndChange,
                    endDate = endDate,
                    initialEndDate = initialEndDate,
                    onEndDateChange = onEndDateChange,
                    onEndCountChange = onEndCountChange,
                    showEndDatePicker = showEndDatePicker,
                    onShowEndDatePicker = { showEndDatePicker = it },
                )
                Spacer(Modifier.height(Spacing.lg))

                TextButton(
                    onClick = onSave,
                    enabled = uiState.canSave,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(s.actionSave)
                }
                Spacer(Modifier.height(Spacing.lg))
            }
        }
    }

    if (showDatePicker) {
        val initial = uiState.date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val state = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            onDateChange(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(s.actionConfirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(s.actionCancel)
                }
            },
        ) {
            DatePicker(state = state)
        }
    }

    if (showTimePicker) {
        val initialHour = uiState.time?.hour ?: 9
        val initialMinute = uiState.time?.minute ?: 0
        val state = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
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
                    Text(s.actionConfirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(s.actionCancel)
                }
            },
            text = {
                TimePicker(state = state)
            },
        )
    }

    if (showEndDatePicker) {
        val initial = initialEndDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val state = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            onEndDateChange(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                        }
                        showEndDatePicker = false
                    },
                ) {
                    Text(s.actionConfirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text(s.actionCancel)
                }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

/** Seletor de repetição: frequência + dias da semana + fim da série. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecurrencePicker(
    option: RecurrenceOption,
    onOptionChange: (RecurrenceOption) -> Unit,
    weekDays: Set<DayOfWeek>,
    onWeekdayToggle: (DayOfWeek) -> Unit,
    end: RecurrenceEnd,
    onEndChange: (RecurrenceEnd) -> Unit,
    endDate: LocalDate?,
    initialEndDate: LocalDate,
    onEndDateChange: (LocalDate) -> Unit,
    onEndCountChange: (Int) -> Unit,
    showEndDatePicker: Boolean,
    onShowEndDatePicker: (Boolean) -> Unit,
) {
    val s = strings()
    val locale = LocalAppLocale.current

    Text(
        text = s.eventsFieldRepeat,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(Spacing.xs))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        RecurrenceOption.entries.forEach { item ->
            FilterChip(
                selected = option == item,
                onClick = { onOptionChange(item) },
                label = { Text(item.label(s)) },
            )
        }
    }

    if (option == RecurrenceOption.WEEKDAYS) {
        Spacer(Modifier.height(Spacing.sm))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            DayOfWeek.entries.forEach { day ->
                FilterChip(
                    selected = day in weekDays,
                    onClick = { onWeekdayToggle(day) },
                    label = { Text(day.getDisplayName(TextStyle.NARROW, locale)) },
                )
            }
        }
    }

    Spacer(Modifier.height(Spacing.sm))
    Text(
        text = s.eventsFieldRepeatEnd,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(Spacing.xs))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        FilterChip(
            selected = end is RecurrenceEnd.Never,
            onClick = { onEndChange(RecurrenceEnd.Never) },
            label = { Text(s.recurrenceEndNever) },
        )
        FilterChip(
            selected = end is RecurrenceEnd.OnDate,
            onClick = { onEndChange(RecurrenceEnd.OnDate(endDate ?: initialEndDate)) },
            label = { Text(s.recurrenceEndOnDate) },
        )
        FilterChip(
            selected = end is RecurrenceEnd.AfterCount,
            onClick = { onEndChange(RecurrenceEnd.AfterCount((end as? RecurrenceEnd.AfterCount)?.count ?: 10)) },
            label = { Text(s.recurrenceEndAfterCount) },
        )
    }

    if (end is RecurrenceEnd.OnDate) {
        Spacer(Modifier.height(Spacing.xs))
        OutlinedButton(
            onClick = { onShowEndDatePicker(true) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text((endDate ?: initialEndDate).humanDate(locale))
        }
    }

    if (end is RecurrenceEnd.AfterCount) {
        Spacer(Modifier.height(Spacing.xs))
        OutlinedTextField(
            value = end.count.toString(),
            onValueChange = { text ->
                text.toIntOrNull()?.let(onEndCountChange)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(s.eventsFieldOccurrences) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}

private fun RecurrenceOption.label(s: AppStrings): String = when (this) {
    RecurrenceOption.NEVER -> s.recurrenceNever
    RecurrenceOption.DAILY -> s.recurrenceDaily
    RecurrenceOption.WEEKDAYS -> s.recurrenceWeekdays
    RecurrenceOption.WEEKLY -> s.recurrenceWeekly
    RecurrenceOption.MONTHLY -> s.recurrenceMonthly
    RecurrenceOption.YEARLY -> s.recurrenceYearly
}

private fun LocalDate.humanDate(locale: java.util.Locale): String =
    format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))

private fun LocalTime.formatHour(locale: java.util.Locale): String =
    format(DateTimeFormatter.ofPattern("HH:mm", locale))

private fun EventEditSnackbar.toMessage(s: AppStrings): String = when (this) {
    EventEditSnackbar.DELETED -> s.eventsDeleted
}