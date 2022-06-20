package fr.pirids.idsapp.controller.view.device

import androidx.navigation.NavHostController

object DeviceViewController {
    fun closeModal(navController: NavHostController) = navController.popBackStack()
}