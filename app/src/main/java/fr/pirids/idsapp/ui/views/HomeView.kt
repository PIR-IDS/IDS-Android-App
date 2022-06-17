@file:OptIn(ExperimentalAnimationApi::class)

package fr.pirids.idsapp.ui.views

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import fr.pirids.idsapp.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.TabRow
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.pager.*
import fr.pirids.idsapp.controller.view.HomeViewController
import fr.pirids.idsapp.model.items.Device
import fr.pirids.idsapp.model.items.Service
import fr.pirids.idsapp.model.view.TabItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(navController: NavHostController) {
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = { TopBar(navController) }
        ) {
            TextTabs(
                navController = navController,
                modifier = Modifier
                    .padding(top = it.calculateTopPadding())
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HomeView(navController = rememberAnimatedNavController())
}

@Composable
fun TopBar(navController: NavHostController) {
    TopAppBar(
        title = {
            Image(painter = painterResource(R.drawable.ids_logo), contentDescription = "")
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = stringResource(id = R.string.app_name),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        backgroundColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        actions = {
            IconButton(
                onClick = {}
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = stringResource(id = R.string.notifications))
            }
            IconButton(
                onClick = {}
            ) {
                Icon(Icons.Outlined.Settings, contentDescription = stringResource(id = R.string.settings))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    TopBar(navController = rememberAnimatedNavController())
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TextTabs(modifier: Modifier, navController: NavHostController) {
    val tabs = listOf(
        TabItem.Services,
        TabItem.Devices,
        TabItem.Network
    )
    val pagerState = rememberPagerState()
    Column(modifier = modifier) {
        Tabs(tabs = tabs, pagerState = pagerState)
        TabsContent(navController = navController, tabs = tabs, pagerState = pagerState)
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Tabs(tabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
            )
        }) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                icon = { Icon(tab.icon, contentDescription = "") },
                text = { Text(stringResource(id = tab.title).uppercase()) },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabsContent(navController: NavHostController, tabs: List<TabItem>, pagerState: PagerState) {
    HorizontalPager(state = pagerState, count = tabs.size) { page ->
        tabs[page].screen(navController)
    }
}

@Composable
fun ServicesScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.tab_text_services),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge
        )
        FlowRow(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Service.list.forEach {
                Box(
                    modifier = Modifier
                        .size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = it.logo),
                        contentDescription = it.name,
                        //contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .clickable(
                                enabled = true,
                                onClickLabel = it.name,
                                onClick = { HomeViewController.showService(navController, it.id)}
                            )
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.AddCircle,
                    contentDescription = stringResource(id = R.string.add_service),
                    //contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .clickable(
                            enabled = true,
                            onClickLabel = stringResource(id = R.string.add_service),
                            onClick = { HomeViewController.addService(navController) }
                        )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServicesScreenPreview() {
    ServicesScreen(navController = rememberAnimatedNavController())
}

@Composable
fun DevicesScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.tab_text_devices),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge
        )
        FlowRow(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Device.list.forEach {
                Box(
                    modifier = Modifier
                        .size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = it.logo),
                        contentDescription = it.name,
                        //contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .clickable(
                                enabled = true,
                                onClickLabel = it.name,
                                onClick = {
                                    Log.i("MainActivity", "Clicked on ${it.name}")
                                }
                            )
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.AddCircle,
                    contentDescription = stringResource(id = R.string.add_device),
                    //contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .clickable(
                            enabled = true,
                            onClickLabel = stringResource(id = R.string.add_device),
                            onClick = {
                                Log.i("MainActivity", "Clicked on ADD DEVICE")
                            }
                        )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DevicesScreenPreview() {
    DevicesScreen(navController = rememberAnimatedNavController())
}

@Composable
fun NetworkScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.tab_text_network),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NetworkScreenPreview() {
    NetworkScreen(navController = rememberAnimatedNavController())
}