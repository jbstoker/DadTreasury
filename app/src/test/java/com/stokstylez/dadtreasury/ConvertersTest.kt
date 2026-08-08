package com.stokstylez.dadtreasury

import com.stokstylez.dadtreasury.data.db.Converters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `string list to string joins with unit separator`() {
        val result = converters.fromStringList(listOf("a", "b", "c"))
        assertEquals("a\u0001b\u0001c", result)
    }

    @Test
    fun `empty list to string produces empty string`() {
        assertEquals("", converters.fromStringList(emptyList()))
    }

    @Test
    fun `string to string list splits on unit separator`() {
        val result = converters.toStringList("x\u0001y\u0001z")
        assertEquals(listOf("x", "y", "z"), result)
    }

    @Test
    fun `empty string to string list produces empty list`() {
        assertTrue(converters.toStringList("").isEmpty())
    }

    @Test
    fun `round trip preserves list`() {
        val original = listOf("chore", "homework", "outside")
        assertEquals(original, converters.toStringList(converters.fromStringList(original)))
    }
}