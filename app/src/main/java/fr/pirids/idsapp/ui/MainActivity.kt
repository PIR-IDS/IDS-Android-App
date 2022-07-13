package fr.pirids.idsapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import fr.pirids.idsapp.controller.bluetooth.BluetoothConnection
import fr.pirids.idsapp.controller.navigation.IDSApp
import fr.pirids.idsapp.ui.theme.IDSAppTheme
import fr.pirids.idsapp.controller.bluetooth.LaunchBluetooth
import fr.pirids.idsapp.controller.daemon.DeviceDaemon
import fr.pirids.idsapp.controller.detection.Detection
import fr.pirids.idsapp.controller.detection.NotificationHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var searchForKnownDevices = false
        val ble = BluetoothConnection(this)

        // We check if there a service to monitor and a device to connect to
        CoroutineScope(Dispatchers.IO).launch {
            searchForKnownDevices = DeviceDaemon.searchForDevice(this@MainActivity, ble)
        }

        setContent {
            IDSAppTheme {
                // If a bluetooth connection has already been established,
                // check bluetooth permissions and start searching for known devices
                if(searchForKnownDevices) {
                    //FIXME: don't relaunch the search if already searching (onStart?)
                    LaunchBluetooth(ble, daemonMode = true)
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