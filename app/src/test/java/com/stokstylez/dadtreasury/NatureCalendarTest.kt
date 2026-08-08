package com.stokstylez.dadtreasury

import com.stokstylez.dadtreasury.domain.naturecalendar.NatureCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class NatureCalendarTest {

    @Test
    fun `spring equinox 2026-03-20 is Genesis Day 1 Monday`() {
        val tart = NatureCalendar.fromGregorian(LocalDate.of(2026, 3, 20))
        assertEquals(1, tart.month)
        assertEquals("Genesis", tart.monthName)
        assertEquals(1, tart.day)
        assertEquals("Monday", tart.dayOfWeek)
        assertTrue(!tart.isDayOutOfTime)
    }

    @Test
    fun `first month is exactly 28 days starting Monday ending Sunday`() {
        val start = NatureCalendar.fromGregorian(LocalDate.of(2026, 3, 20))
        val day28 = NatureCalendar.fromGregorian(LocalDate.of(2026, 4, 16))
        assertEquals(1, start.day)
        assertEquals("Monday", start.dayOfWeek)
        assertEquals(28, day28.day)
        assertEquals("Sunday", day28.dayOfWeek)
    }

    @Test
    fun `april 17 starts Flora Day 1`() {
        val tart = NatureCalendar.fromGregorian(LocalDate.of(2026, 4, 17))
        assertEquals(2, tart.month)
        assertEquals("Flora", tart.monthName)
        assertEquals(1, tart.day)
    }

    @Test
    fun `march 19 is Day Out of Time`() {
        val tart = NatureCalendar.fromGregorian(LocalDate.of(2026, 3, 19))
        assertTrue(tart.isDayOutOfTime)
        assertEquals(1, tart.dayOutOfTimeCount) // 2026 not leap
    }

    @Test
    fun `leap year 2024 has two Days Out of Time on march 18 and 19`() {
        val first = NatureCalendar.fromGregorian(LocalDate.of(2024, 3, 18))
        val second = NatureCalendar.fromGregorian(LocalDate.of(2024, 3, 19))
        assertTrue(first.isDayOutOfTime)
        assertTrue(second.isDayOutOfTime)
        assertEquals(2, first.dayOutOfTimeCount)
        assertEquals(2, second.dayOutOfTimeCount)
    }

    @Test
    fun `month 13 Anima spans feb 19 to march 18 and day 28 is sunday`() {
        val start = NatureCalendar.fromGregorian(LocalDate.of(2026, 2, 19))
        val end = NatureCalendar.fromGregorian(LocalDate.of(2026, 3, 18))
        assertEquals(13, start.month)
        assertEquals("Anima", start.monthName)
        assertEquals(1, start.day)
        assertEquals(28, end.day)
        assertEquals("Sunday", end.dayOfWeek)
    }

    @Test
    fun `template matches the exact scannable format`() {
        val tart = NatureCalendar.fromGregorian(LocalDate.of(2026, 5, 15))
        val template = tart.template
        assertTrue(template.contains("### 🌌 TARTARIAN CALENDAR CONVERSION"))
        assertTrue(template.contains("* **Gregorian Input:** 2026-05-15"))
        assertTrue(template.contains("Month 3 - Solaria"))
        assertTrue(template.contains("Day 1 of 28"))
        assertTrue(template.contains("Day of the Week:** Monday"))
        assertTrue(template.contains("Seasonal Matrix Activity:** Upkeep of structures, heavy outdoor building."))
    }

    @Test
    fun `activity matches month schema`() {
        // May 20 2026 = Month 3 Solaria (day 6)
        val tart = NatureCalendar.fromGregorian(LocalDate.of(2026, 5, 20))
        assertEquals("Solaria", tart.monthName)
        assertEquals(6, tart.day)
    }

    @Test
    fun `year boundary - january belongs to previous tartarian year`() {
        // Jan 1 2026 is before March 20 → matrix year 2025, Month 11 Cosmos
        val tart = NatureCalendar.fromGregorian(LocalDate.of(2026, 1, 1))
        assertEquals(2025, tart.gregorianYear)
        assertEquals("Cosmos", tart.monthName)
    }
}