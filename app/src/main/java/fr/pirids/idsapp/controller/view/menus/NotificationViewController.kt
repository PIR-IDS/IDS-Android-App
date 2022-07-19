package fr.pirids.idsapp.controller.view.menus

import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavHostController
import fr.pirids.idsapp.data.navigation.NavRoutes
import fr.pirids.idsapp.data.notifications.Notification

object NotificationViewController {
    val notificationList = mutableStateOf(listOf(Notification("1"),Notification("2"),Notification("3")))
    fun goBack(navController: NavHostController) = navController.popBackStack()
    fun showDescription(navController: NavHostController) {
        navController.navigate(NavRoutes.NotificationDescription.route + "/" + 1) //TODO: replace 1 with id
    }
}