package com.dailycompany.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Round-trip da serialização de [TaskIcon] (coluna `icon` da BD). */
class TaskIconTest {

    @Test
    fun `material serializa e volta ao mesmo`() {
        val icon = TaskIcon.Material("home")
        assertEquals("m:home", icon.serialize())
        assertEquals(icon, parseTaskIcon("m:home"))
    }

    @Test
    fun `emoji serializa e volta ao mesmo`() {
        val icon = TaskIcon.Emoji("🛒")
        assertEquals("e:🛒", icon.serialize())
        assertEquals(icon, parseTaskIcon("e:🛒"))
    }

    @Test
    fun `null devolve null`() {
        assertNull(parseTaskIcon(null))
    }

    @Test
    fun `valor malformado devolve null`() {
        assertNull(parseTaskIcon(""))
        assertNull(parseTaskIcon("m:"))
        assertNull(parseTaskIcon("x:home"))
        assertNull(parseTaskIcon("home"))
    }
}