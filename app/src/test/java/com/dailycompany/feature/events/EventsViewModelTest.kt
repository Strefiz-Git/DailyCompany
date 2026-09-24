package com.dailycompany.feature.events

import app.cash.turbine.expectMostRecentItem
import app.cash.turbine.test
import com.dailycompany.core.MainDispatcherRule
import com.dailycompany.core.time.FixedClock
import com.dailycompany.domain.model.Event
import com.dailycompany.domain.model.EventFrequency
import com.dailycompany.domain.model.RecurrenceRule
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Testes do `EventsViewModel` (Fase 2 — Agenda). */
@OptIn(ExperimentalCoroutinesApi::class)
class EventsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 9, 22)

    private fun event(
        id: Long,
        title: String = "Evento $id",
        date: LocalDate,
        recurrence: RecurrenceRule = RecurrenceRule(),
    ) = Event(
        id = id,
        uid = "event:$id",
        title = title,
        date = date,
        recurrence = recurrence,
        createdAt = LocalDateTime.of(2026, 1, 1, 8, 0),
    )

    @Test
    fun `carrega eventos e expande as ocorrencias da janela de 3 meses`() = runTest(mainDispatcherRule.dispatcher) {
        val repo = FakeEventRepository(
            initial = listOf(
                event(1L, date = today),                        // hoje
                event(2L, date = today.plusMonths(1)),          // dentro da janela
                event(3L, date = today.minusMonths(1)),         // passado — fora
                event(4L, date = today.plusMonths(6)),          // fora da janela
            ),
        )
        val vm = EventsViewModel(repo, FixedClock(today))

        // O init já carregou os eventos (dispatcher unconfined) — a 1.ª emissão é o estado final.
        vm.uiState.test {
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertEquals(
                listOf(today, today.plusMonths(1)),
                state.occurrences.map { it.date },
            )
        }
    }

    @Test
    fun `repeticao diaria expande todas as datas na janela`() = runTest(mainDispatcherRule.dispatcher) {
        val repo = FakeEventRepository(
            initial = listOf(
                event(1L, date = today, recurrence = RecurrenceRule(frequency = EventFrequency.DAILY)),
            ),
        )
        val vm = EventsViewModel(repo, FixedClock(today))

        vm.uiState.test {
            val state = expectMostRecentItem()
            // da data do evento até hoje+3 meses (inclusive)
            val expectedCount = java.time.temporal.ChronoUnit.DAYS.between(today, today.plusMonths(3)) + 1
            assertEquals(expectedCount, state.occurrences.size.toLong())
            assertEquals(today, state.occurrences.first().date)
            assertEquals(today.plusMonths(3), state.occurrences.last().date)
        }
    }

    @Test
    fun `sem eventos fica vazio e sai do loading`() = runTest(mainDispatcherRule.dispatcher) {
        val vm = EventsViewModel(FakeEventRepository(), FixedClock(today))

        vm.uiState.test {
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertTrue(state.occurrences.isEmpty())
        }
    }

    @Test
    fun `ocorrencias com hora saem ordenadas por data e hora`() = runTest(mainDispatcherRule.dispatcher) {
        val repo = FakeEventRepository(
            initial = listOf(
                event(1L, title = "Manhã", date = today.plusDays(1)).copy(time = LocalTime.of(9, 0)),
                event(2L, title = "Cedo", date = today.plusDays(1)).copy(time = LocalTime.of(8, 0)),
            ),
        )
        val vm = EventsViewModel(repo, FixedClock(today))

        vm.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(listOf("Cedo", "Manhã"), state.occurrences.map { it.event.title })
        }
    }
}