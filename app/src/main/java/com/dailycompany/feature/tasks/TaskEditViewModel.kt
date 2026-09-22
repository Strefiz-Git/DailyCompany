package com.dailycompany.feature.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailycompany.core.time.AppClock
import com.dailycompany.data.repository.TaskRepository
import com.dailycompany.domain.model.Task
import com.dailycompany.domain.model.TaskPriority
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** Mensagem de snackbar do formulário — a UI resolve o texto via AppStrings. */
enum class TaskEditSnackbar {
    DELETED,
}

/** Eventos one-shot: quando fechar o formulário a UI navega para trás. */
sealed interface TaskEditEvent {
    data object Close : TaskEditEvent
}

/** Estado do formulário de criar/editar tarefa. */
data class TaskEditUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val title: String = "",
    val note: String = "",
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val priority: TaskPriority = TaskPriority.NONE,
    val canSave: Boolean = false,
    val snackbar: TaskEditSnackbar? = null,
)

@HiltViewModel
class TaskEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TaskRepository,
    private val clock: AppClock,
) : ViewModel() {

    /** Id da tarefa a editar (0 = criar nova). */
    private val taskId: Long = savedStateHandle.get<Long>(KEY_TASK_ID) ?: 0L

    /** Cópia da tarefa antes de apagar — permite "Anular" repondo-a. */
    private var deletedCopy: Task? = null

    private val _uiState = MutableStateFlow(
        TaskEditUiState(isLoading = taskId > 0L, isEditing = taskId > 0L),
    )
    val uiState: StateFlow<TaskEditUiState> = _uiState.asStateFlow()

    private val _events = Channel<TaskEditEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        if (taskId > 0L) {
            viewModelScope.launch {
                val task = repository.getById(taskId)
                task?.let { loaded ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        title = loaded.title,
                        note = loaded.note,
                        dueDate = loaded.dueDate,
                        dueTime = loaded.dueTime,
                        priority = loaded.priority,
                        canSave = loaded.title.isNotBlank(),
                    )
                } ?: run {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun onTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(title = value, canSave = value.isNotBlank())
    }

    fun onNoteChange(value: String) {
        _uiState.value = _uiState.value.copy(note = value)
    }

    fun onDateChange(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(dueDate = date)
    }

    fun onTimeChange(time: LocalTime?) {
        _uiState.value = _uiState.value.copy(dueTime = time)
    }

    fun onPriorityChange(priority: TaskPriority) {
        _uiState.value = _uiState.value.copy(priority = priority)
    }

    fun save() {
        if (!_uiState.value.canSave) return
        viewModelScope.launch {
            val state = _uiState.value
            repository.save(
                Task(
                    id = taskId,
                    title = state.title.trim(),
                    note = state.note.trim(),
                    dueDate = state.dueDate,
                    dueTime = state.dueTime,
                    priority = state.priority,
                    done = false,
                    createdAt = clock.now(),
                ),
            )
            _events.send(TaskEditEvent.Close)
        }
    }

    /** Apaga a tarefa e pede confirmação visual via snackbar com "Anular". */
    fun delete() {
        viewModelScope.launch {
            val task = repository.getById(taskId)
            deletedCopy = task
            repository.deleteById(taskId)
            _uiState.value = _uiState.value.copy(snackbar = TaskEditSnackbar.DELETED)
        }
    }

    /** Repõe a tarefa apagada (ação "Anular" da snackbar). */
    fun undoDelete() {
        viewModelScope.launch {
            deletedCopy?.let { repository.save(it) }
            deletedCopy = null
            _uiState.value = _uiState.value.copy(snackbar = null)
            _events.send(TaskEditEvent.Close)
        }
    }

    /** Snackbar dispensada sem "Anular" — fecha na mesma. */
    fun closeAfterDelete() {
        viewModelScope.launch {
            _events.send(TaskEditEvent.Close)
        }
    }

    companion object {
        const val KEY_TASK_ID = "taskId"
    }
}