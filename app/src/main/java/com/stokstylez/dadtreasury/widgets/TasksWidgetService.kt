package com.stokstylez.dadtreasury.widgets

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.stokstylez.dadtreasury.R
import com.stokstylez.dadtreasury.data.db.AppDatabase
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RemoteViewsService that feeds task data to the home screen widget.
 */
class TasksWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        TasksRemoteViewsFactory(applicationContext)

    class TasksRemoteViewsFactory(
        private val context: Context,
    ) : RemoteViewsFactory {

        private data class WidgetTask(val title: String, val time: String, val statusColor: Int)

        private val tasks = mutableListOf<WidgetTask>()

        override fun onCreate() {
            // Nothing - load in onDataSetChanged
        }

        override fun onDataSetChanged() {
            tasks.clear()
            try {
                val db = AppDatabase.getInstance(context)
                val entities = runBlocking { db.taskDao().getAllOnce() }
                entities.take(10).forEach { entity ->
                    val time = if (entity.dueTimestamp != null) {
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(entity.dueTimestamp))
                    } else {
                        ""
                    }
                    val color = when (entity.status) {
                        "APPROVED" -> 0xFF39FF88.toInt()
                        "COMPLETED" -> 0xFFFFB300.toInt()
                        else -> 0xFF00E5FF.toInt()
                    }
                    tasks.add(WidgetTask(entity.title, time, color))
                }
            } catch (_: Exception) {
                // Widget content unavailable - show empty
            }
        }

        override fun onDestroy() {
            tasks.clear()
        }

        override fun getCount(): Int = tasks.size

        override fun getViewAt(position: Int): RemoteViews {
            val item = tasks[position]
            val views = RemoteViews(context.packageName, R.layout.appwidget_tasks_row)
            views.setTextViewText(R.id.widget_task_title, item.title)
            views.setTextViewText(R.id.widget_task_time, item.time)
            views.setTextColor(R.id.widget_task_status, item.statusColor)
            return views
        }

        override fun getLoadingView(): RemoteViews? = null

        override fun getViewTypeCount(): Int = 1

        override fun getItemId(position: Int): Long = position.toLong()

        override fun hasStableIds(): Boolean = true
    }
}