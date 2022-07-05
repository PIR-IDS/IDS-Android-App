@file:OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)

package fr.pirids.idsapp.ui.views.menus

import android.annotation.SuppressLint
import android.util.Log
import fr.pirids.idsapp.R
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DismissValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import fr.pirids.idsapp.controller.detection.Service
import fr.pirids.idsapp.controller.view.HomeViewController
import fr.pirids.idsapp.controller.view.menus.NotificationViewController
import fr.pirids.idsapp.data.items.Service as ServiceItem

@Composable
fun NotificationView(navController: NavHostController) {
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    onClick = { NotificationViewController.goBack(navController)
                    }
                ) {
                    Row {
                                Column {
                                    Text(
                                        text = stringResource(id = R.string.wallet_intrusion),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier
                                            .padding(horizontal = 35.dp, vertical = 8.dp),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = stringResource(id = R.string.suspicious_transaction),
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
                            val izlyTest = ServiceItem.list.first()
                            Image(
                                painter = painterResource(id = izlyTest.logo),
                                contentDescription = izlyTest.name,
                                //contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .clickable(
                                        enabled = true,
                                        onClickLabel = izlyTest.name,
                                        onClick = { }
                                    )
                            )
                        }
                    }
                }
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
                onClick = {}
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