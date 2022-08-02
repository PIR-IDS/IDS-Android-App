package fr.pirids.idsapp.controller

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import fr.pirids.idsapp.controller.daemon.DeviceDaemon
import fr.pirids.idsapp.controller.daemon.ServiceDaemon
import fr.pirids.idsapp.controller.detection.Detection
import fr.pirids.idsapp.controller.detection.NotificationHandler
import fr.pirids.idsapp.data.model.AppDatabase
import kotlinx.coroutines.*

object Initiator {
    private val scope = CoroutineScope(Dispatchers.IO)
    val initialized: MutableState<Boolean> = mutableStateOf(false)

    fun init(applicationContext: Context) {
        scope.launch {
            // We parallelize the initialization of the different components
            // and order the execution of the different parts correctly.
            joinAll(
                scope.launch {
                    // We create the main Notification channel
                    NotificationHandler.createNotificationChannel(applicationContext)
                },
                scope.launch {
                    // We initialize the database context with the MainActivity
                    AppDatabase.initInstance(applicationContext)
                }
            ).run {
                joinAll(
                    scope.launch {
                        // We load the known devices
                        DeviceDaemon.loadDevice()
                    },
                    scope.launch {
                        // We check if there is a service to monitor
                        ServiceDaemon.connectToServices()
                    }
                ).run {
                    // We check if there is a device to connect to
                    DeviceDaemon.searchForDevice(applicationContext)
                    scope.launch {
                        // We load the past detections
                        Detection.loadPastDetections(applicationContext)
                    }.join().run {
                        initialized.value = true
                    }
                }
            }
        }
    }

    suspend fun monitorServices(applicationContext: Context) {
        Detection.monitorServices(applicationContext)
    }

    fun monitorServicesIndefinitely(applicationContext: Context) {
        scope.launch {
            while (true) {
                Detection.monitorServices(applicationContext)
                delay(Detection.CHECKING_DELAY_MILLIS)
            }
        }
    }

    fun handleServices() {
        scope.launch {
            ServiceDaemon.handleServiceStatus()
        }
        scope.launch {
            ServiceDaemon.handleDisconnectedKnownServices()
        }
    }
}