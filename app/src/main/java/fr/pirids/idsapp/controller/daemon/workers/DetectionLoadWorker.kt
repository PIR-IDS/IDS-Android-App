package fr.pirids.idsapp.controller.daemon.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.pirids.idsapp.controller.detection.Detection

class DetectionLoadWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result =
        try {
            // We load the past detections
            Detection.loadPastDetections(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
}