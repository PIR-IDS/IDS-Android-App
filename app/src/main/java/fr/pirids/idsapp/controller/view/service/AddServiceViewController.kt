package fr.pirids.idsapp.controller.view.service

import androidx.navigation.NavHostController
import fr.pirids.idsapp.data.items.ServiceId
import fr.pirids.idsapp.data.navigation.NavRoutes

object AddServiceViewController {
    fun closeModal(navController: NavHostController) = navController.popBackStack()

    fun showService(navController: NavHostController, service: ServiceId) {
        navController.navigate(NavRoutes.Service.route + "/" + service.ordinal)
    }

}