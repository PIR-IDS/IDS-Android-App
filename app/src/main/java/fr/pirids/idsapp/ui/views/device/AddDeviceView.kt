@file:OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class
)

package fr.pirids.idsapp.ui.views.device

import android.util.Log
import fr.pirids.idsapp.R
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import fr.pirids.idsapp.controller.bluetooth.BluetoothConnection
import fr.pirids.idsapp.controller.bluetooth.Device
import fr.pirids.idsapp.controller.view.device.AddDeviceViewController
import fr.pirids.idsapp.controller.bluetooth.LaunchBluetooth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AddDeviceView(navController: NavHostController, appSnackbarHostState: SnackbarHostState) {
    val ble = BluetoothConnection(LocalContext.current)
    val scope = rememberCoroutineScope()
    LaunchBluetooth(ble, scope)
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(appSnackbarHostState) },
            topBar = { TopBar(navController) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = it.calculateTopPadding()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.add_device),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.scanned_devices),
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(horizontal = 18.dp)
                            .size(25.dp)
                    )
                }
            }
            DevicesList(navController, ble, scope, appSnackbarHostState, modifier = Modifier.padding(top = it.calculateTopPadding()))
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddDeviceViewPreview() {
    AddDeviceView(navController = rememberAnimatedNavController(), appSnackbarHostState = remember { SnackbarHostState() })
}

@Composable
private fun TopBar(navController: NavHostController) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(
                onClick = { AddDeviceViewController.closeModal(navController) }
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = stringResource(id = R.string.close),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        backgroundColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    )
}

@Preview(showBackground = true)
@Composable
private fun TopBarPreview() {
    TopBar(navController = rememberAnimatedNavController())
}

@Composable
fun DevicesList(navController: NavHostController, ble: BluetoothConnection, scope: CoroutineScope, appSnackbarHostState: SnackbarHostState, modifier: Modifier = Modifier) {
    val successMessage = stringResource(id = R.string.device_connected)
    val failMessage = stringResource(id = R.string.device_connection_failed)
    LazyVerticalGrid(
        columns = GridCells.Adaptive(120.dp),
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.then(Modifier
            .padding(top = 116.dp, start = 10.dp, end = 10.dp)
            .fillMaxSize()
        )) {
        items(Device.foundDevices.value.toList()) {
            val device = Device.getDeviceItemFromBluetoothDevice(it) ?: return@items
            Box(
                modifier = Modifier
                    .size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(id = device.logo),
                        contentDescription = device.name,
                        //contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .clickable(
                                enabled = true,
                                onClickLabel = device.name,
                                onClick = {
                                    try {
                                        Device.connectToDevice(it, ble, scope) { success ->
                                            scope.launch(Dispatchers.Main) {
                                                appSnackbarHostState.showSnackbar("${device.name} | " + if (success) successMessage else failMessage)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("AddDeviceView", "Error while connecting to device", e)
                                    }
                                }
                            )
                    )
                    Text(
                        text = "${device.name} [${it.address}]",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DevicesListPreview() {
    DevicesList(
        navController = rememberAnimatedNavController(),
        ble = BluetoothConnection(LocalContext.current),
        scope = rememberCoroutineScope(),
        appSnackbarHostState = remember { SnackbarHostState() }
    )
}