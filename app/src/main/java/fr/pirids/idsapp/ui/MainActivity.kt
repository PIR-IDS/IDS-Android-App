@file:OptIn(ExperimentalMaterial3Api::class)

package fr.pirids.idsapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import fr.pirids.idsapp.controller.Initiator
import fr.pirids.idsapp.controller.bluetooth.BluetoothConnection
import fr.pirids.idsapp.controller.navigation.IDSApp
import fr.pirids.idsapp.ui.theme.IDSAppTheme
import fr.pirids.idsapp.controller.bluetooth.LaunchBluetooth
import fr.pirids.idsapp.controller.daemon.ServiceDaemon
import fr.pirids.idsapp.controller.daemon.workers.*
import fr.pirids.idsapp.controller.detection.NotificationHandler
import fr.pirids.idsapp.controller.internet.InternetConnection
import fr.pirids.idsapp.data.internet.InternetState
import fr.pirids.idsapp.controller.detection.NotificationPermission

class MainActivity : ComponentActivity() {
    private val ble = BluetoothConnection(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // We launch the background detection for the API < 31, otherwise it is done by the BluetoothCompanionService
        //FIXME: there are complications with some OEMs killing background tasks, see:
        // - https://dontkillmyapp.com/
        // - https://stackoverflow.com/a/53904589
        //TODO: maybe we should launch it for all APIs as a fallback? (the BCS seems not completely stable)
        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "idsapp_daemon",
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequest.from(DetectionWorker::class.java)
            )

        val internet = InternetConnection(this)

        ble.registerBroadCast()

        setContent {
            IDSAppTheme {
                // We ask to enable notifications
                NotificationPermission {
                    if(!Initiator.initialized.value) {
                        // We initialize the app
                        Initiator.init(this@MainActivity)
                        Initiator.handleServices(this@MainActivity)
                    }
                }

                // Loading screen
                AnimatedVisibility(visible = !Initiator.initialized.value) {
                    Surface(
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Scaffold {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(it),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .padding(horizontal = 18.dp)
                                            .size(50.dp),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }

                // Main screen
                AnimatedVisibility(visible = Initiator.initialized.value) {
                    // We launch the IDS monitoring
                    SideEffect {
                        Initiator.monitorServicesIndefinitely(this@MainActivity)
                    }

                    // If a bluetooth connection has already been established,
                    // check bluetooth permissions and start searching for known devices
                    LaunchBluetooth(ble, daemonMode = true)

                    // We monitor the internet connection
                    val internetConnection by internet.dynamicConnectivityState()
                    val internetConnected = internetConnection === InternetState.Available
                    if(!internetConnected) {
                        ServiceDaemon.clearAllConnectedServices()
                        //TODO: display a message to the user?
                    } else {
                        //FIXME: don't relaunch the search if already searching
                        ServiceDaemon.connectToServices()
                    }

                    // We launch the router
                    IDSApp()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // We clear the potential notifications in the notification center
        NotificationHandler.clearAllStatusNotifications(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ble.unregisterBroadCast()
    }
}