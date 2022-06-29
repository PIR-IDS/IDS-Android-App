package fr.pirids.idsapp.data.view

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.DevicesOther
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import fr.pirids.idsapp.R

import fr.pirids.idsapp.ui.views.DevicesScreen
import fr.pirids.idsapp.ui.views.NetworkScreen
import fr.pirids.idsapp.ui.views.ServicesScreen

sealed class TabItem(var icon: ImageVector, @StringRes var title: Int, var screen: @Composable (navController: NavHostController) -> Unit) {
    object Services : TabItem(Icons.Outlined.AccountBalance, R.string.tab_text_services, { ServicesScreen(it) })
    object Devices : TabItem(Icons.Outlined.DevicesOther, R.string.tab_text_devices, { DevicesScreen(it) })
    object Network : TabItem(Icons.Outlined.Sensors, R.string.tab_text_network, { NetworkScreen(it) })
}