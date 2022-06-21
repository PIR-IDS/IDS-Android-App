@file:OptIn(ExperimentalPermissionsApi::class)

package fr.pirids.idsapp.controller.bluetooth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@Composable
fun LaunchBluetooth() {
    val ble = BluetoothConnection(LocalContext.current)
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
                lifecycleOwner.lifecycle.removeObserver(observer) //TODO: maybe remove this block in order to always keep an eye on the BLE connection
            }
        })
    } else {
        ble.onPermissionsResult(multiplePermissionsState.permissions.associate { it.permission to it.status.isGranted })
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { ble.handleBluetoothIntent(it) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    ble.setUpBluetooth(launcher)
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer) //TODO: maybe remove this block in order to always keep an eye on the BLE connection
        }
    })
}