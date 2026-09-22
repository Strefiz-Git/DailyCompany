package com.dailycompany.domain.model

/**
 * Ícone opcional de uma tarefa.
 *
 * - [Material] referencia um ícone do catálogo predefinido pelo seu nome (a UI
 *   mapeia o nome para um `ImageVector` de material-icons).
 * - [Emoji] guarda o próprio caracter Unicode, mostrado como texto.
 *
 * Modelo puro, sem dependências Android/Room. Persistido como String via
 * [serialize]/[parseTaskIcon] (round-trip coberto por testes).
 */
sealed interface TaskIcon {

    data class Material(val name: String) : TaskIcon

    data class Emoji(val char: String) : TaskIcon
}

/** Serializa para a coluna da BD (ex.: "m:home", "e:🛒"). */
fun TaskIcon.serialize(): String = when (this) {
    is TaskIcon.Material -> "m:$name"
    is TaskIcon.Emoji -> "e:$char"
}

/** Lê da coluna da BD; valores desconhecidos/malformados devolvem null. */
fun parseTaskIcon(raw: String?): TaskIcon? = when {
    raw == null -> null
    raw.startsWith("m:") -> raw.removePrefix("m:").takeIf { it.isNotBlank() }?.let { TaskIcon.Material(it) }
    raw.startsWith("e:") -> raw.removePrefix("e:").takeIf { it.isNotBlank() }?.let { TaskIcon.Emoji(it) }
    else -> null
}