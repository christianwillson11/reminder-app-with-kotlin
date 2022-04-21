package com.example.reminderapp

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver: BroadcastReceiver() {

    companion object {
        var contentTitle = "Title"
        var contentText = "Description"
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        val eIntent = Intent(context, ReminderDetailActivity::class.java)
        intent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, eIntent, 0)

        val builder = Notification.Builder(context, "schedule")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(Notification.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context!!)
        notificationManager.notify(123, builder.build())
    }
}