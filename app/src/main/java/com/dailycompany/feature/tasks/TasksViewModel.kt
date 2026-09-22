package com.dailycompany.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailycompany.core.time.AppClock
import com.dailycompany.data.repository.TaskRepository
import com.dailycompany.domain.model.Task
import com.dailycompany.domain.model.TaskPriority
import com.dailycompany.domain.model.TaskRepeat
import com.dailycompany.domain.model.newTask
import com.dailycompany.domain.model.nextDueDate
import java.time.LocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Abas do ecrã Tarefas. */
enum class TasksTab {
    TODAY,
    UPCOMING,
    NO_DATE,
    DONE,
}

/** Mensagem de snackbar emitida pelo ViewModel — a UI resolve o texto via AppStrings. */
enum class TaskSnackbar {
    DONE,
    REOPENED,
    POSTPONED,
}

data class TasksUiState(
    val isLoading: Boolean = true,
    val tasks: List<Task> = emptyList(),
    val selectedTab: TasksTab = TasksTab.TODAY,
    val snackbar: TaskSnackbar? = null,
)

/** Última ação destrutiva para permitir "Desfazer". */
sealed interface UndoAction {
    data class SetDone(val task: Task, val done: Boolean) : UndoAction
    data class MoveToTomorrow(val task: Task) : UndoAction

    /** Concluir uma tarefa repetitiva — repõe o estado anterior completo. */
    data class RollForward(val task: Task) : UndoAction
}

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val clock: AppClock,
) : ViewModel() {

    /** Data de hoje derivada do relógio injetado (testável via FixedClock). */
    val today: LocalDate get() = clock.today()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    private val _selectedTab = MutableStateFlow(TasksTab.TODAY)
    private val _undo = MutableStateFlow<UndoAction?>(null)
    private val _snackbarMessage = MutableStateFlow<TaskSnackbar?>(null)

    val uiState: StateFlow<TasksUiState> = combine(
        _tasks,
        _selectedTab,
        _undo.map { it != null },
        _snackbarMessage,
    ) { tasks, tab, hasUndo, msg ->
        TasksUiState(
            isLoading = false,
            tasks = tasks,
            selectedTab = tab,
            snackbar = msg,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksUiState())

    init {
        viewModelScope.launch {
            repository.observeAll().collect { tasks ->
                _tasks.value = tasks
            }
        }
    }

    fun selectTab(tab: TasksTab) {
        _selectedTab.value = tab
    }

    fun quickAdd(title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.save(
                newTask(
                    title = trimmed,
                    note = "",
                    dueDate = clock.today(),
                    dueTime = null,
                    priority = TaskPriority.NONE,
                    createdAt = clock.now(),
                ),
            )
        }
    }

    fun toggleDone(task: Task) {
        viewModelScope.launch {
            when {
                task.done -> {
                    repository.setDone(task.id, false)
                    _undo.value = UndoAction.SetDone(task, true)
                    _snackbarMessage.value = TaskSnackbar.REOPENED
                }
                task.repeat == TaskRepeat.NONE -> {
                    repository.setDone(task.id, true)
                    _undo.value = UndoAction.SetDone(task, false)
                    _snackbarMessage.value = TaskSnackbar.DONE
                }
                else -> {
                    // Repetitiva: em vez de "concluir", avança para a próxima
                    // ocorrência (continua por concluir no dia certo).
                    repository.save(
                        task.copy(done = false, dueDate = task.nextDueDate(clock.today())),
                    )
                    _undo.value = UndoAction.RollForward(task)
                    _snackbarMessage.value = TaskSnackbar.DONE
                }
            }
        }
    }

    fun postponeToTomorrow(task: Task) {
        viewModelScope.launch {
            repository.setDueDate(task.id, clock.today().plusDays(1))
            _undo.value = UndoAction.MoveToTomorrow(task)
            _snackbarMessage.value = TaskSnackbar.POSTPONED
        }
    }

    fun undo() {
        val action = _undo.value ?: return
        viewModelScope.launch {
            when (action) {
                is UndoAction.SetDone -> repository.setDone(action.task.id, action.task.done)
                is UndoAction.MoveToTomorrow -> repository.setDueDate(action.task.id, action.task.dueDate)
                is UndoAction.RollForward -> repository.save(action.task)
            }
        }
        _undo.value = null
    }

    fun consumeSnackbar() {
        _snackbarMessage.value = null
    }
}
