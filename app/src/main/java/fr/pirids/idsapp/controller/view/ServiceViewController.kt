package fr.pirids.idsapp.controller.view

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavHostController
import fr.pirids.idsapp.controller.api.ApiInterface
import fr.pirids.idsapp.model.api.data.IzlyData
import fr.pirids.idsapp.model.items.Device
import fr.pirids.idsapp.model.items.Service
import fr.pirids.idsapp.model.items.ServiceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import fr.pirids.idsapp.controller.detection.Service as ServiceController

object ServiceViewController {
    private const val checkingDelayMillis = 10_000L
    private const val timeTolerance = 10_000.0

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
                        (it as IzlyData).transactionList.forEach {
                            historyList.add((if(it > 0) "+" else "-") + " : $it")
                        }
                        serviceHistory.value = historyList
                    }
                }
            }
            delay(checkingDelayMillis)
        }
    }
}