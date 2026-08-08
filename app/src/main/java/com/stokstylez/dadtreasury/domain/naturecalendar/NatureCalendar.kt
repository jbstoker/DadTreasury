package com.stokstylez.dadtreasury.domain.naturecalendar

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * 🌌 Tartarian Natural Matrix — 13-Month Dual-Calendar Conversion Engine.
 *
 * Exact rules:
 * 1. 13 fixed months, each exactly 28 days.
 * 2. Every month starts on Monday (Day 1) and ends on Sunday (Day 28).
 * 3. The year always begins on March 20 (Spring Equinox) = Day 1 of Month 1 (Genesis).
 * 4. March 19 is a standalone, calendar-free intercalary day called
 *    "The Day Out of Time." It does not belong to any month or weekday.
 * 5. In Gregorian leap years, add an extra "Day Out of Time" on March 18.
 */
data class TartarianDate(
    val gregorianYear: Int,        // the Gregorian year of the matrix year
    val month: Int,                // 1..13
    val monthName: String,         // e.g. "Genesis"
    val day: Int,                  // 1..28
    val dayOfWeek: String,         // Monday..Sunday (day 1 = Monday)
    val activity: String,          // Seasonal Matrix Activity from schema
    val isDayOutOfTime: Boolean = false, // true for the standalone day(s)
    val dayOutOfTimeCount: Int = 0,      // 1 = normal, 2 = leap year
    val fullLabel: String,         // "🌌 Tartarian Matrix: Month 1 - Genesis, Day 1 (Monday)"
    val template: String,          // exact scannable output template
)

object NatureCalendar {

    private const val DAYS_PER_MONTH = 28
    private const val MONTHS = 13

    /** Month schema: title, date range, activity. */
    data class MonthSchema(val name: String, val activity: String)

    val MONTHS_SCHEMA = listOf(
        MonthSchema("Genesis", "Tilling soil, planting seeds, waking the homestead."),
        MonthSchema("Flora", "Pollination tracking, foraging wild herbs, animal birthing."),
        MonthSchema("Solaria", "Upkeep of structures, heavy outdoor building."),
        MonthSchema("Aether", "Midsummer celebrations, gathering early fruits."),
        MonthSchema("Luna", "Early grain cutting, checking water tables, fruit drying."),
        MonthSchema("Gaia", "Main harvest processing, storage packing, seed saving."),
        MonthSchema("Equinox", "Equalizing food distribution, winterizing roofs."),
        MonthSchema("Terra", "Root vegetable digging, firewood chopping."),
        MonthSchema("Aura", "Livestock sheltering, sealing windows, smoking meats."),
        MonthSchema("Vesta", "Indoor crafting, textile weaving, storytelling."),
        MonthSchema("Cosmos", "Asset tracking, community planning, resting."),
        MonthSchema("Chronos", "Melting snow management, checking stored grain."),
        MonthSchema("Anima", "Maple tapping, tool sharpening, crop planning."),
    )

    private val WEEKDAYS = listOf(
        "Monday", "Tuesday", "Wednesday", "Thursday",
        "Friday", "Saturday", "Sunday",
    )

    /**
     * Convert a Gregorian date to a Tartarian date.
     * The Tartarian year begins March 20. March 19 = Day Out of Time.
     */
    fun fromGregorian(date: LocalDate): TartarianDate {
        val year = date.year

        // Day Out of Time handling — uses the date's own Gregorian leap status,
        // because March 18 (leap) and March 19 are calendar-free standalone days.
        if (date.monthValue == 3 && date.dayOfMonth == 19) {
            val leap = isLeapYear(year)
            return makeDayOutOfTime(year, leap, dayOutOfTimeIndex = 1)
        }
        if (date.monthValue == 3 && date.dayOfMonth == 18 && isLeapYear(year)) {
            return makeDayOutOfTime(year, true, dayOutOfTimeIndex = 2)
        }

        // If date is before March 20, it belongs to the previous Tartarian year
        val startOfYear = LocalDate.of(year, 3, 20)
        val matrixYear = if (date.isBefore(startOfYear)) year - 1 else year
        val matrixStart = LocalDate.of(matrixYear, 3, 20)
        val dayOfMatrix = ChronoUnit.DAYS.between(matrixStart, date)

        // Normal days: dayOfMatrix is 0-based offset within regular months
        // (the Day Out of Time days don't contribute to month math on the Gregorian side,
        //  since the matrix restarts cleanly at March 20 each year)
        return fromMatrixOffset(matrixYear, dayOfMatrix)
    }

    fun fromTimestamp(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): TartarianDate =
        fromGregorian(Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate())

    /**
     * Build a TartarianDate from a zero-based day offset within a matrix year.
     * Offset 0 = March 20 (Genesis, Day 1, Monday).
     */
    private fun fromMatrixOffset(matrixYear: Int, dayOfMatrix: Long): TartarianDate {
        val monthIndex = (dayOfMatrix / DAYS_PER_MONTH).toInt()  // 0..12
        val dayInMonth = (dayOfMatrix % DAYS_PER_MONTH).toInt() // 0..27
        val schema = MONTHS_SCHEMA[monthIndex]

        // Every month starts on Monday, so weekday is day-in-month mod 7
        val weekday = WEEKDAYS[dayInMonth % 7]

        return TartarianDate(
            gregorianYear = matrixYear,
            month = monthIndex + 1,
            monthName = schema.name,
            day = dayInMonth + 1,
            dayOfWeek = weekday,
            activity = schema.activity,
            isDayOutOfTime = false,
            dayOutOfTimeCount = 0,
            fullLabel = "🌌 Tartarian Matrix: Month ${monthIndex + 1} - ${schema.name}, Day ${dayInMonth + 1} ($weekday)",
            template = buildTemplate(
                gregorian = "${dayToGregorian(matrixYear, monthIndex, dayInMonth)}",
                month = monthIndex + 1,
                monthName = schema.name,
                day = dayInMonth + 1,
                weekday = weekday,
                activity = schema.activity,
            ),
        )
    }

    private fun makeDayOutOfTime(matrixYear: Int, leap: Boolean, dayOutOfTimeIndex: Int): TartarianDate {
        val count = if (leap) 2 else 1
        return TartarianDate(
            gregorianYear = matrixYear,
            month = 0,
            monthName = "Day Out of Time",
            day = 0,
            dayOfWeek = "",
            activity = "A calendar-free day for reflection and reset.",
            isDayOutOfTime = true,
            dayOutOfTimeCount = count,
            fullLabel = "🌌 Tartarian Matrix: The Day Out of Time ($dayOutOfTimeIndex of $count)",
            template = """
                ### 🌌 TARTARIAN CALENDAR CONVERSION
                * **Gregorian Input:** ${gregorianForDayOutOfTime(matrixYear, dayOutOfTimeIndex)}
                * **Tartarian Month:** Day Out of Time (intercalary, belongs to no month)
                * **Current Matrix Day:** Day Out of Time ($dayOutOfTimeIndex of $count)
                * **Day of the Week:** None (calendar-free standalone day)
                * **Seasonal Matrix Activity:** A day of rest and reflection
            """.trimIndent(),
        )
    }

    private fun gregorianForDayOutOfTime(gregorianYear: Int, index: Int): LocalDate =
        // index 1 = March 19; index 2 (leap) = March 18, both in the date's own Gregorian year
        if (index == 2) LocalDate.of(gregorianYear, 3, 18) else LocalDate.of(gregorianYear, 3, 19)

    private fun dayToGregorian(matrixYear: Int, monthIndex: Int, dayInMonth: Int): LocalDate =
        LocalDate.of(matrixYear, 3, 20).plusDays((monthIndex * DAYS_PER_MONTH + dayInMonth).toLong())

    private fun buildTemplate(
        gregorian: String,
        month: Int,
        monthName: String,
        day: Int,
        weekday: String,
        activity: String,
    ): String = """
        ### 🌌 TARTARIAN CALENDAR CONVERSION
        * **Gregorian Input:** $gregorian
        * **Tartarian Month:** Month $month - $monthName
        * **Current Matrix Day:** Day $day of 28
        * **Day of the Week:** $weekday
        * **Seasonal Matrix Activity:** $activity
    """.trimIndent()

    private fun isLeapYear(year: Int): Boolean =
        java.time.Year.isLeap(year.toLong())
}