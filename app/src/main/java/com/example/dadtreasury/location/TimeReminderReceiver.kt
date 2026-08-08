package com.example.dadtreasury.location

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.dadtreasury.R
import com.example.dadtreasury.location.ProximityAlertReceiver

/**
 * Receives AlarmManager-triggered time reminders and posts a notification.
 */
class TimeReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID) ?: return

        // Handle dismiss action
        if (ACTION_DISMISS == intent.action) {
            context.getSharedPreferences("dad_treasury_reminders", Context.MODE_PRIVATE)
                .edit()
                .putBoolean(reminderId + "_dismissed", true)
                .apply()
            return
        }

        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Task reminder"

        // Skip if already dismissed
        val prefs = context.getSharedPreferences("dad_treasury_reminders", Context.MODE_PRIVATE)
        if (prefs.getBoolean(reminderId + "_dismissed", false)) return

        ProximityAlertReceiver.ensureChannels(context)

        val builder = NotificationCompat.Builder(context, ProximityAlertReceiver.CHANNEL_ID_SOUND)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        // Add dismiss action
        val dismissIntent = Intent(context, TimeReminderReceiver::class.java)
            .setAction(ACTION_DISMISS)
            .putExtra(EXTRA_REMINDER_ID, reminderId)
        val flags = if (Build.VERSION.SDK_INT >= 23) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val dismissPi = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            dismissIntent,
            flags
        )
        builder.addAction(0, context.getString(R.string.action_disable), dismissPi)

        try {
            NotificationManagerCompat.from(context)
                .notify(reminderId.hashCode(), builder.build())
        } catch (_: SecurityException) {
            // Missing POST_NOTIFICATIONS runtime permission
        }
    }

    companion object {
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_MESSAGE = "extra_message"
        const val ACTION_DISMISS = "com.example.dadtreasury.ACTION_REMINDER_DISMISS"
    }
}