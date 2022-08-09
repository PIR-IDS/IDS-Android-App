@file:OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)

package fr.pirids.idsapp.ui.views.menus

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import fr.pirids.idsapp.BuildConfig
import fr.pirids.idsapp.R
import fr.pirids.idsapp.controller.view.menus.SettingsViewController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SettingsView(navController: NavHostController) {
    val context = LocalContext.current
    SettingsViewController.initPreferences(context)
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
                    .padding(top = it.calculateTopPadding(), start = 20.dp, end = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = stringResource(id = R.string.general),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(15.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 35.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.full_screen_alert),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(id = R.string.full_screen_alert_explanation),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    Switch(
                        checked = SettingsViewController.fullScreenState.value,
                        onCheckedChange = { value ->
                            SettingsViewController.fullScreenState.value = value
                            CoroutineScope(Dispatchers.IO).launch {
                                SettingsViewController.toggleFullScreenAlert(context, value)
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = stringResource(id = R.string.about),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .padding(horizontal = 35.dp, vertical = 8.dp),
                ) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.version),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = BuildConfig.VERSION_NAME,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Column {
                        Text(
                            text = stringResource(id = R.string.developers),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Romain Monier, Morgan Pelloux, Noé Chauveau, Amélie Muller",
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            textAlign = TextAlign.Left,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SettingsViewPreview() {
    SettingsView(navController = rememberAnimatedNavController())
}

@Composable
private fun TopBar(navController: NavHostController) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.settings),
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Left,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        navigationIcon = {
            IconButton(
                onClick = { SettingsViewController.goBack(navController) }
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
    )
}

@Preview(showBackground = true)
@Composable
private fun TopBarPreview() {
    TopBar(navController = rememberAnimatedNavController())
}