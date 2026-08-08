package com.example.dadtreasury.domain.model

/**
 * Location-triggered local reminders.
 *
 * Per spec §10: title, message, latitude, longitude, radius, active hours,
 * repeat behavior, enabled state.
 */
data class GeoRule(
    val id: String,
    val title: String,
    val message: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Int = 100,
    val activeStartHour: Int = 0,
    val activeEndHour: Int = 24,
    val repeatDaily: Boolean = true,
    val isEnabled: Boolean = true,
    val taskId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)