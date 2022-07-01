package fr.pirids.idsapp.controller.view.menus

import androidx.navigation.NavHostController

object NotificationViewController {
    fun goBack(navController: NavHostController) = navController.popBackStack()
}