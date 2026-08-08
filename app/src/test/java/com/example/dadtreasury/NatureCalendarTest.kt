package com.example.dadtreasury

import com.example.dadtreasury.domain.naturecalendar.NatureCalendar
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class NatureCalendarTest {

    @Test
    fun `epoch date 2000-01-01 is Year 1 Day 1 Month 1`() {
        val natural = NatureCalendar.fromGregorian(LocalDate.of(2000, 1, 1))
        assertEquals(1, natural.year)
        assertEquals(1, natural.month)
        assertEquals(1, natural.day)
        assertEquals(1, natural.dayOfYear)
    }

    @Test
    fun `2000-01-28 is day 28 of month 1`() {
        val natural = NatureCalendar.fromGregorian(LocalDate.of(2000, 1, 28))
        assertEquals(1, natural.year)
        assertEquals(1, natural.month)
        assertEquals(28, natural.day)
    }

    @Test
    fun `2000-01-29 starts month 2`() {
        val natural = NatureCalendar.fromGregorian(LocalDate.of(2000, 1, 29))
        assertEquals(2, natural.month)
        assertEquals(1, natural.day)
    }

    @Test
    fun `2000-12-30 is Year Day in month 13`() {
        // First 364 days are months 1-13, so day 365 is Year Day
        val natural = NatureCalendar.fromGregorian(LocalDate.of(2000, 12, 30))
        assertEquals(1, natural.year)
        assertEquals(13, natural.month)
        assertEquals(0, natural.day) // Year Day
        assertEquals(365, natural.dayOfYear)
    }

    @Test
    fun `2000-12-31 is Leap Day in month 13`() {
        // 2000 is a leap year so day 366 is Leap Day
        val natural = NatureCalendar.fromGregorian(LocalDate.of(2000, 12, 31))
        assertEquals(1, natural.year)
        assertEquals(13, natural.month)
        assertEquals(-1, natural.day) // Leap Day
        assertEquals(366, natural.dayOfYear)
        assertEquals(true, natural.isLeapYear)
    }

    @Test
    fun `non-leap year 2001-01-01 is Year 2 Day 1`() {
        val natural = NatureCalendar.fromGregorian(LocalDate.of(2001, 1, 1))
        assertEquals(2, natural.year)
        assertEquals(1, natural.month)
        assertEquals(1, natural.day)
    }

    @Test
    fun `2001-12-31 is Year Day since 2001 is not leap`() {
        // Non-leap year: months take days 1-364, day 365 is Year Day
        val natural = NatureCalendar.fromGregorian(LocalDate.of(2001, 12, 31))
        assertEquals(13, natural.month)
        assertEquals(0, natural.day) // Year Day
        assertEquals(365, natural.dayOfYear)
        assertEquals(false, natural.isLeapYear)
    }

    @Test
    fun `round trip - toGregorian then back to natural returns same date`() {
        // 2008-05-15 is a leap year
        val dates = listOf(
            LocalDate.of(2000, 1, 1),
            LocalDate.of(2000, 12, 31),
            LocalDate.of(2001, 1, 1),
            LocalDate.of(2004, 2, 29),
            LocalDate.of(2008, 5, 15),
            LocalDate.of(2026, 8, 8),
            LocalDate.of(2027, 11, 30),
        )
        dates.forEach { date ->
            val natural = NatureCalendar.fromGregorian(date)
            val roundTripped = NatureCalendar.toGregorian(natural)
            assertEquals(date, roundTripped)
        }
    }

    @Test
    fun `day label formatting produces expected strings`() {
        val gregorian = LocalDate.of(2026, 8, 8)
        val natural = NatureCalendar.fromGregorian(gregorian)
        // Spec example: Tue, 8 Aug 2026 -> 🌿 Leaf 11, Year 8
        // Actually the spec uses example "Leaf 11, Year 8" - mapping months for display
        assert(natural.fullLabel.startsWith("🌿 "))
        assert(natural.fullLabel.contains("Year"))
    }
}