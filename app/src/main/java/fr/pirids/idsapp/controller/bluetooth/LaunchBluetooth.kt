@file:OptIn(ExperimentalPermissionsApi::class, ExperimentalAnimationApi::class)

package fr.pirids.idsapp.controller.bluetooth

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.component2
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import fr.pirids.idsapp.controller.view.device.DeviceViewController
import kotlinx.coroutines.CoroutineScope

@Composable
fun LaunchBluetooth(
    ble: BluetoothConnection = BluetoothConnection(LocalContext.current),
    scope: CoroutineScope = rememberCoroutineScope(),
    daemonMode: Boolean = false
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (daemonMode)
            ble.handleSearchBluetoothIntent(it, scope)
        else
            ble.handleScanBluetoothIntent(it, scope)
    }

    val multiplePermissionsState = rememberMultiplePermissionsState(ble.getNecessaryPermissions()) {
        ble.onPermissionsResult(it, daemonMode, launcher, scope)
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