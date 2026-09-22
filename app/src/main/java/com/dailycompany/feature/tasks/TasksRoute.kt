package com.dailycompany.feature.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailycompany.domain.model.Task
import com.dailycompany.domain.model.TaskGroups
import com.dailycompany.domain.model.TaskPriority
import com.dailycompany.domain.model.TaskRepeat
import com.dailycompany.domain.model.groupTasks
import com.dailycompany.ui.components.EmptyState
import com.dailycompany.ui.components.SectionHeader
import com.dailycompany.ui.components.TopAppBarWithSettings
import com.dailycompany.ui.theme.AppStrings
import com.dailycompany.ui.theme.LocalAppLocale
import com.dailycompany.ui.theme.Spacing
import com.dailycompany.ui.theme.strings
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Rota do ecrã Tarefas. Só aqui o `TasksViewModel` é criado (regra:
 * a UI nunca fala com DAO/repositório diretamente).
 */
@Composable
fun TasksRoute(
    onNavigateToSettings: () -> Unit,
    onCreateTask: () -> Unit,
    onOpenTask: (Long) -> Unit,
    viewModel: TasksViewModel = hiltViewModel(),
) {
    TasksScreen(
        onNavigateToSettings = onNavigateToSettings,
        onCreateTask = onCreateTask,
        onOpenTask = onOpenTask,
        viewModel = viewModel,
    )
}

@Composable
fun TasksScreen(
    onNavigateToSettings: () -> Unit,
    onCreateTask: () -> Unit,
    onOpenTask: (Long) -> Unit,
    viewModel: TasksViewModel,
) {
    val s = strings()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var quickAddText by remember { mutableStateOf("") }

    LaunchedEffect(uiState.snackbar) {
        val snackbar = uiState.snackbar ?: return@LaunchedEffect
        val message = snackbar.toMessage(s) ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = s.actionUndo,
            withDismissAction = true,
        )
        if (result == SnackbarResult.ActionPerformed) viewModel.undo()
        viewModel.consumeSnackbar()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBarWithSettings(
                title = s.tasksTitle,
                onNavigateToSettings = onNavigateToSettings,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateTask,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                    )
                },
                text = { Text(s.tasksNewTitle) },
                elevation = FloatingActionButtonDefaults.elevation(2.dp),
            )
        },
        bottomBar = {
            QuickAddBar(
                value = quickAddText,
                onValueChange = { quickAddText = it },
                onSubmit = {
                    viewModel.quickAdd(quickAddText)
                    quickAddText = ""
                },
                hint = s.tasksQuickAddHint,
                sendLabel = s.actionAdd,
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            PrimaryTabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
            ) {
                TasksTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tab.label(s)) },
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                )
            } else {
                val grouped = groupTasks(uiState.tasks, viewModel.today)
                val visible = grouped.visibleFor(uiState.selectedTab)

                if (visible.isEmpty()) {
                    EmptyState(
                        icon = Icons.Outlined.CheckCircleOutline,
                        message = uiState.selectedTab.emptyLabel(s),
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    TaskList(
                        tasks = visible,
                        today = viewModel.today,
                        onToggleDone = { viewModel.toggleDone(it) },
                        onPostpone = { viewModel.postponeToTomorrow(it) },
                        onOpenTask = onOpenTask,
                    )
                }
            }
        }
    }
}

/** Barra fixa no fundo: campo de texto + enviar, cria com título só. */
@Composable
private fun QuickAddBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    hint: String,
    sendLabel: String,
) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = Spacing.screenH, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(hint) },
                singleLine = true,
            )
            Spacer(Modifier.width(Spacing.sm))
            IconButton(onClick = onSubmit) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = sendLabel,
                )
            }
        }
    }
}

@Composable
private fun TaskList(
    tasks: List<Task>,
    today: LocalDate,
    onToggleDone: (Task) -> Unit,
    onPostpone: (Task) -> Unit,
    onOpenTask: (Long) -> Unit,
) {
    val sections = groupByDay(tasks)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.screenH),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        sections.forEach { (date, sectionTasks) ->
            date?.let {
                item(key = "header-$it") {
                    SectionHeader(title = it.formatDayHeader(today))
                }
            }
            items(sectionTasks, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    today = today,
                    onToggleDone = { onToggleDone(task) },
                    onPostpone = { onPostpone(task) },
                    onOpen = { onOpenTask(task.id) },
                )
                HorizontalDivider()
            }
        }
    }
}

/**
 * Agrupa tarefas por dia preservando a ordem. As listas já vêm ordenadas
 * por prioridade/data, por isso grupos consecutivos por data bastam.
 */
internal fun groupByDay(tasks: List<Task>): List<Pair<LocalDate?, List<Task>>> {
    if (tasks.isEmpty()) return emptyList()
    val result = mutableListOf<Pair<LocalDate?, MutableList<Task>>>()
    for (task in tasks) {
        val last = result.lastOrNull()
        if (last != null && task.dueDate == last.first) {
            last.second.add(task)
        } else {
            result.add(task.dueDate to mutableListOf(task))
        }
    }
    return result.map { it.first to it.second.toList() }
}

@Composable
private fun TaskRow(
    task: Task,
    today: LocalDate,
    onToggleDone: () -> Unit,
    onPostpone: () -> Unit,
    onOpen: () -> Unit,
) {
    val s = strings()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onToggleDone) {
            Icon(
                imageVector = if (task.done) {
                    Icons.Outlined.CheckCircleOutline
                } else {
                    Icons.Outlined.RadioButtonUnchecked
                },
                contentDescription = if (task.done) s.tasksReopen else s.tasksConclude,
                tint = if (task.done) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onOpen),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                task.icon?.let { TaskIconBadge(icon = it) }
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    textDecoration = if (task.done) TextDecoration.LineThrough else null,
                    color = if (task.done) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
            task.note.takeIf { it.isNotBlank() && !task.done }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                task.dueTime?.let {
                    Text(
                        text = it.formatHour(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (task.priority != TaskPriority.NONE) {
                    PriorityDot(priority = task.priority)
                }
                if (task.repeat != TaskRepeat.NONE) {
                    Icon(
                        imageVector = Icons.Outlined.Repeat,
                        contentDescription = task.repeat.label(s),
                        modifier = Modifier.size(Spacing.md),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (task.dueDate != null && task.dueDate < today && !task.done) {
                    Text(
                        text = s.tasksOverdue,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        if (task.dueDate != null && task.dueDate < today && !task.done) {
            TextButton(onClick = onPostpone) {
                Text(s.tasksPostponeTomorrow)
            }
        }
    }
}

/** Ponto de prioridade (alta = erro, média = terciária). */
@Composable
private fun PriorityDot(priority: TaskPriority) {
    val color: Color = when (priority) {
        TaskPriority.HIGH -> MaterialTheme.colorScheme.error
        TaskPriority.MEDIUM -> MaterialTheme.colorScheme.tertiary
        TaskPriority.NONE -> Color.Transparent
    }
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color),
    )
}

private fun TasksTab.emptyLabel(s: AppStrings): String = when (this) {
    TasksTab.TODAY -> s.tasksEmptyToday
    TasksTab.UPCOMING -> s.tasksEmptyUpcoming
    TasksTab.NO_DATE -> s.tasksEmptyNoDate
    TasksTab.DONE -> s.tasksEmptyDone
}

private fun TaskGroups.visibleFor(tab: TasksTab): List<Task> = when (tab) {
    TasksTab.TODAY -> today + overdue
    TasksTab.UPCOMING -> upcoming
    TasksTab.NO_DATE -> noDate
    TasksTab.DONE -> done
}

private fun TasksTab.label(s: AppStrings): String = when (this) {
    TasksTab.TODAY -> s.tasksTabToday
    TasksTab.UPCOMING -> s.tasksTabUpcoming
    TasksTab.NO_DATE -> s.tasksTabNoDate
    TasksTab.DONE -> s.tasksTabDone
}

@Composable
private fun LocalDate.formatDayHeader(today: LocalDate): String {
    val s = strings()
    val locale = LocalAppLocale.current
    return when (this) {
        today -> s.relativeToday.replaceFirstChar { it.uppercase(locale) }
        today.plusDays(1) -> s.relativeTomorrow.replaceFirstChar { it.uppercase(locale) }
        else -> format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
    }
}

@Composable
private fun LocalTime.formatHour(): String =
    format(DateTimeFormatter.ofPattern("HH:mm").withLocale(LocalAppLocale.current))

/** Resolve o texto de cada mensagem de snackbar emitida pelo ViewModel. */
private fun TaskSnackbar?.toMessage(s: AppStrings): String? = when (this) {
    TaskSnackbar.DONE -> s.tasksConcluded
    TaskSnackbar.REOPENED -> s.tasksReopened
    TaskSnackbar.POSTPONED -> s.tasksPostponed
    null -> null
}