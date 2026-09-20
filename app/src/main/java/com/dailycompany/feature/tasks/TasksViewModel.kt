package com.dailycompany.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailycompany.core.time.AppClock
import com.dailycompany.data.repository.TaskRepository
import com.dailycompany.domain.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

data class TasksUiState(
    val isLoading: Boolean = true,
    val tasks: List<Task> = emptyList(),
    val selectedTab: TasksTab = TasksTab.TODAY,
    val snackbarMessage: String? = null,
)

/** Última ação destrutiva para permitir "Desfazer". */
sealed interface UndoAction {
    data class SetDone(val task: Task, val done: Boolean) : UndoAction
    data class MoveToTomorrow(val task: Task) : UndoAction
}

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val clock: AppClock,
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    private val _selectedTab = MutableStateFlow(TasksTab.TODAY)
    private val _undo = MutableStateFlow<UndoAction?>(null)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    private val uiState: StateFlow<TasksUiState> = combine(
        _tasks,
        _selectedTab,
        _undo.map { it != null },
        _snackbarMessage,
    ) { tasks, tab, hasUndo, msg ->
        TasksUiState(
            isLoading = false,
            tasks = tasks,
            selectedTab = tab,
            snackbarMessage = msg,
        )
    }.stateIn(viewModelScope.sharedFlowScope, SharingStarted.WhileSubscribed(5_000), TasksUiState())

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
        val newDone = !task.done
        viewModelScope.launch {
            repository.setDone(task.id, newDone)
            _undo.value = UndoAction.SetDone(task, task.done)
            _snackbarMessage.value = if (newDone) "Concluída" else "Reaberta"
        }
    }

    fun postponeToTomorrow(task: Task) {
        viewModelScope.launch {
            repository.setDueDate(task.id, clock.today().plusDays(1))
            _undo.value = UndoAction.MoveToTomorrow(task)
            _snackbarMessage.value = "Adiada para amanhã"
        }
    }

    fun undo() {
        val action = _undo.value ?: return
        viewModelScope.launch {
            when (action) {
                is UndoAction.SetDone -> repository.setDone(action.task.id, action.task.done)
                is UndoAction.MoveToTomorrow -> repository.setDueDate(action.task.id, action.task.dueDate)
            }
        }
        _undo.value = null
    }

    fun consumeSnackbar() {
        _snackbarMessage.value = null
    }
}
