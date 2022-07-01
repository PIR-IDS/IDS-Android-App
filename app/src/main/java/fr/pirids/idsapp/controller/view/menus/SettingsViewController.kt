package fr.pirids.idsapp.controller.view.menus

import androidx.navigation.NavHostController

object SettingsViewController {
    fun goBack(navController: NavHostController) = navController.popBackStack()
}