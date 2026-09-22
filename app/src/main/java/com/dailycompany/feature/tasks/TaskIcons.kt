package com.dailycompany.feature.tasks

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.dailycompany.domain.model.TaskIcon
import com.dailycompany.ui.theme.Spacing

/** Nomes dos ícones Material escolhíveis (catálogo fechado, mapeados abaixo). */
val materialTaskIconNames: List<String> = listOf(
    "home",
    "shopping_cart",
    "restaurant",
    "fitness_center",
    "school",
    "work",
    "directions_run",
    "local_hospital",
    "favorite",
    "star",
    "calendar_month",
    "email",
    "phone",
    "check_circle",
)

/** Emojis escolhíveis (mostrados como texto). */
val emojiTaskIcons: List<String> = listOf(
    "🛒",
    "🏋️",
    "📚",
    "💼",
    "🏠",
    "🍽️",
    "🏃",
    "💊",
    "⭐",
    "❤️",
    "📅",
    "✈️",
    "🚗",
    "🎉",
    "☕",
    "🧹",
)

/** Constrói o `TaskIcon.Material` para o catálogo. */
fun materialTaskIcon(name: String): TaskIcon.Material = TaskIcon.Material(name)

/** Mapeia o nome do ícone Material para o `ImageVector` correspondente. */
fun materialIconVector(name: String): ImageVector = when (name) {
    "home" -> Icons.Outlined.Home
    "shopping_cart" -> Icons.Outlined.ShoppingCart
    "restaurant" -> Icons.Outlined.Restaurant
    "fitness_center" -> Icons.Outlined.FitnessCenter
    "school" -> Icons.Outlined.School
    "work" -> Icons.Outlined.Work
    "directions_run" -> Icons.AutoMirrored.Outlined.DirectionsRun
    "local_hospital" -> Icons.Outlined.LocalHospital
    "favorite" -> Icons.Outlined.Favorite
    "star" -> Icons.Outlined.Star
    "calendar_month" -> Icons.Outlined.CalendarMonth
    "email" -> Icons.Outlined.Email
    "phone" -> Icons.Outlined.Phone
    "check_circle" -> Icons.Outlined.CheckCircle
    else -> Icons.Outlined.CheckCircle
}

/**
 * Apresenta o ícone/emoji de uma tarefa. `Material` mostra o ícone Material;
 * `Emoji` mostra o caracter como texto. Decorativo: `contentDescription = null`
 * (o título da tarefa já é anunciado pelo leitor de ecrã).
 */
@Composable
fun TaskIconBadge(
    icon: TaskIcon?,
    modifier: Modifier = Modifier,
) {
    when (icon) {
        is TaskIcon.Material -> Icon(
            imageVector = materialIconVector(icon.name),
            contentDescription = null,
            modifier = modifier.size(Spacing.md),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        is TaskIcon.Emoji -> Text(
            text = icon.char,
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier,
        )
        null -> Unit
    }
}