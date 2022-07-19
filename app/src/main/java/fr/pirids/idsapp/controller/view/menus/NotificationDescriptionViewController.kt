package fr.pirids.idsapp.controller.view.menus

import androidx.navigation.NavHostController

object NotificationDescriptionViewController {
    fun closeModal(navController: NavHostController) = navController.popBackStack()
}