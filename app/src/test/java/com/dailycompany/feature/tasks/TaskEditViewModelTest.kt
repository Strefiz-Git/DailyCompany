package com.dailycompany.feature.tasks

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.dailycompany.core.MainDispatcherRule
import com.dailycompany.core.time.FixedClock
import com.dailycompany.domain.model.Task
import com.dailycompany.domain.model.TaskIcon
import com.dailycompany.domain.model.TaskRepeat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskEditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 9, 22)

    private fun task(id: Long = 1L) = Task(
        id = id,
        title = "Tarefa $id",
        note = "nota",
        dueDate = today,
        dueTime = LocalTime.of(9, 0),
        createdAt = LocalDateTime.of(2026, 9, 1, 8, 0),
    )

    @Test
    fun `novo formulario comeca vazio e sem save`() = runTest {
        val vm = TaskEditViewModel(SavedStateHandle(), FakeTaskRepository(), FixedClock(today))

        assertFalse(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.isEditing)
        assertEquals("", vm.uiState.value.title)
        assertFalse(vm.uiState.value.canSave)
    }

    @Test
    fun `titulo preenchido ativa o save`() = runTest {
        val vm = TaskEditViewModel(SavedStateHandle(), FakeTaskRepository(), FixedClock(today))

        vm.onTitleChange("Comprar pão")
        assertTrue(vm.uiState.value.canSave)
        vm.onTitleChange("")
        assertFalse(vm.uiState.value.canSave)
    }

    @Test
    fun `save cria tarefa e fecha`() = runTest {
        val repo = FakeTaskRepository()
        val vm = TaskEditViewModel(SavedStateHandle(), repo, FixedClock(today))

        vm.onTitleChange("Comprar pão")
        vm.onNoteChange("padaria")
        vm.onDateChange(today)
        vm.onTimeChange(LocalTime.of(18, 30))
        vm.onIconChange(TaskIcon.Emoji("🛒"))

        vm.events.test {
            vm.save()
            assertEquals(TaskEditEvent.Close, awaitItem())
        }

        val saved = repo.observeAll().first().single()
        assertEquals("Comprar pão", saved.title)
        assertEquals("padaria", saved.note)
        assertEquals(today, saved.dueDate)
        assertEquals(LocalTime.of(18, 30), saved.dueTime)
        assertEquals(TaskIcon.Emoji("🛒"), saved.icon)
        assertEquals(TaskRepeat.NONE, saved.repeat)
    }

    @Test
    fun `changeRepeat atualiza o estado e save persiste`() = runTest {
        val repo = FakeTaskRepository()
        val vm = TaskEditViewModel(SavedStateHandle(), repo, FixedClock(today))

        vm.onTitleChange("Beber água")
        vm.onRepeatChange(TaskRepeat.DAILY)

        vm.events.test {
            vm.save()
            assertEquals(TaskEditEvent.Close, awaitItem())
        }

        val saved = repo.observeAll().first().single()
        assertEquals(TaskRepeat.DAILY, saved.repeat)
    }

    @Test
    fun `onIconChange atualiza o estado e pode limpar`() = runTest {
        val vm = TaskEditViewModel(SavedStateHandle(), FakeTaskRepository(), FixedClock(today))

        vm.onIconChange(TaskIcon.Material("home"))
        assertEquals(TaskIcon.Material("home"), vm.uiState.value.icon)

        vm.onIconChange(null)
        assertNull(vm.uiState.value.icon)
    }

    @Test
    fun `edicao carrega a tarefa existente`() = runTest {
        val repo = FakeTaskRepository(initial = listOf(task(1L)))
        val vm = TaskEditViewModel(
            SavedStateHandle(mapOf(TaskEditViewModel.KEY_TASK_ID to 1L)),
            repo,
            FixedClock(today),
        )

        assertTrue(vm.uiState.value.isEditing)
        assertEquals("Tarefa 1", vm.uiState.value.title)
        assertEquals("nota", vm.uiState.value.note)
        assertEquals(today, vm.uiState.value.dueDate)
        assertTrue(vm.uiState.value.canSave)
    }

    @Test
    fun `apagar emite snackbar e undo repoe a tarefa`() = runTest {
        val repo = FakeTaskRepository(initial = listOf(task(1L)))
        val vm = TaskEditViewModel(
            SavedStateHandle(mapOf(TaskEditViewModel.KEY_TASK_ID to 1L)),
            repo,
            FixedClock(today),
        )

        vm.delete()
        assertNotNull(vm.uiState.value.snackbar)
        assertEquals(TaskEditSnackbar.DELETED, vm.uiState.value.snackbar)
        assertNull(repo.getById(1L))

        vm.undoDelete()
        assertNull(vm.uiState.value.snackbar)
        assertNotNull(repo.getById(1L))
    }
}