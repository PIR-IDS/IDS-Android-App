@file:OptIn(ExperimentalPermissionsApi::class)

package fr.pirids.idsapp.controller.bluetooth

import android.app.Activity.RESULT_OK
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
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.CoroutineScope

@Composable
fun LaunchBluetooth(
    ble: BluetoothConnection = BluetoothConnection(LocalContext.current),
    scope: CoroutineScope = rememberCoroutineScope(),
    daemonMode: Boolean = false
) {
    val scanResult = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        when (it.resultCode) {
            RESULT_OK -> {
                ble.pair(it)
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (daemonMode)
            ble.handleSearchBluetoothIntent(it, scope)
        else
            ble.handleScanBluetoothIntent(it, scope, scanResult)
    }

    val multiplePermissionsState = rememberMultiplePermissionsState(ble.getNecessaryPermissions()) {
        ble.onPermissionsResult(it, daemonMode, launcher, scope, scanResult)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    ble.permissionsGranted = false
                    multiplePermissionsState.launchMultiplePermissionRequest()
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