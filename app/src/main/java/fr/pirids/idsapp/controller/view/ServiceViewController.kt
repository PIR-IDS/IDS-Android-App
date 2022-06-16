package fr.pirids.idsapp.controller.view

import fr.pirids.idsapp.model.items.Device
import fr.pirids.idsapp.model.items.Service

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
}