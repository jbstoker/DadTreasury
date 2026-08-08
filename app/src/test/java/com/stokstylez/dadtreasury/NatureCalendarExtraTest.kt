package com.stokstylez.dadtreasury

import com.stokstylez.dadtreasury.domain.naturecalendar.NatureCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class NatureCalendarExtraTest {

    @Test
    fun `fromTimestamp converts epoch millis to tartarian date`() {
        // 2026-03-20 12:00 UTC = Genesis Day 1
        val utcMidday = LocalDateTime.of(2026, 3, 20, 12, 0)
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()

        val tart = NatureCalendar.fromTimestamp(utcMidday, ZoneId.of("UTC"))

        assertEquals(1, tart.month)
        assertEquals("Genesis", tart.monthName)
        assertEquals(1, tart.day)
    }

    @Test
    fun `all 13 months have valid schema entries`() {
        assertEquals(13, NatureCalendar.MONTHS_SCHEMA.size)

        val names = NatureCalendar.MONTHS_SCHEMA.map { it.name }
        assertEquals(
            listOf(
                "Genesis", "Flora", "Solaria", "Aether", "Luna", "Gaia", "Equinox",
                "Terra", "Aura", "Vesta", "Cosmos", "Chronos", "Anima",
            ),
            names,
        )

        NatureCalendar.MONTHS_SCHEMA.forEach { schema ->
            assertTrue(schema.name.isNotBlank())
            assertTrue(schema.activity.isNotBlank())
        }
    }

    @Test
    fun `all 13 months have activity descriptions`() {
        assertTrue(NatureCalendar.MONTHS_SCHEMA[0].activity.contains("planting"))
        assertTrue(NatureCalendar.MONTHS_SCHEMA[2].activity.contains("building"))
        assertTrue(NatureCalendar.MONTHS_SCHEMA[5].activity.contains("harvest"))
        assertTrue(NatureCalendar.MONTHS_SCHEMA[12].activity.contains("planning"))
    }

    @Test
    fun `full label format matches expectation`() {
        val tart = NatureCalendar.fromGregorian(LocalDate.of(2026, 3, 20))
        assertEquals(
            "🌌 Natural: Month 1 - Genesis, Day 1 (Monday)",
            tart.fullLabel,
        )
    }

    @Test
    fun `day out of time full label`() {
        val tart = NatureCalendar.fromGregorian(LocalDate.of(2026, 3, 19))
        assertEquals(
            "🌌 Natural: The Day Out of Time (1 of 1)",
            tart.fullLabel,
        )
    }

    @Test
    fun `day out of time template`() {
        val tart = NatureCalendar.fromGregorian(LocalDate.of(2026, 3, 19))
        assertTrue(tart.template.contains("Gregorian Input:** 2026-03-19"))
        assertTrue(tart.template.contains("Day Out of Time (intercalary"))
        assertTrue(tart.template.contains("None (calendar-free standalone day)"))
    }

    @Test
    fun `leap year day out of time template includes both days`() {
        val tart = NatureCalendar.fromGregorian(LocalDate.of(2024, 3, 18))
        assertTrue(tart.template.contains("2024-03-18"))
        assertTrue(tart.template.contains("(2 of 2)"))
    }

    @Test
    fun `march 20 non-leap is never day out of time`() {
        val tart = NatureCalendar.fromGregorian(LocalDate.of(2025, 3, 20))
        assertFalse(tart.isDayOutOfTime)
        assertEquals(0, tart.dayOutOfTimeCount)
    }

    @Test
    fun `march 18 non-leap is not day out of time`() {
        val tart = NatureCalendar.fromGregorian(LocalDate.of(2025, 3, 18))
        assertFalse(tart.isDayOutOfTime)
    }

    @Test
    fun `day 28 of month 13 is sunday`() {
        val tart = NatureCalendar.fromGregorian(LocalDate.of(2026, 3, 18))
        assertEquals(28, tart.day)
        assertEquals("Sunday", tart.dayOfWeek)
    }

    @Test
    fun `matrix year equals gregorian year on or after march 20`() {
        val tart = NatureCalendar.fromGregorian(LocalDate.of(2026, 12, 31))
        assertEquals(2026, tart.gregorianYear)
    }

    @Test
    fun `matrix year lags gregorian before march 20`() {
        val tart = NatureCalendar.fromGregorian(LocalDate.of(2026, 3, 19))
        assertEquals(2026, tart.gregorianYear) // day out of time uses its own year
    }

    @Test
    fun `weekday progression matches gregorian calendar`() {
        val day1 = NatureCalendar.fromGregorian(LocalDate.of(2026, 3, 20))
        val day2 = NatureCalendar.fromGregorian(LocalDate.of(2026, 3, 21))
        val day7 = NatureCalendar.fromGregorian(LocalDate.of(2026, 3, 26))

        assertEquals("Monday", day1.dayOfWeek)
        assertEquals("Tuesday", day2.dayOfWeek)
        assertEquals("Sunday", day7.dayOfWeek)
    }
}