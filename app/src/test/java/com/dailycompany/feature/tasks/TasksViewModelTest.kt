package com.dailycompany.feature.tasks

import app.cash.turbine.expectMostRecentItem
import app.cash.turbine.test
import com.dailycompany.core.MainDispatcherRule
import com.dailycompany.core.time.FixedClock
import com.dailycompany.domain.model.Task
import com.dailycompany.domain.model.TaskRepeat
import com.dailycompany.domain.model.TaskPriority
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

/** Testes do `TasksViewModel` (Fase 1 — Tarefas). */
@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 9, 22)

    private fun task(
        id: Long,
        dueDate: LocalDate? = null,
        done: Boolean = false,
        repeat: TaskRepeat = TaskRepeat.NONE,
    ) = Task(
        id = id,
        title = "Tarefa $id",
        dueDate = dueDate,
        done = done,
        repeat = repeat,
        createdAt = LocalDateTime.of(2026, 9, 1, 8, 0),
    )

    @Test
    fun `quickAdd cria tarefa para hoje`() = runTest(mainDispatcherRule.dispatcher) {
        val vm = TasksViewModel(FakeTaskRepository(), FixedClock(today))

        vm.uiState.test {
            expectMostRecentItem() // estado inicial vazio

            vm.quickAdd("Comprar pão")
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertEquals(1, state.tasks.size)
            val saved = state.tasks.single()
            assertEquals("Comprar pão", saved.title)
            assertEquals(today, saved.dueDate)
            assertFalse(saved.done)
            assertEquals(TaskPriority.NONE, saved.priority)
        }
    }

    @Test
    fun `quickAdd ignora titulo vazio`() = runTest(mainDispatcherRule.dispatcher) {
        val vm = TasksViewModel(FakeTaskRepository(), FixedClock(today))

        vm.uiState.test {
            expectMostRecentItem() // estado inicial vazio

            vm.quickAdd("   ")
            advanceUntilIdle()

            // titulo vazio → sem nova emissao; le o valor corrente (subscricao ativa)
            assertTrue(vm.uiState.value.tasks.isEmpty())
        }
    }

    @Test
    fun `toggleDone emite snackbar e undo repoe`() = runTest(mainDispatcherRule.dispatcher) {
        val vm = TasksViewModel(
            FakeTaskRepository(initial = listOf(task(1, dueDate = today))),
            FixedClock(today),
        )

        vm.uiState.test {
            expectMostRecentItem() // carrega a tarefa

            vm.toggleDone(task(1, dueDate = today))
            advanceUntilIdle()

            val doneState = expectMostRecentItem()
            assertEquals(TaskSnackbar.DONE, doneState.snackbar)
            assertTrue(doneState.tasks.single().done)

            vm.undo()
            vm.consumeSnackbar()
            advanceUntilIdle()

            val restored = expectMostRecentItem()
            assertFalse(restored.tasks.single().done)
        }
    }

    @Test
    fun `postponeToTomorrow move para amanha e undo repoe`() = runTest(mainDispatcherRule.dispatcher) {
        val vm = TasksViewModel(
            FakeTaskRepository(initial = listOf(task(1, dueDate = today))),
            FixedClock(today),
        )

        vm.uiState.test {
            expectMostRecentItem()

            vm.postponeToTomorrow(task(1, dueDate = today))
            advanceUntilIdle()

            val postponed = expectMostRecentItem()
            assertEquals(TaskSnackbar.POSTPONED, postponed.snackbar)
            assertEquals(today.plusDays(1), postponed.tasks.single().dueDate)

            vm.undo()
            vm.consumeSnackbar()
            advanceUntilIdle()

            val restored = expectMostRecentItem()
            assertEquals(today, restored.tasks.single().dueDate)
        }
    }

    @Test
    fun `toggleDone em tarefa repetitiva avanca para a proxima ocorrencia`() = runTest(mainDispatcherRule.dispatcher) {
        val repo = FakeTaskRepository(initial = listOf(task(1L, dueDate = today, repeat = TaskRepeat.DAILY)))
        val vm = TasksViewModel(repo, FixedClock(today))

        vm.uiState.test {
            expectMostRecentItem() // carrega a tarefa não feita

            vm.toggleDone(task(1L, dueDate = today, repeat = TaskRepeat.DAILY))
            advanceUntilIdle()

            val rolled = expectMostRecentItem()
            assertEquals(TaskSnackbar.DONE, rolled.snackbar)
            // não fica concluída: avança para amanhã e mantém-se por fazer
            val advanced = rolled.tasks.single()
            assertFalse(advanced.done)
            assertEquals(today.plusDays(1), advanced.dueDate)

            vm.undo()
            vm.consumeSnackbar()
            advanceUntilIdle()

            val restored = expectMostRecentItem()
            assertEquals(today, restored.tasks.single().dueDate)
            assertFalse(restored.tasks.single().done)
        }
    }
}
