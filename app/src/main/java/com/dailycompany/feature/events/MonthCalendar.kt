package com.dailycompany.feature.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.dailycompany.domain.model.EventOccurrence
import com.dailycompany.ui.components.SectionHeader
import com.dailycompany.ui.theme.LocalAppLocale
import com.dailycompany.ui.theme.Spacing
import com.dailycompany.ui.theme.strings
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

/** Vistas da Agenda: lista agrupada por mês ou calendário mensal (SCREENS.md §4). */
enum class AgendaView { LIST, CALENDAR }

/**
 * Células do mês para a grelha do calendário: `null` nas posições vazias
 * (à frente, até ao início da semana, e atrás, para completar a última semana).
 * A semana começa em [weekStart] — SCREENS.md: segunda-feira.
 */
internal fun monthCells(month: YearMonth, weekStart: DayOfWeek = DayOfWeek.MONDAY): List<LocalDate?> {
    val first = month.atDay(1)
    val leading = (first.dayOfWeek.value - weekStart.value + 7) % 7
    val lastDay = month.atEndOfMonth().dayOfMonth
    val trailing = (7 - (leading + lastDay) % 7) % 7
    return buildList {
        repeat(leading) { add(null) }
        for (day in 1..lastDay) add(month.atDay(day))
        repeat(trailing) { add(null) }
    }
}

/**
 * Calendário mensal: mês com pontos nos dias com conteúdo; toque num dia
 * seleciona-o (a lista desse dia é mostrada por baixo, ver [DayEvents]).
 */
@Composable
internal fun MonthCalendar(
    visibleMonth: YearMonth,
    markedDays: Set<LocalDate>,
    selectedDay: LocalDate?,
    today: LocalDate,
    canGoPrev: Boolean,
    canGoNext: Boolean,
    onGoPrev: () -> Unit,
    onGoNext: () -> Unit,
    onSelectDay: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings()
    val locale = LocalAppLocale.current
    val cells = remember(visibleMonth) { monthCells(visibleMonth) }

    Column(modifier.fillMaxWidth()) {
        // Navegação do mês (limitada à janela da Agenda: hoje..+3 meses).
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onGoPrev, enabled = canGoPrev) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                    contentDescription = s.eventsPrevMonth,
                )
            }
            Text(
                text = visibleMonth.header(locale),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onGoNext, enabled = canGoNext) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = s.eventsNextMonth,
                )
            }
        }

        // Cabeçalho dos dias da semana (etiquetas curtas do locale; semana à segunda).
        Row(Modifier.fillMaxWidth()) {
            DayOfWeek.entries.forEach { day ->
                Text(
                    text = day.getDisplayName(TextStyle.NARROW, locale),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Grelha do mês: uma linha por semana.
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    MonthCell(
                        date = date,
                        isToday = date == today,
                        isSelected = date != null && date == selectedDay,
                        hasEvents = date != null && date in markedDays,
                        onClick = { if (date != null) onSelectDay(date) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/** Célula de um dia: número + ponto se houver conteúdo; hoje/seleção destacados. */
@Composable
private fun MonthCell(
    date: LocalDate?,
    isToday: Boolean,
    isSelected: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val content = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    val dot = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            // Alvo ≥ 48dp (DESIGN.md), células quadradas.
            .height(Spacing.xxl)
            .clip(CircleShape)
            .background(background)
            .then(if (date != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Text(
            text = date?.dayOfMonth?.toString() ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = content,
        )
        // O ponto reserva espaço sempre (números alinhados entre células).
        Box(
            modifier = Modifier
                .size(Spacing.sm)
                .clip(CircleShape)
                .background(if (date != null && hasEvents) dot else Color.Transparent),
        )
    }
}

/** Lista dos eventos de um dia escolhido no calendário (secção "por baixo"). */
@Composable
internal fun DayEvents(
    day: LocalDate,
    occurrences: List<EventOccurrence>,
    onOpenEvent: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings()
    val locale = LocalAppLocale.current

    if (occurrences.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.fillMaxWidth().padding(Spacing.lg),
        ) {
            Text(
                text = s.eventsDayEmpty,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Spacing.screenH, vertical = Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        item(key = "day-header") {
            SectionHeader(title = day.header(locale))
        }
        items(occurrences, key = { "${it.event.id}-${it.date}" }) { occurrence ->
            EventRow(
                occurrence = occurrence,
                onOpen = { onOpenEvent(occurrence.event.id) },
            )
            HorizontalDivider()
        }
    }
}

/** Cabeçalho do dia selecionado: data longa localizada ("22 de setembro de 2026"). */
private fun LocalDate.header(locale: Locale): String =
    format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale))
        .replaceFirstChar { it.uppercase(locale) }