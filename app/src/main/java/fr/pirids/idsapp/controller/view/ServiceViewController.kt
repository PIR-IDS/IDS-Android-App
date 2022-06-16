package fr.pirids.idsapp.controller.view

import androidx.navigation.NavHostController
import fr.pirids.idsapp.controller.api.ApiInterface
import fr.pirids.idsapp.controller.api.IzlyApi
import fr.pirids.idsapp.model.api.auth.IzlyAuth
import fr.pirids.idsapp.model.items.Device
import fr.pirids.idsapp.model.items.Service
import fr.pirids.idsapp.model.items.ServiceId

object ServiceViewController {
    fun getServiceHistory(service: Service): List<String> = listOf(
            "Dépense : ${service.name}",
            "Recharge : ${service.name}",
            "Dépense : ${service.name}",
            "Dépense : ${service.name}",
            "Dépense : ${service.name}",
            "Dépense : ${service.name}",
            "Dépense : ${service.name}",
            "Service : ${service.name}",
            "Service : ${service.name}",
            "Service : ${service.name}",
            "Service : ${service.name}",
            "Service : ${service.name}",
            "Service : ${service.name}",
            "Description : ${service.description}"
    )

    fun getProbesList(service: Service): List<Device> = service.compatibleDevices

    fun closeModal(navController: NavHostController) = navController.popBackStack()

    fun checkService(username: String, password: String, service: Service) : Boolean {
        return connectToService(username, password, service).checkConnection()
    }

    private fun connectToService(username: String, password: String, service: Service): ApiInterface {
        when(service.id) {
            ServiceId.IZLY -> return IzlyApi(IzlyAuth(username, password)) //FIXME: use a thread/coroutine to avoid blocking the UI (StrictMode$AndroidBlockGuardPolicy)
            else -> throw Exception("Service not supported")
        }
    }
}