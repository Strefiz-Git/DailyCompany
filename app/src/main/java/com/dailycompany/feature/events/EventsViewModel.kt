package com.dailycompany.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailycompany.core.time.AppClock
import com.dailycompany.data.repository.EventRepository
import com.dailycompany.domain.model.Event
import com.dailycompany.domain.model.EventOccurrence
import com.dailycompany.domain.model.upcomingOccurrences
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Estado da Agenda: "próximos eventos" expandidos na janela de 3 meses a
 * partir de hoje. As ocorrências são **calculadas** (R6), nunca gravadas.
 */
data class EventsUiState(
    val isLoading: Boolean = true,
    val occurrences: List<EventOccurrence> = emptyList(),
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val repository: EventRepository,
    private val clock: AppClock,
) : ViewModel() {

    /** Data de hoje derivada do relógio injetado (testável via FixedClock). */
    val today: LocalDate get() = clock.today()

    private val _isLoading = MutableStateFlow(true)
    private val _events = MutableStateFlow<List<Event>>(emptyList())

    val uiState: StateFlow<EventsUiState> = combine(_isLoading, _events) { loading, events ->
        EventsUiState(
            isLoading = loading,
            occurrences = upcomingOccurrences(
                events = events,
                from = clock.today(),
                to = clock.today().plusMonths(WINDOW_MONTHS),
            ),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EventsUiState())

    init {
        viewModelScope.launch {
            repository.observeAll().collect { events ->
                _events.value = events
                _isLoading.value = false
            }
        }
    }

    companion object {
        /** Janela da Agenda: hoje .. +3 meses (critério "consulta a 3 meses"). */
        const val WINDOW_MONTHS = 3L
    }
}