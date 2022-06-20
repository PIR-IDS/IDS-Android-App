package fr.pirids.idsapp.controller.view.service

import androidx.navigation.NavHostController

object AddServiceViewController {
    fun closeModal(navController: NavHostController) = navController.popBackStack()
}