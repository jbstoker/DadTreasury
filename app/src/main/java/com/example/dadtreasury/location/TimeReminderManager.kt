package com.example.dadtreasury.location

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Schedules time-based task reminders using AlarmManager (no Google Play Services).
 */
object TimeReminderManager {

    fun scheduleReminder(
        context: Context,
        reminderId: String,
        triggerAtMillis: Long,
        message: String,
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = reminderId.hashCode()

        val intent = Intent(context, TimeReminderReceiver::class.java).apply {
            putExtra(TimeReminderReceiver.EXTRA_REMINDER_ID, reminderId)
            putExtra(TimeReminderReceiver.EXTRA_MESSAGE, message)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val pi = PendingIntent.getBroadcast(context, requestCode, intent, flags)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            }
        } catch (_: SecurityException) {
            // exact alarm permission may not be granted on Android 12+
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }
    }

    fun cancelReminder(context: Context, reminderId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = reminderId.hashCode()
        val intent = Intent(context, TimeReminderReceiver::class.java).apply {
            putExtra(TimeReminderReceiver.EXTRA_REMINDER_ID, reminderId)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val pi = PendingIntent.getBroadcast(context, requestCode, intent, flags)
        alarmManager.cancel(pi)
    }
}