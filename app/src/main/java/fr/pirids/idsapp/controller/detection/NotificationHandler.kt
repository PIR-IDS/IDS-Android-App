package fr.pirids.idsapp.controller.detection

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fr.pirids.idsapp.R
import fr.pirids.idsapp.data.preferences.UserPreferencesRepository
import fr.pirids.idsapp.ui.MainActivity
import fr.pirids.idsapp.ui.views.notifications.AlertNotificationActivity
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.NoSuchElementException

object NotificationHandler {
    private var notificationId = 0
    private const val channelID = "IDS_DETECTION"

    fun createNotificationChannel(context: Context) {
        val name = "IDS"
        val descriptionText = context.resources.getString(R.string.alert_notify)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    suspend fun triggerNotification(context: Context, message: String) {
        val notifIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }
        notifIntent.setAction(Intent.ACTION_MAIN)
        notifIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, notifIntent, PendingIntent.FLAG_IMMUTABLE)

        val fullScreenIntent = Intent(context, AlertNotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(context, 1, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notifBuilder = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.resources.getString(R.string.alert_notify))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)

        // Get the user preferences for the fullscreen notification
        val userPreferences = UserPreferencesRepository(context)
        try {
            if (userPreferences.userPreferencesFlow.first().fullscreenNotification) {
                notifBuilder.setFullScreenIntent(fullScreenPendingIntent, true)
            }
        } catch (e: NoSuchElementException) {
            if(UserPreferencesRepository.DEFAULT_FULLSCREEN_NOTIFICATION) {
                notifBuilder.setFullScreenIntent(fullScreenPendingIntent, true)
            }
        }

        val time = Instant.ofEpochMilli(message.toLong()).atZone(ZoneId.of("UTC")).withZoneSameInstant(TimeZone.getDefault().toZoneId())
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, notifBuilder.setContentText("at $time").build())
            notificationId++
        }
    }
}