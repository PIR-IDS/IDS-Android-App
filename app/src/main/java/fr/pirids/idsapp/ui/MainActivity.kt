package fr.pirids.idsapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import fr.pirids.idsapp.controller.bluetooth.BluetoothConnection
import fr.pirids.idsapp.controller.navigation.IDSApp
import fr.pirids.idsapp.ui.theme.IDSAppTheme
import fr.pirids.idsapp.controller.bluetooth.LaunchBluetooth
import fr.pirids.idsapp.controller.daemon.DeviceDaemon
import fr.pirids.idsapp.controller.daemon.ServiceDaemon
import fr.pirids.idsapp.controller.detection.Detection
import fr.pirids.idsapp.controller.detection.NotificationHandler
import fr.pirids.idsapp.controller.internet.InternetConnection
import fr.pirids.idsapp.data.internet.InternetState
import fr.pirids.idsapp.data.model.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val searchForKnownDevices: MutableState<Boolean> = mutableStateOf(false)
        val ble = BluetoothConnection(this)
        val internet = InternetConnection(this)

        CoroutineScope(Dispatchers.IO).launch {
            // We initialize the database context with the MainActivity
            AppDatabase.initInstance(this@MainActivity)

            // We check if there is a service to monitor and a device to connect to
            searchForKnownDevices.value = DeviceDaemon.searchForDevice(ble)
            ServiceDaemon.connectToServices()
            CoroutineScope(Dispatchers.IO).launch {
                ServiceDaemon.handleServiceStatus()
            }
            ServiceDaemon.handleDisconnectedKnownServices()
        }

        setContent {
            IDSAppTheme {
                // If a bluetooth connection has already been established,
                // check bluetooth permissions and start searching for known devices
                if(searchForKnownDevices.value) { //FIXME: maybe use something else to activate this (visibility?)
                    //FIXME: don't relaunch the search if already searching (onStart?)
                    LaunchBluetooth(ble, daemonMode = true)
                }

                // We monitor the internet connection
                val internetConnection by internet.dynamicConnectivityState()
                val internetConnected = internetConnection === InternetState.Available
                if(!internetConnected) {
                    ServiceDaemon.clearAllConnectedServices()
                    //TODO: display a message to the user
                } else {
                    //FIXME: don't relaunch the search if already searching (onStart?)
                    ServiceDaemon.connectToServices()
                }

                // We create the main Notification channel
                NotificationHandler.createNotificationChannel(this)

                // We launch the IDS monitoring
                Detection.launchDetection(this)

                // We launch the router
                IDSApp()
            }
        }
    }
}