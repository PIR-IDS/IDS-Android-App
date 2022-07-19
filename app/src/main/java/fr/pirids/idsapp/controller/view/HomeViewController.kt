package fr.pirids.idsapp.controller.view

import androidx.navigation.NavHostController
import fr.pirids.idsapp.data.items.DeviceId
import fr.pirids.idsapp.data.items.ServiceId
import fr.pirids.idsapp.data.navigation.NavRoutes

object HomeViewController {
    fun addService(navController: NavHostController) {
        navController.navigate(NavRoutes.AddService.route)
    }

    fun showService(navController: NavHostController, service: ServiceId) {
        navController.navigate(NavRoutes.Service.route + "/" + service.ordinal)
    }

    fun addDevice(navController: NavHostController) {
        navController.navigate(NavRoutes.AddDevice.route)
    }

    fun showDevice(navController: NavHostController, device: DeviceId, address: String) {
        navController.navigate(NavRoutes.Device.route + "/" + device.ordinal + "?address=" + address)
    }

    fun showNotification(navController: NavHostController) {
        navController.navigate(NavRoutes.Notification.route)
    }

    fun showSettings(navController: NavHostController) {
        navController.navigate(NavRoutes.Settings.route)
    }
}