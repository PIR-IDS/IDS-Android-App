package fr.pirids.idsapp.model.view

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.DevicesOther
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import fr.pirids.idsapp.R

import fr.pirids.idsapp.ui.main.DevicesScreen
import fr.pirids.idsapp.ui.main.NetworkScreen
import fr.pirids.idsapp.ui.main.ServicesScreen

typealias ComposableFun = @Composable () -> Unit

sealed class TabItem(var icon: ImageVector, @StringRes var title: Int, var screen: ComposableFun) {
    object Services : TabItem(Icons.Outlined.AccountBalanceWallet, R.string.tab_text_services, { ServicesScreen() })
    object Devices : TabItem(Icons.Outlined.DevicesOther, R.string.tab_text_devices, { DevicesScreen() })
    object Network : TabItem(Icons.Outlined.Sensors, R.string.tab_text_network, { NetworkScreen() })
}