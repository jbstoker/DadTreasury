package com.stokstylez.dadtreasury.domain.model

/**
 * Offline calendar event. Gregorian timestamps are the internal truth.
 */
data class CalendarEvent(
    val id: String,
    val title: String,
    val description: String = "",
    val startTimestamp: Long,
    val endTimestamp: Long,
    val isRecurring: Boolean = false,
    val recurrenceRule: String? = null, // simple RRULE or null
    val reminderMinutes: Int? = null,
    val routineId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)