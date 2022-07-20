@file:OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)

package fr.pirids.idsapp.ui.views.device

import androidx.annotation.StringRes
import fr.pirids.idsapp.R
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import fr.pirids.idsapp.controller.bluetooth.Device
import fr.pirids.idsapp.controller.view.device.DeviceViewController
import fr.pirids.idsapp.data.device.data.WalletCardData
import fr.pirids.idsapp.data.items.DeviceId
import fr.pirids.idsapp.data.items.Device as DeviceItem

@Composable
fun DeviceView(navController: NavHostController, device: DeviceItem, address: String) {
    val bleDevice = Device.getBluetoothDeviceFromAddress(address)
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
                Spacer(modifier = Modifier.height(15.dp))
                Box(
                    modifier = Modifier
                        .size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = device.logo),
                        contentDescription = device.name,
                        //contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = device.name,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = bleDevice?.address ?: "",
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(35.dp))
                Text(
                    text = stringResource(id = R.string.data_history),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(20.dp))

                //TODO: create a tab slider with Detection History linked to the current device

                //TODO: clean this by doing something better (timestamp array in DeviceData because it's needed everywhere anyway)
                bleDevice?.let { bleDev ->
                    when(device.id) {
                        DeviceId.WALLET_CARD -> {
                            (bleDev.data as WalletCardData).whenWalletOutArray.value.forEach { zdt ->
                                DataCard(bleDev.data.dataTitle, bleDev.data.dataMessage, bleDev.data.eventIcon, zdt.toString())
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun DataCard(@StringRes title: Int, @StringRes message: Int, icon: ImageVector, dateTime: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp)
    ) {
        //TODO: add timestamp somewhere
        Row {
            Column {
                Text(
                    text = stringResource(id = title),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(horizontal = 35.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(id = message) + dateTime,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(horizontal = 40.dp, vertical = 0.5.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(15.dp))
            Spacer(modifier = Modifier
                .weight(1f)
                .fillMaxHeight())
            Box(
                modifier = Modifier
                    .size(70.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberVectorPainter(icon),
                    contentDescription = "",
                    //contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )

            }
        }
    }
}

@Preview
@Composable
fun DeviceViewPreview() {
    DeviceView(navController = rememberAnimatedNavController(), DeviceItem.list.first(), "")
}

@Composable
private fun TopBar(navController: NavHostController) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(
                onClick = { DeviceViewController.closeModal(navController) }
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