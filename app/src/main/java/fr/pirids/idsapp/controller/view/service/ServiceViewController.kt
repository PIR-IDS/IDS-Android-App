package fr.pirids.idsapp.controller.view.service

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavHostController
import fr.pirids.idsapp.controller.api.ApiInterface
import fr.pirids.idsapp.data.api.data.IzlyData
import fr.pirids.idsapp.data.items.Device
import fr.pirids.idsapp.data.items.Service
import fr.pirids.idsapp.data.items.ServiceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import fr.pirids.idsapp.controller.detection.Service as ServiceController

object ServiceViewController {
    private const val checkingDelayMillis = 10_000L

    //TODO: adapt this boolean to handle many services
    val isConnected : MutableState<Boolean> = mutableStateOf(false)
    val serviceHistory: MutableState<List<String>> = mutableStateOf(listOf())
    lateinit var serviceScope: CoroutineScope
    lateinit var historyScope: CoroutineScope

    fun getProbesList(service: Service): List<Device> = service.compatibleDevices

    fun closeModal(navController: NavHostController) = navController.popBackStack()

    suspend fun updateServiceData(serviceData: ApiInterface, service: Service) : Nothing {
        while(true) {
            ServiceController.getServiceData(serviceData)?.let {
                when(service.id) {
                    ServiceId.IZLY -> {
                        val historyList: MutableList<String> = mutableListOf()
                        (it as IzlyData).transactionList.forEach { timestamp ->
                            historyList.add((if(timestamp > 0) "+" else "-") + " : $timestamp")
                        }
                        serviceHistory.value = historyList
                    }
                }
            }
            delay(checkingDelayMillis)
        }
    }
}