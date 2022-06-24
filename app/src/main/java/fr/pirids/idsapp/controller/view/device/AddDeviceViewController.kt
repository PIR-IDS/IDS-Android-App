package fr.pirids.idsapp.controller.view.device

import androidx.navigation.NavHostController

object AddDeviceViewController {
    fun closeModal(navController: NavHostController) = navController.popBackStack()
}