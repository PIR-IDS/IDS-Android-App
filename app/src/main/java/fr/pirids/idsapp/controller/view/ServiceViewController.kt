package fr.pirids.idsapp.controller.view

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavHostController
import fr.pirids.idsapp.controller.api.ApiInterface
import fr.pirids.idsapp.controller.api.IzlyApi
import fr.pirids.idsapp.model.api.auth.IzlyAuth
import fr.pirids.idsapp.model.api.data.ApiData
import fr.pirids.idsapp.model.api.data.IzlyData
import fr.pirids.idsapp.model.items.Device
import fr.pirids.idsapp.model.items.Service
import fr.pirids.idsapp.model.items.ServiceId
import kotlinx.coroutines.delay

object ServiceViewController {
    const val checkingDelayMillis = 10_000L
    const val timeTolerance = 10_000.0

    //TODO: adapt this boolean to handle many services
    val isConnected : MutableState<Boolean> = mutableStateOf(false)
    val serviceHistory: MutableState<List<String>> = mutableStateOf(listOf())

    fun getProbesList(service: Service): List<Device> = service.compatibleDevices

    fun closeModal(navController: NavHostController) = navController.popBackStack()

    fun checkService(username: String, password: String, service: Service) : Boolean = getServiceAndStatus(username, password, service).second

    /**
     * @return Pair<ApiInterface, Boolean> the instance of the connected service if success and the status of the service connection
     */
    fun getServiceAndStatus(username: String, password: String, service: Service) : Pair<ApiInterface, Boolean> {
        val serviceData = connectToService(username, password, service)
        return Pair(serviceData, serviceData.checkConnection())
    }

    private fun connectToService(username: String, password: String, service: Service): ApiInterface {
        when(service.id) {
            ServiceId.IZLY -> return IzlyApi(IzlyAuth(username, password))
            else -> throw Exception("Service not supported")
        }
    }

    suspend fun getServiceData(apiInterface: ApiInterface): ApiData {
        return apiInterface.getData()
    }

    suspend fun updateServiceData(serviceData: ApiInterface, service: Service) {
        while(true) {
            val retrievedData = getServiceData(serviceData)
            when(service.id) {
                ServiceId.IZLY -> {
                    val historyList: MutableList<String> = mutableListOf()
                    (retrievedData as IzlyData).transactionList.forEach {
                        historyList.add((if(it > 0) "+" else "-") + " : $it")
                    }
                    serviceHistory.value = historyList
                }
            }
            delay(checkingDelayMillis)
        }
    }
}