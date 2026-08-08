package com.example.dadtreasury.domain.naturecalendar

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Natural 13-month calendar.
 *
 * Per spec §12: 13 months, 28 days each, 364 days total, 1 Year Day outside the
 * months, 1 leap day in leap years. Gregorian is the internal truth; the natural
 * calendar is a derived display layer.
 */
data class NaturalDate(
    val year: Int,
    val month: Int,       // 1..13
    val day: Int,         // 1..28, 0 = Year Day, -1 = Leap Day
    val dayOfYear: Int,   // 1..366
    val isLeapYear: Boolean,
) {
    val monthName: String
        get() = MONTH_NAMES[month - 1]

    val dayLabel: String
        get() = when (day) {
            0 -> "Year Day"
            -1 -> "Leap Day"
            else -> "Day $day"
        }

    val fullLabel: String
        get() = "🌿 $monthName $dayLabel, Year $year"

    companion object {
        val MONTH_NAMES = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November",
            "December", "Thirteenth"
        )
    }
}

/**
 * Gregorian-to-natural conversion - deterministic and testable.
 */
object NatureCalendar {

    /** Reference epoch: 2000-01-01 is Year 1, Day 1. */
    const val EPOCH_YEAR_JAN1_2000 = 2000L
    const val DAYS_PER_YEAR = 364
    const val DAYS_PER_MONTH = 28

    private fun isLeapYear(gregorianYear: Int): Boolean =
        java.time.Year.isLeap((gregorianYear).toLong())

    private fun yearLengthDays(gregorianYear: Int): Int =
        if (isLeapYear(gregorianYear)) 366 else 365

    /**
     * Convert a Gregorian date to a natural date.
     * The natural year increments on Jan 1 of the Gregorian year.
     */
    fun fromGregorian(date: LocalDate): NaturalDate {
        val dayNumber = ChronoUnit.DAYS.between(LocalDate.of(2000, 1, 1), date)
        return fromDayNumber(dayNumber)
    }

    fun fromTimestamp(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): NaturalDate {
        val date = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        return fromGregorian(date)
    }

    /**
     * Convert a day count since 2000-01-01 to a NaturalDate.
     */
    fun fromDayNumber(dayNumber: Long): NaturalDate {
        require(dayNumber >= 0) { "Day number must be non-negative" }

        var remaining = dayNumber
        var naturalYear = 1L

        // Walk forward year by year (natural years map to Gregorian years)
        var gregorianYear = 2000
        while (remaining >= yearLengthDays(gregorianYear)) {
            remaining -= yearLengthDays(gregorianYear)
            gregorianYear++
            naturalYear++
        }

        val isLeap = isLeapYear(gregorianYear)
        val yearLen = if (isLeap) 366 else 365
        val dayOfYear = (remaining + 1).toInt()

        val month: Int
        val day: Int
        when {
            dayOfYear <= 364 -> {
                // 13 months of 28 days
                month = ((dayOfYear - 1) / 28) + 1
                day = ((dayOfYear - 1) % 28) + 1
            }
            dayOfYear == 365 -> {
                // Year Day
                month = 13
                day = 0
            }
            else -> {
                // Leap Day (day 366)
                month = 13
                day = -1
            }
        }

        return NaturalDate(
            year = naturalYear.toInt(),
            month = month,
            day = day,
            dayOfYear = dayOfYear.toInt(),
            isLeapYear = isLeap,
        )
    }

    /**
     * Convert a natural date back to a Gregorian date.
     */
    fun toGregorian(natural: NaturalDate): LocalDate {
        require(natural.year >= 1) { "Year must be >= 1" }
        require(natural.month in 1..13) { "Month must be 1..13" }
        require(natural.day in -1..28) { "Day must be -1..28" }

        var gregorianYear = 2000
        var naturalYearCounter = 1L
        while (naturalYearCounter < natural.year) {
            gregorianYear++
            naturalYearCounter++
        }

        val start = LocalDate.of(gregorianYear, 1, 1)
        val dayOffset: Long = when {
            natural.day > 0 -> (natural.month - 1) * 28L + (natural.day - 1)
            natural.day == 0 -> 364L
            else -> 365L
        }
        return start.plusDays(dayOffset)
    }
}