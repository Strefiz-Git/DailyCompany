package com.dailycompany.feature.alarms

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailycompany.core.time.AppClock
import com.dailycompany.data.alarm.AlarmScheduler
import com.dailycompany.data.repository.AlarmRepository
import com.dailycompany.domain.model.Alarm
import com.dailycompany.domain.model.nextTrigger
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** Mensagem de snackbar do formulário — a UI resolve o texto via AppStrings. */
enum class AlarmEditSnackbar {
    DELETED,
}

/** Quando fechar o formulário a UI navega para trás. */
sealed interface AlarmEditEvent {
    data object Close : AlarmEditEvent
}

/** Estado do formulário de criar/editar alarme (stateless, UDF). */
data class AlarmEditUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val time: LocalTime = LocalTime.of(7, 0),
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val label: String = "",
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val autoSnoozeMinutes: Int = 0,
    val snackbar: AlarmEditSnackbar? = null,
)

@HiltViewModel
class AlarmEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler,
    private val clock: AppClock,
) : ViewModel() {

    private val alarmId: Long = savedStateHandle.get<Long>(KEY_ALARM_ID) ?: 0L

    /** Alarme carregado (edição) — preserva uid/createdAt ao guardar. */
    private var original: Alarm? = null

    /** Cópia antes de apagar — permite "Anular" repor o alarme e reagendar. */
    private var deletedCopy: Alarm? = null

    private val _uiState = MutableStateFlow(
        AlarmEditUiState(isLoading = alarmId > 0L, isEditing = alarmId > 0L),
    )
    val uiState: StateFlow<AlarmEditUiState> = _uiState.asStateFlow()

    private val _events = Channel<AlarmEditEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        if (alarmId > 0L) {
            viewModelScope.launch {
                val loaded = repository.getById(alarmId)
                if (loaded == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@launch
                }
                original = loaded
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    time = loaded.time,
                    repeatDays = loaded.repeatDays,
                    label = loaded.label,
                    soundEnabled = loaded.soundEnabled,
                    vibrateEnabled = loaded.vibrateEnabled,
                    autoSnoozeMinutes = loaded.autoSnoozeMinutes,
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun onTimeChange(time: LocalTime) {
        _uiState.value = _uiState.value.copy(time = time)
    }

    fun onWeekdayToggle(day: DayOfWeek) {
        val days = _uiState.value.repeatDays
        _uiState.value = _uiState.value.copy(
            repeatDays = if (day in days) days - day else days + day,
        )
    }

    fun onLabelChange(value: String) {
        _uiState.value = _uiState.value.copy(label = value)
    }

    fun onSoundChange(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(soundEnabled = enabled)
    }

    fun onVibrateChange(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(vibrateEnabled = enabled)
    }

    fun onAutoSnoozeChange(minutes: Int) {
        _uiState.value = _uiState.value.copy(autoSnoozeMinutes = minutes.coerceAtLeast(0))
    }

    /** Guarda e (re)agenda no `AlarmManager`; fecha só depois de agendado. */
    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            val draft = Alarm(
                id = alarmId,
                uid = original?.uid ?: UUID.randomUUID().toString(),
                time = state.time,
                repeatDays = state.repeatDays,
                label = state.label.trim(),
                soundEnabled = state.soundEnabled,
                vibrateEnabled = state.vibrateEnabled,
                autoSnoozeMinutes = state.autoSnoozeMinutes,
                enabled = true,
                createdAt = original?.createdAt ?: clock.now(),
            )
            // O id real só existe depois do insert (novos alarmes entram com 0).
            val saved = draft.copy(id = repository.save(draft))
            scheduler.schedule(saved, saved.nextTrigger(clock.now()))
            _events.send(AlarmEditEvent.Close)
        }
    }

    fun delete() {
        viewModelScope.launch {
            deletedCopy = repository.getById(alarmId)
            scheduler.cancel(alarmId)
            repository.deleteById(alarmId)
            _uiState.value = _uiState.value.copy(snackbar = AlarmEditSnackbar.DELETED)
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            deletedCopy?.let { copy ->
                repository.save(copy)
                if (copy.enabled) {
                    scheduler.schedule(copy, copy.nextTrigger(clock.now()))
                }
            }
            deletedCopy = null
            _uiState.value = _uiState.value.copy(snackbar = null)
            _events.send(AlarmEditEvent.Close)
        }
    }

    fun closeAfterDelete() {
        viewModelScope.launch {
            _events.send(AlarmEditEvent.Close)
        }
    }

    companion object {
        const val KEY_ALARM_ID = "alarmId"
    }
}