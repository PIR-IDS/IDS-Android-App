package fr.pirids.idsapp.controller.daemon.workers

import android.content.Context
import android.os.Build
import androidx.work.*
import fr.pirids.idsapp.controller.Initiator
import fr.pirids.idsapp.controller.detection.Detection
import java.util.concurrent.TimeUnit

class DetectionWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result =
        try {
            // We launch the background detection for the API < 31, otherwise it is done by the BluetoothCompanionService
            if(Build.VERSION.SDK_INT < 31) {
                Initiator.init(applicationContext)
                Initiator.monitorServices(applicationContext)

                // We repeat this background task periodically
                // We have to use this little hack in order to bypass the minimum period of time of PeriodicWorkRequest
                // which is 15 minutes ; here we use CHECKING_DELAY_MILLIS
                WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                    "idsapp_daemon_detection", ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequestBuilder<DetectionWorker>()
                        .setInitialDelay(Detection.CHECKING_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                        .build()
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
}