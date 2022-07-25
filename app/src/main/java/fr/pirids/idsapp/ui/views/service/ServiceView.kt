@file:OptIn(ExperimentalAnimationApi::class)

package fr.pirids.idsapp.ui.views.service

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import fr.pirids.idsapp.R
import fr.pirids.idsapp.controller.view.service.ServiceViewController
import fr.pirids.idsapp.data.api.data.IzlyData
import fr.pirids.idsapp.data.items.Device
import fr.pirids.idsapp.data.items.DeviceId
import fr.pirids.idsapp.data.items.Service
import fr.pirids.idsapp.data.items.ServiceId
import fr.pirids.idsapp.extensions.custom_success
import java.time.Instant
import java.time.ZoneId
import java.util.*
import fr.pirids.idsapp.controller.detection.Service as ServiceController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceView(navController: NavHostController, service: Service) {
    ServiceViewController.serviceScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = { TopBar(navController) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = it.calculateTopPadding()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val serv = ServiceController.getKnownApiServiceFromServiceItem(service)
                AnimatedVisibility(visible = serv == null) {
                    LoginForm(service = service, snackbarHostState = snackbarHostState)
                }
                AnimatedVisibility(visible = serv != null && serv !in ServiceController.connectedServices.value && !ServiceViewController.isLoading.value) {
                    DisconnectedLabel(service = service)
                }
                AnimatedVisibility(visible = serv != null && serv in ServiceController.connectedServices.value && !ServiceViewController.isLoading.value) {
                    ConnectedLabel(service = service)
                }
                Spacer(modifier = Modifier.height(20.dp))
                AnimatedVisibility(visible = serv != null && serv in ServiceController.monitoredServices.value) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.VerifiedUser,
                            tint = androidx.compose.material.MaterialTheme.colors.custom_success,
                            contentDescription = stringResource(id = R.string.service_monitored),
                            modifier = Modifier
                                .size(80.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.service_monitored),
                            style = MaterialTheme.typography.bodyMedium,
                            color = androidx.compose.material.MaterialTheme.colors.custom_success
                        )
                    }
                }
                AnimatedVisibility(visible = serv != null && serv !in ServiceController.monitoredServices.value) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.GppBad,
                            tint = MaterialTheme.colorScheme.error,
                            contentDescription = stringResource(id = R.string.service_not_monitored),
                            modifier = Modifier
                                .size(80.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.service_not_monitored),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = stringResource(id = R.string.history).uppercase(), style = MaterialTheme.typography.titleLarge)
                HistoryList(service = service)
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = stringResource(id = R.string.linked_probes).uppercase(), style = MaterialTheme.typography.titleLarge)
                ProbesList(service = service)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Preview
@Composable
fun ServiceViewPreview() {
    ServiceView(navController = rememberAnimatedNavController(), Service.list.first())
}

@Composable
private fun TopBar(navController: NavHostController) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(
                onClick = { ServiceViewController.closeModal(navController) }
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
fun ConnectedLabel(service: Service) {
    Column(
        modifier = Modifier
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = service.logo),
                    contentDescription = service.name
                )
                Icon(
                    Icons.Outlined.TaskAlt,
                    modifier = Modifier.size(65.dp),
                    contentDescription = stringResource(id = R.string.connected),
                    tint = androidx.compose.material.MaterialTheme.colors.custom_success
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            text = stringResource(id = R.string.connected),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(5.dp))
        Button(
            onClick = { ServiceViewController.disconnectService(service) },
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = stringResource(id = R.string.reconnect))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectedLabelPreview() {
    ConnectedLabel(Service.list.first())
}

@Composable
fun DisconnectedLabel(service: Service) {
    Column(
        modifier = Modifier
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = service.logo),
                    contentDescription = service.name
                )
                Icon(
                    Icons.Outlined.Error,
                    modifier = Modifier.size(65.dp),
                    contentDescription = stringResource(id = R.string.connected),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            text = stringResource(id = R.string.service_disconnected),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(5.dp))
        Button(
            onClick = { ServiceViewController.disconnectService(service) },
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = stringResource(id = R.string.reconnect))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DisconnectedLabelPreview() {
    DisconnectedLabel(Service.list.first())
}

@Composable
fun LoginForm(modifier: Modifier = Modifier, service: Service, snackbarHostState: SnackbarHostState) {
    Column(
        modifier = Modifier
            .then(modifier)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val username = remember { mutableStateOf(TextFieldValue()) }
        val password = remember { mutableStateOf(TextFieldValue()) }
        val focusManager = LocalFocusManager.current
        val notFoundText = stringResource(id = R.string.service_not_connected)

        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
            Image(
                painter = painterResource(id = service.logo),
                contentDescription = service.name
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            readOnly = ServiceViewController.isLoading.value,
            label = { Text(text = stringResource(id = R.string.username)) },
            value = username.value,
            onValueChange = { username.value = it },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            readOnly = ServiceViewController.isLoading.value,
            label = { Text(text = stringResource(id = R.string.password)) },
            value = password.value,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { ServiceViewController.onLoginAction(
                    focusManager,
                    service,
                    snackbarHostState,
                    username.value.text,
                    password.value.text,
                    notFoundText
                )}
            ),
            onValueChange = { password.value = it },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))
        AnimatedVisibility(visible = !ServiceViewController.isLoading.value) {
            Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
                Button(
                    //TODO: trigger this onclick at each modal opening if
                    // this service was previously connected (credentials in database)
                    onClick = { ServiceViewController.onLoginAction(
                        focusManager,
                        service,
                        snackbarHostState,
                        username.value.text,
                        password.value.text,
                        notFoundText
                    )},
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = stringResource(id = R.string.login))
                }
            }
        }
        AnimatedVisibility(visible = ServiceViewController.isLoading.value) {
            Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .size(50.dp)
                )
            }
        }
    }
}

@Composable
fun HistoryList(modifier: Modifier = Modifier, service: Service) {
    ServiceViewController.historyScope = rememberCoroutineScope()

    //TODO: set timestamp list directly on the ApiInterface as we will always have it anyway
    val dataList = remember { mutableStateOf<List<Long>> (listOf()) }
    when(service.id) {
        ServiceId.IZLY -> {
            ServiceController.getKnownApiServiceFromServiceItem(service)?.let {
                dataList.value = (it.data.value as IzlyData).transactionList.toMutableList()
            }
        }
    }

    AnimatedVisibility(visible = dataList.value.isEmpty()) {
        Column(
            modifier = Modifier
                .then(modifier)
                .heightIn(0.dp, 300.dp)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(id = R.string.no_history))
        }
    }
    LazyColumn(
        modifier = Modifier
            .then(modifier)
            .heightIn(0.dp, 300.dp)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(dataList.value) {
            Item(
                item = Instant
                        .ofEpochMilli(it).atZone(ZoneId.of("UTC"))
                        .withZoneSameInstant(TimeZone.getDefault().toZoneId())
                        .toString(),
                service = service
            )
        }
    }
}

@Composable
fun Item(item: String, service: Service) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .height(40.dp)
            .background(color = MaterialTheme.colorScheme.secondary)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = service.logo),
                contentDescription = item,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .align(CenterVertically)
            )
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .align(CenterVertically),
                text = item,
                color = MaterialTheme.colorScheme.onSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview
@Composable
fun ItemPreview() {
    Item("Item", Service.list.first())
}

@Composable
fun ProbesList(modifier: Modifier = Modifier, service: Service) {
    AnimatedVisibility(visible = ServiceViewController.getProbesList(service).isEmpty()) {
        Column(
            modifier = Modifier
                .then(modifier)
                .heightIn(0.dp, 300.dp)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(id = R.string.no_devices))
        }
    }
    LazyColumn(
        modifier = Modifier
            .then(modifier)
            .heightIn(0.dp, 300.dp)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(ServiceViewController.getProbesList(service)) {
            ItemDevice(item = it)
        }
    }
}

@Composable
fun ItemDevice(item: Device) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .height(40.dp)
            .background(color = MaterialTheme.colorScheme.tertiary)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = item.logo),
                contentDescription = stringResource(id = item.description),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .align(CenterVertically)
            )
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .align(CenterVertically),
                text = item.name,
                color = MaterialTheme.colorScheme.onTertiary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview
@Composable
fun ItemDevicePreview() {
    ItemDevice(Device.get(DeviceId.WALLET_CARD))
}
