@file:OptIn(ExperimentalAnimationApi::class)

package fr.pirids.idsapp.ui.views.service

import android.widget.Toast
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import fr.pirids.idsapp.R
import fr.pirids.idsapp.controller.view.ServiceViewController
import fr.pirids.idsapp.model.items.Device
import fr.pirids.idsapp.model.items.DeviceId
import fr.pirids.idsapp.model.items.Service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceView(navController: NavHostController, service: Service) {
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
                LoginForm(service = service)
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
fun LoginForm(modifier: Modifier = Modifier, service: Service) {
    val scope = rememberCoroutineScope()
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
                onClick = {
                    focusManager.clearFocus()
                    scope.launch(Dispatchers.IO) {
                        val serviceConnected = ServiceViewController.checkService(
                            username.value.text,
                            password.value.text,
                            service
                        )
                        if (serviceConnected) {
                            //TODO
                            scope.launch(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    "service connected!!!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            scope.launch(Dispatchers.Main) {
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
    LazyColumn(
        modifier = Modifier
            .then(modifier)
            .heightIn(0.dp, 300.dp)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(ServiceViewController.getServiceHistory(service)) {
            Item(item = it)
        }
    }
}

@Composable
fun Item(item: String) {
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
                painter = painterResource(id = R.drawable.izly_logo),
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
    Item("Item")
}

@Composable
fun ProbesList(modifier: Modifier = Modifier, service: Service) {
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
