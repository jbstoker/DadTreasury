package com.example.dadtreasury.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.dadtreasury.R

/**
 * Receives proximity alerts from LocationManager and shows a notification
 * with "Done" and "Disable" quick actions (ported from ProximityNotes).
 */
class ProximityAlertReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val ruleId = intent.getStringExtra(EXTRA_RULE_ID) ?: return
        val taskId = intent.getStringExtra(EXTRA_TASK_ID)

        val action = intent.action
        if (ACTION_MARK_DONE == action || ACTION_DISABLE == action) {
            handleAction(context, ruleId, action)
            return
        }

        // Message comes via intent extras (set at registration time)
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Location reached"

        val channelId = getChannelForRule(context, ruleId)
        ensureChannels(context)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        // Add actions: Done and Silent (disable)
        val doneIntent = Intent(context, ProximityAlertReceiver::class.java)
            .setAction(ACTION_MARK_DONE)
            .putExtra(EXTRA_RULE_ID, ruleId)
        val disableIntent = Intent(context, ProximityAlertReceiver::class.java)
            .setAction(ACTION_DISABLE)
            .putExtra(EXTRA_RULE_ID, ruleId)

        val flags = if (Build.VERSION.SDK_INT >= 23) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val donePi = PendingIntent.getBroadcast(
            context,
            (ruleId + "_done").hashCode(),
            doneIntent,
            flags
        )
        val disPi = PendingIntent.getBroadcast(
            context,
            (ruleId + "_dis").hashCode(),
            disableIntent,
            flags
        )
        builder.addAction(0, context.getString(R.string.action_mark_done), donePi)
            .addAction(0, context.getString(R.string.action_disable), disPi)

        try {
            NotificationManagerCompat.from(context)
                .notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (_: SecurityException) {
            // Missing POST_NOTIFICATIONS runtime permission
        }
    }

    private fun handleAction(context: Context, ruleId: String, action: String) {
        // Mark rule as done or disabled locally
        val prefs = context.getSharedPreferences("dad_treasury_geo", Context.MODE_PRIVATE)
        val code = if (ACTION_MARK_DONE == action) 2 else 1
        prefs.edit().putInt("status_" + ruleId, code).apply()
    }

    private fun getChannelForRule(context: Context, ruleId: String): String {
        val prefs = context.getSharedPreferences("dad_treasury_geo", Context.MODE_PRIVATE)
        return if (prefs.getBoolean("silent_" + ruleId, false)) {
            CHANNEL_ID_SILENT
        } else {
            CHANNEL_ID_SOUND
        }
    }

    companion object {
        const val CHANNEL_ID_SOUND = "retronest_proximity"
        const val CHANNEL_ID_SILENT = "retronest_proximity_silent"
        const val EXTRA_RULE_ID = "extra_rule_id"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_MESSAGE = "extra_message"
        const val ACTION_MARK_DONE = "com.example.dadtreasury.ACTION_GEO_DONE"
        const val ACTION_DISABLE = "com.example.dadtreasury.ACTION_GEO_DISABLE"

        fun ensureChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val sound = NotificationChannel(
                    CHANNEL_ID_SOUND,
                    "Proximity Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Location-triggered task reminders"
                    enableLights(true)
                    lightColor = Color.CYAN
                    enableVibration(true)
                }

                val silent = NotificationChannel(
                    CHANNEL_ID_SILENT,
                    "Proximity Alerts (Silent)",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Silent location-triggered reminders"
                    setSound(null, null)
                    enableVibration(false)
                }

                nm.createNotificationChannel(sound)
                nm.createNotificationChannel(silent)
            }
        }
    }
}