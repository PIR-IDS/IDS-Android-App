package fr.pirids.idsapp.controller.daemon.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.pirids.idsapp.controller.detection.NotificationHandler

class NotificationWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result =
        try {
            // We create the main Notification channel
            NotificationHandler.createNotificationChannel(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
}