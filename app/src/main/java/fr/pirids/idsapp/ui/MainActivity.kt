package fr.pirids.idsapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import fr.pirids.idsapp.controller.bluetooth.BluetoothConnection
import fr.pirids.idsapp.controller.navigation.IDSApp
import fr.pirids.idsapp.ui.theme.IDSAppTheme
import fr.pirids.idsapp.controller.bluetooth.LaunchBluetooth
import fr.pirids.idsapp.controller.daemon.ServiceDaemon
import fr.pirids.idsapp.controller.daemon.workers.*
import fr.pirids.idsapp.controller.internet.InternetConnection
import fr.pirids.idsapp.data.internet.InternetState
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val ble = BluetoothConnection(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val internet = InternetConnection(this)

        ble.registerBroadCast()

        // We launch the background tasks
        WorkManager.getInstance(applicationContext)
            .beginUniqueWork("idsapp_daemon", ExistingWorkPolicy.KEEP,
                listOf(
                    OneTimeWorkRequest.from(NotificationWorker::class.java),
                    OneTimeWorkRequest.from(DatabaseWorker::class.java)
                )
            )
            .then(
                listOf(
                    OneTimeWorkRequest.from(DeviceSearchWorker::class.java),
                    OneTimeWorkRequest.from(ServiceSearchWorker::class.java)
                )
            )
            .then(
                listOf(
                    OneTimeWorkRequestBuilder<ServiceStatusWorker>()
                        .setInitialDelay(ServiceDaemon.CHECKING_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                        .build(),
                    OneTimeWorkRequestBuilder<ServiceFallbackWorker>()
                        .setInitialDelay(ServiceDaemon.CHECKING_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                        .build(),
                    OneTimeWorkRequest.from(DetectionLoadWorker::class.java)
                )
            )
            .then(
                //FIXME: THIS DOESNT WORK BECAUSE THE BLUETOOTH CONNECTION IS NOT AVAILABLE IN THE WORKER!!!!!!!!!!!
                OneTimeWorkRequest.from(DetectionWorker::class.java)
            )
            .enqueue()

        setContent {
            IDSAppTheme {
                // If a bluetooth connection has already been established,
                // check bluetooth permissions and start searching for known devices
                //FIXME: don't relaunch the search if already searching
                LaunchBluetooth(ble, daemonMode = true)

                // We monitor the internet connection
                val internetConnection by internet.dynamicConnectivityState()
                val internetConnected = internetConnection === InternetState.Available
                if(!internetConnected) {
                    ServiceDaemon.clearAllConnectedServices()
                    //TODO: display a message to the user
                } else {
                    //FIXME: don't relaunch the search if already searching
                    ServiceDaemon.connectToServices()
                }

                // We launch the router
                IDSApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ble.unregisterBroadCast()
    }
}