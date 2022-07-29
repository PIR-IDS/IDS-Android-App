package fr.pirids.idsapp.controller.daemon.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.pirids.idsapp.controller.daemon.ServiceDaemon

class ServiceSearchWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result =
        try {
            // We check if there is a service to monitor
            ServiceDaemon.connectToServices()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
}