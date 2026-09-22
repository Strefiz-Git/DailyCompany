package com.dailycompany.ui.theme

import com.dailycompany.ui.theme.languages.EnStrings
import com.dailycompany.ui.theme.languages.PtBrStrings
import com.dailycompany.ui.theme.languages.PtPtStrings
import java.lang.reflect.Modifier
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Completude das três línguas (teste obrigatório AGENTS §6): percorre os
 * getters de [AppStrings] em cada objeto e garante que nenhum devolve uma
 * string vazia, "TODO" ou o nome da chave.
 */
class StringsCompletenessTest {

    private val languages = listOf(PtPtStrings, PtBrStrings, EnStrings)

    private val getters: List<java.lang.reflect.Method> =
        AppStrings::class.java.declaredMethods
            .filter { Modifier.isPublic(it.modifiers) && it.parameterCount == 0 }
            .filter { it.returnType == String::class.java }

    @Test
    fun `todas as chaves estao preenchidas nas tres linguas`() {
        assertTrue("AppStrings deve ter getters", getters.isNotEmpty())

        for (lang in languages) {
            for (getter in getters) {
                val value = getter.invoke(lang) as String
                assertFalse("vazia: ${getter.name} (${lang::class.simpleName})", value.isBlank())
                assertFalse(
                    "TODO em ${getter.name} (${lang::class.simpleName})",
                    value.trim().equals("TODO", ignoreCase = true),
                )
                assertFalse(
                    "placeholder do nome da chave em ${getter.name} (${lang::class.simpleName})",
                    value.startsWith("{") && value.endsWith("}"),
                )
            }
        }
    }

    @Test
    fun `funcoes de plural devolvem texto nas tres linguas`() {
        for (lang in languages) {
            assertTrue(lang.tasksCount(0).isNotBlank())
            assertTrue(lang.tasksCount(1).isNotBlank())
            assertTrue(lang.tasksCount(2).isNotBlank())
            assertTrue(lang.relativeInDays(1).isNotBlank())
            assertTrue(lang.relativeInDays(3).isNotBlank())
        }
    }
}