@file:OptIn(ExperimentalMaterial3Api::class)

package fr.pirids.idsapp.ui.views.menus

import android.annotation.SuppressLint
import fr.pirids.idsapp.R
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
import fr.pirids.idsapp.data.items.Device
import fr.pirids.idsapp.data.items.Service as ServiceItem



@Composable
fun NotificationDescriptionView(navController: NavHostController ) {
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
                Spacer(modifier = Modifier.height(40.dp))
                Box(
                    modifier = Modifier
                        .size(90.dp),
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
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Device Name",
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(380.dp))
                Column(
                    modifier = Modifier
                        .padding(start = 40.dp, end = 40.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        onClick = { }
                    ) {
                        Row {
                            Column(
                                modifier = Modifier
                                    .padding(start = 30.dp, end = 30.dp)
                            )
                            {
                                Spacer(
                                    modifier = Modifier.height(10.dp)
                                )
                                Text(
                                    text = stringResource(id = R.string.localisation),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Spacer(
                                modifier = Modifier.weight(1f)
                                    .fillMaxHeight()
                            )
                            Box(
                                modifier = Modifier
                                    .size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.PinDrop,
                                    contentDescription = stringResource(id = R.string.localisation),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                        Spacer( modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            onClick = { }
                        ) {
                            Row {
                                Column(
                                    modifier = Modifier
                                        .padding(start = 30.dp, end = 30.dp)
                                )
                                {
                                    Spacer(
                                        modifier = Modifier.height(10.dp)
                                    )
                                    Text(
                                        text = stringResource(id = R.string.delete),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Spacer(
                                    modifier = Modifier.weight(1f)
                                        .fillMaxHeight()
                                )
                                Box(
                                    modifier = Modifier
                                        .size(40.dp),
                                    contentAlignment = Alignment.Center

                                ) {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = stringResource(id = R.string.localisation),
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )

                            }
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalAnimationApi::class)
@Preview
@Composable
fun NotificationDescriptionViewPreview() {
    NotificationDescriptionView(navController =  rememberAnimatedNavController())
}

@Composable
private fun TopBar(navController: NavHostController) {
    TopAppBar(

        title = {  },
        navigationIcon = {
            IconButton(
                onClick = { NotificationViewController.goBack(navController) }
            ) {Icon(
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

@OptIn(ExperimentalAnimationApi::class)
@Preview(showBackground = true)
@Composable
private fun TopBarPreview() {
    TopBar(navController = rememberAnimatedNavController())
}