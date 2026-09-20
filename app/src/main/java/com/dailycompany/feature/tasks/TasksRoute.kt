package com.dailycompany.feature.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailycompany.data.repository.TaskRepository
import com.dailycompany.domain.model.Task
import com.dailycompany.domain.model.TaskPriority
import com.dailycompany.domain.model.groupTasks
import com.dailycompany.ui.components.EmptyState
import com.dailycompany.ui.components.TopAppBarWithSettings
import com.dailycompany.ui.theme.strings
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Rota do ecrã Tarefas. Só aqui o `TasksViewModel` é criado (regra:
 * a UI nunca fala com DAO/repositório diretamente).
 */
@Composable
fun TasksRoute(
    onNavigateToSettings: () -> Unit,
    viewModel: TasksViewModel = hiltViewModel(),
) {
    TasksScreen(
        onNavigateToSettings = onNavigateToSettings,
        viewModel = viewModel,
    )
}

@Composable
fun TasksScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: TasksViewModel,
) {
    val s = strings()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel.uiState.collectAsState().value?.snackbarMessage) {
        val msg = viewModel.uiState.collectAsState().value?.snackbarMessage ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = msg,
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
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(
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
                val grouped = groupTasks(uiState.tasks, clock.today())
                val visible = when (uiState.selectedTab) {
                    TasksTab.TODAY -> grouped.today
                    TasksTab.UPCOMING -> grouped.upcoming
                    TasksTab.NO_DATE -> grouped.noDate
                    TasksTab.DONE -> grouped.done
                }

                if (visible.isEmpty()) {
                    EmptyState(
                        icon = Icons.Outlined.CheckCircleOutline,
                        message = s.tasksEmpty,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    ) {
                        items(visible, key = { it.id }) { task ->
                            TaskRow(
                                task = task,
                                onToggleDone = { viewModel.toggleDone(task) },
                                onPostpone = { viewModel.postponeToTomorrow(task) },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

private fun TasksTab.label(s: com.dailycompany.ui.theme.AppStrings): String = when (this) {
    TasksTab.TODAY -> s.tasksTabToday
    TasksTab.UPCOMING -> s.tasksTabUpcoming
    TasksTab.NO_DATE -> s.tasksTabNoDate
    TasksTab.DONE -> s.tasksTabDone
}

@Composable
private fun TaskRow(
    task: Task,
    onToggleDone: () -> Unit,
    onPostpone: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onToggleDone) {
            Icon(
                imageVector = if (task.done) {
                    Icons.Outlined.CheckCircleOutline
                } else {
                    Icons.Outlined.Schedule
                },
                contentDescription = if (task.done) "Reabrir" else "Concluir",
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Start,
            )
            task.note.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        task.dueDate?.let {
            Text(
                text = it.format(DateTimeFormatter.ofPattern("d MMM").withLocale(Locale.getDefault())),
                style = MaterialTheme.typography.labelSmall,
            )
        }
        if (task.dueDate != null && task.dueDate < today) {
            TextButton(onClick = onPostpone) {
                Text(s.tasksPostponeTomorrow)
            }
        }
    }
}
