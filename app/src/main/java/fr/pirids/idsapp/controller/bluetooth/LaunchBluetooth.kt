@file:OptIn(ExperimentalPermissionsApi::class)

package fr.pirids.idsapp.controller.bluetooth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.CoroutineScope

@Composable
fun LaunchBluetooth(ble: BluetoothConnection = BluetoothConnection(LocalContext.current), scope: CoroutineScope = rememberCoroutineScope()) {
    val multiplePermissionsState = rememberMultiplePermissionsState(ble.getNecessaryPermissions()) { ble.onPermissionsResult(it) }
    if(!multiplePermissionsState.allPermissionsGranted) {
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(key1 = lifecycleOwner, effect = {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        multiplePermissionsState.launchMultiplePermissionRequest()
                    }
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        })
    } else {
        ble.onPermissionsResult(multiplePermissionsState.permissions.associate { it.permission to it.status.isGranted })
    }

    //FIXME: crash when permissions not granted, it should wait to have an answer before starting the bluetooth scan
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { ble.handleScanBluetoothIntent(it, scope) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    ble.launchScan(launcher, scope)
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            Device.foundDevices.value = setOf() // We clear the list of found devices after the search window is closed
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    })
}