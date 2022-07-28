@file:OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)

package fr.pirids.idsapp.ui.views.menus

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import fr.pirids.idsapp.R
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.vector.ImageVector
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
import fr.pirids.idsapp.data.items.Service as ServiceItem

@Composable
fun NotificationView(navController: NavHostController) {
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = { TopBar(navController) }
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(top = it.calculateTopPadding()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(items = Detection.detectedIntrusions.value.sortedByDescending { i -> i.timestamp }.toList()) { detection ->
                    val notification = NotificationViewController.getNotificationFromDetection(detection)
                    val dismissState = rememberDismissState(
                        confirmStateChange = {
                            NotificationViewController.removeDetectionData(detection)
                            true
                        }
                    )
                    SwipeToDismiss(
                        state = dismissState,
                        background = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(15.dp)
                                    .background(color = Transparent)
                            ) {
                                Column(modifier = Modifier.align(Alignment.CenterEnd)) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                    Spacer(modifier = Modifier.heightIn(5.dp))
                                    Text(
                                        text = stringResource(id = R.string.delete),
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        },
                        dismissContent = {
                            NotificationCard(navController = navController, notification)
                        },
                        directions = setOf(DismissDirection.EndToStart)
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationCard(navController: NavHostController, info: Notification) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = {
            NotificationViewController.showDescription(navController)
        }
    ) {
        Row {
            Column {
                Text(
                    text = stringResource(id = info.title),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(horizontal = 35.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(id = info.message),
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
                    painter = painterResource(id = info.service.logo),
                    contentDescription = info.service.name,
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
fun NotificationViewPreview() {
    NotificationView(navController = rememberAnimatedNavController())
}

@Composable
private fun TopBar(navController: NavHostController) {
    TopAppBar(
        title = {
            Text(
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
                onClick = { NotificationViewController.removeAllDetectionData() }
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