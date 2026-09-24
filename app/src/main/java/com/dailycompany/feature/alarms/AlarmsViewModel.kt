package com.dailycompany.feature.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailycompany.core.time.AppClock
import com.dailycompany.data.alarm.AlarmScheduler
import com.dailycompany.data.repository.AlarmRepository
import com.dailycompany.domain.model.Alarm
import com.dailycompany.domain.model.nextTrigger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado da lista de alarmes: os alarmes ordenados por hora/etiqueta (DAO). */
data class AlarmsUiState(
    val isLoading: Boolean = true,
    val alarms: List<Alarm> = emptyList(),
)

@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler,
    private val clock: AppClock,
) : ViewModel() {

    val uiState: StateFlow<AlarmsUiState> = repository.observeAll()
        .map { AlarmsUiState(isLoading = false, alarms = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AlarmsUiState(),
        )

    /**
     * Liga/desliga um alarme: persiste o estado e (re)agenda a próxima
     * ocorrência ou cancela o toque no `AlarmManager`.
     */
    fun setEnabled(alarm: Alarm, enabled: Boolean) {
        viewModelScope.launch {
            repository.setEnabled(alarm.id, enabled)
            if (enabled) {
                scheduler.schedule(
                    alarm.copy(enabled = true),
                    alarm.nextTrigger(clock.now()),
                )
            } else {
                scheduler.cancel(alarm.id)
            }
        }
    }
}