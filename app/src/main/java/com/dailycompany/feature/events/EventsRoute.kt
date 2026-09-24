package com.dailycompany.feature.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailycompany.domain.model.EventOccurrence
import com.dailycompany.domain.model.EventFrequency
import com.dailycompany.ui.components.EmptyState
import com.dailycompany.ui.components.SectionHeader
import com.dailycompany.ui.components.TopAppBarWithSettings
import com.dailycompany.ui.theme.AppStrings
import com.dailycompany.ui.theme.LocalAppLocale
import com.dailycompany.ui.theme.Spacing
import com.dailycompany.ui.theme.strings
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Rota da Agenda (Eventos). Só aqui o `EventsViewModel` é criado (regra:
 * a UI nunca fala com DAO/repositório diretamente).
 */
@Composable
fun EventsRoute(
    onNavigateToSettings: () -> Unit,
    onCreateEvent: () -> Unit,
    onOpenEvent: (Long) -> Unit,
    onOpenAlarms: () -> Unit,
    viewModel: EventsViewModel = hiltViewModel(),
) {
    EventsScreen(
        onNavigateToSettings = onNavigateToSettings,
        onCreateEvent = onCreateEvent,
        onOpenEvent = onOpenEvent,
        onOpenAlarms = onOpenAlarms,
        viewModel = viewModel,
    )
}

@Composable
fun EventsScreen(
    onNavigateToSettings: () -> Unit,
    onCreateEvent: () -> Unit,
    onOpenEvent: (Long) -> Unit,
    onOpenAlarms: () -> Unit,
    viewModel: EventsViewModel,
) {
    val s = strings()
    val uiState by viewModel.uiState.collectAsState()
    val today = viewModel.today
    val minMonth = YearMonth.from(today)
    val maxMonth = YearMonth.from(today.plusMonths(EventsViewModel.WINDOW_MONTHS))

    // Estado local do ecrã (sobrevive à rotação): vista, mês visível, dia escolhido.
    var viewName by rememberSaveable { mutableStateOf(AgendaView.LIST.name) }
    var visibleMonthCode by rememberSaveable { mutableIntStateOf(monthCode(minMonth)) }
    var selectedEpochDay by rememberSaveable { mutableLongStateOf(today.toEpochDay()) }

    val view = AgendaView.valueOf(viewName)
    val visibleMonth = remember(visibleMonthCode) {
        YearMonth.of(visibleMonthCode / 12, (visibleMonthCode % 12) + 1)
    }
    val selectedDay = remember(selectedEpochDay) {
        if (selectedEpochDay < 0L) null else LocalDate.ofEpochDay(selectedEpochDay)
    }

    Scaffold(
        topBar = {
            TopAppBarWithSettings(
                title = s.eventsTitle,
                onNavigateToSettings = onNavigateToSettings,
                onOpenAlarms = onOpenAlarms,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateEvent,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                    )
                },
                text = { Text(s.eventsNewTitle) },
                elevation = FloatingActionButtonDefaults.elevation(2.dp),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            ViewToggle(
                view = view,
                onViewChange = { viewName = it.name },
            )
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
                uiState.occurrences.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Outlined.CalendarMonth,
                        message = s.eventsEmpty,
                        modifier = Modifier.weight(1f),
                    )
                }
                view == AgendaView.LIST -> {
                    EventsList(
                        occurrences = uiState.occurrences,
                        onOpenEvent = onOpenEvent,
                        modifier = Modifier.weight(1f),
                    )
                }
                else -> {
                    CalendarContent(
                        visibleMonth = visibleMonth,
                        minMonth = minMonth,
                        maxMonth = maxMonth,
                        markedDays = remember(uiState.occurrences) { uiState.occurrences.map { it.date }.toSet() },
                        selectedDay = selectedDay,
                        today = today,
                        occurrences = uiState.occurrences,
                        onMonthChange = { month ->
                            visibleMonthCode = monthCode(month)
                            selectedEpochDay = -1L
                        },
                        onSelectDay = { selectedEpochDay = it.toEpochDay() },
                        onOpenEvent = onOpenEvent,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/** Alternador Lista / Calendário da Agenda (SCREENS.md §4). */
@Composable
private fun ViewToggle(
    view: AgendaView,
    onViewChange: (AgendaView) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings()
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenH, vertical = Spacing.xs),
    ) {
        SegmentedButton(
            selected = view == AgendaView.LIST,
            onClick = { onViewChange(AgendaView.LIST) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
        ) {
            Text(s.eventsViewList)
        }
        SegmentedButton(
            selected = view == AgendaView.CALENDAR,
            onClick = { onViewChange(AgendaView.CALENDAR) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
        ) {
            Text(s.eventsViewCalendar)
        }
    }
}

/** Calendário mensal + lista do dia escolhido (mantém os dois na mesma vista). */
@Composable
private fun CalendarContent(
    visibleMonth: YearMonth,
    minMonth: YearMonth,
    maxMonth: YearMonth,
    markedDays: Set<LocalDate>,
    selectedDay: LocalDate?,
    today: LocalDate,
    occurrences: List<EventOccurrence>,
    onMonthChange: (YearMonth) -> Unit,
    onSelectDay: (LocalDate) -> Unit,
    onOpenEvent: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.screenH, vertical = Spacing.xs),
    ) {
        MonthCalendar(
            visibleMonth = visibleMonth,
            markedDays = markedDays,
            selectedDay = selectedDay,
            today = today,
            canGoPrev = visibleMonth > minMonth,
            canGoNext = visibleMonth < maxMonth,
            onGoPrev = { onMonthChange(visibleMonth.minusMonths(1)) },
            onGoNext = { onMonthChange(visibleMonth.plusMonths(1)) },
            onSelectDay = onSelectDay,
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))
        if (selectedDay != null) {
            DayEvents(
                day = selectedDay,
                occurrences = occurrences.filter { it.date == selectedDay },
                onOpenEvent = onOpenEvent,
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
        }
    }
}

/** Lista de próximos eventos, agrupada por mês (Agenda). */
@Composable
private fun EventsList(
    occurrences: List<EventOccurrence>,
    onOpenEvent: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings()
    val locale = LocalAppLocale.current
    val groups = groupByMonth(occurrences)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.screenH),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        groups.forEach { (month, monthOccurrences) ->
            item(key = "header-$month") {
                SectionHeader(title = month.header(locale))
            }
            items(monthOccurrences, key = { "${it.event.id}-${it.date}" }) { occurrence ->
                EventRow(
                    occurrence = occurrence,
                    onOpen = { onOpenEvent(occurrence.event.id) },
                )
                HorizontalDivider()
            }
        }
    }
}

/** Agrupa ocorrências (já ordenadas por data) por mês, preservando a ordem. */
internal fun groupByMonth(occurrences: List<EventOccurrence>): List<Pair<YearMonth, List<EventOccurrence>>> {
    if (occurrences.isEmpty()) return emptyList()
    val result = mutableListOf<Pair<YearMonth, MutableList<EventOccurrence>>>()
    for (occurrence in occurrences) {
        val month = YearMonth.from(occurrence.date)
        val last = result.lastOrNull()
        if (last != null && month == last.first) {
            last.second.add(occurrence)
        } else {
            result.add(month to mutableListOf(occurrence))
        }
    }
    return result.map { it.first to it.second.toList() }
}

/** Linha de evento: dia grande + título + hora/dia inteiro + repetição/aniversário. */
@Composable
internal fun EventRow(
    occurrence: EventOccurrence,
    onOpen: () -> Unit,
) {
    val s = strings()
    val locale = LocalAppLocale.current
    val event = occurrence.event
    val date = occurrence.date

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Dia grande + dia da semana abreviado.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(Spacing.xxl),
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = Spacing.sm),
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                if (event.isAllDay) {
                    Text(
                        text = s.eventsFieldAllDay,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    event.time?.let {
                        Text(
                            text = it.format(DateTimeFormatter.ofPattern("HH:mm", locale)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (event.recurrence.frequency != EventFrequency.NONE) {
                    Icon(
                        imageVector = Icons.Outlined.Repeat,
                        contentDescription = null,
                        modifier = Modifier.size(Spacing.md),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                occurrence.birthdayLabel(s)?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
    }
}

/** Para eventos anuais (aniversários): "faz N anos" na linha da Agenda. */
@Composable
private fun EventOccurrence.birthdayLabel(s: AppStrings): String? {
    if (event.recurrence.frequency != EventFrequency.YEARLY) return null
    val years = date.year - event.date.year
    return if (years > 0) s.eventTurnsYears(years) else null
}

internal fun YearMonth.header(locale: Locale): String =
    format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
        .replaceFirstChar { it.uppercase(locale) }

/** Codifica um mês num Int estável para `rememberSaveable` (ano*12 + mês-1). */
private fun monthCode(month: YearMonth): Int = month.year * 12 + (month.monthValue - 1)