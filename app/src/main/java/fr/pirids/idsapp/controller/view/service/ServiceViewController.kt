package fr.pirids.idsapp.controller.view.service

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusManager
import androidx.navigation.NavHostController
import fr.pirids.idsapp.controller.api.ApiInterface
import fr.pirids.idsapp.data.api.data.IzlyData
import fr.pirids.idsapp.data.items.Device
import fr.pirids.idsapp.data.items.Service
import fr.pirids.idsapp.data.items.ServiceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    fun onLoginAction(
        focusManager: FocusManager,
        service: Service,
        snackbarHostState: SnackbarHostState,
        username: String,
        password: String,
        notFoundText: String
    ) {
        focusManager.clearFocus()
        serviceScope.launch(Dispatchers.IO) {
            val (serviceData, serviceConnected) = ServiceController.getServiceAndStatus(
                username,
                password,
                service
            )
            if (serviceConnected) {
                isConnected.value = true
                historyScope.launch(Dispatchers.IO) {
                    updateServiceData(serviceData, service)
                }
            } else {
                isConnected.value = false
                serviceScope.launch(Dispatchers.Main) {
                    snackbarHostState.showSnackbar(notFoundText)
                }
            }
        }
    }

    private suspend fun updateServiceData(serviceData: ApiInterface, service: Service) : Nothing {
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