@file:OptIn(ExperimentalAnimationApi::class)

package fr.pirids.idsapp.ui.views.service

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import fr.pirids.idsapp.R
import fr.pirids.idsapp.controller.view.ServiceViewController
import fr.pirids.idsapp.extensions.custom_success
import fr.pirids.idsapp.model.items.Device
import fr.pirids.idsapp.model.items.DeviceId
import fr.pirids.idsapp.model.items.Service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import fr.pirids.idsapp.controller.detection.Service as ServiceController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceView(navController: NavHostController, service: Service) {
    ServiceViewController.serviceScope = rememberCoroutineScope()
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
                AnimatedVisibility(visible = !ServiceViewController.isConnected.value) {
                    LoginForm(service = service)
                }
                AnimatedVisibility(visible = ServiceViewController.isConnected.value) {
                    ConnectedLabel(service = service)
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
fun TopBar(navController: NavHostController) {
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
fun TopBarPreview() {
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
            style = MaterialTheme.typography.displaySmall
        )

        Spacer(modifier = Modifier.height(5.dp))
        Button(
            onClick = { ServiceViewController.isConnected.value = false },
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
fun LoginForm(modifier: Modifier = Modifier, service: Service) {
    Column(
        modifier = Modifier
            .then(modifier)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val username = remember { mutableStateOf(TextFieldValue()) }
        val password = remember { mutableStateOf(TextFieldValue()) }

        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
            Image(
                painter = painterResource(id = service.logo),
                contentDescription = service.name
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            label = { Text(text = stringResource(id = R.string.username)) },
            value = username.value,
            onValueChange = { username.value = it }
        )

        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            label = { Text(text = stringResource(id = R.string.password)) },
            value = password.value,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            onValueChange = { password.value = it }
        )

        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
            val context = LocalContext.current
            val focusManager = LocalFocusManager.current
            val notFoundText = stringResource(id = R.string.service_not_connected)
            Button(
                //TODO: trigger this onclick at each modal opening if
                // this service was previously connected (credentials in database)
                onClick = {
                    focusManager.clearFocus()
                    ServiceViewController.serviceScope.launch(Dispatchers.IO) {
                        val (serviceData, serviceConnected) = ServiceController.getServiceAndStatus(
                            username.value.text,
                            password.value.text,
                            service
                        )
                        if (serviceConnected) {
                            ServiceViewController.isConnected.value = true
                            ServiceViewController.historyScope.launch(Dispatchers.IO) {
                                ServiceViewController.updateServiceData(serviceData, service)
                            }
                        } else {
                            ServiceViewController.isConnected.value = false
                            ServiceViewController.serviceScope.launch(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    notFoundText,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = stringResource(id = R.string.login))
            }
        }
    }
}

@Composable
fun HistoryList(modifier: Modifier = Modifier, service: Service) {
    ServiceViewController.historyScope = rememberCoroutineScope()
    AnimatedVisibility(visible = ServiceViewController.serviceHistory.value.isEmpty()) {
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
        items(ServiceViewController.serviceHistory.value) {
            Item(item = it, service = service)
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
