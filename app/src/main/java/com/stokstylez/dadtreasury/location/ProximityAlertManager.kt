package com.stokstylez.dadtreasury.location

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import com.stokstylez.dadtreasury.location.ProximityAlertReceiver.Companion.EXTRA_MESSAGE
import com.stokstylez.dadtreasury.location.ProximityAlertReceiver.Companion.EXTRA_RULE_ID
import com.stokstylez.dadtreasury.location.ProximityAlertReceiver.Companion.EXTRA_TASK_ID

/**
 * Native geofencing via LocationManager.addProximityAlert (no Google Play Services).
 *
 * Fully offline and privacy-first: all location logic runs on-device with Android's
 * built-in location stack.
 */
object ProximityAlertManager {

    private const val PROXIMITY_REQUEST_CODE_BASE = 7000

    /**
     * Register a proximity alert for a geo rule.
     * Returns the request code used for unregistering.
     */
    fun registerProximityAlert(
        context: Context,
        ruleId: String,
        taskId: String?,
        message: String,
        latitude: Double,
        longitude: Double,
        radiusMeters: Float,
        expirationMillis: Long = -1L, // -1 = never expire
    ): Int {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val requestCode = PROXIMITY_REQUEST_CODE_BASE + ruleId.hashCode() % 10000

        val pi = proximityPendingIntent(context, ruleId, taskId, message, requestCode)

        try {
            locationManager.addProximityAlert(
                latitude,
                longitude,
                radiusMeters,
                expirationMillis,
                pi
            )
        } catch (_: SecurityException) {
            // Location permission not granted yet
        } catch (_: IllegalArgumentException) {
            // Invalid radius or coords
        }

        return requestCode
    }

    /**
     * Remove a registered proximity alert by rule id.
     */
    fun removeProximityAlert(context: Context, ruleId: String) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val requestCode = PROXIMITY_REQUEST_CODE_BASE + ruleId.hashCode() % 10000
        try {
            val pi = proximityPendingIntent(context, ruleId, null, "", requestCode)
            locationManager.removeProximityAlert(pi)
        } catch (_: Exception) {
            // Not registered - ignore
        }
    }

    private fun proximityPendingIntent(
        context: Context,
        ruleId: String,
        taskId: String?,
        message: String,
        requestCode: Int,
    ): PendingIntent {
        val intent = Intent(context, ProximityAlertReceiver::class.java).apply {
            putExtra(EXTRA_RULE_ID, ruleId)
            putExtra(EXTRA_MESSAGE, message)
            if (taskId != null) putExtra(EXTRA_TASK_ID, taskId)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }
}