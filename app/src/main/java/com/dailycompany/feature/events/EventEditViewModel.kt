package com.dailycompany.feature.events

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailycompany.core.time.AppClock
import com.dailycompany.data.repository.EventRepository
import com.dailycompany.domain.model.Event
import com.dailycompany.domain.model.EventFrequency
import com.dailycompany.domain.model.RecurrenceEnd
import com.dailycompany.domain.model.RecurrenceRule
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalDate
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
enum class EventEditSnackbar {
    DELETED,
}

/** Eventos one-shot: quando fechar o formulário a UI navega para trás. */
sealed interface EventEditEvent {
    data object Close : EventEditEvent
}

/**
 * Opções de repetição apresentadas no seletor (mapeiam para [RecurrenceRule]).
 * "Dias da semana" e "Semanal" são ambas `WEEKLY` — a primeira com dias
 * escolhidos, a segunda sem (mesmo dia da semana).
 */
enum class RecurrenceOption {
    NEVER,
    DAILY,
    WEEKDAYS,
    WEEKLY,
    MONTHLY,
    YEARLY;

    companion object {
        fun from(rule: RecurrenceRule): RecurrenceOption = when {
            rule.frequency == EventFrequency.NONE -> NEVER
            rule.frequency == EventFrequency.DAILY -> DAILY
            rule.frequency == EventFrequency.WEEKLY && rule.weekDays.isNotEmpty() -> WEEKDAYS
            rule.frequency == EventFrequency.WEEKLY -> WEEKLY
            rule.frequency == EventFrequency.MONTHLY -> MONTHLY
            else -> YEARLY
        }
    }
}

/** Estado do formulário de criar/editar evento. */
data class EventEditUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val title: String = "",
    val date: LocalDate,
    val isAllDay: Boolean = false,
    val time: LocalTime? = null,
    val location: String = "",
    val notes: String = "",
    val recurrence: RecurrenceRule = RecurrenceRule(),
    val canSave: Boolean = false,
    val snackbar: EventEditSnackbar? = null,
)

@HiltViewModel
class EventEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EventRepository,
    private val clock: AppClock,
) : ViewModel() {

    /** Id do evento a editar (0 = criar novo). */
    private val eventId: Long = savedStateHandle.get<Long>(KEY_EVENT_ID) ?: 0L

    /** Evento carregado (edição) — preserva uid/createdAt; base do "Anular". */
    private var original: Event? = null

    /** Cópia do evento antes de apagar — permite "Anular" repondo-o. */
    private var deletedCopy: Event? = null

    private val _uiState = MutableStateFlow(
        EventEditUiState(
            isLoading = eventId > 0L,
            isEditing = eventId > 0L,
            date = clock.today(),
        ),
    )
    val uiState: StateFlow<EventEditUiState> = _uiState.asStateFlow()

    private val _events = Channel<EventEditEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        if (eventId > 0L) {
            viewModelScope.launch {
                val loaded = repository.getById(eventId)
                loaded?.let { event ->
                    original = event
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        title = event.title,
                        date = event.date,
                        isAllDay = event.isAllDay,
                        time = event.time,
                        location = event.location.orEmpty(),
                        notes = event.notes.orEmpty(),
                        recurrence = event.recurrence,
                        canSave = event.title.isNotBlank(),
                    )
                } ?: run {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        } else {
            // Novo evento: formulário já nasce com a data de hoje (relógio injetado).
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun onTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(title = value, canSave = value.isNotBlank())
    }

    fun onDateChange(date: LocalDate) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    /** Dia inteiro: sem hora (limpa o tempo escolhido). */
    fun onAllDayChange(allDay: Boolean) {
        _uiState.value = _uiState.value.copy(isAllDay = allDay, time = if (allDay) null else _uiState.value.time)
    }

    fun onTimeChange(time: LocalTime?) {
        _uiState.value = _uiState.value.copy(isAllDay = false, time = time)
    }

    fun onLocationChange(value: String) {
        _uiState.value = _uiState.value.copy(location = value)
    }

    fun onNotesChange(value: String) {
        _uiState.value = _uiState.value.copy(notes = value)
    }

    fun onRecurrenceOptionChange(option: RecurrenceOption) {
        val current = _uiState.value.recurrence
        _uiState.value = _uiState.value.copy(
            recurrence = when (option) {
                RecurrenceOption.NEVER -> current.copy(frequency = EventFrequency.NONE, weekDays = emptySet())
                RecurrenceOption.DAILY -> current.copy(frequency = EventFrequency.DAILY, weekDays = emptySet())
                RecurrenceOption.WEEKDAYS -> current.copy(
                    frequency = EventFrequency.WEEKLY,
                    weekDays = current.weekDays.ifEmpty { defaultWeekdays },
                )
                RecurrenceOption.WEEKLY -> current.copy(frequency = EventFrequency.WEEKLY, weekDays = emptySet())
                RecurrenceOption.MONTHLY -> current.copy(
                    frequency = EventFrequency.MONTHLY,
                    weekDays = emptySet(),
                    monthPosition = null,
                )
                RecurrenceOption.YEARLY -> current.copy(
                    frequency = EventFrequency.YEARLY,
                    weekDays = emptySet(),
                    monthPosition = null,
                )
            },
        )
    }

    fun onWeekdayToggle(day: DayOfWeek) {
        val current = _uiState.value.recurrence
        val days = if (day in current.weekDays) current.weekDays - day else current.weekDays + day
        _uiState.value = _uiState.value.copy(recurrence = current.copy(weekDays = days))
    }

    fun onEndChange(end: RecurrenceEnd) {
        _uiState.value = _uiState.value.copy(recurrence = _uiState.value.recurrence.copy(end = end))
    }

    fun onEndDateChange(date: LocalDate) {
        onEndChange(RecurrenceEnd.OnDate(date))
    }

    fun onEndCountChange(count: Int) {
        onEndChange(RecurrenceEnd.AfterCount(count.coerceAtLeast(1)))
    }

    fun save() {
        if (!_uiState.value.canSave) return
        viewModelScope.launch {
            val state = _uiState.value
            repository.save(
                Event(
                    id = eventId,
                    uid = original?.uid ?: UUID.randomUUID().toString(),
                    title = state.title.trim(),
                    date = state.date,
                    time = if (state.isAllDay) null else state.time,
                    isAllDay = state.isAllDay,
                    location = state.location.trim().ifBlank { null },
                    notes = state.notes.trim().ifBlank { null },
                    colorKey = null,
                    recurrence = state.recurrence,
                    createdAt = original?.createdAt ?: clock.now(),
                ),
            )
            _events.send(EventEditEvent.Close)
        }
    }

    /** Apaga o evento e pede confirmação visual via snackbar com "Anular". */
    fun delete() {
        viewModelScope.launch {
            deletedCopy = repository.getById(eventId)
            repository.deleteById(eventId)
            _uiState.value = _uiState.value.copy(snackbar = EventEditSnackbar.DELETED)
        }
    }

    /** Repõe o evento apagado (ação "Anular" da snackbar). */
    fun undoDelete() {
        viewModelScope.launch {
            deletedCopy?.let { repository.save(it) }
            deletedCopy = null
            _uiState.value = _uiState.value.copy(snackbar = null)
            _events.send(EventEditEvent.Close)
        }
    }

    /** Snackbar dispensada sem "Anular" — fecha na mesma. */
    fun closeAfterDelete() {
        viewModelScope.launch {
            _events.send(EventEditEvent.Close)
        }
    }

    companion object {
        const val KEY_EVENT_ID = "eventId"

        /** Segunda a sexta — pré-seleção de "Dias da semana". */
        private val defaultWeekdays: Set<DayOfWeek> =
            setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
    }
}