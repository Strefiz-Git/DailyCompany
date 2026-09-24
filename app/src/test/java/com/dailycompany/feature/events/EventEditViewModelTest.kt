package com.dailycompany.feature.events

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.dailycompany.core.MainDispatcherRule
import com.dailycompany.core.time.FixedClock
import com.dailycompany.domain.model.Event
import com.dailycompany.domain.model.EventFrequency
import com.dailycompany.domain.model.RecurrenceEnd
import com.dailycompany.domain.model.RecurrenceRule
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Testes do `EventEditViewModel` (Fase 2 — formulário de evento). */
@OptIn(ExperimentalCoroutinesApi::class)
class EventEditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 9, 22)

    private fun event(id: Long = 1L) = Event(
        id = id,
        uid = "event:uid-$id",
        title = "Aniversário",
        date = today,
        time = LocalTime.of(10, 0),
        location = "Casa",
        notes = "trazer bolo",
        createdAt = LocalDateTime.of(2026, 1, 1, 8, 0),
    )

    @Test
    fun `novo formulario comeca vazio com data de hoje e sem save`() = runTest {
        val vm = EventEditViewModel(SavedStateHandle(), FakeEventRepository(), FixedClock(today))

        assertFalse(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.isEditing)
        assertEquals("", vm.uiState.value.title)
        assertEquals(today, vm.uiState.value.date)
        assertFalse(vm.uiState.value.canSave)
    }

    @Test
    fun `titulo preenchido ativa o save`() = runTest {
        val vm = EventEditViewModel(SavedStateHandle(), FakeEventRepository(), FixedClock(today))

        vm.onTitleChange("Consulta")
        assertTrue(vm.uiState.value.canSave)
        vm.onTitleChange("")
        assertFalse(vm.uiState.value.canSave)
    }

    @Test
    fun `save cria evento com uid e fecha`() = runTest {
        val repo = FakeEventRepository()
        val vm = EventEditViewModel(SavedStateHandle(), repo, FixedClock(today))

        vm.onTitleChange("Consulta médica")
        vm.onDateChange(today)
        vm.onTimeChange(LocalTime.of(9, 30))
        vm.onLocationChange("Centro de saúde")

        vm.events.test {
            vm.save()
            assertEquals(EventEditEvent.Close, awaitItem())
        }

        val saved = repo.observeAll().first().single()
        assertEquals("Consulta médica", saved.title)
        assertEquals(today, saved.date)
        assertEquals(LocalTime.of(9, 30), saved.time)
        assertEquals("Centro de saúde", saved.location)
        assertFalse(saved.isAllDay)
        assertTrue(saved.uid.isNotBlank())
        assertEquals(EventFrequency.NONE, saved.recurrence.frequency)
    }

    @Test
    fun `dia inteiro limpa a hora`() = runTest {
        val vm = EventEditViewModel(SavedStateHandle(), FakeEventRepository(), FixedClock(today))

        vm.onTimeChange(LocalTime.of(9, 0))
        vm.onAllDayChange(true)
        assertTrue(vm.uiState.value.isAllDay)
        assertNull(vm.uiState.value.time)

        vm.onAllDayChange(false)
        assertFalse(vm.uiState.value.isAllDay)
        assertNull(vm.uiState.value.time)
    }

    @Test
    fun `escolher dias da semana preenche seg a sex e permite alternar`() = runTest {
        val vm = EventEditViewModel(SavedStateHandle(), FakeEventRepository(), FixedClock(today))

        vm.onRecurrenceOptionChange(RecurrenceOption.WEEKDAYS)
        val rule = vm.uiState.value.recurrence
        assertEquals(EventFrequency.WEEKLY, rule.frequency)
        assertEquals(
            setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            rule.weekDays,
        )

        vm.onWeekdayToggle(DayOfWeek.MONDAY)
        assertFalse(vm.uiState.value.recurrence.weekDays.contains(DayOfWeek.MONDAY))
        vm.onWeekdayToggle(DayOfWeek.MONDAY)
        assertTrue(vm.uiState.value.recurrence.weekDays.contains(DayOfWeek.MONDAY))
    }

    @Test
    fun `fim da serie alterna para data e contagem`() = runTest {
        val vm = EventEditViewModel(SavedStateHandle(), FakeEventRepository(), FixedClock(today))

        vm.onEndChange(RecurrenceEnd.OnDate(today.plusYears(1)))
        assertEquals(RecurrenceEnd.OnDate(today.plusYears(1)), vm.uiState.value.recurrence.end)

        vm.onEndCountChange(12)
        assertEquals(RecurrenceEnd.AfterCount(12), vm.uiState.value.recurrence.end)

        vm.onEndChange(RecurrenceEnd.Never)
        assertEquals(RecurrenceEnd.Never, vm.uiState.value.recurrence.end)
    }

    @Test
    fun `save persiste a regra de repeticao escolhida`() = runTest {
        val repo = FakeEventRepository()
        val vm = EventEditViewModel(SavedStateHandle(), repo, FixedClock(today))

        vm.onTitleChange("Ginásio")
        vm.onRecurrenceOptionChange(RecurrenceOption.WEEKDAYS)

        vm.events.test {
            vm.save()
            assertEquals(EventEditEvent.Close, awaitItem())
        }

        val saved = repo.observeAll().first().single()
        assertEquals(EventFrequency.WEEKLY, saved.recurrence.frequency)
        assertEquals(5, saved.recurrence.weekDays.size)
    }

    @Test
    fun `edicao carrega o evento existente`() = runTest {
        val repo = FakeEventRepository(initial = listOf(event(1L)))
        val vm = EventEditViewModel(
            SavedStateHandle(mapOf(EventEditViewModel.KEY_EVENT_ID to 1L)),
            repo,
            FixedClock(today),
        )

        assertTrue(vm.uiState.value.isEditing) // carrega em background (unconfined: já concluído)

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isEditing)
        assertEquals("Aniversário", state.title)
        assertEquals(today, state.date)
        assertEquals(LocalTime.of(10, 0), state.time)
        assertEquals("Casa", state.location)
        assertEquals("trazer bolo", state.notes)
        assertTrue(state.canSave)
    }

    @Test
    fun `edicao guarda preservando uid e createdAt`() = runTest {
        val repo = FakeEventRepository(initial = listOf(event(1L)))
        val vm = EventEditViewModel(
            SavedStateHandle(mapOf(EventEditViewModel.KEY_EVENT_ID to 1L)),
            repo,
            FixedClock(today),
        )
        advanceUntilIdle()

        vm.onTitleChange("Aniversário da Mãe")
        vm.onRecurrenceOptionChange(RecurrenceOption.YEARLY)

        vm.events.test {
            vm.save()
            assertEquals(EventEditEvent.Close, awaitItem())
        }

        val saved = repo.observeAll().first().single()
        assertEquals(1L, saved.id)
        assertEquals("event:uid-1", saved.uid)
        assertEquals(LocalDateTime.of(2026, 1, 1, 8, 0), saved.createdAt)
        assertEquals(EventFrequency.YEARLY, saved.recurrence.frequency)
    }

    @Test
    fun `apagar emite snackbar e undo repoe o evento`() = runTest {
        val repo = FakeEventRepository(initial = listOf(event(1L)))
        val vm = EventEditViewModel(
            SavedStateHandle(mapOf(EventEditViewModel.KEY_EVENT_ID to 1L)),
            repo,
            FixedClock(today),
        )

        vm.delete()
        assertEquals(EventEditSnackbar.DELETED, vm.uiState.value.snackbar)
        assertNull(repo.getById(1L))

        vm.undoDelete()
        assertNull(vm.uiState.value.snackbar)
        assertNotNull(repo.getById(1L))
    }

    @Test
    fun `novo evento recebe uid gerado`() = runTest {
        val repo = FakeEventRepository()
        val vm = EventEditViewModel(SavedStateHandle(), repo, FixedClock(today))
        vm.onTitleChange("Sem uid ainda")

        vm.events.test {
            vm.save()
            assertEquals(EventEditEvent.Close, awaitItem())
        }

        val saved = repo.observeAll().first().single()
        assertTrue(saved.uid.isNotBlank())
        assertNotEquals("", saved.uid)
    }
}