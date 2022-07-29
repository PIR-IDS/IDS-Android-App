package fr.pirids.idsapp.controller.daemon.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.pirids.idsapp.controller.daemon.DeviceDaemon

class DeviceSearchWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result =
        try {
            // We check if there is a device to connect to
            DeviceDaemon.searchForDevice()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
}