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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import fr.pirids.idsapp.controller.bluetooth.Device
import fr.pirids.idsapp.controller.view.device.AddDeviceViewController
import fr.pirids.idsapp.controller.bluetooth.LaunchBluetooth
import fr.pirids.idsapp.controller.view.service.ServiceViewController
import fr.pirids.idsapp.ui.views.service.Item

@Composable
fun AddDeviceView(navController: NavHostController) {
    LaunchBluetooth()
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = { TopBar(navController) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = it.calculateTopPadding()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(horizontal = 18.dp)
                            .size(30.dp)
                    )
                }
            }
            DevicesList(navController, modifier = Modifier.padding(top = it.calculateTopPadding()))
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddDeviceViewPreview() {
    AddDeviceView(navController = rememberAnimatedNavController())
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
fun DevicesList(navController: NavHostController, modifier: Modifier = Modifier) {
    //TODO: implement a FlowRow to display the devices (but LazyFlowRow does not exist yet...)
    LazyColumn(modifier = modifier.then(Modifier.padding(top = 56.dp))) {
        items(AddDeviceViewController.getScannedDevices()) {
            Box(
                modifier = Modifier
                    .size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = it.logo),
                    contentDescription = it.name,
                    //contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .clickable(
                            enabled = true,
                            onClickLabel = it.name,
                            onClick = {
                                try {
                                    AddDeviceViewController.connectToDevice(it)
                                } catch (e: Exception) {
                                    Log.e("AddDeviceView", "Error while connecting to device", e)
                                }
                            }
                        )
                )
            }
        }
    }
}

@Preview
@Composable
fun DevicesListPreview() {
    DevicesList(navController = rememberAnimatedNavController())
}