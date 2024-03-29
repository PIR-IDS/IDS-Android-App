@file:OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)

package fr.pirids.idsapp.ui.views.device

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import fr.pirids.idsapp.R
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
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
import fr.pirids.idsapp.extensions.custom_success
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
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
                    .padding(top = it.calculateTopPadding()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(15.dp))
                Box(
                    modifier = Modifier
                        .size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Image(
                            painter = painterResource(id = device.logo),
                            contentDescription = device.name,
                            //contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                        )
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AnimatedVisibility(visible = bleDevice in Device.connectedDevices.value) {
                                androidx.compose.material3.Icon(
                                    Icons.Filled.CheckCircle,
                                    tint = androidx.compose.material.MaterialTheme.colors.custom_success,
                                    contentDescription = stringResource(id = R.string.connected),
                                    modifier = Modifier
                                        .size(40.dp)
                                )
                            }
                            AnimatedVisibility(visible = bleDevice !in Device.connectedDevices.value) {
                                androidx.compose.material3.Icon(
                                    Icons.Filled.Cancel,
                                    tint = MaterialTheme.colorScheme.error,
                                    contentDescription = stringResource(id = R.string.not_connected),
                                    modifier = Modifier
                                        .size(40.dp)
                                )
                            }
                        }
                    }
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
                val dataSetEmpty = remember { mutableStateOf(true) }
                bleDevice?.let { bleDev ->
                    LazyColumn(
                        modifier = Modifier
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when(device.id) {
                            DeviceId.WALLET_CARD -> {
                                val dataSet = (bleDev.data as WalletCardData).whenWalletOutArray
                                items(
                                    dataSet.value.sortedByDescending { zdt -> zdt },
                                    { zdt: ZonedDateTime -> zdt }
                                ) { zdt ->
                                    dataSetEmpty.value = dataSet.value.isEmpty()
                                    DataCard(
                                        bleDev.data.dataTitle,
                                        bleDev.data.dataMessage,
                                        bleDev.data.eventIcon,
                                        zdt.format(
                                            DateTimeFormatter.ofPattern("HH'H'mm:ss (d MMMM yyyy)")
                                        ),
                                        //TODO: maybe find a way to display the new ones instead of only filling the invisible top
                                        Modifier.animateItemPlacement()
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
                }
                AnimatedVisibility(visible = dataSetEmpty.value) {
                    Column(
                        modifier = Modifier
                            .heightIn(0.dp, 300.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(id = R.string.no_history))
                    }
                }
            }
        }
    }
}

@Composable
fun DataCard(@StringRes title: Int, @StringRes message: Int, icon: ImageVector, dateTime: String, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .then(modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(0.dp, 275.dp)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            ) {
                Text(
                    text = stringResource(id = title),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = stringResource(id = message) + " $dateTime",
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(15.dp))
            Spacer(modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
            )
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .width(IntrinsicSize.Min)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberVectorPainter(icon),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(30.dp)
                        .alpha(0.7f)
                        .clip(CircleShape),
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