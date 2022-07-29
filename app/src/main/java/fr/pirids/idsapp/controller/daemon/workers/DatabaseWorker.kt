package fr.pirids.idsapp.controller.daemon.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.pirids.idsapp.data.model.AppDatabase

class DatabaseWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result =
        try {
            // We initialize the database context with the MainActivity
            AppDatabase.initInstance(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
}