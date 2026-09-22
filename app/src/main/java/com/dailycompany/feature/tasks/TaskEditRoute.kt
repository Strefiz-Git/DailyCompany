package com.dailycompany.feature.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailycompany.domain.model.TaskIcon
import com.dailycompany.domain.model.TaskPriority
import com.dailycompany.domain.model.TaskRepeat
import com.dailycompany.ui.theme.AppStrings
import com.dailycompany.ui.theme.LocalAppLocale
import com.dailycompany.ui.theme.Spacing
import com.dailycompany.ui.theme.strings
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Rota de criar/editar uma tarefa (`task_edit`). */
@Composable
fun TaskEditRoute(
    onBack: () -> Unit,
    viewModel: TaskEditViewModel = hiltViewModel(),
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

    TaskEditScreen(
        uiState = uiState,
        onBack = onBack,
        onTitleChange = viewModel::onTitleChange,
        onNoteChange = viewModel::onNoteChange,
        onDateChange = viewModel::onDateChange,
        onTimeChange = viewModel::onTimeChange,
        onPriorityChange = viewModel::onPriorityChange,
        onIconChange = viewModel::onIconChange,
        onRepeatChange = viewModel::onRepeatChange,
        onSave = viewModel::save,
        onDelete = viewModel::delete,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskEditScreen(
    uiState: TaskEditUiState,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onDateChange: (LocalDate?) -> Unit,
    onTimeChange: (LocalTime?) -> Unit,
    onPriorityChange: (TaskPriority) -> Unit,
    onIconChange: (TaskIcon?) -> Unit,
    onRepeatChange: (TaskRepeat) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    snackbarHost: @Composable () -> Unit,
) {
    val s = strings()
    val titleFocus = remember { FocusRequester() }
    var menuOpen by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (uiState.title.isBlank()) titleFocus.requestFocus()
    }

    Scaffold(
        snackbarHost = snackbarHost,
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) s.tasksEditTitle else s.tasksNewTitle) },
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
                    label = { Text(s.tasksFieldTitle) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                Spacer(Modifier.height(Spacing.sm))
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = onNoteChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(s.tasksFieldNote) },
                    minLines = 3,
                )
                Spacer(Modifier.height(Spacing.sm))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(uiState.dueDate?.humanDate(s) ?: s.tasksFieldDate)
                    }
                    Spacer(Modifier.width(Spacing.xs))
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(uiState.dueTime?.let { formatHour(it) } ?: s.tasksFieldTime)
                    }
                }
                Spacer(Modifier.height(Spacing.md))

                Text(
                    text = s.tasksFieldPriority,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.xs))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    TaskPriority.entries.forEachIndexed { index, priority ->
                        SegmentedButton(
                            selected = uiState.priority == priority,
                            onClick = { onPriorityChange(priority) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = TaskPriority.entries.size,
                            ),
                        ) {
                            Text(priority.label(s))
                        }
                    }
                }
                Spacer(Modifier.height(Spacing.md))

                Text(
                    text = s.tasksFieldIcon,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.xs))
                IconPicker(
                    selected = uiState.icon,
                    onSelect = onIconChange,
                )
                Spacer(Modifier.height(Spacing.lg))

                Text(
                    text = s.tasksFieldRepeat,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.xs))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    TaskRepeat.entries.forEach { option ->
                        FilterChip(
                            selected = uiState.repeat == option,
                            onClick = { onRepeatChange(option) },
                            label = { Text(option.label(s)) },
                        )
                    }
                }
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
        val initial = uiState.dueDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        val state = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            onDateChange(
                                Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate(),
                            )
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
            androidx.compose.material3.DatePicker(state = state)
        }
    }

    if (showTimePicker) {
        val initialHour = uiState.dueTime?.hour ?: 9
        val initialMinute = uiState.dueTime?.minute ?: 0
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IconPicker(
    selected: TaskIcon?,
    onSelect: (TaskIcon?) -> Unit,
) {
    val s = strings()
    var mode by remember { mutableStateOf(IconMode.ICONS) }
    var emojiText by remember { mutableStateOf((selected as? TaskIcon.Emoji)?.char.orEmpty()) }

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        IconMode.entries.forEachIndexed { index, item ->
            SegmentedButton(
                selected = mode == item,
                onClick = { mode = item },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = IconMode.entries.size,
                ),
            ) {
                Text(item.label(s))
            }
        }
    }
    Spacer(Modifier.height(Spacing.sm))

    val palette = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(Spacing.sm)

    when (mode) {
        IconMode.ICONS -> FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            materialTaskIconNames.forEach { name ->
                val icon = materialTaskIcon(name)
                val isSelected = selected == icon
                Box(
                    modifier = Modifier
                        .size(Spacing.xxl)
                        .background(
                            color = if (isSelected) {
                                palette.secondaryContainer
                            } else {
                                palette.surfaceVariant
                            },
                            shape = shape,
                        )
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = Spacing.xxs,
                                    color = palette.primary,
                                    shape = shape,
                                )
                            } else {
                                Modifier
                            },
                        )
                        .clickable { onSelect(icon) }
                        .padding(Spacing.xs),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = materialIconVector(name),
                        contentDescription = name,
                        tint = if (isSelected) palette.onSecondaryContainer else palette.onSurfaceVariant,
                    )
                }
            }
        }

        IconMode.EMOJIS -> {
            val preview = (selected as? TaskIcon.Emoji)?.char.orEmpty()
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = emojiText,
                    onValueChange = { text ->
                        emojiText = text
                        val trimmed = text.trim()
                        onSelect(if (trimmed.isEmpty()) null else TaskIcon.Emoji(trimmed))
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(s.tasksEmojiHint) },
                    singleLine = true,
                )
                Spacer(Modifier.width(Spacing.sm))
                Box(
                    modifier = Modifier
                        .size(Spacing.xxl)
                        .background(palette.surfaceVariant, shape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }
    }

    if (selected != null) {
        Spacer(Modifier.height(Spacing.xs))
        TextButton(onClick = { onSelect(null) }) {
            Text(s.tasksIconNone)
        }
    }
}

/** Abas do seletor de ícone. */
private enum class IconMode {
    ICONS,
    EMOJIS;

    fun label(s: AppStrings): String = when (this) {
        ICONS -> s.tasksIconTabIcons
        EMOJIS -> s.tasksIconTabEmojis
    }
}

private fun TaskPriority.label(s: AppStrings): String = when (this) {
    TaskPriority.NONE -> s.tasksPriorityNone
    TaskPriority.MEDIUM -> s.tasksPriorityMedium
    TaskPriority.HIGH -> s.tasksPriorityHigh
}

/** Rótulo da repetição — reutiliza o vocabulário de recorrência (Eventos). */
internal fun TaskRepeat.label(s: AppStrings): String = when (this) {
    TaskRepeat.NONE -> s.recurrenceNever
    TaskRepeat.DAILY -> s.recurrenceDaily
    TaskRepeat.WEEKLY -> s.recurrenceWeekly
    TaskRepeat.MONTHLY -> s.recurrenceMonthly
    TaskRepeat.YEARLY -> s.recurrenceYearly
}

@Composable
private fun LocalDate.humanDate(s: AppStrings): String =
    format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(LocalAppLocale.current))

@Composable
private fun formatHour(time: LocalTime): String =
    time.format(DateTimeFormatter.ofPattern("HH:mm").withLocale(LocalAppLocale.current))

private fun TaskEditSnackbar?.toMessage(s: AppStrings): String = when (this) {
    TaskEditSnackbar.DELETED -> s.tasksDeleted
    null -> ""
}