@file:OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class
)

package fr.pirids.idsapp.ui.views.menus

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.TweenSpec
import fr.pirids.idsapp.R
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import fr.pirids.idsapp.controller.detection.Detection
import fr.pirids.idsapp.controller.view.menus.NotificationViewController
import fr.pirids.idsapp.data.notifications.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.compose.material3.Text as Material3Text
import androidx.compose.material3.TextButton as Material3TextButton
import fr.pirids.idsapp.data.detection.Detection as DetectionData
import fr.pirids.idsapp.data.items.Service as ServiceItem

@Composable
fun NotificationView(navController: NavHostController, appSnackbarHostState: SnackbarHostState) {
    ConfirmDialog()
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(appSnackbarHostState) },
            topBar = { TopBar(navController) }
        ) {
            AnimatedVisibility(visible = Detection.detectedIntrusions.value.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = it.calculateTopPadding(), bottom = 200.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.Icon(
                        Icons.Filled.VerifiedUser,
                        tint = MaterialTheme.colorScheme.onBackground,
                        contentDescription = stringResource(id = R.string.no_detection),
                        modifier = Modifier
                            .size(110.dp)
                            .alpha(0.2f)
                    )
                    Material3Text(
                        text = stringResource(id = R.string.no_detection),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.alpha(0.3f)
                    )
                }
            }
            AnimatedVisibility(visible = Detection.detectedIntrusions.value.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .padding(top = it.calculateTopPadding()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(
                        items = Detection.detectedIntrusions.value.sortedByDescending { i -> i.timestamp }.toList(),
                        { detectionData : DetectionData -> detectionData.timestamp }
                    ) { detection ->
                        val notification = NotificationViewController.getNotificationFromDetection(detection)
                        val message = stringResource(id = R.string.detection_removed)
                        val visible = remember { mutableStateOf(true) }
                        val dismissState = rememberDismissState(
                            confirmStateChange = { dv ->
                                if(dv == DismissValue.DismissedToStart) {
                                    visible.value = false
                                    NotificationViewController.removeDetectionData(detection)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        appSnackbarHostState.showSnackbar(message)
                                    }
                                }
                                true
                            }
                        )
                        SwipeToDismiss(
                            state = dismissState,
                            modifier = Modifier
                                .animateItemPlacement(),
                            background = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(15.dp)
                                        .background(color = Transparent),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Column(modifier = Modifier.align(Alignment.CenterEnd)) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.delete),
                                            tint = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )
                                        Spacer(modifier = Modifier.heightIn(5.dp))
                                        Material3Text(
                                            text = stringResource(id = R.string.delete),
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            },
                            dismissContent = {
                                AnimatedVisibility(visible = visible.value,
                                    exit = fadeOut(
                                        animationSpec = TweenSpec(200, 200, FastOutLinearInEasing)
                                    )
                                ) {
                                    NotificationCard(navController = navController, notification)
                                }
                            },
                            directions = setOf(DismissDirection.EndToStart)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun NotificationViewPreview() {
    NotificationView(navController = rememberAnimatedNavController(), appSnackbarHostState = remember { SnackbarHostState() })
}

@Composable
fun NotificationCard(navController: NavHostController, info: Notification) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = {
            NotificationViewController.showDescription(navController)
        }
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
                Material3Text(
                    text = stringResource(id = info.title),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(5.dp))
                Material3Text(
                    text = stringResource(id = info.message) + " " + Instant
                        .ofEpochMilli(info.timestamp).atZone(ZoneId.of("UTC"))
                        .withZoneSameInstant(TimeZone.getDefault().toZoneId())
                        .format(DateTimeFormatter.ofPattern("HH'H'mm:ss (d MMMM yyyy)")),
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
                    .size(70.dp)
                    .width(IntrinsicSize.Min)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = info.service.logo),
                    contentDescription = info.service.name,
                    contentScale = ContentScale.Fit,
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
fun NotificationCardPreview() {
    NotificationCard(
        navController = rememberAnimatedNavController(),
        info = Notification(
            title = R.string.wallet_intrusion,
            message = R.string.suspicious_transaction,
            timestamp = System.currentTimeMillis(),
            service = ServiceItem.list.first()
        )
    )
}

@Composable
private fun TopBar(navController: NavHostController) {
    TopAppBar(
        title = {
            Material3Text(
                text = stringResource(id = R.string.notifications),
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Left,
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(
                modifier = Modifier
                    .fillMaxHeight(),
            )
        },
        navigationIcon = {
            IconButton(
                onClick = { NotificationViewController.goBack(navController) }
            ) {
                Icon(
                    Icons.Outlined.ArrowBack,
                    contentDescription = stringResource(id = R.string.go_back),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        backgroundColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        actions = {
            IconButton(
                onClick = { NotificationViewController.deleteAllDialog.value = true }
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = stringResource(id = R.string.delete),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun TopBarPreview() {
    TopBar(navController = rememberAnimatedNavController())
}

@Composable
fun ConfirmDialog() {
    if (NotificationViewController.deleteAllDialog.value) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onDismissRequest.
                NotificationViewController.deleteAllDialog.value = false
            },
            icon = {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(id = R.string.delete),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            title = {
                Material3Text(text = stringResource(id = R.string.delete_title))
            },
            text = {
                Material3Text(text = stringResource(id = R.string.delete_message))
            },
            confirmButton = {
                Material3TextButton(
                    onClick = {
                        NotificationViewController.deleteAllDialog.value = false
                        NotificationViewController.removeAllDetectionData()
                    }
                ) {
                    Material3Text(text = stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                Material3TextButton(
                    onClick = {
                        NotificationViewController.deleteAllDialog.value = false
                    }
                ) {
                    Material3Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }
}